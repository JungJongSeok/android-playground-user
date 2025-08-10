package com.sample.android.repository

import com.sample.android.domain.entity.UserSearchResult
import com.sample.android.domain.repository.SearchRepository
import com.sample.android.mapper.toUserSearchResult
import com.sample.android.network.UserService
import com.sample.android.network.request.UserRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val userService: UserService
) : SearchRepository {

    override suspend fun searchUsers(query: String, page: Int): UserSearchResult {
        return withContext(Dispatchers.IO) {
            val request = UserRequest(query, page)
            val response = userService.search(request)
            response.toUserSearchResult()
        }
    }
}