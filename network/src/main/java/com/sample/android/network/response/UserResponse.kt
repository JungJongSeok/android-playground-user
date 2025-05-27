package com.sample.android.network.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("info")
    val info: UserResponseInfo?,
    @SerializedName("results")
    val results: List<UserResult>?
)

data class UserResponseInfo(
    @SerializedName("page")
    val page: Int?,
    @SerializedName("results")
    val results: Int?,
    @SerializedName("seed")
    val seed: String?,
    @SerializedName("version")
    val version: String?
)

data class UserResult(
    @SerializedName("cell")
    val cell: String?,
    @SerializedName("dob")
    val dob: UserDob?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("id")
    val id: UserId?,
    @SerializedName("location")
    val location: UserLocation?,
    @SerializedName("login")
    val login: UserLogin?,
    @SerializedName("name")
    val name: UserName?,
    @SerializedName("nat")
    val nat: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("picture")
    val picture: UserPicture?,
    @SerializedName("registered")
    val registered: UserRegistered?
)

data class UserDob(
    @SerializedName("age")
    val age: Int?,
    @SerializedName("date")
    val date: String?
)

data class UserId(
    @SerializedName("name")
    val name: String?,
    @SerializedName("value")
    val value: String?
)

data class UserLocation(
    @SerializedName("city")
    val city: String?,
    @SerializedName("coordinates")
    val coordinates: UserCoordinates?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("postcode")
    val postcode: String?,
    @SerializedName("state")
    val state: String?,
    @SerializedName("street")
    val street: UserStreet?,
    @SerializedName("timezone")
    val timezone: UserTimezone?
)

data class UserLogin(
    @SerializedName("md5")
    val md5: String?,
    @SerializedName("password")
    val password: String?,
    @SerializedName("salt")
    val salt: String?,
    @SerializedName("sha1")
    val sha1: String?,
    @SerializedName("sha256")
    val sha256: String?,
    @SerializedName("username")
    val username: String?,
    @SerializedName("uuid")
    val uuid: String?
)

data class UserName(
    @SerializedName("first")
    val first: String?,
    @SerializedName("last")
    val last: String?,
    @SerializedName("title")
    val title: String?
)

data class UserPicture(
    @SerializedName("large")
    val large: String?,
    @SerializedName("medium")
    val medium: String?,
    @SerializedName("thumbnail")
    val thumbnail: String?
)

data class UserRegistered(
    @SerializedName("age")
    val age: Int?,
    @SerializedName("date")
    val date: String?
)

data class UserCoordinates(
    @SerializedName("latitude")
    val latitude: String?,
    @SerializedName("longitude")
    val longitude: String?
)

data class UserStreet(
    @SerializedName("name")
    val name: String?,
    @SerializedName("number")
    val number: Int?
)

data class UserTimezone(
    @SerializedName("description")
    val description: String?,
    @SerializedName("offset")
    val offset: String?
)