package com.sample.android.domain.usecase

import com.sample.android.domain.entity.UserSearchResult
import com.sample.android.domain.repository.SearchRepository
import javax.inject.Inject

/**
 * Use case for searching users
 * Encapsulates the business logic for user search operations
 */
class SearchUsersUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    /**
     * Execute user search
     * @param query Search query
     * @param page Page number for pagination
     * @return UserSearchResult
     */
    suspend operator fun invoke(query: String, page: Int): UserSearchResult {
        require(query.isNotBlank()) { "Search query cannot be blank" }
        require(page > 0) { "Page number must be positive" }

        return searchRepository.searchUsers(query, page)
    }
}