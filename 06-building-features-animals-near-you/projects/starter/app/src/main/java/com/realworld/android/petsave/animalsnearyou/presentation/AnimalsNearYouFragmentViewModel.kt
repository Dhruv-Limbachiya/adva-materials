package com.realworld.android.petsave.animalsnearyou.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.logging.Logger
import com.realworld.android.petsave.animalsnearyou.domain.usecases.GetAnimals
import com.realworld.android.petsave.animalsnearyou.domain.usecases.RequestNextPageOfAnimals
import com.realworld.android.petsave.common.domain.model.NetworkException
import com.realworld.android.petsave.common.domain.model.NetworkUnavailableException
import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.domain.model.animal.Animal
import com.realworld.android.petsave.common.domain.model.pagination.Pagination
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.common.presentation.model.UIAnimal
import com.realworld.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.realworld.android.petsave.common.utils.DispatchersProvider
import com.realworld.android.petsave.common.utils.createExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 31-10-2022.
 */
@HiltViewModel
class AnimalsNearYouFragmentViewModel @Inject constructor(
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatcherProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable,
    private val requestNextPageOfAnimals: RequestNextPageOfAnimals,
    private val getAnimals: GetAnimals
) : ViewModel() {

    val state: LiveData<AnimalsNearYouViewState> get() = _state
    private val _state = MutableLiveData<AnimalsNearYouViewState>()

    private var currentPage = 0

    var isLastPage = false
    var isLoadingMoreAnimals = false

    init {
        _state.value = AnimalsNearYouViewState()
        subscribeToAnimalsUpdates()
    }

    companion object {
        const val UI_PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE
    }

    fun onEvent(event: AnimalsNearYouEvent) {
        when (event) {
            is AnimalsNearYouEvent.RequestInitialAnimalsList -> loadAnimals()
            is AnimalsNearYouEvent.RequestMoreAnimal -> loadNextAnimalPage()
        }
    }

    private fun subscribeToAnimalsUpdates() {
        viewModelScope.launch {
            getAnimals()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { onNewAnimalList(it) },
                    { onFailure(it) }
                )
                .addTo(compositeDisposable)
        }
    }

    private fun onNewAnimalList(animals: List<Animal>) {
        Logger.d("Got more animals!")
        val animalsNearYou = animals.map {
            uiAnimalMapper.mapToView(it)
        }
        val currentList = state.value!!.animals
        val newList = animalsNearYou.subtract(currentList.toSet())
        val updatedList = currentList + newList

        _state.value = state.value!!.copy(
            loading = false,
            animals = updatedList
        )
    }

    private fun loadAnimals() {
        if (_state.value!!.animals.isEmpty()) {
            loadNextAnimalPage()
        }
    }

    private fun loadNextAnimalPage() {
        isLoadingMoreAnimals = true
        val message = "Failed to load near by animals"
        val exceptionHandler = viewModelScope.createExceptionHandler(message) {
            onFailure(it)
        }
        viewModelScope.launch(exceptionHandler) {
            Logger.d("Requesting more animals.")
            val pagination = withContext(dispatcherProvider.io()) {
                requestNextPageOfAnimals(++currentPage)
            }
            onPageInfoObtained(pagination)
            isLoadingMoreAnimals = false
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