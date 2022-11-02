package com.realworld.android.petsave.animalsnearyou.presentation

/**
 * Created by Dhruv Limbachiya on 31-10-2022.
 */

sealed class AnimalsNearYouEvent {
    object RequestInitialAnimalsList : AnimalsNearYouEvent()
}