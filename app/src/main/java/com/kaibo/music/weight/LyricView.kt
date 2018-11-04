package com.kaibo.music.weight

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.kaibo.music.R
import com.kaibo.music.bean.LyricRowBean
import com.yan.pullrefreshlayout.BezierInterpolator
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.dip
import java.util.concurrent.TimeUnit

/**
 * @author kaibo
 * @date 2018/11/4 16:46
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class LyricView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    companion object {
        private const val MIN_DURATION = 200L
        private const val MAX_DURATION = 600L
    }

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val mPaint: Paint = Paint()

    /**
     * Y中心
     */
    private var centerY: Float = 0f
    private var centerX: Float = 0f

    /**
     * 当前View的宽高
     */
    private var width = 0f
    private var height = 0f

    /**
     * 基准线
     */
    private var textDy: Float = 0f

    @Px
    var textSize = 12f
        set(value) {
            field = value
            // 设置字体大小
            mPaint.textSize = value
            // 这里主要是测量文本的高度
            mPaint.getTextBounds("测", 0, 1, rectBound)
            //dy 代表的是：高度的一半到 baseLine的距离
            val fontMetrics = mPaint.fontMetricsInt
            // top 是一个负值  bottom 是一个正值    top，bttom的值代表是  bottom是baseLine到文字底部的距离（正值）
            // 必须要清楚的，可以自己打印就好
            textDy = rectBound.height() / 2 + ((fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom)
            // 重新计算top
            resetTop()
            // 触发重绘
            invalidate()
        }

    @ColorInt
    var textColor = Color.GRAY
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var highTextColor = Color.RED
        set(value) {
            field = value
            invalidate()
        }

    @Px
    var lyricMargin = dip(4)
        set(value) {
            field = value
            // 重新计算top
            resetTop()
            invalidate()
        }

    /**
     * 当前的top值  修改这个值可以移动歌词上下移动
     */
    var top = 0f
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 初始化的top值
     */
    private var initTop = 0f

    /**
     * 最大top和最小top
     */
    private var maxTop = 0f
    private var minTop = 0f

    private fun createAnimator(start: Float, to: Float): Animator {
        val animator = ObjectAnimator.ofFloat(this, "top", start, to)
        animator.interpolator = BezierInterpolator()
        animator.duration = (MIN_DURATION + Math.abs(to - start) / Math.abs(maxTop - minTop) * (MAX_DURATION - MIN_DURATION)).toLong()
        return animator
    }

    private var moveAnimator: Animator? = null

    /**
     * 当前需要显示哪一条歌词
     */
    var showPosition: Int = 0
        set(value) {
            if (field != value) {
                field = value
                // 重新测量top值
                if (!isTouch) {
                    if (moveAnimator != null && moveAnimator!!.isRunning) {
                        moveAnimator!!.cancel()
                    }
                    // 创建移动动画
                    moveAnimator = createAnimator(top, initTop - value * (rectBound.height() + lyricMargin))
                    moveAnimator!!.start()
                }
                invalidate()
            }
        }

    /**
     * 歌词列表
     */
    var lyricRowBeans: List<LyricRowBean>? = null
        set(value) {
            if (value != null) {
                field = value
                resetTop()
                // 重绘
                invalidate()
            }
        }

    /**
     * 每个字母占据的位置
     */
    private val rectBound by lazy {
        Rect()
    }

    @Volatile
    private var isTouch = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LyricView)
        // 文字大小
        textSize = typedArray.getDimensionPixelSize(R.styleable.LyricView_android_textSize, 12).toFloat()
        // 行间距
        lyricMargin = typedArray.getDimensionPixelSize(R.styleable.LyricView_lyric_margin, 12)
        // 文字颜色
        textColor = typedArray.getColor(R.styleable.LyricView_android_textColor, Color.GRAY)
        // 选中文字颜色
        highTextColor = typedArray.getColor(R.styleable.LyricView_high_textColor, Color.RED)
        typedArray.recycle()

        mPaint.textSize = this@LyricView.textSize
        mPaint.color = this@LyricView.textColor
        mPaint.isAntiAlias = true
        mPaint.textAlign = Paint.Align.CENTER
    }

    private var lastY = 0f

    private var timerDisposable: Disposable? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 释放定时器
                if (timerDisposable != null && !timerDisposable!!.isDisposed) {
                    timerDisposable!!.dispose()
                }
                if (moveAnimator != null && moveAnimator!!.isRunning) {
                    moveAnimator!!.cancel()
                }
                isTouch = true
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                // 修改top值
                top += (event.y - lastY)
                if (top < minTop) {
                    top = minTop
                } else if (top > maxTop) {
                    top = maxTop
                }
                lastY = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 可以延迟5s再设置为false  可以防止松手后立即恢复的现象
                timerDisposable = Observable
                        .timer(5000L, TimeUnit.MILLISECONDS)
                        .subscribe {
                            isTouch = false
                        }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        // 计算可以绘制多少行
        lyricRowBeans?.forEachIndexed { index, lyricRowBean ->
            if (index == showPosition) {
                mPaint.color = highTextColor
            } else {
                mPaint.color = textColor
            }
            canvas.drawText(lyricRowBean.rowText, centerX, top + (rectBound.height() + lyricMargin) * index + textDy, mPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        width = w.toFloat()
        height = h.toFloat()
        centerX = w / 2.0f
        centerY = h / 2.0f
        resetTop()
    }

    private fun resetTop() {
        initTop = centerY - rectBound.height() / 2
        maxTop = initTop
        minTop = maxTop - (rectBound.height() + lyricMargin) * ((lyricRowBeans?.size ?: 0) - 1)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (moveAnimator != null && moveAnimator!!.isRunning) {
            moveAnimator!!.cancel()
        }
        if (timerDisposable != null && !timerDisposable!!.isDisposed) {
            timerDisposable!!.dispose()
        }
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }
}