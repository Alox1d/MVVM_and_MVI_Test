package ru.otus.tomvi.presentation.start

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.otus.tomvi.databinding.FragmentCharactersBinding
import ru.otus.tomvi.getServiceLocator
import ru.otus.tomvi.presentation.CharactersAdapter
import ru.otus.tomvi.presentation.OnFavoriteClickListener
import ru.otus.tomvi.presentation.UiState

class CharactersFragment : Fragment() {

    private var _binding: FragmentCharactersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharactersViewModel by viewModels(
        factoryProducer = { getServiceLocator().provideViewModelFactory() })

    private val adapter = CharactersAdapter(FavoriteClickListener())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharactersBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uiRecyclerView.adapter = adapter
        binding.uiRecyclerView.layoutManager = LinearLayoutManager(context)

        subscribeUI()

        binding.uiSwipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(
                searchName: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                viewModel.refresh(searchName.toString())
            }

            override fun afterTextChanged(s: Editable?) = Unit

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun subscribeUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(State.STARTED) {

                viewModel.state.collectLatest { state: UiState ->
                    when (state) {
                        UiState.Loading -> showLoading()

                        is UiState.Success -> {
                            adapter.submitList(state.characters)
                            showList()
                        }

                        is UiState.Error -> showError()
                    }
//                    when {
//                        state.isLoading -> showLoading()
//                        else -> {
//                            adapter.submitList(state.characters)
//                            showList()
//                        }
//                    }

//                    if (state.hasError) {
//                        showError()
//                        viewModel.errorHasShown()
//                    }
                }
            }
        }
    }

    private fun showLoading() {
        hideAll()
        binding.uiProgressBar.visibility = View.VISIBLE
    }

    private fun showList() {
        hideAll()
        binding.uiRecyclerView.visibility = View.VISIBLE
    }

    private fun showError() {
        hideAll()
        Toast.makeText(
            requireContext(), "Error wile loading data", Toast.LENGTH_SHORT
        ).show()
    }

    private fun hideAll() {
        binding.uiRecyclerView.visibility = View.GONE
        binding.uiProgressBar.visibility = View.GONE
        binding.uiMessage.visibility = View.GONE
        binding.uiSwipeRefreshLayout.isRefreshing = false
    }

    inner class FavoriteClickListener : OnFavoriteClickListener {
        override fun onClick(id: Long, favorite: Boolean) {
            if (favorite) {
                viewModel.addToFavorites(id)
            } else {
                viewModel.removeFromFavorites(id)
            }
        }
    }
}
