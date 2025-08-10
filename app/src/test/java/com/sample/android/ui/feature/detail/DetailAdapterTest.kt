package com.sample.android.ui.feature.detail

import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.sample.android.data.UserMetaData
import com.sample.android.databinding.HolderDetailBinding
import com.sample.android.ui.feature.main.model.UserUiData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DetailAdapterTest {

    @MockK
    private lateinit var requestManager: RequestManager

    private lateinit var property: DetailProperty
    private lateinit var adapter: DetailAdapter
    private lateinit var userUiDataList: List<UserUiData>

    private val testUserMetaData1 = UserMetaData(
        thumbnail = "https://example.com/image1.jpg",
        title = "Test Title 1",
        url = "https://example.com/1",
        datetime = "2025-01-01T00:00:00.000+09:00"
    )

    private val testUserMetaData2 = UserMetaData(
        thumbnail = "https://example.com/image2.jpg",
        title = "Test Title 2",
        url = "https://example.com/2",
        datetime = "2025-01-02T00:00:00.000+09:00"
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        // DetailProperty 구현체 생성
        property = object : DetailProperty {
            override val requestManager: RequestManager = this@DetailAdapterTest.requestManager
        }

        adapter = DetailAdapter(property)

        // 테스트용 데이터 생성
        userUiDataList = listOf(
            UserUiData(isFavorite = true, data = testUserMetaData1),
            UserUiData(isFavorite = false, data = testUserMetaData2)
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `adapter should be created with correct property`() {
        // Given & When
        val newAdapter = DetailAdapter(property)

        assertNotNull(newAdapter)
        assertEquals(0, newAdapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder should return ViewHolder with correct binding`() {
        val context = RuntimeEnvironment.getApplication()
        val parent = mockk<ViewGroup>(relaxed = true)
        every { parent.context } returns context

        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(viewHolder)
        assertNotNull(viewHolder.binding)
        assertTrue(viewHolder is DetailAdapter.ViewHolder)
    }

    @Test
    fun `onBindViewHolder should bind data correctly`() {
        val context = RuntimeEnvironment.getApplication()
        val parent = mockk<ViewGroup>(relaxed = true)
        every { parent.context } returns context

        val binding = mockk<HolderDetailBinding>(relaxed = true)
        val viewHolder = spyk(DetailAdapter(property).ViewHolder(binding))

        adapter.submitList(userUiDataList)

        adapter.onBindViewHolder(viewHolder, 0)

        verify { binding.requestManager = requestManager }
        verify { binding.thumbnail = testUserMetaData1.thumbnail }
    }

    @Test
    fun `onBindViewHolder should not bind data when position is negative`() {
        val context = RuntimeEnvironment.getApplication()
        val parent = mockk<ViewGroup>(relaxed = true)
        every { parent.context } returns context

        val binding = mockk<HolderDetailBinding>(relaxed = true)
        val viewHolder = spyk(DetailAdapter(property).ViewHolder(binding))

        adapter.submitList(userUiDataList)

        adapter.onBindViewHolder(viewHolder, -1)

        verify(exactly = 0) { binding.requestManager = any() }
        verify(exactly = 0) { binding.thumbnail = any() }
    }

    @Test
    fun `submitList should update adapter item count`() {
        assertEquals(0, adapter.itemCount)

        adapter.submitList(userUiDataList)

        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `currentList should return correct item at position`() {
        adapter.submitList(userUiDataList)

        val item = adapter.currentList[0]

        assertEquals(userUiDataList[0], item)
        assertEquals(testUserMetaData1, item.data)
        assertTrue(item.isFavorite)
    }

    @Test
    fun `adapter should handle list updates correctly`() {
        adapter.submitList(userUiDataList)

        // When - 리스트 업데이트
        val newUserUiData = UserUiData(isFavorite = true, data = testUserMetaData2)
        adapter.submitList(listOf(newUserUiData)) {
            // Then - 콜백에서 검증
            assertEquals(1, adapter.itemCount)
            assertEquals(newUserUiData, adapter.currentList[0])
            assertEquals(testUserMetaData2, adapter.currentList[0].data)
            assertTrue(adapter.currentList[0].isFavorite)
        }
    }

    @Test
    fun `adapter should handle multiple data bindings correctly`() {
        val context = RuntimeEnvironment.getApplication()
        val parent = mockk<ViewGroup>(relaxed = true)
        every { parent.context } returns context

        val binding1 = mockk<HolderDetailBinding>(relaxed = true)
        val binding2 = mockk<HolderDetailBinding>(relaxed = true)
        val viewHolder1 = spyk(DetailAdapter(property).ViewHolder(binding1))
        val viewHolder2 = spyk(DetailAdapter(property).ViewHolder(binding2))

        adapter.submitList(userUiDataList)

        adapter.onBindViewHolder(viewHolder1, 0)
        adapter.onBindViewHolder(viewHolder2, 1)

        verify { binding1.requestManager = requestManager }
        verify { binding1.thumbnail = testUserMetaData1.thumbnail }
        verify { binding2.requestManager = requestManager }
        verify { binding2.thumbnail = testUserMetaData2.thumbnail }
    }

    @Test
    fun `empty list should work correctly`() {
        val emptyList = emptyList<UserUiData>()

        adapter.submitList(emptyList)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `single item list should work correctly`() {
        val singleItemList = listOf(UserUiData(isFavorite = true, data = testUserMetaData1))

        adapter.submitList(singleItemList)

        assertEquals(1, adapter.itemCount)
        assertEquals(singleItemList[0], adapter.currentList[0])
    }
}