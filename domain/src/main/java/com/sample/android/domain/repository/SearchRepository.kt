package com.sample.android.domain.repository

import com.sample.android.domain.entity.UserSearchResult

/**
 * Domain repository interface for search operations
 * Defines the contract for search functionality without implementation details
 */
interface SearchRepository {
    /**
     * Search for users by query with pagination
     * @param query Search query string
     * @param page Page number for pagination
     * @return UserSearchResult containing users and metadata
     */
    suspend fun searchUsers(query: String, page: Int): UserSearchResult
}