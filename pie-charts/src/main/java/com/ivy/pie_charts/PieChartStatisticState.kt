package com.ivy.pie_charts

import com.ivy.core.ui.temp.trash.TimePeriod
import com.ivy.data.transaction.TransactionOld
import com.ivy.data.transaction.TrnType
import com.ivy.pie_charts.model.CategoryAmount
import com.ivy.wallet.ui.theme.modal.ChoosePeriodModalData
import java.util.*

data class PieChartStatisticState(
    val transactionType: TrnType = TrnType.INCOME,
    val period: TimePeriod = TimePeriod(),
    val baseCurrency: String = "",
    val totalAmount: Double = 0.0,
    val categoryAmounts: List<CategoryAmount> = emptyList(),
    val pieChartCategoryAmount: List<CategoryAmount> = emptyList(),
    val selectedCategory: SelectedCategory? = null,
    val accountIdFilterList: List<UUID> = emptyList(),
    val showCloseButtonOnly: Boolean = false,
    val filterExcluded: Boolean = false,
    val transactions: List<TransactionOld> = emptyList(),
    val choosePeriodModal: ChoosePeriodModalData? = null,
    val showUnpackOption: Boolean = false,
    val unpackAllSubCategories: Boolean = false
)