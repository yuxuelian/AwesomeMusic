package com.kaibo.music.play

import com.kaibo.music.bean.SongBean

/**
 * @author kaibo
 * @date 2018/10/16 17:22
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

sealed class PlayerCommand

data class DataSourceCommand(val songBean: SongBean) : PlayerCommand()

data class SeekCommand(val seek: Int) : PlayerCommand()

