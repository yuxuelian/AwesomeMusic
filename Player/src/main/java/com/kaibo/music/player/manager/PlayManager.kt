package com.kaibo.music.player.manager

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException

import com.kaibo.music.ISongService
import com.kaibo.music.bean.SongBean
import com.kaibo.music.player.service.MusicPlayerService
import com.orhanobut.logger.Logger

import java.util.ArrayList
import java.util.WeakHashMap

/**
 * 对外提供的播放控制
 */
object PlayManager {

    private var mService: ISongService? = null

    private val mConnectionMap = WeakHashMap<Context, ServiceBinder>()

    val audioSessionId: Int
        get() {
            try {
                if (mService != null) {
                    return mService!!.audioSessionId
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return 0
        }

    val currentPosition: Int
        get() {
            try {
                if (mService != null) {
                    return mService!!.currentPosition
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return 0
        }

    val duration: Int
        get() {
            try {
                if (mService != null) {
                    return mService!!.duration
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            return 0
        }

    val songName: String
        get() {
            try {
                if (mService != null) {
                    return mService!!.songName
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            return " DelicateMusic"
        }

    val songArtist: String
        get() {
            try {
                if (mService != null) {
                    return mService!!.songArtist
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            return " DelicateMusic"
        }

    val isPlaying: Boolean
        get() {
            try {
                if (mService != null) {
                    return mService!!.isPlaying
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            return false
        }

    val isPause: Boolean
        get() {
            try {
                if (mService != null) {
                    return mService!!.isPause
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            return false
        }

    /**
     * 获取正在播放的歌曲
     *
     * @return
     */
    val playingMusic: SongBean?
        get() {
            try {
                if (mService != null) {
                    return mService!!.playingSongBean
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            return null
        }

    /**
     * 获取正在播放音乐的mid
     *
     * @return
     */
    val playingId: String
        get() {
            try {
                if (mService != null && mService!!.playingSongBean != null) {
                    return mService!!.playingSongBean.mid
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            return ""
        }

    val playList: List<SongBean>
        get() {
            try {
                if (mService != null) {
                    return mService!!.playList
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            return ArrayList()
        }

    fun playOnline(music: SongBean) {
        try {
            if (mService != null) {
                mService!!.playSong(music)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun nextPlay(music: SongBean) {
        try {
            if (mService != null) {
                mService!!.nextPlay(music)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun bindToService(context: Context, callback: ServiceConnection): ServiceToken? {
        var realActivity: Activity? = (context as Activity).parent
        if (realActivity == null) {
            realActivity = context
        }
        val contextWrapper = ContextWrapper(realActivity)
        contextWrapper.startService(Intent(contextWrapper, MusicPlayerService::class.java))
        val binder = ServiceBinder(callback)
        return if (contextWrapper.bindService(Intent().setClass(contextWrapper, MusicPlayerService::class.java), binder, 0)) {
            mConnectionMap[contextWrapper] = binder
            Logger.d("绑定成功")
            ServiceToken(contextWrapper)
        } else {
            Logger.d("绑定失败")
            null
        }
    }

    fun unbindFromService(token: ServiceToken?) {
        if (token == null) {
            return
        }
        val mContextWrapper = token.mWrappedContext
        val mBinder = mConnectionMap[mContextWrapper] ?: return
        mContextWrapper.unbindService(mBinder)
        if (mConnectionMap.isEmpty()) {
            mService = null
        }
    }

    fun play(id: Int) {
        try {
            if (mService != null) {
                mService!!.play(id)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun play(id: Int, musicList: List<SongBean>, pid: String) {
        try {
            if (mService != null) {
                mService!!.playPlaylist(musicList, id, pid)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun playPause() {
        try {
            if (mService != null) {
                mService!!.playPause()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun prev() {
        try {
            if (mService != null) {
                mService!!.prev()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    operator fun next() {
        try {
            if (mService != null) {
                mService!!.next()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun setPlayMode(loopmode: Int) {
        try {
            if (mService != null) {
                mService!!.setPlayMode(loopmode)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun seekTo(ms: Int) {
        try {
            if (mService != null) {
                mService!!.seekTo(ms)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun position(): Int {
        try {
            if (mService != null) {
                return mService!!.position()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

        return 0
    }

    fun clearQueue() {
        try {
            if (mService != null) {
                mService!!.clearQueue()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun removeFromQueue(adapterPosition: Int) {
        try {
            if (mService != null) {
                mService!!.removeFromQueue(adapterPosition)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun showDesktopLyric(isShow: Boolean) {
        try {
            if (mService != null) {
                mService!!.showDesktopLyric(isShow)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    class ServiceBinder(private val mCallback: ServiceConnection?) : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // 获取到远端的Binder
            mService = ISongService.Stub.asInterface(service)
            mCallback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mCallback?.onServiceDisconnected(className)
            mService = null
        }
    }

    class ServiceToken(var mWrappedContext: ContextWrapper)
}
