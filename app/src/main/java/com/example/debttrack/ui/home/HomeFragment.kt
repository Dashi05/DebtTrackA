package com.example.debttrack.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.debttrack.R
import com.example.debttrack.adapters.DebtListAdapter
import com.example.debttrack.data.entities.Category
import com.example.debttrack.data.entities.DebtStatus
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.data.repository.DebtSortBy
import com.example.debttrack.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

/**
 * Главный экран: список долгов, сводка "Мне должны/Я должен/Чистый баланс",
 * поиск, фильтры (тип/категория/статус) и сортировка.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    private lateinit var adapter: DebtListAdapter
    private var currentCategories: List<Category> = emptyList()
    private var currentSortBy: DebtSortBy = DebtSortBy.DUE_DATE

    private val searchDebounceHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var searchDebounceRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupList()
        setupSearch()
        setupTypeFilter()
        setupStatusFilter()
        setupSortButton()
        setupFab()
        observeCategories()
        observeUiState()
    }

    private fun setupList() {
        adapter = DebtListAdapter(
            onItemClick = { item ->
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeToDebtDetail(item.id)
                )
            }
        )
        binding.debtsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.debtsRecyclerView.adapter = adapter
        binding.debtsRecyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.addDebtFab.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeToDebtForm())
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchDebounceRunnable?.let { searchDebounceHandler.removeCallbacks(it) }
                val runnable = Runnable { viewModel.onQueryChanged(s?.toString().orEmpty()) }
                searchDebounceRunnable = runnable
                searchDebounceHandler.postDelayed(runnable, 300)
            }
        })
    }

    private fun setupTypeFilter() {
        binding.typeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val type = when (checkedIds.firstOrNull()) {
                binding.chipOwedToMe.id -> DebtType.OWED_TO_ME
                binding.chipIOwe.id -> DebtType.I_OWE
                else -> null
            }
            viewModel.onTypeFilterChanged(type)
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    currentCategories = categories
                    val labels = listOf("Все категории") + categories.map { it.name }
                    binding.categoryFilterSpinner.adapter = ArrayAdapter(
                        requireContext(), android.R.layout.simple_spinner_dropdown_item, labels
                    )
                    binding.categoryFilterSpinner.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?, view: View?, position: Int, id: Long
                        ) {
                            val categoryId = if (position == 0) null else currentCategories[position - 1].id
                            viewModel.onCategoryFilterChanged(categoryId)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            }
        }
    }

    private fun setupStatusFilter() {
        val labels = listOf("Все статусы", "Активный", "Частично", "Погашен", "Просрочено")
        val statuses = listOf(null, DebtStatus.ACTIVE, DebtStatus.PARTIAL, DebtStatus.PAID, DebtStatus.OVERDUE)

        binding.statusFilterSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, labels
        )
        binding.statusFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.onStatusFilterChanged(statuses[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSortButton() {
        binding.sortButton.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            popup.menu.add(0, 0, 0, "По сроку возврата")
            popup.menu.add(0, 1, 1, "По сумме")
            popup.menu.add(0, 2, 2, "По имени")
            popup.setOnMenuItemClickListener { menuItem ->
                currentSortBy = when (menuItem.itemId) {
                    1 -> DebtSortBy.AMOUNT
                    2 -> DebtSortBy.NAME
                    else -> DebtSortBy.DUE_DATE
                }
                viewModel.onSortChanged(currentSortBy)
                true
            }
            popup.show()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.items)

                    binding.owedToMeText.text = state.owedToMeText
                    binding.iOweText.text = state.iOweText
                    binding.netBalanceText.text = state.netBalanceText
                    binding.netBalanceText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (state.netBalanceIsPositive) R.color.color_owed_to_me else R.color.color_i_owe
                        )
                    )

                    binding.debtsRecyclerView.visibility = if (state.isEmpty) View.GONE else View.VISIBLE
                    binding.emptyStateLayout.visibility = if (state.isEmpty) View.VISIBLE else View.GONE

                    if (state.isEmpty) {
                        binding.emptyStateTitle.text = if (state.hasActiveFilters) {
                            "Ничего не найдено"
                        } else {
                            "Пока нет ни одного долга"
                        }
                        binding.emptyStateSubtitle.text = if (state.hasActiveFilters) {
                            "Попробуйте изменить поиск или фильтры"
                        } else {
                            "Нажмите «+», чтобы добавить первый долг"
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchDebounceRunnable?.let { searchDebounceHandler.removeCallbacks(it) }
        _binding = null
    }
}
