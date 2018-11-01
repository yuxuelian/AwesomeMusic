package com.kaibo.music.player.manager

import android.app.Activity
import android.content.*
import android.os.IBinder
import com.kaibo.music.ISongService
import com.kaibo.music.bean.SongBean
import com.kaibo.music.player.service.MusicPlayerService
import com.orhanobut.logger.Logger
import java.util.*

/**
 * 对外提供的播放控制
 */
object PlayManager {

    private var mService: ISongService? = null

    private val mConnectionMap = WeakHashMap<Context, ServiceBinder>()

    /**
     * 获取当前的所播放歌曲的播放进度
     */
    val currentPosition: Int
        get() {
            return try {
                mService?.currentPosition ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }

    /**
     * 获取当前所播放的歌曲的总时长
     */
    val duration: Int
        get() {
            return try {
                mService?.duration ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }

    /**
     * 是否正在播放
     */
    val isPlaying: Boolean
        get() {
            return try {
                mService?.isPlaying ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    /**
     * 获取正在播放的歌曲  没有播放返回null
     *
     * @return
     */
    val playSong: SongBean?
        get() {
            return try {
                mService?.playSong
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    /**
     * 获取当前正在播放的播放队列
     */
    val playSongQueue: List<SongBean>
        get() {
            return try {
                mService?.playSongQueue ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

    /**
     * 设置一首歌曲到播放队列  并执行播放
     */
    fun setPlaySong(songBean: SongBean) {
        try {
            mService?.playSong = songBean
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置一首歌曲到播放队列  但在下一次切歌的时候才执行播放
     */
    fun nextPlay(songBean: SongBean) {
        try {
            mService?.setNextSong(songBean)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 播放位置
     */
    var playPosition: Int
        set(value) {
            try {
                mService?.playPosition = value
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        get() {
            return try {
                mService?.playPosition ?: -1
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }
        }

    /**
     * 指定一个播放列表开始播放
     */
    fun setPlaySongList(playlist: List<SongBean>, position: Int, playListId: String) {
        try {
            mService?.setPlaySongList(playlist, position, playListId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 切换播放暂停
     */
    fun togglePlayer() {
        try {
            mService?.togglePlayer()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 上一曲
     */
    fun prev() {
        try {
            mService?.prev()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 下一曲
     */
    fun next() {
        try {
            mService?.next()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekTo(pos: Int) {
        try {
            mService?.seekTo(pos)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun clearQueue() {
        try {
            mService?.clearQueue()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun removeFromQueue(adapterPosition: Int) {
        try {
            mService?.removeFromQueue(adapterPosition)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun showDesktopLyric(isShow: Boolean) {
        try {
            mService?.showDesktopLyric(isShow)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 切换播放模式
     */
    fun updatePlayMode(): String {
        return try {
            mService?.updatePlayMode() ?: "切换失败"
        } catch (e: Exception) {
            e.printStackTrace()
            "切换失败"
        }
    }

    /**
     * 绑定播放Service到指定的Activity
     */
    fun bindToService(context: Context): ServiceToken? {
        var realActivity: Activity? = (context as Activity).parent
        if (realActivity == null) {
            realActivity = context
        }
        val contextWrapper = ContextWrapper(realActivity)
        // 启动Service
        contextWrapper.startService(Intent(contextWrapper, MusicPlayerService::class.java))
        val binder = ServiceBinder()
        // 绑定Service
        return if (contextWrapper.bindService(Intent().setClass(contextWrapper, MusicPlayerService::class.java), binder, 0)) {
            mConnectionMap[contextWrapper] = binder
            Logger.d("绑定成功")
            ServiceToken(contextWrapper)
        } else {
            Logger.e("绑定失败")
            null
        }
    }

    /**
     * 解除绑定
     */
    fun unbindFromService(token: ServiceToken?) {
        val mContextWrapper = token?.mWrappedContext ?: return
        val mBinder = mConnectionMap[mContextWrapper] ?: return
        mContextWrapper.unbindService(mBinder)
        if (mConnectionMap.isEmpty()) {
            mService = null
        }
    }

    class ServiceBinder : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // 获取到远端的Binder
            mService = ISongService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
        }
    }

    class ServiceToken(var mWrappedContext: ContextWrapper)
}
