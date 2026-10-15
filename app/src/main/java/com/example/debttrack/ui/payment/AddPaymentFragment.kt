package com.example.debttrack.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.debttrack.R
import com.example.debttrack.databinding.FragmentAddPaymentBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/** Добавление платежа по долгу args.debtId. */
class AddPaymentFragment : Fragment() {

    private var _binding: FragmentAddPaymentBinding? = null
    private val binding get() = _binding!!
    private val args: AddPaymentFragmentArgs by navArgs()

    private val viewModel: AddPaymentViewModel by viewModels {
        AddPaymentViewModel.factory(args.debtId)
    }

    private var remainingAmount: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.confirmButton.setOnClickListener { onConfirmClicked() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading) return@collect
                    remainingAmount = state.remainingAmount
                    binding.remainingLabelText.text = "Осталось внести: ${state.remainingAmountText}"
                }
            }
        }
    }

    private fun onConfirmClicked() {
        val amount = binding.amountEditText.text.toString().trim().replace(',', '.').toDoubleOrNull()

        val isValid = amount != null && amount > 0.0 && amount <= remainingAmount
        binding.amountError.visibility = if (isValid) View.GONE else View.VISIBLE
        binding.amountEditText.background = ContextCompat.getDrawable(
            requireContext(),
            if (isValid) R.drawable.bg_input_field else R.drawable.bg_input_field_error
        )
        if (!isValid) return

        binding.confirmButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            val success = viewModel.submitPayment(amount!!)
            if (success) {
                findNavController().navigateUp()
            } else {
                binding.confirmButton.isEnabled = true
                Snackbar.make(binding.root, "Не удалось сохранить платёж. Попробуйте ещё раз", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
