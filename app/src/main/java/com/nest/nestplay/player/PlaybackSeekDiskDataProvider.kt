package com.nest.nestplay.player

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.os.Environment
import java.io.File

    class PlaybackSeekDiskDataProvider internal constructor(
        interval: Long,
        videoUrl: String,
        var mPathPattern: String
    ) :
        PlaybackSeekAsyncDataProvider(16, 24, videoUrl, interval) {
        var mPaint: Paint? = null
        init {
            val interval = if (interval == 0L) 1L else interval
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoUrl, HashMap<String, String>())
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            val size = (duration / interval + 1).toInt()
            mSeekPositions = LongArray(size) { it * interval }
            mPaint = Paint()
            mPaint!!.setTextSize(16f)
            mPaint!!.setColor(Color.WHITE)
        }

    override fun doInBackground(task: Any?, index: Int, position: Long): Bitmap {
        println(position)
        try {
            Thread.sleep(100)
        } catch (ex: InterruptedException) {
        }
        val path = String.format(mPathPattern, index + 1)

        println(path)

        return if (File(path).exists()) {
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap == null) {
                createYellowBitmapWithErrorText(path, index, position)
            } else {
                bitmap
            }
        } else {
            createYellowBitmapWithErrorText(path, index, position)
        }
    }

        override fun reset() {
            for (i in 0 until mRequests.size()) {
                val task = mRequests.valueAt(i)
                task!!.cancel(true)
            }
            mRequests.clear()
            mCache.evictAll()
            mPrefetchCache.evictAll()
            mLastRequestedIndex = -1
        }
    private fun createYellowBitmapWithErrorText(path: String, index: Int, position: Long): Bitmap {
        val bmp = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.TRANSPARENT)
        val paint = Paint()
        paint.textSize = 16f
        paint.color = Color.WHITE
        canvas.drawText("$position", 10f, 150f, paint)
        return bmp
    }

    companion object {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/seek/frame_%04d.jpg"
    }
}