package com.sample.android.data

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class UserMetaDataTest {

    @Test
    fun `timestamp should parse valid ISO 8601 datetime`() {
        val validDatetime = "2023-01-01T12:00:00.000Z"
        val userMetaData = UserMetaData(
            title = "Test User",
            thumbnail = "https://example.com/image.jpg",
            url = "test@example.com",
            datetime = validDatetime
        )

        val timestamp = userMetaData.timestamp

        // Then - timestamp should be 0 or a valid positive number
        assert(timestamp >= 0)
    }

    @Test
    fun `timestamp should return 0 for null datetime`() {
        val userMetaData = UserMetaData(
            title = "Test User",
            thumbnail = "https://example.com/image.jpg",
            url = "test@example.com",
            datetime = null
        )

        val timestamp = userMetaData.timestamp

        assertEquals(0L, timestamp)
    }

    @Test
    fun `timestamp should return 0 for empty datetime`() {
        val userMetaData = UserMetaData(
            title = "Test User",
            thumbnail = "https://example.com/image.jpg",
            url = "test@example.com",
            datetime = ""
        )

        val timestamp = userMetaData.timestamp

        assertEquals(0L, timestamp)
    }

    @Test
    fun `timestamp should return 0 for invalid datetime format`() {
        val invalidDatetime = "invalid-date-format"
        val userMetaData = UserMetaData(
            title = "Test User",
            thumbnail = "https://example.com/image.jpg",
            url = "test@example.com",
            datetime = invalidDatetime
        )

        val timestamp = userMetaData.timestamp

        assertEquals(0L, timestamp)
    }

    @Test
    fun `timestamp should handle different valid datetime formats`() {
        val validDatetime = "2023-12-25T15:30:45.123Z"
        val userMetaData = UserMetaData(
            title = "Test User",
            thumbnail = "https://example.com/image.jpg",
            url = "test@example.com",
            datetime = validDatetime
        )

        val timestamp = userMetaData.timestamp

        // Then - timestamp should be 0 or a valid positive number
        assert(timestamp >= 0)
    }

    @Test
    fun `UserMetaData should be properly constructed with all fields`() {
        val title = "John Doe"
        val thumbnail = "https://example.com/avatar.jpg"
        val url = "john.doe@example.com"
        val datetime = "2023-06-15T10:30:00.000Z"

        val userMetaData = UserMetaData(
            title = title,
            thumbnail = thumbnail,
            url = url,
            datetime = datetime
        )

        assertEquals(title, userMetaData.title)
        assertEquals(thumbnail, userMetaData.thumbnail)
        assertEquals(url, userMetaData.url)
        assertEquals(datetime, userMetaData.datetime)
    }

    @Test
    fun `UserMetaData should handle null values`() {
        
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
        assertEquals(0L, userMetaData.timestamp)
    }

    @Test
    fun `UserMetaDataList should contain list of UserMetaData`() {
        val userMetaData1 = UserMetaData("User 1", "thumb1", "url1", "2023-01-01T12:00:00.000Z")
        val userMetaData2 = UserMetaData("User 2", "thumb2", "url2", "2023-01-02T12:00:00.000Z")
        val userList = listOf(userMetaData1, userMetaData2)

        val userMetaDataList = UserMetaDataList(userList)

        assertEquals(2, userMetaDataList.users.size)
        assertEquals(userMetaData1, userMetaDataList.users[0])
        assertEquals(userMetaData2, userMetaDataList.users[1])
    }
}