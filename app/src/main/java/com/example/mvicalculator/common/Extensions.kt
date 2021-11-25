package com.example.mvicalculator

import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import com.example.mvicalculator.calculator.CalculatorState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addTo(disposable: CompositeDisposable) {
    disposable.add(this)
}

fun Editable.getInt(): Int? {
    if (this.isNotEmpty() && this.toString() != "-") {
        if (this.startsWith("-")) return this.toString().substring(1).toInt() * -1
        return this.toString().toInt()
    }
    return null
}

fun CalculatorState.submitInputAction(action: InputAction): CalculatorState =
    when (action) {
        is FirstInput -> this.copy(firstInput = action.digit)
        is SecondInput -> this.copy(secondInput = action.digit)
        is ResultInput -> this.copy(result = action.digit)
        is DifficultResultInput -> this.copy(result = action.digit)
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