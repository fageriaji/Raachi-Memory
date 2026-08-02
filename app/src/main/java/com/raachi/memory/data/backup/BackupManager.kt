package com.raachi.memory.data.backup

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import com.raachi.memory.data.activity.ActivityDao
import com.raachi.memory.data.activity.ActivityLogEntity
import com.raachi.memory.data.database.RaachiDatabase
import com.raachi.memory.data.ledger.LedgerDao
import com.raachi.memory.data.ledger.LedgerEntryEntity
import com.raachi.memory.data.ledger.toDomain
import com.raachi.memory.data.expense.ExpenseAccountEntity
import com.raachi.memory.data.expense.ExpenseDao
import com.raachi.memory.data.expense.ExpenseTransactionEntity
import com.raachi.memory.data.profile.UserProfileDao
import com.raachi.memory.data.profile.UserProfileEntity
import com.raachi.memory.data.reminder.ReminderDao
import com.raachi.memory.data.reminder.ReminderEntity
import com.raachi.memory.data.reminder.toDomain
import com.raachi.memory.domain.model.ActivityEventType
import com.raachi.memory.domain.model.AppPreferences
import com.raachi.memory.domain.model.Gender
import com.raachi.memory.domain.model.LedgerDirection
import com.raachi.memory.domain.model.LedgerKind
import com.raachi.memory.domain.model.ReminderCategory
import com.raachi.memory.domain.model.ReminderRepeatType
import com.raachi.memory.domain.model.ReminderStatus
import com.raachi.memory.domain.model.ThemeMode
import com.raachi.memory.domain.model.ExpenseAccountType
import com.raachi.memory.domain.model.ExpenseCategory
import com.raachi.memory.domain.model.ExpensePaymentMethod
import com.raachi.memory.domain.model.ExpenseTransactionType
import com.raachi.memory.domain.repository.AppSettingsRepository
import com.raachi.memory.domain.repository.LedgerAlertScheduler
import com.raachi.memory.domain.repository.ReminderScheduler
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackupSummary(
    val reminders: Int,
    val ledgerEntries: Int,
    val activities: Int,
    val expenseAccounts: Int = 0,
    val expenseTransactions: Int = 0,
)

class BackupManager @Inject constructor(
    private val contentResolver: ContentResolver,
    private val database: RaachiDatabase,
    private val profileDao: UserProfileDao,
    private val reminderDao: ReminderDao,
    private val ledgerDao: LedgerDao,
    private val activityDao: ActivityDao,
    private val expenseDao: ExpenseDao,
    private val settingsRepository: AppSettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    private val ledgerScheduler: LedgerAlertScheduler,
    private val clock: Clock,
) {
    suspend fun exportTo(uri: Uri): BackupSummary = withContext(Dispatchers.IO) {
        val profile = profileDao.getProfile()
        val reminders = reminderDao.getAll()
        val ledgerEntries = ledgerDao.getAll()
        val activities = activityDao.getAll()
        val expenseAccounts = expenseDao.getAllAccounts()
        val expenseTransactions = expenseDao.getAllTransactions()
        val preferences = settingsRepository.preferences.first()
        val root = JSONObject()
            .put("schemaVersion", BACKUP_SCHEMA_VERSION)
            .put("exportedAtMillis", clock.millis())
            .put("profile", profile?.toJson() ?: JSONObject.NULL)
            .put("reminders", reminders.toJsonArray(ReminderEntity::toJson))
            .put("ledgerEntries", ledgerEntries.toJsonArray(LedgerEntryEntity::toJson))
            .put("activityLogs", activities.toJsonArray(ActivityLogEntity::toJson))
            .put("expenseAccounts", expenseAccounts.toJsonArray(ExpenseAccountEntity::toJson))
            .put("expenseTransactions", expenseTransactions.toJsonArray(ExpenseTransactionEntity::toJson))
            .put("settings", preferences.toJson())

        val output = requireNotNull(contentResolver.openOutputStream(uri, "wt")) { "Unable to open export file." }
        output.bufferedWriter(Charsets.UTF_8).use { it.write(root.toString(2)) }
        BackupSummary(reminders.size, ledgerEntries.size, activities.size, expenseAccounts.size, expenseTransactions.size)
    }

    suspend fun importFrom(uri: Uri): BackupSummary = withContext(Dispatchers.IO) {
        val text = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
            buildString {
                val buffer = CharArray(DEFAULT_BUFFER_SIZE)
                var total = 0
                while (true) {
                    val read = reader.read(buffer)
                    if (read < 0) break
                    total += read
                    require(total <= MAX_BACKUP_CHARACTERS) { "Backup file is too large." }
                    append(buffer, 0, read)
                }
            }
        } ?: error("Unable to open backup file.")
        val payload = parseBackup(text)
        val oldReminderIds = reminderDao.getAll().map(ReminderEntity::id)
        val oldLedgerIds = ledgerDao.getAll().map(LedgerEntryEntity::id)

        database.withTransaction {
            expenseDao.deleteAllTransactions()
            expenseDao.deleteAllAccounts()
            activityDao.deleteAll()
            ledgerDao.deleteAll()
            reminderDao.deleteAll()
            profileDao.deleteAll()
            payload.profile?.let { profileDao.upsert(it) }
            reminderDao.upsertAll(payload.reminders)
            ledgerDao.upsertAll(payload.ledgerEntries)
            activityDao.insertAll(payload.activities)
            expenseDao.upsertAccounts(payload.expenseAccounts)
            expenseDao.upsertTransactions(payload.expenseTransactions)
        }
        settingsRepository.replacePreferences(payload.preferences)

        oldReminderIds.forEach(reminderScheduler::cancel)
        oldLedgerIds.forEach(ledgerScheduler::cancel)
        payload.reminders.map(ReminderEntity::toDomain)
            .filter { it.status in setOf(ReminderStatus.ACTIVE, ReminderStatus.SNOOZED) && it.nextTriggerAt != null }
            .forEach(reminderScheduler::schedule)
        payload.ledgerEntries.map(LedgerEntryEntity::toDomain)
            .filter { !it.isReturned && it.dueDate != null }
            .forEach(ledgerScheduler::schedule)

        BackupSummary(
            payload.reminders.size,
            payload.ledgerEntries.size,
            payload.activities.size,
            payload.expenseAccounts.size,
            payload.expenseTransactions.size,
        )
    }
}

