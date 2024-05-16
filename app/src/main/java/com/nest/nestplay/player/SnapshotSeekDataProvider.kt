package com.nest.nestplay.player
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.leanback.widget.PlaybackSeekDataProvider
import java.io.File

class SnapshotSeekDataProvider(
    private val duration: Long,
    private val interval: Long,
    private val pathPattern: String
) : PlaybackSeekDataProvider() {

    private lateinit var seekPositions: MutableList<Long>

    init {
        var currentPosition = 0L
        while (currentPosition <= duration) {
            seekPositions.add(currentPosition)
            currentPosition += interval
        }
    }

    private fun calculateSeekPositions() {
        val size = (duration / interval + 1).toInt()
        seekPositions = mutableListOf()
        for (index in 0 until size) {
            val position = index * interval
            seekPositions.add(position)
        }
    }

    override fun getSeekPositions(): LongArray  {
        return seekPositions as LongArray
    }

    override fun getThumbnail(index: Int, callback: ResultCallback?) {
        super.getThumbnail(index, callback)
    }
    fun loadSnapshot(position: Long): Bitmap? {
        val path = String.format(pathPattern, position)
        return if (File(path).exists()) {
            BitmapFactory.decodeFile(path)
        } else {
            null
        }
    }
}