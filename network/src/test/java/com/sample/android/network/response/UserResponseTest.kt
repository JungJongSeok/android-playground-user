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

    @Test
    fun `UserResult deserializes with complete nested data`() {
        val jsonString = """
            {
                "email": "complete@example.com",
                "gender": "female",
                "name": {
                    "title": "Dr",
                    "first": "Sarah",
                    "last": "Wilson"
                },
                "location": {
                    "city": "Los Angeles",
                    "state": "CA",
                    "country": "US",
                    "postcode": "90210",
                    "coordinates": {
                        "latitude": "34.0522",
                        "longitude": "-118.2437"
                    },
                    "street": {
                        "number": 456,
                        "name": "Sunset Blvd"
                    },
                    "timezone": {
                        "offset": "-8:00",
                        "description": "Pacific Time"
                    }
                },
                "login": {
                    "uuid": "abc-123-def-456",
                    "username": "sarahw",
                    "password": "securepass",
                    "salt": "randomsalt",
                    "md5": "hash123",
                    "sha1": "hash456",
                    "sha256": "hash789"
                },
                "dob": {
                    "date": "1990-12-25T00:00:00Z",
                    "age": 33
                },
                "registered": {
                    "date": "2015-06-15T10:30:00Z",
                    "age": 8
                },
                "id": {
                    "name": "SSN",
                    "value": "555-66-7777"
                },
                "picture": {
                    "large": "https://example.com/large.jpg",
                    "medium": "https://example.com/medium.jpg",
                    "thumbnail": "https://example.com/thumb.jpg"
                },
                "phone": "+1-555-0123",
                "cell": "+1-555-0456",
                "nat": "US"
            }
        """

        val userResult = gson.fromJson(jsonString, UserResult::class.java)

        assertEquals("complete@example.com", userResult.email)
        assertEquals("female", userResult.gender)
        assertEquals("+1-555-0123", userResult.phone)
        assertEquals("+1-555-0456", userResult.cell)
        assertEquals("US", userResult.nat)

        assertEquals("Dr", userResult.name?.title)
        assertEquals("Sarah", userResult.name?.first)
        assertEquals("Wilson", userResult.name?.last)

        assertEquals("Los Angeles", userResult.location?.city)
        assertEquals("CA", userResult.location?.state)
        assertEquals("US", userResult.location?.country)
        assertEquals("90210", userResult.location?.postcode)
        assertEquals("34.0522", userResult.location?.coordinates?.latitude)
        assertEquals("-118.2437", userResult.location?.coordinates?.longitude)
        assertEquals(456, userResult.location?.street?.number)
        assertEquals("Sunset Blvd", userResult.location?.street?.name)
        assertEquals("-8:00", userResult.location?.timezone?.offset)
        assertEquals("Pacific Time", userResult.location?.timezone?.description)

        assertEquals("abc-123-def-456", userResult.login?.uuid)
        assertEquals("sarahw", userResult.login?.username)
        assertEquals("securepass", userResult.login?.password)
        assertEquals("randomsalt", userResult.login?.salt)
        assertEquals("hash123", userResult.login?.md5)
        assertEquals("hash456", userResult.login?.sha1)
        assertEquals("hash789", userResult.login?.sha256)

        assertEquals("1990-12-25T00:00:00Z", userResult.dob?.date)
        assertEquals(33, userResult.dob?.age)
        assertEquals("2015-06-15T10:30:00Z", userResult.registered?.date)
        assertEquals(8, userResult.registered?.age)

        assertEquals("SSN", userResult.id?.name)
        assertEquals("555-66-7777", userResult.id?.value)

        assertEquals("https://example.com/large.jpg", userResult.picture?.large)
        assertEquals("https://example.com/medium.jpg", userResult.picture?.medium)
        assertEquals("https://example.com/thumb.jpg", userResult.picture?.thumbnail)
    }

    @Test
    fun `UserResult deserializes with all null fields`() {
        val jsonString = """
            {
                "email": null,
                "gender": null,
                "name": null,
                "location": null,
                "login": null,
                "dob": null,
                "registered": null,
                "id": null,
                "picture": null,
                "phone": null,
                "cell": null,
                "nat": null
            }
        """

        val userResult = gson.fromJson(jsonString, UserResult::class.java)

        assertNull(userResult.email)
        assertNull(userResult.gender)
        assertNull(userResult.name)
        assertNull(userResult.location)
        assertNull(userResult.login)
        assertNull(userResult.dob)
        assertNull(userResult.registered)
        assertNull(userResult.id)
        assertNull(userResult.picture)
        assertNull(userResult.phone)
        assertNull(userResult.cell)
        assertNull(userResult.nat)
    }

    @Test
    fun `UserResponse deserializes with empty results array`() {
        val jsonString = """
            {
                "info": {
                    "page": 1,
                    "results": 0,
                    "seed": "emptyseed",
                    "version": "1.4"
                },
                "results": []
            }
        """

        val userResponse = gson.fromJson(jsonString, UserResponse::class.java)

        assertEquals(1, userResponse.info?.page)
        assertEquals(0, userResponse.info?.results)
        assertEquals("emptyseed", userResponse.info?.seed)
        assertEquals("1.4", userResponse.info?.version)
        assertEquals(0, userResponse.results?.size)
    }

    @Test
    fun `UserResponse deserializes with multiple users`() {
        val jsonString = """
            {
                "info": {
                    "page": 1,
                    "results": 3,
                    "seed": "multiseed",
                    "version": "1.4"
                },
                "results": [
                    {
                        "email": "user1@example.com",
                        "gender": "male",
                        "name": {
                            "title": "Mr",
                            "first": "John",
                            "last": "Doe"
                        }
                    },
                    {
                        "email": "user2@example.com",
                        "gender": "female",
                        "name": {
                            "title": "Ms",
                            "first": "Jane",
                            "last": "Smith"
                        }
                    },
                    {
                        "email": "user3@example.com",
                        "gender": "male",
                        "name": {
                            "title": "Dr",
                            "first": "Bob",
                            "last": "Johnson"
                        }
                    }
                ]
            }
        """

        val userResponse = gson.fromJson(jsonString, UserResponse::class.java)

        assertEquals(3, userResponse.results?.size)
        assertEquals("user1@example.com", userResponse.results?.get(0)?.email)
        assertEquals("user2@example.com", userResponse.results?.get(1)?.email)
        assertEquals("user3@example.com", userResponse.results?.get(2)?.email)
    }

    @Test
    fun `UserCoordinates deserializes correctly`() {
        val jsonString = """
            {
                "latitude": "40.7128",
                "longitude": "-74.0060"
            }
        """

        val coordinates = gson.fromJson(jsonString, UserCoordinates::class.java)

        assertEquals("40.7128", coordinates.latitude)
        assertEquals("-74.0060", coordinates.longitude)
    }

    @Test
    fun `UserStreet deserializes correctly`() {
        val jsonString = """
            {
                "number": 123,
                "name": "Main Street"
            }
        """

        val street = gson.fromJson(jsonString, UserStreet::class.java)

        assertEquals(123, street.number)
        assertEquals("Main Street", street.name)
    }

    @Test
    fun `UserTimezone deserializes correctly`() {
        val jsonString = """
            {
                "offset": "+05:30",
                "description": "India Standard Time"
            }
        """

        val timezone = gson.fromJson(jsonString, UserTimezone::class.java)

        assertEquals("+05:30", timezone.offset)
        assertEquals("India Standard Time", timezone.description)
    }

    @Test
    fun `UserResponse serializes correctly`() {
        val userResponse = UserResponse(
            info = UserResponseInfo(
                page = 2,
                results = 50,
                seed = "test123",
                version = "1.4"
            ),
            results = listOf(
                UserResult(
                    email = "test@example.com",
                    gender = "male",
                    name = UserName(title = "Mr", first = "Test", last = "User"),
                    phone = "+1234567890",
                    cell = "+0987654321",
                    nat = "US",
                    dob = null,
                    id = null,
                    location = null,
                    login = null,
                    picture = null,
                    registered = null
                )
            )
        )

        val jsonObj = gson.toJsonTree(userResponse).asJsonObject

        assertEquals(2, jsonObj["info"].asJsonObject["page"].asInt)
        assertEquals(50, jsonObj["info"].asJsonObject["results"].asInt)
        assertEquals("test123", jsonObj["info"].asJsonObject["seed"].asString)
        assertEquals("1.4", jsonObj["info"].asJsonObject["version"].asString)

        val resultsArray = jsonObj["results"].asJsonArray
        assertEquals(1, resultsArray.size())
        assertEquals("test@example.com", resultsArray[0].asJsonObject["email"].asString)
    }

    @Test
    fun `UserResponseInfo deserializes with null fields`() {
        val jsonString = """
            {
                "page": null,
                "results": null,
                "seed": null,
                "version": null
            }
        """

        val userInfo = gson.fromJson(jsonString, UserResponseInfo::class.java)

        assertNull(userInfo.page)
        assertNull(userInfo.results)
        assertNull(userInfo.seed)
        assertNull(userInfo.version)
    }

    @Test
    fun `UserLocation deserializes with partial data`() {
        val jsonString = """
            {
                "city": "San Francisco",
                "state": "CA",
                "country": "US",
                "postcode": null,
                "coordinates": null,
                "street": null,
                "timezone": null
            }
        """

        val userLocation = gson.fromJson(jsonString, UserLocation::class.java)

        assertEquals("San Francisco", userLocation.city)
        assertEquals("CA", userLocation.state)
        assertEquals("US", userLocation.country)
        assertNull(userLocation.postcode)
        assertNull(userLocation.coordinates)
        assertNull(userLocation.street)
        assertNull(userLocation.timezone)
    }

    @Test
    fun `UserResponse constructor creates instance with all parameters`() {
        val info = UserResponseInfo(
            page = 1,
            results = 10,
            seed = "test-seed",
            version = "1.4"
        )
        val results = listOf(
            UserResult(
                email = "test@example.com",
                gender = "male",
                name = null,
                phone = null,
                cell = null,
                nat = null,
                dob = null,
                id = null,
                location = null,
                login = null,
                picture = null,
                registered = null
            )
        )

        val userResponse = UserResponse(info = info, results = results)

        assertEquals(info, userResponse.info)
        assertEquals(results, userResponse.results)
    }

    @Test
    fun `UserResponse constructor creates instance with null parameters`() {
        val userResponse = UserResponse(info = null, results = null)

        assertNull(userResponse.info)
        assertNull(userResponse.results)
    }

    @Test
    fun `UserResponseInfo constructor creates instance with all parameters`() {
        val userInfo = UserResponseInfo(
            page = 5,
            results = 25,
            seed = "constructor-seed",
            version = "2.0"
        )

        assertEquals(5, userInfo.page)
        assertEquals(25, userInfo.results)
        assertEquals("constructor-seed", userInfo.seed)
        assertEquals("2.0", userInfo.version)
    }

    @Test
    fun `UserResponseInfo constructor creates instance with null parameters`() {
        val userInfo = UserResponseInfo(
            page = null,
            results = null,
            seed = null,
            version = null
        )

        assertNull(userInfo.page)
        assertNull(userInfo.results)
        assertNull(userInfo.seed)
        assertNull(userInfo.version)
    }

    @Test
    fun `UserResult constructor creates instance with all parameters`() {
        val name = UserName(title = "Mr", first = "John", last = "Doe")
        val location = UserLocation(
            city = "New York",
            state = "NY",
            country = "US",
            postcode = "10001",
            coordinates = null,
            street = null,
            timezone = null
        )
        val login = UserLogin(
            uuid = "test-uuid",
            username = "testuser",
            password = "testpass",
            salt = "testsalt",
            md5 = "testmd5",
            sha1 = "testsha1",
            sha256 = "testsha256"
        )
        val dob = UserDob(date = "1990-01-01T00:00:00Z", age = 33)
        val registered = UserRegistered(date = "2020-01-01T00:00:00Z", age = 3)
        val id = UserId(name = "SSN", value = "123-45-6789")
        val picture = UserPicture(
            large = "https://example.com/large.jpg",
            medium = "https://example.com/medium.jpg",
            thumbnail = "https://example.com/thumb.jpg"
        )

        val userResult = UserResult(
            email = "test@example.com",
            gender = "male",
            name = name,
            phone = "+1234567890",
            cell = "+0987654321",
            nat = "US",
            dob = dob,
            id = id,
            location = location,
            login = login,
            picture = picture,
            registered = registered
        )

        assertEquals("test@example.com", userResult.email)
        assertEquals("male", userResult.gender)
        assertEquals(name, userResult.name)
        assertEquals("+1234567890", userResult.phone)
        assertEquals("+0987654321", userResult.cell)
        assertEquals("US", userResult.nat)
        assertEquals(dob, userResult.dob)
        assertEquals(id, userResult.id)
        assertEquals(location, userResult.location)
        assertEquals(login, userResult.login)
        assertEquals(picture, userResult.picture)
        assertEquals(registered, userResult.registered)
    }

    @Test
    fun `UserResult constructor creates instance with null parameters`() {
        val userResult = UserResult(
            email = null,
            gender = null,
            name = null,
            phone = null,
            cell = null,
            nat = null,
            dob = null,
            id = null,
            location = null,
            login = null,
            picture = null,
            registered = null
        )

        assertNull(userResult.email)
        assertNull(userResult.gender)
        assertNull(userResult.name)
        assertNull(userResult.phone)
        assertNull(userResult.cell)
        assertNull(userResult.nat)
        assertNull(userResult.dob)
        assertNull(userResult.id)
        assertNull(userResult.location)
        assertNull(userResult.login)
        assertNull(userResult.picture)
        assertNull(userResult.registered)
    }

    @Test
    fun `UserName constructor creates instance with all parameters`() {
        val userName = UserName(title = "Dr", first = "Jane", last = "Smith")

        assertEquals("Dr", userName.title)
        assertEquals("Jane", userName.first)
        assertEquals("Smith", userName.last)
    }

    @Test
    fun `UserName constructor creates instance with null parameters`() {
        val userName = UserName(title = null, first = null, last = null)

        assertNull(userName.title)
        assertNull(userName.first)
        assertNull(userName.last)
    }

    @Test
    fun `UserLocation constructor creates instance with all parameters`() {
        val coordinates = UserCoordinates(latitude = "40.7128", longitude = "-74.0060")
        val street = UserStreet(number = 123, name = "Main St")
        val timezone = UserTimezone(offset = "-4:00", description = "Eastern Time")

        val userLocation = UserLocation(
            city = "New York",
            state = "NY",
            country = "US",
            postcode = "10001",
            coordinates = coordinates,
            street = street,
            timezone = timezone
        )

        assertEquals("New York", userLocation.city)
        assertEquals("NY", userLocation.state)
        assertEquals("US", userLocation.country)
        assertEquals("10001", userLocation.postcode)
        assertEquals(coordinates, userLocation.coordinates)
        assertEquals(street, userLocation.street)
        assertEquals(timezone, userLocation.timezone)
    }

    @Test
    fun `UserLocation constructor creates instance with null parameters`() {
        val userLocation = UserLocation(
            city = null,
            state = null,
            country = null,
            postcode = null,
            coordinates = null,
            street = null,
            timezone = null
        )

        assertNull(userLocation.city)
        assertNull(userLocation.state)
        assertNull(userLocation.country)
        assertNull(userLocation.postcode)
        assertNull(userLocation.coordinates)
        assertNull(userLocation.street)
        assertNull(userLocation.timezone)
    }

    @Test
    fun `UserLogin constructor creates instance with all parameters`() {
        val userLogin = UserLogin(
            uuid = "test-uuid",
            username = "testuser",
            password = "testpass",
            salt = "testsalt",
            md5 = "testmd5",
            sha1 = "testsha1",
            sha256 = "testsha256"
        )

        assertEquals("test-uuid", userLogin.uuid)
        assertEquals("testuser", userLogin.username)
        assertEquals("testpass", userLogin.password)
        assertEquals("testsalt", userLogin.salt)
        assertEquals("testmd5", userLogin.md5)
        assertEquals("testsha1", userLogin.sha1)
        assertEquals("testsha256", userLogin.sha256)
    }

    @Test
    fun `UserLogin constructor creates instance with null parameters`() {
        val userLogin = UserLogin(
            uuid = null,
            username = null,
            password = null,
            salt = null,
            md5 = null,
            sha1 = null,
            sha256 = null
        )

        assertNull(userLogin.uuid)
        assertNull(userLogin.username)
        assertNull(userLogin.password)
        assertNull(userLogin.salt)
        assertNull(userLogin.md5)
        assertNull(userLogin.sha1)
        assertNull(userLogin.sha256)
    }

    @Test
    fun `UserDob constructor creates instance with all parameters`() {
        val userDob = UserDob(date = "1990-01-01T00:00:00Z", age = 33)

        assertEquals("1990-01-01T00:00:00Z", userDob.date)
        assertEquals(33, userDob.age)
    }

    @Test
    fun `UserDob constructor creates instance with null parameters`() {
        val userDob = UserDob(date = null, age = null)

        assertNull(userDob.date)
        assertNull(userDob.age)
    }

    @Test
    fun `UserId constructor creates instance with all parameters`() {
        val userId = UserId(name = "SSN", value = "123-45-6789")

        assertEquals("SSN", userId.name)
        assertEquals("123-45-6789", userId.value)
    }

    @Test
    fun `UserId constructor creates instance with null parameters`() {
        val userId = UserId(name = null, value = null)

        assertNull(userId.name)
        assertNull(userId.value)
    }

    @Test
    fun `UserPicture constructor creates instance with all parameters`() {
        val userPicture = UserPicture(
            large = "https://example.com/large.jpg",
            medium = "https://example.com/medium.jpg",
            thumbnail = "https://example.com/thumb.jpg"
        )

        assertEquals("https://example.com/large.jpg", userPicture.large)
        assertEquals("https://example.com/medium.jpg", userPicture.medium)
        assertEquals("https://example.com/thumb.jpg", userPicture.thumbnail)
    }

    @Test
    fun `UserPicture constructor creates instance with null parameters`() {
        val userPicture = UserPicture(
            large = null,
            medium = null,
            thumbnail = null
        )

        assertNull(userPicture.large)
        assertNull(userPicture.medium)
        assertNull(userPicture.thumbnail)
    }

    @Test
    fun `UserRegistered constructor creates instance with all parameters`() {
        val userRegistered = UserRegistered(date = "2020-01-01T00:00:00Z", age = 3)

        assertEquals("2020-01-01T00:00:00Z", userRegistered.date)
        assertEquals(3, userRegistered.age)
    }

    @Test
    fun `UserRegistered constructor creates instance with null parameters`() {
        val userRegistered = UserRegistered(date = null, age = null)

        assertNull(userRegistered.date)
        assertNull(userRegistered.age)
    }

    @Test
    fun `UserCoordinates constructor creates instance with all parameters`() {
        val userCoordinates = UserCoordinates(latitude = "40.7128", longitude = "-74.0060")

        assertEquals("40.7128", userCoordinates.latitude)
        assertEquals("-74.0060", userCoordinates.longitude)
    }

    @Test
    fun `UserCoordinates constructor creates instance with null parameters`() {
        val userCoordinates = UserCoordinates(latitude = null, longitude = null)

        assertNull(userCoordinates.latitude)
        assertNull(userCoordinates.longitude)
    }

    @Test
    fun `UserStreet constructor creates instance with all parameters`() {
        val userStreet = UserStreet(number = 456, name = "Oak Avenue")

        assertEquals(456, userStreet.number)
        assertEquals("Oak Avenue", userStreet.name)
    }

    @Test
    fun `UserStreet constructor creates instance with null parameters`() {
        val userStreet = UserStreet(number = null, name = null)

        assertNull(userStreet.number)
        assertNull(userStreet.name)
    }

    @Test
    fun `UserTimezone constructor creates instance with all parameters`() {
        val userTimezone = UserTimezone(offset = "+05:30", description = "India Standard Time")

        assertEquals("+05:30", userTimezone.offset)
        assertEquals("India Standard Time", userTimezone.description)
    }

    @Test
    fun `UserTimezone constructor creates instance with null parameters`() {
        val userTimezone = UserTimezone(offset = null, description = null)

        assertNull(userTimezone.offset)
        assertNull(userTimezone.description)
    }
}