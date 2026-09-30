package com.example.debttrack.ui.home

import com.example.debttrack.R
import com.example.debttrack.data.entities.Category
import com.example.debttrack.data.entities.Debt
import com.example.debttrack.data.entities.DebtStatus
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.utils.CurrencyFormatter
import com.example.debttrack.utils.DateFormatter

object DebtListItemMapper {

    fun map(debt: Debt, category: Category?): DebtListItem {
        val typeLabel = if (debt.type == DebtType.OWED_TO_ME) "Мне должны" else "Я должен"
        val subtitle = if (category != null) "$typeLabel · ${category.name}" else typeLabel

        val remaining = debt.amount - debt.paidAmount
        val amountColorRes = if (debt.type == DebtType.OWED_TO_ME) {
            R.color.color_owed_to_me
        } else {
            R.color.color_i_owe
        }

        val progressPercent = if (debt.amount > 0) {
            ((debt.paidAmount / debt.amount) * 100).toInt().coerceIn(0, 100)
        } else 0

        val daysLeftText = "${DateFormatter.formatDaysLeft(debt.dueDate)} " +
            "(до ${DateFormatter.formatShortDate(debt.dueDate)})"

        return DebtListItem(
            id = debt.id,
            avatarInitials = initialsOf(debt.personName),
            avatarColorHex = category?.colorHex ?: "#3B6FE0",
            name = debt.personName,
            subtitle = subtitle,
            amountText = CurrencyFormatter.format(remaining),
            amountColorRes = amountColorRes,
            statusText = statusLabel(debt.status),
            statusColorRes = statusColorRes(debt.status),
            progressPercent = progressPercent,
            daysLeftText = daysLeftText
        )
    }

    private fun initialsOf(fullName: String): String {
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }

    private fun statusLabel(status: DebtStatus): String = when (status) {
        DebtStatus.ACTIVE -> "Активный"
        DebtStatus.PARTIAL -> "Частично"
        DebtStatus.PAID -> "Погашен"
        DebtStatus.OVERDUE -> "Просрочено"
    }

    private fun statusColorRes(status: DebtStatus): Int = when (status) {
        DebtStatus.ACTIVE -> R.color.color_status_active
        DebtStatus.PARTIAL -> R.color.color_status_partial
        DebtStatus.PAID -> R.color.color_owed_to_me
        DebtStatus.OVERDUE -> R.color.color_i_owe
    }
}
