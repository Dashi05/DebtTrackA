package com.example.debttrack.ui.debtform

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.debttrack.R
import com.example.debttrack.data.entities.Category
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.databinding.FragmentDebtFormBinding
import com.example.debttrack.utils.DateFormatter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Форма долга: используется и для создания (Неделя 7), и для редактирования (Неделя 9).
 * debtId == -1L означает режим создания, иначе — режим редактирования существующего долга.
 */
class DebtFormFragment : Fragment() {

    private var _binding: FragmentDebtFormBinding? = null
    private val binding get() = _binding!!
    private val args: DebtFormFragmentArgs by navArgs()
    private val viewModel: DebtFormViewModel by viewModels { DebtFormViewModel.Factory }

    private val isEditMode: Boolean get() = args.debtId != -1L

    private var currentCategories: List<Category> = emptyList()
    private var selectedDueDateMillis: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDebtFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleText.text = if (isEditMode) "Редактировать долг" else "Новый долг"
        binding.saveButton.text = if (isEditMode) "Сохранить изменения" else "Сохранить долг"
        binding.typeOwedToMeButton.isChecked = true

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        setupDueDateField()
        observeCategories()

        binding.saveButton.setOnClickListener { onSaveClicked() }

        // TODO (Неделя 9): если isEditMode — загрузить долг по args.debtId и предзаполнить поля
    }

    private fun setupDueDateField() {
        binding.dueDateField.setOnClickListener {
            val calendar = Calendar.getInstance()
            selectedDueDateMillis?.let { calendar.timeInMillis = it }

            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val picked = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    selectedDueDateMillis = picked.timeInMillis
                    binding.dueDateText.text = DateFormatter.formatDate(picked.timeInMillis)
                    binding.dueDateField.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_input_field)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    currentCategories = categories
                    binding.categorySpinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        categories.map { it.name }
                    )
                }
            }
        }
    }

    private fun onSaveClicked() {
        var isValid = true

        val personName = binding.personNameEditText.text.toString().trim()
        if (personName.isEmpty()) {
            setFieldError(binding.personNameEditText, binding.personNameError, true)
            isValid = false
        } else {
            setFieldError(binding.personNameEditText, binding.personNameError, false)
        }

        val amount = binding.amountEditText.text.toString().trim().replace(',', '.').toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            setFieldError(binding.amountEditText, binding.amountError, true)
            isValid = false
        } else {
            setFieldError(binding.amountEditText, binding.amountError, false)
        }

        if (selectedDueDateMillis == null) {
            Snackbar.make(binding.root, "Укажите срок возврата", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        if (currentCategories.isEmpty()) {
            Snackbar.make(binding.root, "Список категорий ещё загружается, попробуйте снова", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid) return

        val type = if (binding.typeOwedToMeButton.isChecked) DebtType.OWED_TO_ME else DebtType.I_OWE
        val category = currentCategories[binding.categorySpinner.selectedItemPosition]
        val description = binding.descriptionEditText.text.toString().trim()

        binding.saveButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.createDebt(
                    personName = personName,
                    type = type,
                    amount = amount!!,
                    categoryId = category.id,
                    description = description,
                    dueDate = selectedDueDateMillis!!
                )
                findNavController().navigateUp()
            } catch (e: Exception) {
                binding.saveButton.isEnabled = true
                Snackbar.make(binding.root, "Не удалось сохранить долг. Попробуйте ещё раз", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setFieldError(field: EditText, errorText: TextView, hasError: Boolean) {
        errorText.visibility = if (hasError) View.VISIBLE else View.GONE
        field.background = ContextCompat.getDrawable(
            requireContext(),
            if (hasError) R.drawable.bg_input_field_error else R.drawable.bg_input_field
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
