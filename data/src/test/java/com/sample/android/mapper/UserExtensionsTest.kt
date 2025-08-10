package com.sample.android.mapper

import com.sample.android.network.response.UserDob
import com.sample.android.network.response.UserId
import com.sample.android.network.response.UserLogin
import com.sample.android.network.response.UserPicture
import com.sample.android.network.response.UserResponse
import com.sample.android.network.response.UserResponseInfo
import com.sample.android.network.response.UserResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for UserExtensions mapper functions
 */
class UserExtensionsTest {

    @Test
    fun `UserResponse toUserSearchResult converts network response correctly`() {
        // Given
        val userResult1 = UserResult(
            id = UserId(name = "SSN", value = "id1"),
            login = UserLogin(
                md5 = "md5hash1",
                password = "password1",
                salt = "salt1",
                sha1 = "sha1hash1",
                sha256 = "sha256hash1",
                username = "user1",
                uuid = "uuid1"
            ),
            email = "user1@example.com",
            picture = UserPicture(
                large = "https://avatar.com/user1/large",
                medium = "https://avatar.com/user1/medium",
                thumbnail = "https://avatar.com/user1"
            ),
            cell = "123-456-789",
            dob = UserDob(age = 25, date = "1998-01-01"),
            gender = "male",
            location = null,
            name = null,
            nat = null,
            phone = null,
            registered = null
        )

        val userResult2 = UserResult(
            id = UserId(name = "SSN", value = "id2"),
            login = UserLogin(
                md5 = "md5hash2",
                password = "password2",
                salt = "salt2",
                sha1 = "sha1hash2",
                sha256 = "sha256hash2",
                username = "user2",
                uuid = "uuid2"
            ),
            email = "user2@example.com",
            picture = UserPicture(
                large = "https://avatar.com/user2/large",
                medium = "https://avatar.com/user2/medium",
                thumbnail = "https://avatar.com/user2"
            ),
            cell = "987-654-321",
            dob = UserDob(age = 30, date = "1993-05-15"),
            gender = "female",
            location = null,
            name = null,
            nat = null,
            phone = null,
            registered = null
        )

        val userResponse = UserResponse(
            info = UserResponseInfo(1, 2, "test", "1.0"),
            results = listOf(userResult1, userResult2)
        )

        // When
        val userSearchResult = userResponse.toUserSearchResult()

        // Then
        assertEquals(2, userSearchResult.users.size)
        assertEquals(2, userSearchResult.totalCount)
        assertFalse(userSearchResult.incompleteResults)
    }

    @Test
    fun `UserResponse toUserSearchResult handles empty results`() {
        // Given
        val userResponse = UserResponse(
            info = UserResponseInfo(1, 0, "test", "1.0"),
            results = emptyList()
        )

        // When
        val userSearchResult = userResponse.toUserSearchResult()

        // Then
        assertTrue(userSearchResult.users.isEmpty())
        assertEquals(0, userSearchResult.totalCount)
        assertFalse(userSearchResult.incompleteResults)
    }

    @Test
    fun `UserResponse toUserSearchResult handles null results`() {
        // Given
        val userResponse = UserResponse(
            info = UserResponseInfo(1, 0, "test", "1.0"),
            results = null
        )

        // When
        val userSearchResult = userResponse.toUserSearchResult()

        // Then
        assertTrue(userSearchResult.users.isEmpty())
        assertEquals(0, userSearchResult.totalCount)
        assertFalse(userSearchResult.incompleteResults)
    }

    @Test
    fun `UserResult toUser converts network model to domain entity correctly`() {
        // Given
        val userResult = UserResult(
            id = UserId(name = "SSN", value = "test-id"),
            login = UserLogin(
                md5 = "md5hash",
                password = "password",
                salt = "salt",
                sha1 = "sha1hash",
                sha256 = "sha256hash",
                username = "testuser",
                uuid = "testuuid"
            ),
            email = "testuser@example.com",
            picture = UserPicture(
                large = "https://avatar.com/testuser/large",
                medium = "https://avatar.com/testuser/medium",
                thumbnail = "https://avatar.com/testuser"
            ),
            cell = "123-456-789",
            dob = UserDob(age = 28, date = "1995-03-10"),
            gender = "male",
            location = null,
            name = null,
            nat = null,
            phone = null,
            registered = null
        )

        // When
        val user = userResult.toUser()

        // Then
        assertEquals("testuser", user.login)
        assertEquals("https://avatar.com/testuser", user.avatarUrl)
        assertEquals("testuser@example.com", user.htmlUrl)
        assertEquals("user", user.type)
        assertEquals(0.0, user.score, 0.0)
        assertEquals("test-id".hashCode().toLong(), user.id)
    }

