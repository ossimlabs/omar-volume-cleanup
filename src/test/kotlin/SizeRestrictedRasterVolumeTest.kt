import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
    fun `Threshold of 0%, 1%, 50%, 99%, 100% removes equal number of rasters on 100L volume`() =
        listOf(0, 1, 50, 99, 100).forEach { percent ->

            val rasters: MutableList<RasterEntry> = MutableList(100) { mockRaster(size = 1L) }

            val sizeRestrictedRasterVolume = SizeRestrictedRasterVolume(
                mockVolume(rasters, 100L, 1L),
                mockClient(rasters),
                mockDatabase(rasters),
                percentThreshold = percent.toDouble() / 100
            )

            runBlocking {
                sizeRestrictedRasterVolume.cleanVolume()
            }

            println("Percent of $percent, there are ${rasters.size} remaining rasters.")
            assertEquals(
                expected = percent,
                actual = rasters.size,
                message = "Remaining rasters ${rasters.size} did not match expected value of $percent"
            )
        }

    private fun mockVolume(rasters: List<RasterEntry>, size: Long, rasterSize: Long): File = mockk {
        every { totalSpace } returns size
        every { usableSpace } answers { totalSpace - (rasters.size * rasterSize) }
    }

    /**
     * Creates a mock [RasterDatabase] who's [cursor][RasterDatabase.rasterCursor] iterates over a copy of
     * the provided list.
     */
    private fun mockDatabase(rasters: List<RasterEntry>): RasterDatabase = mockk {
        // We want to use a copy of rasters to avoid accidental concurrent modifications
        every { rasterCursor().iterator() } returns rasters.toList().iterator()
        every { rasterCursor().close() } returns Unit
    }

    /**
     * Creates a mock [RasterClient] who's [remove][RasterClient.remove] removes the raster from the provided
     * mutable list.
     */
    private fun mockClient(rasters: MutableList<RasterEntry>): RasterClient = mockk {
        val raster = slot<RasterEntry>()
        coEvery { remove(capture(raster)) } answers { rasters.remove(raster.captured) }
    }

    private fun mockRaster(size: Long) = mockk<RasterEntry> {
        every { length } returns size
    }
}