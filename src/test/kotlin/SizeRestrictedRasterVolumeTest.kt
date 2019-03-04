import io.mockk.*
import io.ossim.omar.apps.volume.cleanup.raster.RasterClient
import io.ossim.omar.apps.volume.cleanup.raster.RasterEntry
import io.ossim.omar.apps.volume.cleanup.raster.SizeRestrictedRasterVolume
import io.ossim.omar.apps.volume.cleanup.raster.database.RasterDatabase
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class SizeRestrictedRasterVolumeTest {
    @Test
    fun `Threshold of 50% removes 50 of 100 rasters on 100L volume`() {

        // We mock the length of each raster since there are no real files.
        val rasters = MutableList(100) {
            mockk<RasterEntry> {
                every { length } returns 1L
            }
        }

        val srrv = spyk(
            SizeRestrictedRasterVolume(
                mockVolume(rasters, 100L, 1L),
                mockClient(rasters),
                mockDatabase(rasters),
                percentThreshold = 0.5
            )
        )

        runBlocking {
            srrv.cleanVolume()
        }

        assertEquals(50, rasters.size)
    }

    fun mockVolume(rasters: List<RasterEntry>, size: Long, rasterSize: Long): File = mockk {
        every { totalSpace } returns size
        every { usableSpace } answers { totalSpace - (rasters.size * rasterSize) }
    }

    fun mockDatabase(rasters: List<RasterEntry>): RasterDatabase = mockk {
        every { rasterCursor().iterator() } returns rasters.iterator()
        every { rasterCursor().close() } returns Unit
    }

    fun mockClient(rasters: MutableList<RasterEntry>): RasterClient = mockk {
        val raster = slot<RasterEntry>()
        coEvery { remove(capture(raster)) } answers { rasters.remove(raster.captured) }
    }
}