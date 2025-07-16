package com.sample.android.network

import com.sample.android.network.request.UserRequest
import com.sample.android.network.response.UserResponse
import com.sample.android.network.response.UserResponseInfo
import com.sample.android.network.response.UserResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserServiceTest {

    @MockK
    private lateinit var mockRequest: UserRequest

    private lateinit var userService: UserService

    private val mockResponse = UserResponse(
        info = UserResponseInfo(
            page = 1,
            results = 10,
            seed = "test-seed",
            version = "1.0"
        ),
        results = listOf(
            UserResult(
                email = "test@example.com",
                gender = "male",
                cell = "123-456-7890",
                nat = "US",
                phone = "987-654-3210",
                dob = null,
                id = null,
                location = null,
                login = null,
                name = null,
                picture = null,
                registered = null
            )
        )
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        userService = UserServiceImpl()

        // Mock the UserRequest properties
        every { mockRequest.seed } returns "test-seed"
        every { mockRequest.page } returns 1
        every { mockRequest.results } returns 10

        // Mock NetworkModule
        mockkObject(NetworkModule)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `search should call NetworkModule with correct parameters and return UserResponse`() =
        runTest {
            // Given
            coEvery {
                NetworkModule.call<UserResponse>(
                    NetworkModule.Method.GET,
                    "/api",
                    queries = any()
                )
            } returns mockResponse

            // When
            val result = userService.search(mockRequest)

            // Then
            assertEquals(mockResponse, result)
            coVerify {
                NetworkModule.call<UserResponse>(
                    NetworkModule.Method.GET,
                    "/api",
                    queries = any()
                )
            }
    }

    @Test
    fun `search should handle NetworkCommonException with network failure`() = runTest {
        // Given
        val networkException = NetworkCommonException(
            code = NetworkCommonException.CODE_FAILED_NETWORK,
            message = "Network failure",
            cause = Exception("Connection timeout")
        )

        coEvery {
            NetworkModule.call<UserResponse>(
                NetworkModule.Method.GET,
                "/api",
                queries = any()
            )
        } throws networkException

        // When & Then
        try {
            userService.search(mockRequest)
            assertTrue("Expected NetworkCommonException to be thrown", false)
        } catch (e: NetworkCommonException) {
            assertEquals(NetworkCommonException.CODE_FAILED_NETWORK, e.code)
            assertEquals("Network failure", e.message)
            assertNotNull(e.cause)
        }
    }

    @Test
    fun `search should handle NetworkCommonException with JSON parsing failure`() = runTest {
        // Given
        val jsonException = NetworkCommonException(
            code = NetworkCommonException.CODE_FAILED_JSON_PARSING,
            message = "JSON parsing failed"
        )

        coEvery {
            NetworkModule.call<UserResponse>(
                NetworkModule.Method.GET,
                "/api",
                queries = any()
            )
        } throws jsonException

        // When & Then
        try {
            userService.search(mockRequest)
            assertTrue("Expected NetworkCommonException to be thrown", false)
        } catch (e: NetworkCommonException) {
            assertEquals(NetworkCommonException.CODE_FAILED_JSON_PARSING, e.code)
            assertEquals("JSON parsing failed", e.message)
        }
    }

    @Test
    fun `search should handle NetworkCommonException with null pointer error`() = runTest {
        // Given
        val nullPointerException = NetworkCommonException(
            code = NetworkCommonException.CODE_NULL_POINTER_ERROR,
            message = "Null pointer error"
        )

        coEvery {
            NetworkModule.call<UserResponse>(
                NetworkModule.Method.GET,
                "/api",
                queries = any()
            )
        } throws nullPointerException

        // When & Then
        try {
            userService.search(mockRequest)
            assertTrue("Expected NetworkCommonException to be thrown", false)
        } catch (e: NetworkCommonException) {
            assertEquals(NetworkCommonException.CODE_NULL_POINTER_ERROR, e.code)
            assertEquals("Null pointer error", e.message)
        }
    }

    @Test
    fun `search should handle empty response`() = runTest {
        // Given
        val emptyResponse = UserResponse(
            info = null,
            results = null
        )

        coEvery {
            NetworkModule.call<UserResponse>(
                NetworkModule.Method.GET,
                "/api",
                queries = any()
            )
        } returns emptyResponse

        // When
        val result = userService.search(mockRequest)

        // Then
        assertEquals(emptyResponse, result)
        assertEquals(null, result.info)
        assertEquals(null, result.results)
    }

    @Test
    fun `search should handle request with different parameters`() = runTest {
        // Given
        val customRequest = UserRequest(
            seed = "custom-seed",
            page = 2,
            results = 20
        )

        coEvery {
            NetworkModule.call<UserResponse>(
                NetworkModule.Method.GET,
                "/api",
                queries = any()
            )
        } returns mockResponse

        // When
        val result = userService.search(customRequest)

        // Then
        assertEquals(mockResponse, result)
        coVerify {
            NetworkModule.call<UserResponse>(
                NetworkModule.Method.GET,
                "/api",
                queries = any()
            )
        }
    }
}