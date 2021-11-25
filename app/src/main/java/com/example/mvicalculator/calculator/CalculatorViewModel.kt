package com.example.mvicalculator.calculator

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mvicalculator.*
import com.freeletics.rxredux.reduxStore
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class CalculatorViewModel : ViewModel() {
    val initialState = CalculatorState()

    var previousAction: InputAction? = null
    var lastAction: InputAction? = null

    // var actionEmitter : BehaviorSubject<Action> = BehaviorSubject.create()

    fun observeState(upstreamActions: Observable<Action>): Observable<CalculatorState> =
        upstreamActions
            .switchMap { action ->
                Log.d("MYTAG", "switch map $action")
                if (action is DifficultResultInput) {
                    replaceLastAction(action)
                    return@switchMap Observable
                        .concat(
                            Observable.just(StartLoading(action.info)),
                            Observable.just(action).delay(5, TimeUnit.SECONDS)
                        )
                }
                Observable.just(action)
            }
            .reduxStore(
                initialState, listOf()
            ) { state, action ->
                Log.d("MYTAG", "redux action: $action")
                when (action) {
                    is StartLoading -> {
                        getLoadingState(state, action.info)
                    }
                    is InputAction -> {
                        replaceLastAction(action)
                        previousAction ?: run {
                            return@reduxStore state.submitInputAction(action)
                        }
                        calculateResultState()
                    }
                    else -> state
                }
            }.distinctUntilChanged()


    fun replaceLastAction(action: InputAction) {
        when (action) {
            is FirstInput -> if (lastAction !is FirstInput) {
                previousAction = lastAction
            }
            is SecondInput -> if (lastAction !is SecondInput) {
                previousAction = lastAction
            }
            is ResultInput -> if (lastAction !is ResultInput) {
                previousAction = lastAction
            }
        }
        lastAction = action
    }

    fun getLoadingState(
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

    fun calculateResultState(): CalculatorState {
        val actionList = listOf(lastAction, previousAction)
        var firstDigit = actionList.find { action -> action is FirstInput }?.digit
        var secondDigit = actionList.find { action -> action is SecondInput }?.digit
        var resultDigit = actionList.find { action -> action is ResultInput }?.digit
        Log.d(
            "MYTAG",
            "calculateResult state last actions: $actionList \n first: :$firstDigit \n second: $secondDigit third: $resultDigit"
        )
        firstDigit ?: run {
            firstDigit = resultDigit!! - secondDigit!!
        }
        secondDigit ?: run {
            secondDigit = resultDigit!! - firstDigit!!
        }
        resultDigit ?: run {
            resultDigit = firstDigit!! + secondDigit!!
        }
        Log.d(
            "MYTAG",
            "calculateResult result:: first: :$firstDigit \n second: $secondDigit third: $resultDigit"
        )

        return CalculatorState(
            firstDigit,
            secondDigit,
            resultDigit
        )
    }


}