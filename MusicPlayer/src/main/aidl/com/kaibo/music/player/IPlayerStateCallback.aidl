package com.kaibo.music.player;

import com.kaibo.music.player.bean.PlayerStateBean;

interface IPlayerStateCallback {
    void callback(in PlayerStateBean playerState);
}
