package com.example.mvicalculator.calculator

sealed class Action {
    data class StartLoading(
        val info: AllInputsInfo
    ) : Action()

    sealed class InputAction(
        open val digit: Int
    ) : Action()

    data class FirstInput(
        override val digit: Int
    ) : InputAction(digit)

    data class SecondInput(
        override val digit: Int
    ) : InputAction(digit)

    open class ResultInput(
        override val digit: Int
    ) : InputAction(digit)

    data class DifficultResultInput(
        override val digit: Int,
        val info: AllInputsInfo
    ) : ResultInput(digit)
}
