package com.sample.android.repository

import com.sample.android.data.toData
import com.sample.android.network.UserService
import com.sample.android.network.request.UserRequest
import com.sample.android.network.response.UserDob
import com.sample.android.network.response.UserResponse
import com.sample.android.network.response.UserResponseInfo
import com.sample.android.network.response.UserResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SearchRepositoryTest {
    @MockK(relaxed = true)
    lateinit var userService: UserService

    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        repository = SearchRepositoryImpl(userService)
    }

    @Test
    fun `user documents are empty, users is empty and isEnd true`() =
        runTest {
            val userRequest = UserRequest(seed = "seed", page = 1)
            val response = UserResponse(
                info = UserResponseInfo(
                    page = 1,
                    results = 1,
                    seed = "seed",
                    version = "version"
                ),
                results = emptyList()
            )
            coEvery { userService.search(userRequest) } returns response

            val result = repository.searchItem(userRequest)

            assertTrue(result.users.isEmpty())
        }

    @Test
    fun `user have documents, they are merged and sorted by timestamp`() = runTest {
        val userRequest = UserRequest(seed = "test", page = 1)

        val data1 = UserResult(
            cell = null,
            dob = UserDob(age = null, date = "2023-05-21T09:42:29.000+09:00"),
            email = null,
            gender = null,
            id = null,
            location = null,
            login = null,
            name = null,
            nat = null,
            phone = null,
            picture = null,
            registered = null
        )
        val data2 = UserResult(
            cell = null,
            dob = UserDob(age = null, date = "2023-05-18T09:42:29.000+09:00"),
            email = null,
            gender = null,
            id = null,
            location = null,
            login = null,
            name = null,
            nat = null,
            phone = null,
            picture = null,
            registered = null
        )
        val data3 = UserResult(
            cell = null,
            dob = UserDob(age = null, date = "2023-05-20T09:42:29.000+09:00"),
            email = null,
            gender = null,
            id = null,
            location = null,
            login = null,
            name = null,
            nat = null,
            phone = null,
            picture = null,
            registered = null
        )

        val dataList = listOf(data1, data2, data3)
        val response = UserResponse(
            info = UserResponseInfo(
                page = 1,
                results = 1,
                seed = "seed",
                version = "version"
            ),
            results = dataList
        )

        coEvery { userService.search(userRequest) } returns response

        val result = repository.searchItem(userRequest)
        assertEquals(
            listOf(data1.toData(), data3.toData(), data2.toData()),
            result.users.sortedByDescending { it.timestamp })
    }
}