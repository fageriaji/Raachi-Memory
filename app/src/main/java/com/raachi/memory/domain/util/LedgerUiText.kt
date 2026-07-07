package com.raachi.memory.features.ledger

import androidx.annotation.StringRes
import com.raachi.memory.R
import com.raachi.memory.domain.model.ItemType

@StringRes
fun ItemType.labelResId(): Int {
    return when (this) {
        ItemType.MONEY -> R.string.ledger_item_type_money
        ItemType.BOOK -> R.string.ledger_item_type_book
        ItemType.CHARGER -> R.string.ledger_item_type_charger
        ItemType.DOCUMENTS -> R.string.ledger_item_type_documents
        ItemType.OTHER -> R.string.ledger_item_type_other
    }
}

@StringRes
fun LedgerFilter.labelResId(): Int {
    return when (this) {
        LedgerFilter.ALL -> R.string.ledger_filter_all
        LedgerFilter.PENDING -> R.string.ledger_filter_pending
        LedgerFilter.RETURNED -> R.string.ledger_filter_returned
        LedgerFilter.OVERDUE -> R.string.ledger_filter_overdue
    }
}

@StringRes
fun LedgerSort.labelResId(): Int {
    return when (this) {
        LedgerSort.DUE_EARLIEST -> R.string.ledger_sort_due_earliest
        LedgerSort.DUE_LATEST -> R.string.ledger_sort_due_latest
        LedgerSort.PERSON_NAME -> R.string.ledger_sort_person_name
        LedgerSort.RECENTLY_ADDED -> R.string.ledger_sort_recently_added
    }
}
