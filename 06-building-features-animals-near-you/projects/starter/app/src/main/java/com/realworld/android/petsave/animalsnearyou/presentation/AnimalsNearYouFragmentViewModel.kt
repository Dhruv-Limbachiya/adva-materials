package com.realworld.android.petsave.animalsnearyou.presentation

import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.logging.Logger.d
import com.realworld.android.petsave.animalsnearyou.domain.usecases.RequestNextPageOfAnimals
import com.realworld.android.petsave.common.domain.model.NetworkException
import com.realworld.android.petsave.common.domain.model.NetworkUnavailableException
import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.domain.model.pagination.Pagination
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.realworld.android.petsave.common.utils.DispatchersProvider
import com.realworld.android.petsave.common.utils.createExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 31-10-2022.
 */
@HiltViewModel
class AnimalsNearYouFragmentViewModel @Inject constructor(
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatcherProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable,
    private val requestNextPageOfAnimals: RequestNextPageOfAnimals
) : ViewModel() {

    val state: LiveData<AnimalsNearYouViewState> get() = _state
    private val _state = MutableLiveData<AnimalsNearYouViewState>()

    private var currentPage = 0

    init {
        _state.value = AnimalsNearYouViewState()
    }

    fun onEvent(event: AnimalsNearYouEvent) {
        when (event) {
            is AnimalsNearYouEvent.RequestInitialAnimalsList -> loadAnimals()
        }
    }

    private fun loadAnimals() {
        if (_state.value!!.animals.isEmpty()) {
            loadNextAnimalPage()
        }
    }

    private fun loadNextAnimalPage() {
        val message = "Failed to load near by animals"
        val exceptionHandler = viewModelScope.createExceptionHandler(message) {
            onFailure(it)
        }
        viewModelScope.launch(exceptionHandler) {
            com.realworld.android.logging.Logger.d("Requesting more animals.")
            val pagination = withContext(dispatcherProvider.io()) {
                requestNextPageOfAnimals(++currentPage)
            }
            onPageInfoObtained(pagination)
        }
    }

    private fun onPageInfoObtained(pagination: Pagination) {
        currentPage = pagination.currentPage
    }

    private fun onFailure(failure: Throwable) {
        when (failure) {
            is NetworkException,
            is NetworkUnavailableException -> {
                _state.value = state.value!!.copy(
                    loading = false,
                    failure = Event(failure)
                )
            }
            is NoMoreAnimalsException -> {
                _state.value = state.value!!.copy(
                    noMoreAnimalsNearBy = true,
                    failure = Event(failure)
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}