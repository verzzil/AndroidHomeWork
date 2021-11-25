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
import com.example.mvicalculator.*
import com.example.mvicalculator.databinding.FragmentCalculatorBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class CalculatorFragment : Fragment() {

    private var _binding: FragmentCalculatorBinding? = null

    private lateinit var viewModel: CalculatorViewModel

    private val binding get() = _binding!!

    private val actionSubject: Subject<Action> = PublishSubject.create()

    private val disposables = CompositeDisposable()

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
    ): View? {

        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel
            .observeState(actionSubject.hide())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                with(binding) {
                    etFirst.applyWithoutTextWatcherTriggering(firstTextWatcher) {
                        etFirst.setDigit(
                            state.firstInput
                        )
                    }
                    etSecond.applyWithoutTextWatcherTriggering(secondTextWatcher) {
                        etSecond.setDigit(
                            state.secondInput
                        )
                    }
                    etResult.applyWithoutTextWatcherTriggering(resultTextWatcher) {
                        etResult.setDigit(
                            state.result
                        )
                    }
                    pb.isVisible = state.isLoading
                }
            }.addTo(disposables)
    }

    override fun onStart() {
        super.onStart()

        with(binding) {
            firstTextWatcher = createTextWatcher {
                it?.getInt()?.let { digit ->
                    actionSubject.onNext(FirstInput(digit))
                }
            }
            etFirst.addTextChangedListener(firstTextWatcher)
            secondTextWatcher = createTextWatcher {
                it?.getInt()?.let { digit ->
                    actionSubject.onNext(SecondInput(digit))
                }
            }
            etSecond.addTextChangedListener(secondTextWatcher)
            resultTextWatcher = createTextWatcher {
                it?.getInt()?.let { digit ->
                    if (digit < 0) actionSubject.onNext(
                        DifficultResultInput(
                            digit,
                            getAllInputsInfo()
                        )
                    )
                    else actionSubject.onNext(ResultInput(digit))
                }
            }
            etResult.addTextChangedListener(resultTextWatcher)
        }
    }

    fun getAllInputsInfo(): AllInputsInfo =
        with(binding) {
            AllInputsInfo(
                etFirst.text?.getInt(),
                etSecond.text?.getInt(),
                etResult.text?.getInt()
            )
        }

    fun createTextWatcher(listener: (Editable?) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                listener.invoke(p0)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}