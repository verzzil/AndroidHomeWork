package com.example.mvicalculator.calculator

import androidx.lifecycle.ViewModel
import com.example.mvicalculator.*
import com.example.mvicalculator.common.submitInputAction
import com.freeletics.rxredux.reduxStore
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class CalculatorViewModel : ViewModel() {
    private val initialState = CalculatorState()

    private var previousAction: Action.InputAction? = null
    private var lastAction: Action.InputAction? = null

    fun observeState(upstreamActions: Observable<Action>): Observable<CalculatorState> =
        upstreamActions
            .switchMap { action ->
                if (action is Action.DifficultResultInput) {
                    replaceLastAction(action)
                    return@switchMap Observable
                        .concat(
                            Observable.just(Action.StartLoading(action.info)),
                            Observable.just(action).delay(5, TimeUnit.SECONDS)
                        )
                }
                Observable.just(action)
            }
            .reduxStore(
                initialState, listOf()
            ) { state, action ->
                when (action) {
                    is Action.StartLoading -> {
                        getLoadingState(state, action.info)
                    }
                    is Action.InputAction -> {
                        replaceLastAction(action)
                        previousAction ?: run {
                            return@reduxStore state.submitInputAction(action)
                        }
                        calculateResultState()
                    }
                    else -> state
                }
            }.distinctUntilChanged()


    private fun replaceLastAction(action: Action.InputAction) {
        when (action) {
            is Action.FirstInput -> if (lastAction !is Action.FirstInput) {
                previousAction = lastAction
            }
            is Action.SecondInput -> if (lastAction !is Action.SecondInput) {
                previousAction = lastAction
            }
            is Action.ResultInput -> if (lastAction !is Action.ResultInput) {
                previousAction = lastAction
            }
        }
        lastAction = action
    }

    private fun getLoadingState(
        currentState: CalculatorState,
        allInputsInfo: AllInputsInfo
    ): CalculatorState =
        with(allInputsInfo) {
            currentState.copy(
                firstInput = firstDigit,
                secondInput = secondDigit,
                result = resultDigit,
                isLoading = true
            )
        }

    private fun calculateResultState(): CalculatorState {
        val actionList = listOf(lastAction, previousAction)
        var firstDigit = actionList.find { action -> action is Action.FirstInput }?.digit
        var secondDigit = actionList.find { action -> action is Action.SecondInput }?.digit
        var resultDigit = actionList.find { action -> action is Action.ResultInput }?.digit
        firstDigit ?: run {
            firstDigit = resultDigit!! - secondDigit!!
        }
        secondDigit ?: run {
            secondDigit = resultDigit!! - firstDigit!!
        }
        resultDigit ?: run {
            resultDigit = firstDigit!! + secondDigit!!
        }

        return CalculatorState(
            firstDigit,
            secondDigit,
            resultDigit
        )
    }


}
