package com.raachi.memory.domain.usecase

import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.ActivityEventType
import com.raachi.memory.domain.model.LedgerInput
import com.raachi.memory.domain.model.LedgerKind
import com.raachi.memory.domain.model.LedgerValidation
import com.raachi.memory.domain.repository.LedgerRepository
import com.raachi.memory.domain.repository.LedgerAlertScheduler
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SaveLedgerEntryUseCase @Inject constructor(
    private val repository: LedgerRepository,
    private val scheduler: LedgerAlertScheduler,
    private val clock: Clock,
    private val logActivity: LogActivityUseCase,
) {
    suspend operator fun invoke(input: LedgerInput): LedgerValidation {
        val validation = validateLedger(input)
        if (!validation.isValid) return validation
        val existing = input.id.takeIf { it != 0L }?.let { repository.getById(it) }
        val now = clock.instant()
        val entry = LedgerEntry(
                id = input.id,
                personName = input.personName.trim(),
                mobileNumber = input.mobileNumber.trim().ifBlank { null },
                kind = input.kind,
                direction = input.direction,
                itemName = input.itemName.trim().takeIf { input.kind == LedgerKind.ITEM },
                amountPaise = input.amount.toPaise().takeIf { input.kind == LedgerKind.MONEY },
                transactionDate = input.transactionDate,
                dueDate = input.dueDate,
                isReturned = existing?.isReturned ?: false,
                returnedAt = existing?.returnedAt,
                notes = input.notes.trim().ifBlank { null },
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            )
        val insertedId = repository.save(entry)
        val saved = entry.copy(id = input.id.takeIf { it != 0L } ?: insertedId)
        if (!saved.isReturned && saved.dueDate != null) scheduler.schedule(saved) else scheduler.cancel(saved.id)
        logActivity(
            eventType = if (existing == null) ActivityEventType.LEDGER_CREATED else ActivityEventType.LEDGER_UPDATED,
            referenceId = saved.id,
            title = if (existing == null) "Ledger entry created" else "Ledger entry updated",
            description = saved.activityDescription(),
        )
        return validation
    }
}

class DeleteLedgerEntryUseCase @Inject constructor(
    private val repository: LedgerRepository,
    private val scheduler: LedgerAlertScheduler,
    private val logActivity: LogActivityUseCase,
) {
    suspend operator fun invoke(id: Long) {
        val entry = repository.getById(id) ?: return
        scheduler.cancel(id)
        repository.delete(id)
        logActivity(ActivityEventType.LEDGER_DELETED, id, "Ledger entry deleted", entry.activityDescription())
    }
}

class MarkLedgerReturnedUseCase @Inject constructor(
    private val repository: LedgerRepository,
    private val scheduler: LedgerAlertScheduler,
    private val clock: Clock,
    private val logActivity: LogActivityUseCase,
) {
    suspend operator fun invoke(id: Long) {
        val entry = repository.getById(id) ?: return
        if (entry.isReturned) return
        scheduler.cancel(id)
        repository.save(entry.copy(isReturned = true, returnedAt = clock.instant(), updatedAt = clock.instant()))
        logActivity(ActivityEventType.LEDGER_RETURNED, id, "Ledger entry returned", entry.activityDescription())
    }
}

fun validateLedger(input: LedgerInput): LedgerValidation {
    val amount = input.amount.toPaise()
    return LedgerValidation(
        personError = input.personName.isBlank(),
        mobileError = input.mobileNumber.isNotBlank() && !LEDGER_MOBILE.matches(input.mobileNumber),
        amountError = input.kind == LedgerKind.MONEY && (amount == null || amount <= 0),
        itemError = input.kind == LedgerKind.ITEM && input.itemName.isBlank(),
    )
}

fun ledgerShareMessage(entry: LedgerEntry): String {
    val subject = entry.amountPaise?.let { "₹${formatPaisePlain(it)}" } ?: entry.itemName.orEmpty()
    val transactionDate = entry.transactionDate.format(LEDGER_SHARE_DATE)

    return when (entry.direction) {
        com.raachi.memory.domain.model.LedgerDirection.LENT -> {
            val due = entry.dueDate?.let { "\nRequest you to please return it by ${it.format(LEDGER_SHARE_DATE)}." }.orEmpty()
            "Hi ${entry.personName}, hope you are doing well.\nJust a gentle reminder about the $subject lent on $transactionDate.$due\nThank You"
        }
        com.raachi.memory.domain.model.LedgerDirection.BORROWED -> {
            val due = entry.dueDate?.let { "\nI will try to return it by ${it.format(LEDGER_SHARE_DATE)}." }.orEmpty()
            "Hi ${entry.personName}, hope you are doing well.\nA note from my side: I borrowed $subject on $transactionDate.$due\nThank You"
        }
    }
}

private fun String.toPaise(): Long? = runCatching {
    BigDecimal(trim()).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()
}.getOrNull()

private fun formatPaisePlain(paise: Long): String = BigDecimal(paise).movePointLeft(2).stripTrailingZeros().toPlainString()

private val LEDGER_MOBILE = Regex("^[5-9][0-9]{9}$")
private val LEDGER_SHARE_DATE = DateTimeFormatter.ofPattern("dd-MM-uuuu")

private fun LedgerEntry.activityDescription(): String = when (kind) {
    LedgerKind.MONEY -> "$personName - ${amountPaise?.let(::formatPaisePlain).orEmpty()} rupees"
    LedgerKind.ITEM -> "$personName - ${itemName.orEmpty()}"
}