private data class BackupPayload(
    val profile: UserProfileEntity?,
    val reminders: List<ReminderEntity>,
    val ledgerEntries: List<LedgerEntryEntity>,
    val activities: List<ActivityLogEntity>,
    val expenseAccounts: List<ExpenseAccountEntity>,
    val expenseTransactions: List<ExpenseTransactionEntity>,
    val preferences: AppPreferences,
)

private fun parseBackup(text: String): BackupPayload {
    val root = JSONObject(text)
    val schemaVersion = root.getInt("schemaVersion")
    require(schemaVersion in MIN_SUPPORTED_BACKUP_SCHEMA_VERSION..BACKUP_SCHEMA_VERSION) { "Unsupported backup version." }
    val profile = if (root.isNull("profile")) null else root.getJSONObject("profile").toProfile()
    require(profile != null && profile.name.isNotBlank()) { "Backup does not contain a valid profile." }
    val expenseAccounts = if (schemaVersion >= 2) {
        root.getJSONArray("expenseAccounts").mapObjects(JSONObject::toExpenseAccount)
    } else {
        emptyList()
    }
    val accountIds = expenseAccounts.map(ExpenseAccountEntity::id).toSet()
    val expenseTransactions = if (schemaVersion >= 2) {
        root.getJSONArray("expenseTransactions").mapObjects(JSONObject::toExpenseTransaction).also { transactions ->
            transactions.forEach { transaction ->
                require(transaction.sourceAccountId == null || transaction.sourceAccountId in accountIds)
                require(transaction.destinationAccountId == null || transaction.destinationAccountId in accountIds)
            }
        }
    } else {
        emptyList()
    }
    return BackupPayload(
        profile = profile,
        reminders = root.getJSONArray("reminders").mapObjects(JSONObject::toReminder),
        ledgerEntries = root.getJSONArray("ledgerEntries").mapObjects(JSONObject::toLedgerEntry),
        activities = root.getJSONArray("activityLogs").mapObjects(JSONObject::toActivity),
        expenseAccounts = expenseAccounts,
        expenseTransactions = expenseTransactions,
        preferences = root.getJSONObject("settings").toPreferences(),
    )
}

private fun UserProfileEntity.toJson() = JSONObject()
    .put("id", id).put("name", name).putNullable("dateOfBirth", dateOfBirth)
    .putNullable("mobile", mobile).putNullable("gender", gender).putNullable("email", email)
    .putNullable("heightCm", heightCm).putNullable("weightKg", weightKg)
    .putNullable("profilePhotoUri", profilePhotoUri).put("createdAtMillis", createdAtMillis)
    .put("updatedAtMillis", updatedAtMillis)

