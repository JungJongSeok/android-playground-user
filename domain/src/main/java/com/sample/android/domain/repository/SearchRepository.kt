package com.sample.android.domain.repository

import com.sample.android.domain.entity.UserSearchResult

interface SearchRepository {
    suspend fun searchUsers(query: String, page: Int): UserSearchResult
}