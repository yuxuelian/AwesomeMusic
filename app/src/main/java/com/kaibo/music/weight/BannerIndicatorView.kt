package com.kaibo.music.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import com.kaibo.music.R
import org.jetbrains.anko.dip

/**
 * @author kaibo
 * @date 2018/10/9 14:27
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class BannerIndicatorView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    private val selectedWidth: Int
    private val circleRadius: Int
    private val circleMargin: Int
    private val selectedColor: Int
    private val circleColor: Int

    /**
     * 圆圈总个数
     */
    var circleCount: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 当前选中的圆圈
     */
    private var currentSelectCircle: Int = 0

    /**
     * 偏移率
     */
    private var positionOffset: Float = 0F

    /**
     * 滑动方向   true  从右向左滑动   false  从左向右滑动
     */
    private var scrollOrientation = true

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * 监听ViewPager的滑动事件
     */
    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {

        //记录上一次的偏移值
        private var lastPixels = 0

        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//            scrollOrientation = positionOffsetPixels > lastPixels
//            lastPixels = positionOffsetPixels
//            this@BannerIndicatorView.positionOffset = positionOffset
//            // 触发重绘
//            invalidate()
        }

        override fun onPageSelected(position: Int) {
            // 同步一下current
            this@BannerIndicatorView.currentSelectCircle = position - 1
            invalidate()
        }
    }

    init {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.BannerIndicatorView)
        selectedWidth = ty.getDimensionPixelSize(R.styleable.BannerIndicatorView_selectedWidth, dip(10))
        circleRadius = ty.getDimensionPixelSize(R.styleable.BannerIndicatorView_circleRadius, dip(5))
        circleMargin = ty.getDimensionPixelSize(R.styleable.BannerIndicatorView_circleMargin, dip(10))
        selectedColor = ty.getColor(R.styleable.BannerIndicatorView_selectedColor, Color.RED)
        circleColor = ty.getColor(R.styleable.BannerIndicatorView_circleColor, Color.WHITE)
        ty.recycle()
    }

    /**
     * 关联ViewPager
     * 关联前需要设置总个数
     */
    fun setupWithViewPager(viewPager: ViewPager) {
        // 首先移出一次防止重复添加
        viewPager.removeOnPageChangeListener(onPageChangeListener)
        // 添加
        viewPager.addOnPageChangeListener(onPageChangeListener)
    }

    private fun getWidth(size: Int, measureSpec: Int): Int {
        return when (MeasureSpec.getMode(measureSpec)) {
        // match_parent count*circleRadius*2+
            MeasureSpec.AT_MOST -> {
                circleCount * circleRadius * 2 + (circleCount - 1) * circleMargin + selectedWidth + paddingLeft + paddingStart + paddingRight + paddingEnd
            }
        // wrap_content
            MeasureSpec.UNSPECIFIED -> {
                circleCount * circleRadius * 2 + (circleCount - 1) * circleMargin + selectedWidth + paddingLeft + paddingStart + paddingRight + paddingEnd
            }
        // 精确测量   比如xml中指定了尺寸的
            MeasureSpec.EXACTLY -> {
                MeasureSpec.getSize(measureSpec)
            }
            else -> size
        }
    }

    private fun getHeight(size: Int, measureSpec: Int): Int {
        return when (MeasureSpec.getMode(measureSpec)) {
        // match_parent  高度为两个圆半径
            MeasureSpec.AT_MOST -> {
                size
            }
        // wrap_content
            MeasureSpec.UNSPECIFIED -> {
                circleRadius * 2 + paddingBottom + paddingTop
            }
        // 精确测量   比如xml中指定了尺寸的
            MeasureSpec.EXACTLY -> {
                MeasureSpec.getSize(measureSpec)
            }
            else -> size
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(getWidth(suggestedMinimumWidth, widthMeasureSpec), getHeight(suggestedMinimumHeight, heightMeasureSpec))
    }

    private val tempRectF = RectF()

    override fun onDraw(canvas: Canvas) {
//        if (scrollOrientation) {
//            // currentSelectCircle 和  currentSelectCircle+1之间切换
//            // [0,currentSelectCircle)
//            (0 until currentSelectCircle).forEach {
//                drawCircle(canvas, it, true)
//            }
//
//            // currentSelectCircle
//            drawRoundRect(canvas, currentSelectCircle, selectedWidth * positionOffset)
//            // currentSelectCircle+1
//            drawRoundRect(canvas, currentSelectCircle + 1, selectedWidth * (1 - positionOffset))
//            // [currentSelectCircle + 2,circleCount)
//            (currentSelectCircle + 2 until circleCount).forEach {
//                drawCircle(canvas, it, false)
//            }
//        } else {
//            // currentSelectCircle 和  currentSelectCircle-1之间切换
//            // [0,currentSelectCircle - 1)
//            (0 until currentSelectCircle - 1).forEach {
//                //绘制圆
//                drawCircle(canvas, it, true)
//            }
//            // currentSelectCircle - 1
//            drawRoundRect(canvas, currentSelectCircle, selectedWidth * (1 - positionOffset))
//            // currentSelectCircle
//            drawRoundRect(canvas, currentSelectCircle + 1, selectedWidth * (1 - positionOffset))
//            // [currentSelectCircle + 1,circleCount)
//            (currentSelectCircle + 1 until circleCount).forEach {
//                //绘制圆
//                drawCircle(canvas, it, false)
//            }
//        }

        (0 until currentSelectCircle).forEach {
            drawCircle(canvas, it, true)
        }
        // currentSelectCircle
        drawRoundRect(canvas, currentSelectCircle, selectedWidth.toFloat())
        // [currentSelectCircle + 2,circleCount)
        (currentSelectCircle + 1 until circleCount).forEach {
            drawCircle(canvas, it, false)
        }
    }

    private fun drawCircle(canvas: Canvas, index: Int, currentLeft: Boolean) {
        paint.color = circleColor
        val cx = circleMargin * index + circleRadius * 2 * index + circleRadius + if (currentLeft) {
            0
        } else {
            selectedWidth
        }.toFloat()
        val cy = circleRadius.toFloat()
        canvas.drawCircle(cx, cy, circleRadius.toFloat(), paint)
    }

    private fun drawRoundRect(canvas: Canvas, index: Int, roundWidth: Float) {
        paint.color = if (positionOffset - 0F < 0.000001F) {
            selectedColor
        } else {
            circleColor
        }
        val left = (circleMargin + circleRadius * 2) * index.toFloat()
        tempRectF.set(left, 0F, left + roundWidth + circleRadius * 2, (circleRadius * 2).toFloat())
        canvas.drawRoundRect(tempRectF, circleRadius.toFloat(), circleRadius.toFloat(), paint)
    }

}