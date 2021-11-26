package com.example.mvicalculator.calculator

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.mvicalculator.common.applyWithoutTextWatcherTriggering
import com.example.mvicalculator.common.getInt
import com.example.mvicalculator.common.setDigit
import com.example.mvicalculator.databinding.FragmentCalculatorBinding
import com.google.android.material.internal.TextWatcherAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class CalculatorFragment : Fragment() {

    private lateinit var binding: FragmentCalculatorBinding

    private lateinit var viewModel: CalculatorViewModel
    private val disposables = CompositeDisposable()

    private val actionSubject: Subject<Action> = PublishSubject.create()

    private lateinit var firstTextWatcher: TextWatcher
    private lateinit var secondTextWatcher: TextWatcher
    private lateinit var resultTextWatcher: TextWatcher

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel =
            ViewModelProvider(this).get(CalculatorViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        disposables.add(viewModel
            .observeState(actionSubject.hide())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                with(binding) {
                    etFirst.applyWithoutTextWatcherTriggering(firstTextWatcher) {
                        etFirst.setDigit(state.firstInput)
                    }
                    etSecond.applyWithoutTextWatcherTriggering(secondTextWatcher) {
                        etSecond.setDigit(state.secondInput)
                    }
                    etResult.applyWithoutTextWatcherTriggering(resultTextWatcher) {
                        etResult.setDigit(state.result)
                    }
                    pb.isVisible = state.isLoading
                }
            })
    }

    private fun initViews() {
        with(binding) {
            firstTextWatcher = createTextWatcher {
                it?.getInt()?.let { digit ->
                    actionSubject.onNext(Action.FirstInput(digit))
                }
            }
            etFirst.addTextChangedListener(firstTextWatcher)
            secondTextWatcher = createTextWatcher {
                it?.getInt()?.let { digit ->
                    actionSubject.onNext(Action.SecondInput(digit))
                }
            }
            etSecond.addTextChangedListener(secondTextWatcher)
            resultTextWatcher = createTextWatcher {
                it?.getInt()?.let { digit ->
                    if (digit < 0) actionSubject.onNext(
                        Action.DifficultResultInput(
                            digit,
                            getAllInputsInfo()
                        )
                    )
                    else actionSubject.onNext(Action.ResultInput(digit))
                }
            }
            etResult.addTextChangedListener(resultTextWatcher)
        }
    }

    private fun getAllInputsInfo(): AllInputsInfo =
        with(binding) {
            AllInputsInfo(
                etFirst.text?.getInt(),
                etSecond.text?.getInt(),
                etResult.text?.getInt()
            )
        }

    private fun createTextWatcher(listener: (Editable?) -> Unit): TextWatcher {
        return object : TextWatcherAdapter() {
            override fun afterTextChanged(newText: Editable) {
                listener.invoke(newText)
            }
        }
    }

    companion object {

        fun newInstance(): CalculatorFragment =
            CalculatorFragment()
    }
}
