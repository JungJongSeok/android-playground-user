package com.sample.android.data

import com.sample.android.data.model.UserMetaData
import com.sample.android.data.model.UserMetaDataList
import com.sample.android.data.model.toData
import com.sample.android.network.response.UserDob
import com.sample.android.network.response.UserName
import com.sample.android.network.response.UserPicture
import com.sample.android.network.response.UserResult
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class UserMetaDataTest {

    @Test
    fun `describeContents returns 0`() {
        val userMetaData = UserMetaData(
            title = "title",
            thumbnail = "thumbnail",
            url = "url",
            datetime = "2025-05-19T09:42:29.000+09:00"
        )

        val result = userMetaData.describeContents()

        assertEquals(0, result)
    }

    @Test
    fun `UserMetaData creates properly with all fields`() {
        val userMetaData = UserMetaData(
            title = "John Doe",
            thumbnail = "thumbnail.jpg",
            url = "john@example.com",
            datetime = "2025-05-19T09:42:29.000+09:00"
        )

        assertEquals("John Doe", userMetaData.title)
        assertEquals("thumbnail.jpg", userMetaData.thumbnail)
        assertEquals("john@example.com", userMetaData.url)
        assertEquals("2025-05-19T09:42:29.000+09:00", userMetaData.datetime)
    }

    @Test
    fun `UserMetaData creates properly with null fields`() {
        val userMetaData = UserMetaData(
            title = null,
            thumbnail = null,
            url = null,
            datetime = null
        )

        assertEquals(null, userMetaData.title)
        assertEquals(null, userMetaData.thumbnail)
        assertEquals(null, userMetaData.url)
        assertEquals(null, userMetaData.datetime)
    }

    @Test
    fun `UserMetaData equals and hashCode work correctly`() {
        val userMetaData1 = UserMetaData(
            title = "John Doe",
            thumbnail = "thumbnail.jpg",
            url = "john@example.com",
            datetime = "2025-05-19T09:42:29.000+09:00"
        )
        val userMetaData2 = UserMetaData(
            title = "John Doe",
            thumbnail = "thumbnail.jpg",
            url = "john@example.com",
            datetime = "2025-05-19T09:42:29.000+09:00"
        )
        val userMetaData3 = UserMetaData(
            title = "Jane Doe",
            thumbnail = "thumbnail.jpg",
            url = "jane@example.com",
            datetime = "2025-05-19T09:42:29.000+09:00"
        )

        assertEquals(userMetaData1, userMetaData2)
        assertEquals(userMetaData1.hashCode(), userMetaData2.hashCode())
        assertFalse(userMetaData1 == userMetaData3)
    }

    @Test
    fun `UserMetaData toString works correctly`() {
        val userMetaData = UserMetaData(
            title = "John Doe",
            thumbnail = "thumbnail.jpg",
            url = "john@example.com",
            datetime = "2025-05-19T09:42:29.000+09:00"
        )

        val result = userMetaData.toString()

        assertTrue(result.contains("John Doe"))
        assertTrue(result.contains("thumbnail.jpg"))
        assertTrue(result.contains("john@example.com"))
        assertTrue(result.contains("2025-05-19T09:42:29.000+09:00"))
    }

    @Test
    fun `UserMetaDataList creates properly with user list`() {
        val user1 = UserMetaData(
            "John Doe",
            "thumb1.jpg",
            "john@example.com",
            "2025-05-19T09:42:29.000+09:00"
        )
        val user2 = UserMetaData(
            "Jane Smith",
            "thumb2.jpg",
            "jane@example.com",
            "2025-05-20T10:30:00.000+09:00"
        )
        val userList = listOf(user1, user2)

        val userMetaDataList = UserMetaDataList(userList)

        assertEquals(2, userMetaDataList.users.size)
        assertEquals("John Doe", userMetaDataList.users[0].title)
        assertEquals("Jane Smith", userMetaDataList.users[1].title)
    }

    @Test
    fun `UserMetaDataList creates properly with empty list`() {
        val userMetaDataList = UserMetaDataList(emptyList())

        assertEquals(0, userMetaDataList.users.size)
    }

    @Test
    fun `UserMetaDataList equals and hashCode work correctly`() {
        val user1 =
            UserMetaData("John", "thumb1.jpg", "john@example.com", "2025-05-19T09:42:29.000+09:00")
        val user2 =
            UserMetaData("Jane", "thumb2.jpg", "jane@example.com", "2025-05-20T10:30:00.000+09:00")

        val userList1 = UserMetaDataList(listOf(user1, user2))
        val userList2 = UserMetaDataList(listOf(user1, user2))
        val userList3 = UserMetaDataList(listOf(user1))

        assertEquals(userList1, userList2)
        assertEquals(userList1.hashCode(), userList2.hashCode())
        assertFalse(userList1 == userList3)
    }

    @Test
    fun `toData converts UserResult to UserMetaData correctly`() {
        val userResult = UserResult(
            cell = null,
            dob = UserDob(age = 25, date = "2025-05-19T09:42:29.000+09:00"),
            email = "test@example.com",
            gender = "male",
            id = null,
            location = null,
            login = null,
            name = UserName(first = "John", last = "Doe", title = "Mr"),
            nat = null,
            phone = null,
            picture = UserPicture(
                large = "large.jpg",
                medium = "medium.jpg",
                thumbnail = "thumbnail.jpg"
            ),
            registered = null
        )

        val result = userResult.toData()

        assertEquals("John Doe", result.title)
        assertEquals("thumbnail.jpg", result.thumbnail)
        assertEquals("test@example.com", result.url)
        assertEquals("2025-05-19T09:42:29.000+09:00", result.datetime)
    }

    @Test
    fun `toData handles null values gracefully`() {
        val userResult = UserResult(
            cell = null,
            dob = null,
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

        val result = userResult.toData()

        assertEquals(" ", result.title)
        assertEquals(null, result.thumbnail)
        assertEquals(null, result.url)
        assertEquals(null, result.datetime)
    }

    @Test
    fun `toData handles partial name`() {
        val userResult = UserResult(
            cell = null,
            dob = null,
            email = null,
            gender = null,
            id = null,
            location = null,
            login = null,
            name = UserName(first = "John", last = null, title = null),
            nat = null,
            phone = null,
            picture = null,
            registered = null
        )

        val result = userResult.toData()

        assertEquals("John ", result.title)
    }

    @Test
    fun `toData handles only last name`() {
        val userResult = UserResult(
            cell = null,
            dob = null,
            email = null,
            gender = null,
            id = null,
            location = null,
            login = null,
            name = UserName(first = null, last = "Doe", title = null),
            nat = null,
            phone = null,
            picture = null,
            registered = null
        )

        val result = userResult.toData()

        assertEquals(" Doe", result.title)
    }

    @Test
    fun `toData handles empty name parts`() {
        val userResult = UserResult(
            cell = null,
            dob = null,
            email = null,
            gender = null,
            id = null,
            location = null,
            login = null,
            name = UserName(first = "", last = "", title = null),
            nat = null,
            phone = null,
            picture = null,
            registered = null
        )

        val result = userResult.toData()

        assertEquals(" ", result.title)
    }

    @Test
    fun `toData handles different picture fields`() {
        val userResult = UserResult(
            cell = null,
            dob = null,
            email = null,
            gender = null,
            id = null,
            location = null,
            login = null,
            name = null,
            nat = null,
            phone = null,
            picture = UserPicture(
                large = "large.jpg",
                medium = "medium.jpg",
                thumbnail = null
            ),
            registered = null
        )

        val result = userResult.toData()

        assertEquals(null, result.thumbnail)
    }
}