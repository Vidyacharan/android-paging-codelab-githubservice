package com.example.android.codelabs.paging.network

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.android.codelabs.paging.data.GithubRepository.Companion.NETWORK_PAGE_SIZE
import com.example.android.codelabs.paging.network.api.GITHUB_STARTING_PAGE_INDEX
import com.example.android.codelabs.paging.network.api.GithubService
import com.example.android.codelabs.paging.network.api.IN_QUALIFIER
import com.example.android.codelabs.paging.network.model.RepoApiModel
import retrofit2.HttpException
import java.io.IOException


/**
 * To build the PagingSource you need to define the following:
 * The type of the paging key - in our case, the Github API uses 1-based index numbers for pages, so the type is Int.
 * The type of data loaded - in our case, we're loading Repo items.
 * Where is the data retrieved from - we're getting the data from GithubService.
 * Our data source will be specific to a certain query, so we need to make sure we're also passing the query information to GithubService.
 */

class GithubPagingSource(
    private val service: GithubService,
    private val query: String
) : PagingSource<Int, RepoApiModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RepoApiModel> {
        val currentKey = params.key ?: GITHUB_STARTING_PAGE_INDEX
        val apiQuery = query + IN_QUALIFIER
        Log.d(TAG, "currentKey: $currentKey, Load Size: ${params.loadSize}")

        return try {
            val response = service.searchRepos(apiQuery, currentKey, params.loadSize)
            val repoItems = response.items
            Log.d(TAG, "Response items size: ${repoItems.size}")

            val nextPosition = if (repoItems.isNullOrEmpty()) {
                null
            } else {
                Log.d(TAG, "Current Key: $currentKey, Addition: ${(params.loadSize / NETWORK_PAGE_SIZE)}")
                currentKey + (params.loadSize / NETWORK_PAGE_SIZE)
            }
            LoadResult.Page(
                data = repoItems,
                prevKey = if (currentKey == GITHUB_STARTING_PAGE_INDEX) null else currentKey - 1,
                nextKey = nextPosition
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RepoApiModel>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}

private val TAG = GithubPagingSource::class.java.name