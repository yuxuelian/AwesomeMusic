package com.kaibo.music.player.player.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.kaibo.music.player.IMusicPlayer

/**
 * @author kaibo
 * @date 2019/4/3 12:10
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class HeadsetReceiver(private val iMusicPlayer: IMusicPlayer) : BroadcastReceiver() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            //当前是正在运行的时候才能通过媒体按键来操作音频
            var action = intent.action
            action = action ?: ""
            when (action) {
                // 蓝牙耳机的连接状态发生改变
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    if (bluetoothAdapter != null) {
                        val profileConnectionState = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)
                        if (BluetoothProfile.STATE_DISCONNECTED == profileConnectionState && iMusicPlayer.isPlaying) {
                            //蓝牙耳机断开连接 同时当前音乐正在播放 则将其暂停
                            iMusicPlayer.pause()
                        }
                    }
                }
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    if (iMusicPlayer.isPlaying) {
                        //有线耳机断开连接 同时当前音乐正在播放 则将其暂停
                        iMusicPlayer.pause()
                    }
                }
                else -> {
                }
            }
        }
    }
}