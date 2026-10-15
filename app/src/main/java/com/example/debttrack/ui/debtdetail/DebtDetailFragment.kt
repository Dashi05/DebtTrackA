package com.example.debttrack.ui.debtdetail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.debttrack.R
import com.example.debttrack.adapters.PaymentAdapter
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.databinding.FragmentDebtDetailBinding
import kotlinx.coroutines.launch

/**
 * Детали долга: полная информация, история платежей, редактирование/удаление.
 * Принимает debtId через Safe Args.
 */
class DebtDetailFragment : Fragment() {

    private var _binding: FragmentDebtDetailBinding? = null
    private val binding get() = _binding!!
    private val args: DebtDetailFragmentArgs by navArgs()

    private val viewModel: DebtDetailViewModel by viewModels {
        DebtDetailViewModel.factory(args.debtId)
    }

    private lateinit var paymentAdapter: PaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDebtDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStaticRowLabels()
        setupPaymentsList()

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        binding.addPaymentButton.setOnClickListener {
            findNavController().navigate(
                DebtDetailFragmentDirections.actionDetailToAddPayment(args.debtId)
            )
        }

        binding.editButton.setOnClickListener {
            findNavController().navigate(
                DebtDetailFragmentDirections.actionDetailToDebtForm(args.debtId)
            )
        }

        binding.deleteButton.setOnClickListener { confirmDelete() }

        observeUiState()
    }

    private fun setupStaticRowLabels() = with(binding) {
        rowWhoOwes.iconImage.setImageResource(R.drawable.ic_person)
        rowWhoOwes.labelText.text = "Кто должен"

        rowCreatedDate.iconImage.setImageResource(R.drawable.ic_calendar)
        rowCreatedDate.labelText.text = "Дата создания"

        rowDueDate.iconImage.setImageResource(R.drawable.ic_calendar)
        rowDueDate.labelText.text = "Срок возврата"

        rowDaysLeft.iconImage.setImageResource(R.drawable.ic_clock)
        rowDaysLeft.labelText.text = "Осталось дней"

        rowCategory.iconImage.setImageResource(R.drawable.ic_category)
        rowCategory.labelText.text = "Категория"

        rowDescription.iconImage.setImageResource(R.drawable.ic_description)
        rowDescription.labelText.text = "Описание"
    }

    private fun setupPaymentsList() {
        paymentAdapter = PaymentAdapter()
        binding.paymentsRecyclerView.adapter = paymentAdapter
        binding.paymentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading) return@collect
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: DebtDetailUiState) = with(binding) {
        val headerColorRes = if (state.debtType == DebtType.OWED_TO_ME) {
            R.color.color_owed_to_me
        } else {
            R.color.color_i_owe
        }
        headerContainer.setBackgroundColor(ContextCompat.getColor(requireContext(), headerColorRes))

        personNameText.text = state.personName
        statusBadgeText.text = state.statusText
        remainingAmountText.text = state.remainingAmountText
        totalAmountText.text = state.totalAmountText
        progressIndicator.progress = state.progressPercent

        rowWhoOwes.valueText.text = state.whoOwesText
        rowCreatedDate.valueText.text = state.createdDateText
        rowDueDate.valueText.text = state.dueDateText
        rowDaysLeft.valueText.text = state.daysLeftText
        rowCategory.valueText.text = state.categoryText
        rowDescription.valueText.text = state.descriptionText

        paymentAdapter.submitList(state.payments)
        val hasPayments = state.payments.isNotEmpty()
        paymentsRecyclerView.visibility = if (hasPayments) View.VISIBLE else View.GONE
        noPaymentsText.visibility = if (hasPayments) View.GONE else View.VISIBLE
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить долг?")
            .setMessage("Это действие нельзя отменить. Вся история платежей по этому долгу также будет удалена.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deleteDebt()
                    findNavController().navigateUp()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