private fun JSONObject.toProfile() = UserProfileEntity(
    id = getLong("id").also { require(it == 1L) },
    name = getString("name").also { require(it.isNotBlank()) },
    dateOfBirth = nullableString("dateOfBirth"),
    mobile = nullableString("mobile"),
    gender = nullableString("gender")?.also { value -> require(Gender.entries.any { it.name == value }) },
    email = nullableString("email"),
    heightCm = nullableDouble("heightCm"),
    weightKg = nullableDouble("weightKg"),
    profilePhotoUri = nullableString("profilePhotoUri"),
    createdAtMillis = getLong("createdAtMillis"),
    updatedAtMillis = getLong("updatedAtMillis"),
)

private fun ReminderEntity.toJson() = JSONObject()
    .put("id", id).put("title", title).put("category", category).putNullable("description", description)
    .put("repeatType", repeatType).putNullable("intervalHours", intervalHours)
    .put("scheduledAtMillis", scheduledAtMillis).putNullable("nextTriggerAtMillis", nextTriggerAtMillis)
    .put("soundEnabled", soundEnabled).put("vibrationEnabled", vibrationEnabled).put("status", status)
    .put("createdAtMillis", createdAtMillis).put("updatedAtMillis", updatedAtMillis)

private fun JSONObject.toReminder() = ReminderEntity(
    id = positiveId("id"),
    title = getString("title").also { require(it.isNotBlank()) },
    category = enumName("category", ReminderCategory.entries.map { it.name }),
    description = nullableString("description"),
    repeatType = enumName("repeatType", ReminderRepeatType.entries.map { it.name }),
    intervalHours = nullableInt("intervalHours"),
    scheduledAtMillis = getLong("scheduledAtMillis"),
    nextTriggerAtMillis = nullableLong("nextTriggerAtMillis"),
    soundEnabled = getBoolean("soundEnabled"),
    vibrationEnabled = getBoolean("vibrationEnabled"),
    status = enumName("status", ReminderStatus.entries.map { it.name }),
    createdAtMillis = getLong("createdAtMillis"),
    updatedAtMillis = getLong("updatedAtMillis"),
)

private fun LedgerEntryEntity.toJson() = JSONObject()
    .put("id", id).put("personName", personName).putNullable("mobileNumber", mobileNumber)
    .put("kind", kind).put("direction", direction).putNullable("itemName", itemName)
    .putNullable("amountPaise", amountPaise).put("transactionDateEpochDay", transactionDateEpochDay)
    .putNullable("dueDateEpochDay", dueDateEpochDay)
    .put("isReturned", isReturned).putNullable("returnedAtMillis", returnedAtMillis)
    .putNullable("notes", notes).put("createdAtMillis", createdAtMillis).put("updatedAtMillis", updatedAtMillis)

private fun JSONObject.toLedgerEntry() = LedgerEntryEntity(
    id = positiveId("id"),
    personName = getString("personName").also { require(it.isNotBlank()) },
    mobileNumber = nullableString("mobileNumber"),
    kind = enumName("kind", LedgerKind.entries.map { it.name }),
    direction = enumName("direction", LedgerDirection.entries.map { it.name }),
    itemName = nullableString("itemName"),
    amountPaise = nullableLong("amountPaise"),
    transactionDateEpochDay = if (has("transactionDateEpochDay") && !isNull("transactionDateEpochDay")) {
        getLong("transactionDateEpochDay")
    } else {
        getLong("createdAtMillis") / MILLIS_PER_DAY
    },
    dueDateEpochDay = nullableLong("dueDateEpochDay"),
    isReturned = getBoolean("isReturned"),
    returnedAtMillis = nullableLong("returnedAtMillis"),
    notes = nullableString("notes"),
    createdAtMillis = getLong("createdAtMillis"),
    updatedAtMillis = getLong("updatedAtMillis"),
)

private fun ActivityLogEntity.toJson() = JSONObject()
    .put("id", id).put("eventType", eventType).putNullable("referenceId", referenceId)
    .put("title", title).putNullable("description", description).put("eventTimeMillis", eventTimeMillis)

private fun JSONObject.toActivity() = ActivityLogEntity(
    id = positiveId("id"),
    eventType = enumName("eventType", ActivityEventType.entries.map { it.name }),
    referenceId = nullableLong("referenceId"),
    title = getString("title").also { require(it.isNotBlank()) },
    description = nullableString("description"),
    eventTimeMillis = getLong("eventTimeMillis"),
)

private fun ExpenseAccountEntity.toJson() = JSONObject()
    .put("id", id).put("name", name).put("type", type)
    .put("openingBalancePaise", openingBalancePaise).put("colorValue", colorValue)
    .put("isArchived", isArchived).put("createdAtMillis", createdAtMillis).put("updatedAtMillis", updatedAtMillis)

