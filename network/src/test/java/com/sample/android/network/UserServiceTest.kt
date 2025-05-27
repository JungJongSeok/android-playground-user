package com.sample.android.network

import com.sample.android.network.request.UserRequest
import com.sample.android.network.response.UserResponse
import com.sample.android.network.response.UserResponseInfo
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class UserServiceTest {
    @MockK(relaxed = true)
    lateinit var mockRequest: UserRequest

    private lateinit var service: UserService

    private val response = UserResponse(
        info = UserResponseInfo(
            page = 1,
            results = 1,
            seed = "seed",
            version = "version"
        ),
        results = emptyList()
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        service = object : UserService {
            override suspend fun search(request: UserRequest): UserResponse {
                return response
            }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `search should delegate to NetworkModule and return UserResponse`() = runTest {
        val result = service.search(mockRequest)
        assertEquals(response, result)
    }
}