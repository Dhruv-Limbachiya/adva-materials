package com.realworld.android.petsave.common.data.repositories

import com.realworld.android.petsave.common.data.api.PetFinderApi
import com.realworld.android.petsave.common.data.api.model.mappers.ApiAnimalMapper
import com.realworld.android.petsave.common.data.api.model.mappers.ApiPaginationMapper
import com.realworld.android.petsave.common.data.cache.Cache
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import com.realworld.android.petsave.common.data.cache.model.cachedorganization.CachedOrganization
import com.realworld.android.petsave.common.domain.model.animal.Animal
import com.realworld.android.petsave.common.domain.model.animal.details.AnimalWithDetails
import com.realworld.android.petsave.common.domain.model.pagination.PaginatedAnimals
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import io.reactivex.Flowable
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 03-10-2022.
 */
class PetFinderAnimalRepository @Inject constructor(
    private val cache: Cache,
    private val petFinderApi: PetFinderApi,
    private val apiAnimalMapper: ApiAnimalMapper,
    private val apiPaginationMapper: ApiPaginationMapper
) : AnimalRepository {

    private val postalCode = "07097"
    private val maxDistanceInMiles = 100 // 3

    override fun getAnimals(): Flowable<List<Animal>> {
        return cache.getNearbyAnimals().map { animalList ->
            animalList.map { animal ->
                animal.animal.toAnimalDomain(
                    photos = animal.photos,
                    videos = animal.videos,
                    tags = animal.tags
                )
            }
        }
    }

    override suspend fun storeAnimals(animals: List<AnimalWithDetails>) {
        cache.storeNearbyAnimals(
            animals.map {
                CachedAnimalAggregate.fromDomain(it)
            }
        )

        cache.storeOrganizations(
            animals.map {
                CachedOrganization.fromDomain(
                    it.details.organization
                )
            }
        )
    }

    override suspend fun requestMoreAnimals(pageToLoad: Int, numberOfItems: Int): PaginatedAnimals {
        val (apiAnimals, apiPagination) = petFinderApi.getNearbyAnimals(
            pageToLoad,
            numberOfItems,
            postalCode,
            maxDistanceInMiles
        )

        return PaginatedAnimals(
            apiAnimals?.map {
                apiAnimalMapper.mapToDomain(it)
            } ?: emptyList(),
            apiPaginationMapper.mapToDomain(apiPagination)
        )
    }

}