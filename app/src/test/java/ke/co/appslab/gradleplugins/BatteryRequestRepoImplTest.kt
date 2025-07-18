package ke.co.appslab.gradleplugins

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import keronei.swapper.data.BatteryRequestRepoImpl
import keronei.swapper.data.domain.BatteryRequestModel
import keronei.swapper.data.domain.BatteryRequestUpdateModel
import keronei.swapper.data.local.BatteryRequestDao
import keronei.swapper.data.local.BatteryRequestUpdateEntity
import keronei.swapper.data.local.BatteryRequestsEntity
import keronei.swapper.data.local.UpdatedRequestsEmbed
import keronei.swapper.utils.RequestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class BatteryRequestRepoImplTest {

    private lateinit var batteryRequestDao: BatteryRequestDao
    private lateinit var repository: BatteryRequestRepoImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        batteryRequestDao = mockk()
        repository = BatteryRequestRepoImpl(batteryRequestDao)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createBatteryRequest should return success`() = runTest {
        val model = BatteryRequestModel(1, "comment", 2, "now",  false, "station", "pending")

        coEvery { batteryRequestDao.createBatteryRequest(any()) } returns 1L

        val result = repository.createBatteryRequest(model)

        assertTrue(result is RequestResult.Success)
    }

    @Test
    fun `removeBatteryRequest should return success if item is removed`() = runTest {
        val model = BatteryRequestModel(1, "comment", 2, "now",  false, "station", "Accepted")

        coEvery { batteryRequestDao.removeBatteryRequest(1) } returns 1

        val result = repository.removeBatteryRequest(model)

        assertTrue(result is RequestResult.Success)
    }

    @Test
    fun `removeBatteryRequest should return error if no item is removed`() = runTest {
        val model = BatteryRequestModel(1, "comment", 2, "now",  false, "station", "Rejected")

        coEvery { batteryRequestDao.removeBatteryRequest(1) } returns 0

        val result = repository.removeBatteryRequest(model)

        assertTrue(result is RequestResult.Error)
    }

    @Test
    fun `updateBatteryRequest should return success when insert returns positive`() = runTest {
        val updateModel = BatteryRequestUpdateModel(1, 3, listOf("B1", "B2"), 1, "OK", "now")

        coEvery { batteryRequestDao.updateBatteryRequest(any()) } returns 1L

        val result = repository.updateBatteryRequest(updateModel)

        assertTrue(result is RequestResult.Success)
    }

    @Test
    fun `queryLocalRequests maps data correctly`() = runTest {
        val fakeEntity = BatteryRequestsEntity(
            id = 1, comment = "comment", requestedCount = 2,
            createdTime = "now", status = "pending",
            requestedByStation = "station", synced = false
        )

        val updateEntity = BatteryRequestUpdateEntity(
            id = 1, batteryCount = 2, batteries = "B1,B2",
            requestId = 1, comment = "done", updateAt = "yesterday"
        )

        val embed = UpdatedRequestsEmbed(fakeEntity, listOf(updateEntity))

        every { batteryRequestDao.getLocalRequests() } returns flowOf(listOf(embed))

        val result = repository.queryLocalRequests().first()

        assertEquals(1, result.size)
        assertEquals("comment", result[0].request.comment)
        assertEquals(2, result[0].updates[0].batteries.size)
    }
}
