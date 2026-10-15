package com.example.debttrack.ui.statistics

import android.graphics.Color
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
import com.example.debttrack.R
import com.example.debttrack.adapters.TopDebtAdapter
import com.example.debttrack.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch

/** Экран статистики: суммы, круговая диаграмма распределения, топ долгов. */
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels { StatisticsViewModel.Factory }
    private lateinit var topDebtAdapter: TopDebtAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()
        setupTopDebtsList()
        observeUiState()
    }

    private fun setupChart() = with(binding.distributionChart) {
        description.isEnabled = false
        legend.isEnabled = false
        isRotationEnabled = false
        setTouchEnabled(false)
        setDrawEntryLabels(false)
        setUsePercentValues(false)
        holeRadius = 65f
        transparentCircleRadius = 68f
        setHoleColor(Color.TRANSPARENT)
        setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        setCenterTextSize(16f)
    }

    private fun setupTopDebtsList() {
        topDebtAdapter = TopDebtAdapter()
        binding.topDebtsRecyclerView.adapter = topDebtAdapter
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

    private fun renderState(state: StatisticsUiState) = with(binding) {
        iOweText.text = state.iOweText
        owedToMeText.text = state.owedToMeText
        overdueText.text = state.overdueText
        owedToMeLegendText.text = "Мне должны (${state.owedToMePercent}%)"
        iOweLegendText.text = "Я должен (${state.iOwePercent}%)"
        paidSummaryText.text = "Оплачено: ${state.paidText} из всех долгов"

        updateChart(state)

        topDebtAdapter.submitList(state.topDebts)
        val hasDebts = state.topDebts.isNotEmpty()
        topDebtsRecyclerView.visibility = if (hasDebts) View.VISIBLE else View.GONE
        noDebtsText.visibility = if (hasDebts) View.GONE else View.VISIBLE
    }

    private fun updateChart(state: StatisticsUiState) {
        val entries = listOf(
            PieEntry(state.owedToMePercent.toFloat(), "Мне должны"),
            PieEntry(state.iOwePercent.toFloat(), "Я должен")
        )

        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.color_owed_to_me),
            ContextCompat.getColor(requireContext(), R.color.color_i_owe)
        )

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            sliceSpace = 3f
            setDrawValues(false)
        }

        binding.distributionChart.apply {
            data = PieData(dataSet)
            centerText = if (state.hasAnyDebts) "100%" else "—"
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
