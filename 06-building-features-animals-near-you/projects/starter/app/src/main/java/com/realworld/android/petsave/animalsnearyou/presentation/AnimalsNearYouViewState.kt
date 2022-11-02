package com.realworld.android.petsave.animalsnearyou.presentation

import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.common.presentation.model.UIAnimal

data class AnimalsNearYouViewState(
    val loading: Boolean = false,
    val animals: List<UIAnimal> = emptyList(),
    val noMoreAnimalsNearBy: Boolean = false,
    val failure: Event<Throwable>? = null
)
