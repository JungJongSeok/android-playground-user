package com.sample.android.repository

import com.sample.android.data.UserMetaDataList
import com.sample.android.data.toData
import com.sample.android.network.UserService
import com.sample.android.network.request.UserRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface SearchRepository {
    suspend fun searchItem(userRequest: UserRequest): UserMetaDataList
}

class SearchRepositoryImpl(private val userService: UserService) : SearchRepository {
    override suspend fun searchItem(userRequest: UserRequest): UserMetaDataList {
        return withContext(Dispatchers.Unconfined) {
            val response = userService.search(userRequest)
            val data = response.results?.map { it.toData() } ?: emptyList()
            return@withContext UserMetaDataList(data)
        }
    }
}
