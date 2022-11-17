package com.realworld.android.petsave.search.domain.usecases

import com.realworld.android.petsave.common.domain.model.animal.details.Age
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.search.domain.model.SearchFilter
import java.util.*
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 17-11-2022.
 */

class GetSearchFilters @Inject constructor(
    private val repository: AnimalRepository
) {

    companion object {
        const val NO_FILTER_ADDED = "Any"
    }

    suspend operator fun invoke(): SearchFilter {
        val unknown = Age.UNKNOWN.name
        val types = listOf(NO_FILTER_ADDED) + repository.getAnimalTypes()
        val ages = repository.getAnimalAges().map { age ->
            if (age.name == unknown) {
                NO_FILTER_ADDED
            } else {
                age.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }
            }
        }

        return SearchFilter(ages, types)
    }
}