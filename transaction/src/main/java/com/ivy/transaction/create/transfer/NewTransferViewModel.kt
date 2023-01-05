package com.ivy.transaction.create.transfer

import com.ivy.common.time.provider.TimeProvider
import com.ivy.core.domain.SimpleFlowViewModel
import com.ivy.core.domain.action.account.AccountByIdAct
import com.ivy.core.domain.action.account.AccountsAct
import com.ivy.core.domain.action.category.CategoryByIdAct
import com.ivy.core.domain.action.exchange.ExchangeAct
import com.ivy.core.domain.action.settings.basecurrency.BaseCurrencyAct
import com.ivy.core.domain.action.transaction.transfer.ModifyTransfer
import com.ivy.core.domain.action.transaction.transfer.TransferData
import com.ivy.core.domain.action.transaction.transfer.WriteTransferAct
import com.ivy.core.domain.pure.format.CombinedValueUi
import com.ivy.core.domain.pure.util.combine
import com.ivy.core.domain.pure.util.flattenLatest
import com.ivy.core.domain.pure.util.takeIfNotBlank
import com.ivy.core.ui.action.BaseCurrencyRepresentationFlow
import com.ivy.core.ui.action.mapping.MapCategoryUiAct
import com.ivy.core.ui.action.mapping.account.MapAccountUiAct
import com.ivy.core.ui.action.mapping.trn.MapTrnTimeUiAct
import com.ivy.core.ui.data.account.dummyAccountUi
import com.ivy.core.ui.data.transaction.TrnTimeUi
import com.ivy.data.transaction.TrnTime
import com.ivy.design.l2_components.modal.IvyModal
import com.ivy.navigation.Navigator
import com.ivy.transaction.action.TitleSuggestionsFlow
import com.ivy.transaction.create.CreateTrnController
import com.ivy.transaction.create.action.CreateTrnStepsAct
import com.ivy.transaction.create.action.WriteLastUsedAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class NewTransferViewModel @Inject constructor(
    timeProvider: TimeProvider,
    private val titleSuggestionsFlow: TitleSuggestionsFlow,
    private val createTrnStepsAct: CreateTrnStepsAct,
    private val mapTrnTimeUiAct: MapTrnTimeUiAct,
    private val navigator: Navigator,
    private val accountByIdAct: AccountByIdAct,
    private val categoryByIdAct: CategoryByIdAct,
    private val mapCategoryUiAct: MapCategoryUiAct,
    private val baseCurrencyAct: BaseCurrencyAct,
    private val writeLastUsedAccount: WriteLastUsedAccount,
    private val baseCurrencyRepresentationFlow: BaseCurrencyRepresentationFlow,
    private val accountsAct: AccountsAct,
    private val mapAccountUiAct: MapAccountUiAct,
    private val writeTransferAct: WriteTransferAct,
    private val exchangeAct: ExchangeAct,
    private val createTrnController: CreateTrnController,
) : SimpleFlowViewModel<NewTransferState, NewTransferEvent>() {
    private val feeModal = IvyModal()

    override val initialUi = NewTransferState(
        accountFrom = dummyAccountUi(),
        accountTo = dummyAccountUi(),
        amountFrom = CombinedValueUi.initial(),
        amountTo = CombinedValueUi.initial(),
        category = null,
        timeUi = TrnTimeUi.Actual("", ""),
        time = TrnTime.Actual(timeProvider.timeNow()),
        title = null,
        description = null,
        fee = null,

        titleSuggestions = emptyList(),
        createFlow = createTrnController.uiFlow,
        feeModal = feeModal,
    )

    // region State
    private val amountFrom = MutableStateFlow(initialUi.amountFrom)
    private val amountTo = MutableStateFlow(initialUi.amountTo)
    private val accountFrom = MutableStateFlow(initialUi.accountFrom)
    private val accountTo = MutableStateFlow(initialUi.accountTo)
    private val category = MutableStateFlow(initialUi.category)
    private val time = MutableStateFlow<TrnTime>(TrnTime.Actual(timeProvider.timeNow()))
    private val timeUi = MutableStateFlow(initialUi.timeUi)
    private val title = MutableStateFlow(initialUi.title)
    private val description = MutableStateFlow(initialUi.description)
    private val fee = MutableStateFlow(initialUi.fee)
    // endregion


    override val uiFlow = combine(
        amountFrom, amountTo,
        accountFrom, accountTo, category, time, timeUi,
        title, description, fee,
    )
    { amountFrom, amountTo,
      accountFrom, accountTo, category, time, timeUi,
      title, description, fee ->
        titleSuggestionsFlow(
            TitleSuggestionsFlow.Input(
                title = title,
                categoryUi = category,
                transfer = true,
            )
        ).map { titleSuggestions ->
            NewTransferState(
                amountFrom = amountFrom,
                amountTo = amountTo,
                accountFrom = accountFrom,
                accountTo = accountTo,
                category = category,
                time = time,
                timeUi = timeUi,
                title = title,
                description = description,
                fee = fee,

                titleSuggestions = titleSuggestions,
                createFlow = createTrnController.uiFlow,
                feeModal = feeModal,
            )
        }
    }.flattenLatest()


    // region Event Handling
    override suspend fun handleEvent(event: NewTransferEvent) = when (event) {
        NewTransferEvent.Initial -> handleInitial()
        NewTransferEvent.Close -> handleClose()
        NewTransferEvent.Add -> handleAdd()
        is NewTransferEvent.TransferAmountChange -> handleTransferAmountChange(event)
        is NewTransferEvent.ToAmountChange -> handleToAmountChange(event)
        is NewTransferEvent.FromAmountChange -> handleFromAmountChange(event)
        is NewTransferEvent.FromAccountChange -> handleFromAccountChange(event)
        is NewTransferEvent.ToAccountChange -> handleToAccountChange(event)
        is NewTransferEvent.FeeChange -> handleFeeChange(event)
        is NewTransferEvent.TitleChange -> handleTitleChange(event)
        is NewTransferEvent.DescriptionChange -> handleDescriptionChange(event)
        is NewTransferEvent.CategoryChange -> handleCategoryChange(event)
        is NewTransferEvent.TrnTimeChange -> handleTimeChange(event)
    }

    private suspend fun handleInitial() {
        createTrnController.startFlow()

        val accounts = accountsAct(Unit)
        if (accounts.size < 2) {
            // cannot do transfers with less than 2 accounts
            closeScreen()
            return
        }
        val fromAcc = accounts.first()
        val toAcc = accounts[1] // 2nd

        accountFrom.value = mapAccountUiAct(fromAcc)
        accountTo.value = mapAccountUiAct(toAcc)

        amountFrom.value = CombinedValueUi(
            amount = 0.0,
            currency = fromAcc.currency,
            shortenFiat = false,
        )
        amountTo.value = CombinedValueUi(
            amount = 0.0,
            currency = toAcc.currency,
            shortenFiat = false,
        )

        timeUi.value = mapTrnTimeUiAct(time.value)
    }

    private suspend fun handleAdd() {
        val accountFrom = accountByIdAct(accountFrom.value.id) ?: return
        val accountTo = accountByIdAct(accountTo.value.id) ?: return
        val category = category.value?.let { categoryByIdAct(it.id) }

        val data = TransferData(
            accountFrom = accountFrom,
            accountTo = accountTo,
            amountFrom = amountFrom.value.value,
            amountTo = amountTo.value.value,
            category = category,
            time = time.value,
            title = title.value,
            description = description.value,
            fee = fee.value?.value,
        )

        writeTransferAct(ModifyTransfer.add(data))

        closeScreen()
    }

    private fun handleClose() {
        closeScreen()
    }

    private fun closeScreen() {
        createTrnController.hideKeyboard()
        navigator.back()
    }

    // region Handle value changes
    private suspend fun handleTransferAmountChange(event: NewTransferEvent.TransferAmountChange) {
        // Called initially when the transfer modal is shown

        val toAccount = accountByIdAct(accountTo.value.id) ?: return

        amountFrom.value = CombinedValueUi(
            value = event.amount,
            shortenFiat = false,
        )
        amountTo.value = CombinedValueUi(
            value = exchangeAct(
                ExchangeAct.Input(
                    value = event.amount,
                    outputCurrency = toAccount.currency
                )
            ),
            shortenFiat = false,
        )

    }

    private fun handleFromAmountChange(event: NewTransferEvent.FromAmountChange) {
        amountFrom.value = CombinedValueUi(
            value = event.amount,
            shortenFiat = false,
        )
    }

    private fun handleToAmountChange(event: NewTransferEvent.ToAmountChange) {
        amountTo.value = CombinedValueUi(
            value = event.amount,
            shortenFiat = false,
        )
    }

    private suspend fun handleFromAccountChange(event: NewTransferEvent.FromAccountChange) {
        accountFrom.value = event.account

        accountByIdAct(event.account.id)?.let {
            amountFrom.value = CombinedValueUi(
                amount = amountFrom.value.value.amount,
                currency = it.currency,
                shortenFiat = false,
            )
        }
    }

    private fun handleToAccountChange(event: NewTransferEvent.ToAccountChange) {
        accountTo.value = event.account
    }

    private fun handleFeeChange(event: NewTransferEvent.FeeChange) {
        fee.value = if (event.value != null) CombinedValueUi(
            value = event.value,
            shortenFiat = false,
        ).takeIf { it.value.amount > 0.0 } else null
    }

    private fun handleTitleChange(event: NewTransferEvent.TitleChange) {
        title.value = event.title.takeIfNotBlank()
    }

    private fun handleDescriptionChange(event: NewTransferEvent.DescriptionChange) {
        description.value = event.description.takeIfNotBlank()
    }

    private fun handleCategoryChange(event: NewTransferEvent.CategoryChange) {
        category.value = event.category
    }

    private suspend fun handleTimeChange(event: NewTransferEvent.TrnTimeChange) {
        time.value = event.time
        timeUi.value = mapTrnTimeUiAct(event.time)
    }
    // endregion
    // endregion
}