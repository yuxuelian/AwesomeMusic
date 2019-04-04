package com.kaibo.music.weight

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.kaibo.core.util.dip
import com.kaibo.core.util.sp
import com.kaibo.music.R

/**
 * @author kaibo
 * @createDate 2018/10/13 16:39
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class SlideBar
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    /**
     * 选中的字母索引
     */
    var index: Int = 0
        set(value) {
            if (value in 0 until letters.size) {
                field = value
                invalidate()
            } else {
                throw IllegalArgumentException("value must is 0 until letters.size")
            }
        }

    /**
     * 字母默认颜色
     */
    @ColorInt
    var textColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 字母选中颜色
     */
    @ColorInt
    var selectedTextColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 字母字体大小
     */
    @Px
    var textSize: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * 上下边距的位置
     */
    @Px
    var letterMargin: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * 字母改变监听
     */
    var letterChangeListener: ((index: Int, letter: String) -> Unit)? = null

    /**
     * 字母数组
     */
    var letters = emptyList<String>()
        set(value) {
            val _value = value.toMutableList()
            // 参数验证
            if (_value.isEmpty()) {
                throw IllegalArgumentException("letters is empty")
            }
            _value.forEachIndexed { index, letter ->
                if (letter.isEmpty()) {
                    throw IllegalArgumentException("letter is empty")
                } else if (letter.length > 1) {
                    // 长度超过1的字符串只取第一个字符
                    _value[index] = _value[index].substring(0, 1)
                }
            }
            // 修改大小
            lettersHeight = FloatArray(_value.size + 1)
            field = _value
            requestLayout()
            invalidate()
        }

    private var lettersHeight = FloatArray(letters.size + 1)

    /**
     * 画笔
     */
    private val paint by lazy {
        Paint()
    }

    /**
     * 每个字母占据的位置
     */
    private val rectBound by lazy {
        Rect()
    }

    init {
        paint.textAlign = Align.CENTER
        // 抗锯齿
        paint.isAntiAlias = true
        val a = context.obtainStyledAttributes(attrs, R.styleable.SlideBar)
        // 获取字符间距
        letterMargin = a.getDimensionPixelSize(R.styleable.SlideBar_letterMargin, dip(3f))
        // 字体大小
        textSize = a.getDimensionPixelSize(R.styleable.SlideBar_android_textSize, sp(12f))
        // 默认显示的字体颜色
        textColor = a.getColor(R.styleable.SlideBar_android_textColor, Color.WHITE)
        // 选中字体颜色
        selectedTextColor = a.getColor(R.styleable.SlideBar_selectedTextColor, Color.RED)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        paint.textSize = textSize.toFloat()
        // 测量字体的宽高
        var maxWidth = 0
        var maxHeight = 0
        for (letter in letters) {
            paint.getTextBounds(letter, 0, 1, rectBound)
            if (rectBound.width() > maxWidth) {
                //更新宽度值
                maxWidth = rectBound.width()
            }
            if (rectBound.height() > maxHeight) {
                //更新宽度值
                maxHeight = rectBound.height()
            }
        }
        lettersHeight[0] = paddingTop.toFloat()
        for (i in 1 until lettersHeight.size) {
            lettersHeight[i] = lettersHeight[i - 1] + maxHeight.toFloat() + (letterMargin * 2).toFloat()
        }
        val defaultWidth = maxWidth + paddingLeft + paddingRight
        val defaultHeight = (maxHeight + letterMargin * 2) * letters.size + paddingTop + paddingBottom
        val width = measureHandler(widthMeasureSpec, defaultWidth)
        val height = measureHandler(heightMeasureSpec, defaultHeight)
        setMeasuredDimension(width, height)
    }

    private fun measureHandler(measureSpec: Int, defaultSize: Int): Int {
        var result = defaultSize
        val measureMode = View.MeasureSpec.getMode(measureSpec)
        val measureSize = View.MeasureSpec.getSize(measureSpec)
        if (measureMode == View.MeasureSpec.EXACTLY) {
            result = measureSize
        } else if (measureMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, measureSize)
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        // 设置字体大小
        paint.textSize = textSize.toFloat()
        //dy 代表的是：高度的一半到 baseLine的距离
        val fontMetrics = paint.fontMetricsInt
        // top 是一个负值  bottom 是一个正值    top，bttom的值代表是  bottom是baseLine到文字底部的距离（正值）
        // 必须要清楚的，可以自己打印就好
        val dy = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
        for (i in letters.indices) {
            // 测量文字宽度
            paint.getTextBounds(letters[i], 0, 1, rectBound)
            // 计算文字的基线 x y坐标
            val x = (measuredWidth / 2).toFloat()
            val centerY = (lettersHeight[i] + lettersHeight[i + 1]) / 2f
            val y = centerY + dy
            // 这只画笔颜色
            if (i == index) {
                //选中时的画笔颜色
                paint.color = selectedTextColor
            } else {
                //未选中时的画笔颜色
                paint.color = textColor
            }
            // 绘制文字
            canvas.drawText(letters[i], x, y, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> updateDraw(event.y)
            else -> {
            }
        }
        return true
    }

    private fun updateDraw(y: Float) {
        var _y = y
        if (_y < lettersHeight[0]) {
            _y = lettersHeight[0]
        }
        if (_y > lettersHeight[lettersHeight.size - 1]) {
            _y = lettersHeight[lettersHeight.size - 1]
        }
        val currentIndex = findIndex(_y)
        if (currentIndex != index) {
            index = currentIndex
            //触发重绘
            invalidate()
            letterChangeListener?.invoke(index, letters[index])
        }
    }

    /**
     * 找当前索引位置
     *
     * @param y
     * @return
     */
    private fun findIndex(y: Float): Int {
        for (i in 0 until lettersHeight.size - 1) {
            val start = lettersHeight[i]
            val end = lettersHeight[i + 1]
            if (y in start..end) {
                return i
            }
        }
        throw IllegalStateException("exec is not expect result")
    }
}


