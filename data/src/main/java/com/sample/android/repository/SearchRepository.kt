package com.sample.android.repository

import com.sample.android.data.UserMetaDataList
import com.sample.android.data.toData
import com.sample.android.network.UserService
import com.sample.android.network.request.UserRequest
import kotlinx.coroutines.coroutineScope

interface SearchRepository {
    suspend fun searchItem(userRequest: UserRequest): UserMetaDataList
}

class SearchRepositoryImpl(private val userService: UserService) : SearchRepository {
    override suspend fun searchItem(userRequest: UserRequest): UserMetaDataList {
        return coroutineScope {
            val response = userService.search(userRequest)
            val data = response.results?.map { it.toData() } ?: emptyList()
            return@coroutineScope UserMetaDataList(data)
        }
    }
}
