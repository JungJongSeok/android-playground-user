package com.sample.android.network

import com.sample.android.network.extension.toMap
import com.sample.android.network.request.UserRequest
import com.sample.android.network.response.UserResponse


interface UserService {
    suspend fun search(request: UserRequest): UserResponse
}

class UserServiceImpl : UserService {
    override suspend fun search(request: UserRequest): UserResponse {
        return NetworkModule.call(
            NetworkModule.Method.GET,
            "https://randomuser.me/api",
            queries = request.toMap(),
        )
    }
}

