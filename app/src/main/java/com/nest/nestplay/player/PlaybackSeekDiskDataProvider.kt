package com.nest.nestplay.player

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import java.io.File

    class PlaybackSeekDiskDataProvider internal constructor(
        duration: Long,
        interval: Long,
        var mPathPattern: String
    ) :
        PlaybackSeekAsyncDataProvider() {
        var mPaint: Paint? = null
        init {
            val interval = if (interval == 0L) 1L else interval
            val size = (duration / interval).toInt() + 1
            val pos = LongArray(size)
            for (i in pos.indices) {
                pos[i] = i * duration / pos.size
            }
            setSeekPositions(pos);
            mPaint = Paint()
            mPaint!!.setTextSize(16f)
            mPaint!!.setColor(Color.WHITE)
        }

    override fun doInBackground(task: Any?, index: Int, position: Long): Bitmap {

        try {
            Thread.sleep(100)
        } catch (ex: InterruptedException) {
        }
        val path = String.format(mPathPattern, index + 1)
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