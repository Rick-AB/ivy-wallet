package com.ivy.main


import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ivy.base.MainTab
import com.ivy.design.util.IvyPreview
import com.ivy.navigation.destinations.main.Main
import com.ivy.wallet.domain.deprecated.logic.model.CreateAccountData
import com.ivy.wallet.ui.theme.modal.edit.AccountModal
import com.ivy.wallet.ui.theme.modal.edit.AccountModalData

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.MainScreen(main: Main.Tab?) {
    val viewModel: MainViewModel = hiltViewModel()
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
//    screen: Main,
    tab: MainTab,

    baseCurrency: String,

    selectTab: (MainTab) -> Unit,
    onCreateAccount: (CreateAccountData) -> Unit,
) {
    when (tab) {
        MainTab.HOME -> com.ivy.home.HomeTab()
        MainTab.ACCOUNTS -> TODO() //AccountsTab(screen = screen)
    }

    var accountModalData: AccountModalData? by remember { mutableStateOf(null) }


    BottomBar(
        tab = tab,
        selectTab = selectTab,

        onAddIncome = {
//            nav.navigateTo(
//                EditTransaction(
//                    initialTransactionId = null,
//                    type = TrnTypeOld.INCOME
//                )
//            )
        },
        onAddExpense = {
//            nav.navigateTo(
//                EditTransaction(
//                    initialTransactionId = null,
//                    type = TrnTypeOld.EXPENSE
//                )
//            )
        },
        onAddTransfer = {
//            nav.navigateTo(
//                EditTransaction(
//                    initialTransactionId = null,
//                    type = TrnTypeOld.TRANSFER
//                )
//            )
        },
        onAddPlannedPayment = {
//            nav.navigateTo(
//                EditPlanned(
//                    type = TrnTypeOld.EXPENSE,
//                    plannedPaymentRuleId = null
//                )
//            )
        },

        showAddAccountModal = {
            accountModalData = AccountModalData(
                account = null,
                balance = 0.0,
                baseCurrency = baseCurrency
            )
        }
    )

    AccountModal(
        modal = accountModalData,
        onCreateAccount = onCreateAccount,
        onEditAccount = { _, _ -> },
        dismiss = {
            accountModalData = null
        }
    )
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun PreviewMainScreen() {
    IvyPreview {
        UI(
            tab = MainTab.HOME,
            baseCurrency = "BGN",
            selectTab = {},
            onCreateAccount = { }
        )
    }
}