package com.example.mvicalculator.common

import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import com.example.mvicalculator.calculator.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Editable.getInt(): Int? {
    if (this.isNotEmpty() && this.toString() != "-") {
        if (this.startsWith("-")) return this.toString().substring(1).toInt() * -1
        return this.toString().toInt()
    }
    return null
}

fun CalculatorState.submitInputAction(action: Action.InputAction): CalculatorState =
    when (action) {
        is Action.FirstInput -> this.copy(firstInput = action.digit)
        is Action.SecondInput -> this.copy(secondInput = action.digit)
        is Action.ResultInput -> this.copy(result = action.digit)
        is Action.DifficultResultInput -> this.copy(result = action.digit)
    }

fun AppCompatEditText.setDigit(digit: Int?) {
    if (this.editableText.getInt() != digit) this.setText(digit.toString())
}

fun AppCompatEditText.applyWithoutTextWatcherTriggering(
    textWatcher: TextWatcher,
    codeBlock: () -> Unit
) {
    this.removeTextChangedListener(textWatcher)
    codeBlock()
    this.addTextChangedListener(textWatcher)
}
