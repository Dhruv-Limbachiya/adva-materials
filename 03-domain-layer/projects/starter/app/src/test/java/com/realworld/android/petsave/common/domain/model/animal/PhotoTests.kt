package com.realworld.android.petsave.common.domain.model.animal

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoTests {
    private val mediumPhoto = "mediumPhoto"
    private val fullPhoto = "fullPhoto"
    private val invalidPhoto = "" // what’s tested in

    @Test
    fun photo_getSmallestAvailablePhoto_hasMediumPhoto() {
        // Give
        val photo = Media.Photo(mediumPhoto, fullPhoto)
        val expectedValue = mediumPhoto
        // When
        val smallestPhoto = photo.getSmallestAvailablePhoto()
        // Then
        assertEquals(smallestPhoto, expectedValue)
    }

    @Test
    fun photo_getSmallestAvailablePhoto_noMediumPhoto() {
        // Given
        val photo = Media.Photo(invalidPhoto, fullPhoto)
        val expectedValue = fullPhoto
        // When
        val smallestPhoto = photo.getSmallestAvailablePhoto()
        // Then
        assertEquals(smallestPhoto, expectedValue)
    }

    @Test
    fun photo_getSmallestAvailablePhoto_noPhotos() {
        // Given
        val photo = Media.Photo(invalidPhoto, invalidPhoto)
        val expectedValue = Media.Photo.NO_SIZE_AVAILABLE
        // When
        val smallestPhoto = photo.getSmallestAvailablePhoto()
        // Then
        assertEquals(smallestPhoto, expectedValue)
    }
}