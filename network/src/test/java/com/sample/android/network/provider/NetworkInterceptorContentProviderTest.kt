package com.sample.android.network.provider

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import com.sample.android.network.InterceptorModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NetworkInterceptorContentProviderTest {

    private lateinit var contentProvider: NetworkInterceptorContentProvider
    private lateinit var mockApplication: Application
    private lateinit var mockUri: Uri

    @Before
    fun setUp() {
        contentProvider = NetworkInterceptorContentProvider()
        mockApplication = mockk(relaxed = true)
        mockUri = mockk()

        // Mock the InterceptorModule
        mockkObject(InterceptorModule)
        every { InterceptorModule.initializeWithDefaults(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkObject(InterceptorModule)
    }

    @Test
    fun onCreate_shouldInitializeInterceptorModule() {
        val contextField: Field =
            android.content.ContentProvider::class.java.getDeclaredField("mContext")
        contextField.isAccessible = true
        contextField.set(contentProvider, mockApplication)

        val result = contentProvider.onCreate()

        assertTrue(result)
        verify { InterceptorModule.initializeWithDefaults(mockApplication) }
    }

    @Test
    fun insert_shouldThrowUnsupportedOperationException() {
        val contentValues = ContentValues()

        assertThrows(UnsupportedOperationException::class.java) {
            contentProvider.insert(mockUri, contentValues)
        }
    }

    @Test
    fun query_shouldThrowUnsupportedOperationException() {
        val projection = arrayOf("column1")
        val selection = "selection"
        val selectionArgs = arrayOf("arg1")
        val sortOrder = "sortOrder"

        assertThrows(UnsupportedOperationException::class.java) {
            contentProvider.query(mockUri, projection, selection, selectionArgs, sortOrder)
        }
    }

    @Test
    fun update_shouldThrowUnsupportedOperationException() {
        val contentValues = ContentValues()
        val selection = "selection"
        val selectionArgs = arrayOf("arg1")

        assertThrows(UnsupportedOperationException::class.java) {
            contentProvider.update(mockUri, contentValues, selection, selectionArgs)
        }
    }

    @Test
    fun delete_shouldThrowUnsupportedOperationException() {
        val selection = "selection"
        val selectionArgs = arrayOf("arg1")

        assertThrows(UnsupportedOperationException::class.java) {
            contentProvider.delete(mockUri, selection, selectionArgs)
        }
    }

    @Test
    fun getType_shouldThrowUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException::class.java) {
            contentProvider.getType(mockUri)
        }
    }
}