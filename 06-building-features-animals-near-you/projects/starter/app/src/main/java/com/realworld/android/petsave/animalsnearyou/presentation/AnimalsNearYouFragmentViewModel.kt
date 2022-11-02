package com.realworld.android.petsave.animalsnearyou.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.petsave.common.domain.model.NetworkException
import com.realworld.android.petsave.common.domain.model.NetworkUnavailableException
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.realworld.android.petsave.common.utils.DispatchersProvider
import com.realworld.android.petsave.common.utils.createExceptionHandler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch

/**
 * Created by Dhruv Limbachiya on 31-10-2022.
 */
class AnimalsNearYouFragmentViewModel constructor(
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatcherProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    val state: LiveData<AnimalsNearYouViewState> get() = _state
    private val _state = MutableLiveData<AnimalsNearYouViewState>()

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
        }
    }

    private fun onFailure(failure: Throwable) {
        when(failure) {
            is NetworkException,
            is NetworkUnavailableException -> {
                _state.value = state.value!!.copy(
                    loading = false,
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