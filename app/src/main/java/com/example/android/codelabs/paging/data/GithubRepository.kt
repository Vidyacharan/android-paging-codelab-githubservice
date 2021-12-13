/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.codelabs.paging.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.android.codelabs.paging.network.api.GithubService
import com.example.android.codelabs.paging.network.model.RepoApiModel
import com.example.android.codelabs.paging.network.GithubPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository class that works with local and remote data sources.
 */
class GithubRepository(private val service: GithubService) {

    /**
     * Search repositories whose names match the query, exposed as a stream of data that will emit
     * every time we get more data from the network.
     */

    fun getSearchResult(query: String): Flow<PagingData<RepoModel>> = Pager(
        config = PagingConfig(
            pageSize = NETWORK_PAGE_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { GithubPagingSource(service, query) }
    ).flow
        .map { pagingData ->
            pagingData.map { it.toRepoModel() }
        }

    private fun RepoApiModel.toRepoModel() = RepoModel(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        url = url,
        stars = stars,
        forks = forks,
        language = language
    )

    companion object {
        const val NETWORK_PAGE_SIZE = 30
    }
}
