package com.example.debttrack.ui.debtdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.debttrack.databinding.FragmentDebtDetailBinding

/**
 * Детали долга: полная информация, история платежей, редактирование/удаление.
 * Наполняется на Неделе 8. Принимает debtId через Safe Args.
 */
class DebtDetailFragment : Fragment() {

    private var _binding: FragmentDebtDetailBinding? = null
    private val binding get() = _binding!!
    private val args: DebtDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDebtDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO (Неделя 8): загрузить долг по args.debtId через DebtRepository
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