private fun JSONObject.toExpenseAccount() = ExpenseAccountEntity(
    id = positiveId("id"),
    name = getString("name").also { require(it.isNotBlank()) },
    type = enumName("type", ExpenseAccountType.entries.map { it.name }),
    openingBalancePaise = getLong("openingBalancePaise").also { require(it >= 0) },
    colorValue = getLong("colorValue"),
    isArchived = getBoolean("isArchived"),
    createdAtMillis = getLong("createdAtMillis"),
    updatedAtMillis = getLong("updatedAtMillis"),
)

private fun ExpenseTransactionEntity.toJson() = JSONObject()
    .put("id", id).put("type", type).put("amountPaise", amountPaise)
    .putNullable("sourceAccountId", sourceAccountId).putNullable("destinationAccountId", destinationAccountId)
    .put("category", category).putNullable("paymentMethod", paymentMethod)
    .put("transactionDateEpochDay", transactionDateEpochDay).putNullable("transactionTimeMinutes", transactionTimeMinutes)
    .putNullable("note", note).put("createdAtMillis", createdAtMillis).put("updatedAtMillis", updatedAtMillis)

private fun JSONObject.toExpenseTransaction(): ExpenseTransactionEntity {
    val type = enumName("type", ExpenseTransactionType.entries.map { it.name })
    val sourceAccountId = nullableLong("sourceAccountId")
    val destinationAccountId = nullableLong("destinationAccountId")
    require(type == ExpenseTransactionType.CREDIT.name || sourceAccountId != null)
    require(type == ExpenseTransactionType.DEBIT.name || destinationAccountId != null)
    require(type != ExpenseTransactionType.TRANSFER.name || sourceAccountId != destinationAccountId)
    return ExpenseTransactionEntity(
        id = positiveId("id"),
        type = type,
        amountPaise = getLong("amountPaise").also { require(it > 0) },
        sourceAccountId = sourceAccountId,
        destinationAccountId = destinationAccountId,
        category = enumName("category", ExpenseCategory.entries.map { it.name }),
        paymentMethod = nullableString("paymentMethod")?.also { value ->
            require(ExpensePaymentMethod.entries.any { it.name == value })
        },
        transactionDateEpochDay = getLong("transactionDateEpochDay"),
        transactionTimeMinutes = nullableInt("transactionTimeMinutes")?.also { require(it in 0..1439) },
        note = nullableString("note"),
        createdAtMillis = getLong("createdAtMillis"),
        updatedAtMillis = getLong("updatedAtMillis"),
    )
}

private fun AppPreferences.toJson() = JSONObject()
    .put("onboardingCompleted", onboardingCompleted).put("themeMode", themeMode.name)
    .put("reminderSoundEnabled", reminderSoundEnabled).put("defaultSnoozeMinutes", defaultSnoozeMinutes)

private fun JSONObject.toPreferences(): AppPreferences {
    val snooze = getInt("defaultSnoozeMinutes")
    require(snooze in ALLOWED_SNOOZE_MINUTES)
    return AppPreferences(
        onboardingCompleted = getBoolean("onboardingCompleted"),
        themeMode = ThemeMode.valueOf(getString("themeMode")),
        reminderSoundEnabled = getBoolean("reminderSoundEnabled"),
        defaultSnoozeMinutes = snooze,
    )
}

private fun JSONObject.putNullable(name: String, value: Any?): JSONObject = put(name, value ?: JSONObject.NULL)
private fun JSONObject.nullableString(name: String): String? = if (isNull(name)) null else getString(name)
private fun JSONObject.nullableLong(name: String): Long? = if (isNull(name)) null else getLong(name)
private fun JSONObject.nullableInt(name: String): Int? = if (isNull(name)) null else getInt(name)
private fun JSONObject.nullableDouble(name: String): Double? = if (isNull(name)) null else getDouble(name)
private fun JSONObject.positiveId(name: String): Long = getLong(name).also { require(it > 0) }
private fun JSONObject.enumName(name: String, allowed: List<String>): String = getString(name).also { require(it in allowed) }

private fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray = JSONArray().also { array ->
    forEach { array.put(transform(it)) }
}

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
    require(length() <= MAX_RECORDS_PER_SECTION) { "Backup contains too many records." }
    return List(length()) { index -> transform(getJSONObject(index)) }
}

private const val BACKUP_SCHEMA_VERSION = 2
private const val MIN_SUPPORTED_BACKUP_SCHEMA_VERSION = 1
private const val MILLIS_PER_DAY = 86_400_000L
private const val MAX_BACKUP_CHARACTERS = 20 * 1024 * 1024
private const val MAX_RECORDS_PER_SECTION = 100_000
private val ALLOWED_SNOOZE_MINUTES = setOf(5, 10, 15, 30, 60)
