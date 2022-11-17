/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.realworld.android.petsave.animalsnearyou.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.realworld.android.petsave.R
import com.realworld.android.petsave.common.presentation.AnimalsAdapter
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.databinding.FragmentAnimalsNearYouBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimalsNearYouFragment : Fragment() {

    companion object {
        private const val ITEMS_PER_ROW = 2
        private const val TAG = "AnimalsNearYouFragment"
    }

    private val binding get() = _binding!!

    private var _binding: FragmentAnimalsNearYouBinding? = null

    private val viewModel: AnimalsNearYouFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimalsNearYouBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        requestInitialAnimalList()
    }

    private fun setupUI() {
        val adapter = createAdapter();
        setRecyclerView(adapter)
        observeViewStateUpdates(adapter)
    }


    private fun requestInitialAnimalList() {
        viewModel.onEvent(AnimalsNearYouEvent.RequestInitialAnimalsList)
    }

    private fun createAdapter(): AnimalsAdapter = AnimalsAdapter()

    private fun setRecyclerView(animalsAdapter: AnimalsAdapter) {
        binding.animalsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), ITEMS_PER_ROW)
            adapter = animalsAdapter
            setHasFixedSize(true)
            addOnScrollListener(createInfiniteScrollListener(layoutManager as GridLayoutManager))
        }
    }

    private fun createInfiniteScrollListener(layoutManager: GridLayoutManager) =
        object : InfiniteScrollListener(
            layoutManager,
            AnimalsNearYouFragmentViewModel.UI_PAGE_SIZE
        ) {
            override fun loadMoreItems() {
                requestMoreAnimals()
            }

            override fun isLastPage(): Boolean {
              return viewModel.isLastPage
            }

            override fun isLoading(): Boolean {
               return viewModel.isLoadingMoreAnimals
            }
        }

    private fun requestMoreAnimals() {
        viewModel.onEvent(AnimalsNearYouEvent.RequestMoreAnimal)
    }

    private fun observeViewStateUpdates(adapter: AnimalsAdapter) {
        viewModel.state.observe(viewLifecycleOwner) { animalNearYouViewState ->
            animalNearYouViewState?.let {
                updateScreenState(it, adapter)
            }
        }
    }

    private fun updateScreenState(viewState: AnimalsNearYouViewState, adapter: AnimalsAdapter) {
        binding.progressBar.isVisible = viewState.loading
        adapter.submitList(viewState.animals)
        handleNoMoreAnimalsNearBy(viewState.noMoreAnimalsNearBy)
        handleFailure(viewState.failure)
    }

    private fun handleFailure(failure: Event<Throwable>?) {
        val unhandledFailure = failure?.getContentIfNotHandled() ?: return
        val fallbackMessage = getString(R.string.an_error_occurred)
        val snackBarMessage = if (!unhandledFailure.message.isNullOrEmpty()) {
            unhandledFailure.message!!
        } else {
            fallbackMessage
        }

        if (snackBarMessage.isNotEmpty()) {
            Snackbar.make(requireView(), snackBarMessage, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun handleNoMoreAnimalsNearBy(noMoreAnimalsNearBy: Boolean) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
