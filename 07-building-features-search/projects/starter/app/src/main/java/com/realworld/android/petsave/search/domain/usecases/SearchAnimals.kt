package com.realworld.android.petsave.search.domain.usecases

import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.search.domain.model.SearchParameters
import com.realworld.android.petsave.search.domain.model.SearchResults
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 18-11-2022.
 */
class SearchAnimals @Inject constructor(
    private val animalRepository: AnimalRepository
) {

    operator fun invoke(
        querySubject: BehaviorSubject<String>,
        ageSubject: BehaviorSubject<String>,
        typeSubject: BehaviorSubject<String>
    ): Flowable<SearchResults> {

        val query = querySubject
            .debounce(500L, TimeUnit.MILLISECONDS)
            .map { it.trim() }
            .filter { it.length >= 2 }

        val age = ageSubject.removeUIEmptyValue()
        val type = typeSubject.removeUIEmptyValue()

        return Observable.combineLatest(query,age,type,combiningFunction)
            .toFlowable(BackpressureStrategy.LATEST)
            .switchMap { parameters: SearchParameters ->
                animalRepository.searchCachedAnimalsBy(parameters)
            }
    }

    private val combiningFunction: Function3<String, String, String, SearchParameters> =
        Function3 { name, age, type ->
            SearchParameters(name, age, type)
        }

    private fun BehaviorSubject<String>.removeUIEmptyValue() = map {
        if (it == GetSearchFilters.NO_FILTER_ADDED) "" else it
    }
}