    @Test
    fun `UserResult toUser handles null id correctly`() {
        // Given
        val userResult = UserResult(
            id = null,
            login = UserLogin(
                md5 = "md5hash",
                password = "password",
                salt = "salt",
                sha1 = "sha1hash",
                sha256 = "sha256hash",
                username = "testuser",
                uuid = "testuuid"
            ),
            email = "testuser@example.com",
            picture = UserPicture(
                large = "https://avatar.com/testuser/large",
                medium = "https://avatar.com/testuser/medium",
                thumbnail = "https://avatar.com/testuser"
            ),
            cell = null,
            dob = null,
            gender = null,
            location = null,
            name = null,
            nat = null,
            phone = null,
            registered = null
        )

        // When
        val user = userResult.toUser()

        // Then
        assertEquals("testuser", user.login)
        assertEquals("https://avatar.com/testuser", user.avatarUrl)
        assertEquals("testuser@example.com", user.htmlUrl)
        assertEquals("user", user.type)
        assertEquals(0.0, user.score, 0.0)
        assertEquals(0L, user.id)
    }

    @Test
    fun `UserResult toUser handles null login correctly`() {
        // Given
        val userResult = UserResult(
            id = UserId(name = "SSN", value = "test-id"),
            login = null,
            email = "user@example.com",
            picture = UserPicture(
                large = "https://avatar.com/user/large",
                medium = "https://avatar.com/user/medium",
                thumbnail = "https://avatar.com/user"
            ),
            cell = null,
            dob = null,
            gender = null,
            location = null,
            name = null,
            nat = null,
            phone = null,
            registered = null
        )

        // When
        val user = userResult.toUser()

        // Then
        assertEquals("", user.login)
        assertEquals("https://avatar.com/user", user.avatarUrl)
        assertEquals("user@example.com", user.htmlUrl)
        assertEquals("user", user.type)
        assertEquals(0.0, user.score, 0.0)
        assertEquals("test-id".hashCode().toLong(), user.id)
    }

    @Test
    fun `UserResult toUser handles null picture correctly`() {
        // Given
        val userResult = UserResult(
            id = UserId(name = "SSN", value = "test-id"),
            login = UserLogin(
                md5 = "md5hash",
                password = "password",
                salt = "salt",
                sha1 = "sha1hash",
                sha256 = "sha256hash",
                username = "testuser",
                uuid = "testuuid"
            ),
            email = "testuser@example.com",
            picture = null,
            cell = null,
            dob = null,
            gender = null,
            location = null,
            name = null,
            nat = null,
            phone = null,
            registered = null
        )

        // When
        val user = userResult.toUser()

        // Then
        assertEquals("testuser", user.login)
        assertEquals("", user.avatarUrl)
        assertEquals("testuser@example.com", user.htmlUrl)
        assertEquals("user", user.type)
        assertEquals(0.0, user.score, 0.0)
        assertEquals("test-id".hashCode().toLong(), user.id)
    }

    @Test
    fun `UserResult toUser handles all null values correctly`() {
        // Given
        val userResult = UserResult(
            id = null,
            login = null,
            email = null,
            picture = null,
            cell = null,
            dob = null,
            gender = null,
            location = null,
            name = null,
            nat = null,
            phone = null,
            registered = null
        )

        // When
        val user = userResult.toUser()

        // Then
        assertEquals("", user.login)
        assertEquals("", user.avatarUrl)
        assertEquals("", user.htmlUrl)
        assertEquals("user", user.type)
        assertEquals(0.0, user.score, 0.0)
        assertEquals(0L, user.id)
    }
}