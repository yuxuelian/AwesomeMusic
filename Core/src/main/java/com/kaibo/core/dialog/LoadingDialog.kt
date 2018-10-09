package com.kaibo.core.dialog

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.KeyEvent
import android.view.WindowManager
import com.wang.avi.indicators.*
import com.kaibo.core.R
import com.kaibo.core.util.getRandom
import kotlinx.android.synthetic.main.dialog_loading.*

/**
 * @author kaibo
 * @date 2018/6/27 14:29
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class LoadingDialog : BaseDialog() {

    companion object {
        val loadingStyle by lazy {
            arrayOf(
                    BallBeatIndicator::class.java,
                    BallClipRotateIndicator::class.java,
                    BallClipRotateMultipleIndicator::class.java,
                    BallClipRotatePulseIndicator::class.java,
                    BallGridBeatIndicator::class.java,
                    BallGridPulseIndicator::class.java,
                    BallPulseIndicator::class.java,
                    BallPulseRiseIndicator::class.java,
                    BallPulseSyncIndicator::class.java,
                    BallRotateIndicator::class.java,
                    BallScaleIndicator::class.java,
                    BallScaleMultipleIndicator::class.java,
                    BallScaleRippleIndicator::class.java,
                    BallScaleRippleMultipleIndicator::class.java,
                    BallSpinFadeLoaderIndicator::class.java,
                    BallTrianglePathIndicator::class.java,
                    BallZigZagDeflectIndicator::class.java,
                    BallZigZagIndicator::class.java,
                    CubeTransitionIndicator::class.java,
                    LineScaleIndicator::class.java,
                    LineScalePartyIndicator::class.java,
                    LineScalePulseOutIndicator::class.java,
                    LineScalePulseOutRapidIndicator::class.java,
                    LineSpinFadeLoaderIndicator::class.java,
                    PacmanIndicator::class.java,
                    SemiCircleSpinIndicator::class.java,
                    SquareSpinIndicator::class.java,
                    TriangleSkewSpinIndicator::class.java
            )
        }
    }

    override fun getLayoutRes() = R.layout.dialog_loading

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        isCancelable = false
        //屏蔽返回键
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
        avLoadingIndicatorView.indicator = loadingStyle[getRandom(0, loadingStyle.size)].newInstance()
    }

    fun show(manager: FragmentManager) {
        show(manager, toString())
    }

    fun hide() {
        dismiss()
    }

    override fun getSize() = Pair(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
}
