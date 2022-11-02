package com.realworld.android.petsave.common.data.repositories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.realworld.android.petsave.common.data.api.PetFinderApi
import com.realworld.android.petsave.common.data.api.model.mappers.ApiAnimalMapper
import com.realworld.android.petsave.common.data.api.model.mappers.ApiPaginationMapper
import com.realworld.android.petsave.common.data.api.utils.FakeServer
import com.realworld.android.petsave.common.data.cache.Cache
import com.realworld.android.petsave.common.data.cache.PetSaveDatabase
import com.realworld.android.petsave.common.data.cache.RoomCache
import com.realworld.android.petsave.common.data.di.CacheModule
import com.realworld.android.petsave.common.data.di.PreferencesModule
import com.realworld.android.petsave.common.data.preferences.FakePreferences
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import java.time.Instant
import javax.inject.Inject


/**
 * Created by Dhruv Limbachiya on 03-10-2022.
 */

@HiltAndroidTest // Marks the test class for injection.
@UninstallModules(
    PreferencesModule::class,
    CacheModule::class
) // uninstall the previously(original) installed module, so that we can test it with test module.
class PetFinderAnimalRepositoryTest {
    private val fakeServer = FakeServer()
    private lateinit var repository: AnimalRepository
    private lateinit var api: PetFinderApi

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var cache: Cache

    @Inject
    lateinit var database: PetSaveDatabase

    @Inject
    lateinit var retrofitBuilder: Retrofit.Builder

    @Inject
    lateinit var apiAnimalMapper: ApiAnimalMapper

    @Inject
    lateinit var apiPaginationMapper: ApiPaginationMapper

    @BindValue
    @JvmField
    val preferences = FakePreferences()

    @Before
    fun setup() {
        fakeServer.start();

        preferences.deleteTokenInfo()
        preferences.putToken("valid token")
        preferences.putTokenExpirationTime(Instant.now().plusSeconds(3600).epochSecond)
        preferences.putTokenType("Bearer")

        hiltRule.inject();

        api = retrofitBuilder.baseUrl(fakeServer.baseEndpoint)
            .build()
            .create(PetFinderApi::class.java)

        cache = RoomCache(
            animalsDao = database.animalsDao(),
            organizationsDao = database.organizationsDao()
        )

        repository = PetFinderAnimalRepository(cache, api, apiAnimalMapper, apiPaginationMapper)
    }

    @After
    fun teardown() {
        fakeServer.shutdown()
    }

    @Test
    fun requestMoreAnimals_successful() {
        runBlocking {
            // Given
            val expectedAnimalId = 124
            fakeServer.setHappyPathDispatcher()
            // When
            val animals = repository.requestMoreAnimals(1, 100)
            // Then
            val animal = animals.animals.first()
            assertThat(animal.id).isEqualTo(expectedAnimalId)
        }
    }

    @Test
    fun insertAnimals_successful() {
        // Given
        val expectedAnimalId = 124L
        runBlocking {
            fakeServer.setHappyPathDispatcher()
            // When
            val animals = repository.requestMoreAnimals(1, 100)
            val animal = animals.animals.first()
            repository.storeAnimals(listOf(animal))
        }

        val animalObserver =
            repository.getAnimals().test() // subscribe to getAnimals() flowable stream

        animalObserver.assertNoErrors()
        animalObserver.assertNotComplete()
        animalObserver.assertValue { it.first().id == expectedAnimalId }
    }
}