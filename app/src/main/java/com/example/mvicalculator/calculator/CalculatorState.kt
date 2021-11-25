package com.example.mvicalculator.calculator

data class CalculatorState(
    val firstInput: Int? = null,
    val secondInput: Int? = null,
    val result: Int? = null,
    val isLoading: Boolean = false
)
