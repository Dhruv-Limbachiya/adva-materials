package com.realworld.android.petsave.search.domain.model

import com.realworld.android.petsave.common.domain.model.animal.Animal

/**
 * Created by Dhruv Limbachiya on 18-11-2022.
 */
data class SearchResults(
    val animals: List<Animal>,
    val searchParameters: SearchParameters
)

data class SearchParameters(
    val name: String,
    val age: String,
    val type: String
)