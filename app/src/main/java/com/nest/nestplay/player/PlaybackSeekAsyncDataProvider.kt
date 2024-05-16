package com.nest.nestplay.player

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.util.Log
import android.util.SparseArray
import androidx.collection.LruCache
import androidx.leanback.widget.PlaybackSeekDataProvider


abstract class PlaybackSeekAsyncDataProvider @JvmOverloads constructor(
    cacheSize: Int = 16,
    prefetchCacheSize: Int = 24,
    linkUrl: String = "",
    interval: Long
) :
    PlaybackSeekDataProvider() {
    val videoUrl: String = linkUrl
    lateinit var mSeekPositions: LongArray
    var linkUrl: String = linkUrl
    private val mTasks: SparseArray<LoadBitmapAsyncTask> = SparseArray()
    val mCache: LruCache<Int, Bitmap?>


    val mPrefetchCache: LruCache<Int, Bitmap?>
    val mRequests = SparseArray<LoadBitmapTask?>()
    var mLastRequestedIndex = -1
    protected fun isCancelled(task: Any): Boolean {
        return (task as AsyncTask<*, *, *>).isCancelled
    }

    init {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoUrl, HashMap<String, String>())
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0

        val size = (duration / interval + 1).toInt()
        mSeekPositions = LongArray(size) { it * interval }
    }

    protected abstract fun doInBackground(task: Any?, index: Int, position: Long): Bitmap
    inner class LoadBitmapTask(var mIndex: Int, var mResultCallback: ResultCallback?) :
        AsyncTask<Any?, Any?, Bitmap>() {
        override fun doInBackground(params: Array<Any?>): Bitmap {
            return this@PlaybackSeekAsyncDataProvider
                .doInBackground(this, mIndex, mSeekPositions[mIndex])
        }

        override fun onPostExecute(bitmap: Bitmap) {
            mRequests.remove(mIndex)
            Log.d(TAG, "thumb Loaded $mIndex")
            if (mResultCallback != null) {
                mCache.put(mIndex, bitmap)
                mResultCallback!!.onThumbnailLoaded(bitmap, mIndex)
            } else {
                mPrefetchCache.put(mIndex, bitmap)
            }
        }
    }

    init {
        mCache = LruCache(cacheSize)
        mPrefetchCache = LruCache(prefetchCacheSize)
    }

    fun setSeekPositions(positions: LongArray) {
        mSeekPositions = positions
    }

    override fun getSeekPositions(): LongArray {
        return mSeekPositions
    }

    override fun getThumbnail(index: Int, callback: ResultCallback) {
        val task = mTasks[index]
        if (task == null) {
            val newTask = LoadBitmapAsyncTask(index, callback)
            mTasks.put(index, newTask)
            newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    protected fun prefetch(hintIndex: Int, forward: Boolean) {
        val it: MutableIterator<MutableMap.MutableEntry<Int, Bitmap?>> = mPrefetchCache.snapshot().entries.iterator()
        while (it.hasNext()) {
            val (key) = it.next()
            if (if (forward) key < hintIndex else key > hintIndex) {
                mPrefetchCache.remove(key)
            }
        }
        val inc = if (forward) 1 else -1
        var i = hintIndex
        while ((mRequests.size() + mPrefetchCache.size()
                    < mPrefetchCache.maxSize()) && if (inc > 0) i < mSeekPositions.size else i >= 0
        ) {
            val key = i
            if (mCache[key] == null && mPrefetchCache[key] == null) {
                var task = mRequests[i]
                if (task == null) {
                    task = LoadBitmapTask(key, null)
                    mRequests.put(i, task)
                    task!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                }
            }
            i += inc
        }
    }

    private inner class LoadBitmapAsyncTask(private val mIndex: Int, private val mResultCallback: ResultCallback) :
        AsyncTask<Void, Void, Bitmap?>() {

        override fun doInBackground(vararg voids: Void): Bitmap? {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoUrl, HashMap<String, String>())
            val position = mSeekPositions[mIndex]
            Log.d("SeekProvider", "position: $position")

            val thumbnail = retriever.getFrameAtTime(position * 1000, MediaMetadataRetriever.OPTION_CLOSEST)
            retriever.release()
            return thumbnail
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            super.onPostExecute(bitmap)
            mResultCallback.onThumbnailLoaded(bitmap, mIndex)
        }
    }
    override fun toString(): String {
        val b = StringBuilder()
        println(b)
        b.append("Requests<")
        for (i in 0 until mRequests.size()) {
            b.append(mRequests.keyAt(i))
            b.append(",")
        }
        b.append("> Cache<")
        run {
            val it: MutableIterator<Int> = mCache.snapshot().keys.iterator()
            while (it.hasNext()) {
                val key = it.next()
                if (mCache[key] != null) {
                    b.append(key)
                    b.append(",")
                }
            }
        }
        b.append(">")
        b.append("> PrefetchCache<")
        val it: MutableIterator<Int> = mPrefetchCache.snapshot().keys.iterator()
        while (it.hasNext()) {
            val key = it.next()
            if (mPrefetchCache[key] != null) {
                b.append(key)
                b.append(",")
            }
        }
        b.append(">")
        return b.toString()
    }

    companion object {
        const val TAG = "SeekAsyncProvider"
    }
}