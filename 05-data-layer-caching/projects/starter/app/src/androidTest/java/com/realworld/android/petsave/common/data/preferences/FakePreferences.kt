package com.realworld.android.petsave.common.data.preferences

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.realworld.android.petsave.common.data.cache.PetSaveDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class FakePreferences : Preferences {
    private val preferences = mutableMapOf<String, Any>()

    override fun putToken(token: String) {
        preferences[PreferencesConstants.KEY_TOKEN] = token
    }

    override fun putTokenExpirationTime(time: Long) {
        preferences[PreferencesConstants.KEY_TOKEN_EXPIRATION_TIME] = time
    }

    override fun putTokenType(tokenType: String) {
        preferences[PreferencesConstants.KEY_TOKEN_TYPE] = tokenType
    }

    override fun getToken(): String {
        return preferences[PreferencesConstants.KEY_TOKEN] as String
    }

    override fun getTokenExpirationTime(): Long {
        return preferences[PreferencesConstants.KEY_TOKEN_EXPIRATION_TIME] as Long
    }

    override fun getTokenType(): String {
        return preferences[PreferencesConstants.KEY_TOKEN_TYPE] as String
    }

    override fun deleteTokenInfo() {
        preferences.clear()
    }

}

@Module
@InstallIn(SingletonComponent::class)
object TestCacheModule {

    @Singleton
    @Provides
    fun provideRoomDatabase(): PetSaveDatabase {
        return Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            PetSaveDatabase::class.java
        ).allowMainThreadQueries().build()
    }

}