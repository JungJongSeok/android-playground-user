package com.sample.android.network.response

import com.google.gson.GsonBuilder
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class UserResponseTest {

    private val gson = GsonBuilder()
        .serializeNulls()
        .create()

    @Test
    fun `UserResponse deserializes with complete data`() {
        val jsonString = """
            {
                "info": {
                    "page": 1,
                    "results": 1,
                    "seed": "foobar",
                    "version": "1.4"
                },
                "results": [
                    {
                        "email": "test@example.com",
                        "gender": "male",
                        "name": {
                            "title": "Mr",
                            "first": "John",
                            "last": "Doe"
                        },
                        "phone": "+1234567890",
                        "cell": "+0987654321",
                        "nat": "US"
                    }
                ]
            }
        """

        val userResponse = gson.fromJson(jsonString, UserResponse::class.java)

        assertEquals(1, userResponse.info?.page)
        assertEquals(1, userResponse.info?.results)
        assertEquals("foobar", userResponse.info?.seed)
        assertEquals("1.4", userResponse.info?.version)

        assertEquals(1, userResponse.results?.size)
        val firstUser = userResponse.results?.first()
        assertEquals("test@example.com", firstUser?.email)
        assertEquals("male", firstUser?.gender)
        assertEquals("Mr", firstUser?.name?.title)
        assertEquals("John", firstUser?.name?.first)
        assertEquals("Doe", firstUser?.name?.last)
        assertEquals("+1234567890", firstUser?.phone)
        assertEquals("+0987654321", firstUser?.cell)
        assertEquals("US", firstUser?.nat)
    }

    @Test
    fun `UserResponse deserializes with null values`() {
        val jsonString = """
            {
                "info": null,
                "results": null
            }
        """

        val userResponse = gson.fromJson(jsonString, UserResponse::class.java)

        assertNull(userResponse.info)
        assertNull(userResponse.results)
    }

    @Test
    fun `UserResponseInfo deserializes correctly`() {
        val jsonString = """
            {
                "page": 2,
                "results": 10,
                "seed": "test-seed",
                "version": "1.4"
            }
        """

        val userInfo = gson.fromJson(jsonString, UserResponseInfo::class.java)

        assertEquals(2, userInfo.page)
        assertEquals(10, userInfo.results)
        assertEquals("test-seed", userInfo.seed)
        assertEquals("1.4", userInfo.version)
    }

    @Test
    fun `UserName deserializes correctly`() {
        val jsonString = """
            {
                "title": "Ms",
                "first": "Jane",
                "last": "Smith"
            }
        """

        val userName = gson.fromJson(jsonString, UserName::class.java)

        assertEquals("Ms", userName.title)
        assertEquals("Jane", userName.first)
        assertEquals("Smith", userName.last)
    }

    @Test
    fun `UserPicture deserializes correctly`() {
        val jsonString = """
            {
                "large": "https://example.com/large.jpg",
                "medium": "https://example.com/medium.jpg",
                "thumbnail": "https://example.com/thumb.jpg"
            }
        """

        val userPicture = gson.fromJson(jsonString, UserPicture::class.java)

        assertEquals("https://example.com/large.jpg", userPicture.large)
        assertEquals("https://example.com/medium.jpg", userPicture.medium)
        assertEquals("https://example.com/thumb.jpg", userPicture.thumbnail)
    }

    @Test
    fun `UserLocation deserializes correctly`() {
        val jsonString = """
            {
                "city": "New York",
                "state": "NY",
                "country": "US",
                "postcode": "10001",
                "coordinates": {
                    "latitude": "40.7128",
                    "longitude": "-74.0060"
                },
                "street": {
                    "number": 123,
                    "name": "Main St"
                },
                "timezone": {
                    "offset": "-4:00",
                    "description": "Eastern Time"
                }
            }
        """

        val userLocation = gson.fromJson(jsonString, UserLocation::class.java)

        assertEquals("New York", userLocation.city)
        assertEquals("NY", userLocation.state)
        assertEquals("US", userLocation.country)
        assertEquals("10001", userLocation.postcode)
        assertEquals("40.7128", userLocation.coordinates?.latitude)
        assertEquals("-74.0060", userLocation.coordinates?.longitude)
        assertEquals(123, userLocation.street?.number)
        assertEquals("Main St", userLocation.street?.name)
        assertEquals("-4:00", userLocation.timezone?.offset)
        assertEquals("Eastern Time", userLocation.timezone?.description)
    }

    @Test
    fun `UserDob deserializes correctly`() {
        val jsonString = """
            {
                "date": "1990-01-01T00:00:00Z",
                "age": 33
            }
        """

        val userDob = gson.fromJson(jsonString, UserDob::class.java)

        assertEquals("1990-01-01T00:00:00Z", userDob.date)
        assertEquals(33, userDob.age)
    }

    @Test
    fun `UserLogin deserializes correctly`() {
        val jsonString = """
            {
                "uuid": "12345678-1234-1234-1234-123456789012",
                "username": "testuser",
                "password": "testpass",
                "salt": "testsalt",
                "md5": "testmd5",
                "sha1": "testsha1",
                "sha256": "testsha256"
            }
        """

        val userLogin = gson.fromJson(jsonString, UserLogin::class.java)

        assertEquals("12345678-1234-1234-1234-123456789012", userLogin.uuid)
        assertEquals("testuser", userLogin.username)
        assertEquals("testpass", userLogin.password)
        assertEquals("testsalt", userLogin.salt)
        assertEquals("testmd5", userLogin.md5)
        assertEquals("testsha1", userLogin.sha1)
        assertEquals("testsha256", userLogin.sha256)
    }

    @Test
    fun `UserId deserializes correctly`() {
        val jsonString = """
            {
                "name": "SSN",
                "value": "123-45-6789"
            }
        """

        val userId = gson.fromJson(jsonString, UserId::class.java)

        assertEquals("SSN", userId.name)
        assertEquals("123-45-6789", userId.value)
    }

    @Test
    fun `UserRegistered deserializes correctly`() {
        val jsonString = """
            {
                "date": "2020-01-01T00:00:00Z",
                "age": 3
            }
        """

        val userRegistered = gson.fromJson(jsonString, UserRegistered::class.java)

        assertEquals("2020-01-01T00:00:00Z", userRegistered.date)
        assertEquals(3, userRegistered.age)
    }

    @Test
    fun `UserResult serializes correctly`() {
        val userResult = UserResult(
            email = "test@example.com",
            gender = "female",
            name = UserName(title = "Dr", first = "Alice", last = "Johnson"),
            phone = "+1111111111",
            cell = "+2222222222",
            nat = "CA",
            dob = UserDob(date = "1985-05-15T00:00:00Z", age = 38),
            id = UserId(name = "ID", value = "ABC123"),
            location = null,
            login = null,
            picture = null,
            registered = null
        )

        val jsonObj = gson.toJsonTree(userResult).asJsonObject

        assertEquals("test@example.com", jsonObj["email"].asString)
        assertEquals("female", jsonObj["gender"].asString)
        assertEquals("Dr", jsonObj["name"].asJsonObject["title"].asString)
        assertEquals("Alice", jsonObj["name"].asJsonObject["first"].asString)
        assertEquals("Johnson", jsonObj["name"].asJsonObject["last"].asString)
        assertEquals("+1111111111", jsonObj["phone"].asString)
        assertEquals("+2222222222", jsonObj["cell"].asString)
        assertEquals("CA", jsonObj["nat"].asString)
    }
}