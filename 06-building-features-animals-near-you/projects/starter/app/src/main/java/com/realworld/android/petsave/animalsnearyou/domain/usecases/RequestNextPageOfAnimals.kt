package com.realworld.android.petsave.animalsnearyou.domain.usecases

import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.domain.model.pagination.Pagination
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 02-11-2022.
 */
class RequestNextPageOfAnimals @Inject constructor(
    private val animalRepository: AnimalRepository
)  {
    suspend operator fun invoke(
        pageToLoad: Int,
        pageSize: Int = Pagination.DEFAULT_PAGE_SIZE
    ) : Pagination {
        val (animals,pagination) = animalRepository.requestMoreAnimals(pageToLoad,pageSize)

        if(animals.isEmpty()) {
            throw NoMoreAnimalsException("No animals nearby :(")
        }

        animalRepository.storeAnimals(animals)

        return pagination
    }
}