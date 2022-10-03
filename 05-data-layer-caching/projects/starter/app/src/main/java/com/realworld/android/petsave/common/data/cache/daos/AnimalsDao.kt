package com.realworld.android.petsave.common.data.cache.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.*
import io.reactivex.Flowable

/**
 * Created by Dhruv Limbachiya on 03-10-2022.
 */
@Dao
abstract class AnimalsDao {

    @Transaction
    @Query("SELECT * FROM animals")
    abstract fun getAllAnimals(): Flowable<List<CachedAnimalAggregate>>

    @Insert
    abstract suspend fun insertAnimalAggregate(
        animalWithDetails: CachedAnimalWithDetails,
        photos: List<CachedPhoto>,
        videos: List<CachedVideo>,
        tags: List<CachedTag>
    )

    suspend fun insertAnimalsWithDetails(animalAggregates: List<CachedAnimalAggregate>) {
        for (animalAggregate in animalAggregates) {
            insertAnimalAggregate(
                animalWithDetails = animalAggregate.animal,
                photos = animalAggregate.photos,
                videos = animalAggregate.videos,
                tags = animalAggregate.tags
            )
        }
    }
}