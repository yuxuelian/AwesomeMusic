package com.kaibo.music.player.player.listener

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.kaibo.music.player.IMusicPlayer

/**
 * @author kaibo
 * @date 2019/4/3 11:53
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class ServicePhoneStateListener(private val iMusicPlayer: IMusicPlayer) : PhoneStateListener() {

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        when (state) {
            //通话状态
            TelephonyManager.CALL_STATE_OFFHOOK,
                //通话状态
            TelephonyManager.CALL_STATE_RINGING -> iMusicPlayer.pause()
            TelephonyManager.CALL_STATE_IDLE -> {
            }
            else -> {
            }
        }
    }

}