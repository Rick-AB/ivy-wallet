package com.ivy.screens

import com.ivy.data.transaction.TransactionOld
import com.ivy.data.transaction.TrnType
import com.ivy.frp.view.navigation.Screen
import java.util.*

data class ItemStatistic(
    val accountId: UUID? = null,
    val categoryId: UUID? = null,
    val unspecifiedCategory: Boolean? = false,
    val transactionType: TrnType? = null,
    val accountIdFilterList: List<UUID> = Collections.emptyList(),
    val transactions: List<TransactionOld> = Collections.emptyList()
) : Screen