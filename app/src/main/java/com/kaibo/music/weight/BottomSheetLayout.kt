package com.kaibo.music.weight

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.ListView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.view.*
import androidx.core.widget.ListViewCompat
import com.kaibo.core.util.dip
import com.yan.pullrefreshlayout.BezierInterpolator

class BottomSheetLayout @JvmOverloads constructor(
        @NonNull context: Context,
        @Nullable attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr),
        NestedScrollingParent,
        NestedScrollingChild {

    private val mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var mTotalUnconsumed: Float = 0.toFloat()
    private val mNestedScrollingParentHelper: NestedScrollingParentHelper
    private val mNestedScrollingChildHelper: NestedScrollingChildHelper
    private val mParentScrollConsumed = IntArray(2)
    private val mParentOffsetInWindow = IntArray(2)
    private var mNestedScrollInProgress: Boolean = false
    private var mInitialMotionY: Float = 0.toFloat()
    private var mInitialDownY: Float = 0.toFloat()

    /**
     * 标记是否正在拖拽
     */
    private var mIsBeingDragged: Boolean = false

    /**
     * 多点触控  标记触摸手指id
     */
    private var mActivePointerId = INVALID_POINTER

    /**
     * 回弹View
     */
    private lateinit var mTargetView: View

    /**
     * 执行动画
     */
    private var mSpringBackAnimator: Animator? = null

    /**
     * targetView最大动画移动距离
     */
    private var maxSpringBackDistance: Float = 0.toFloat()

    var onCollapseListener: (() -> Unit)? = null

    /**
     * 记录开始滑动的时间戳
     */
    private var startTouchTime: Long = 0

    /**
     * 当前TargetView的Top值
     */
    private var mLayoutTop: Float = 0f

    init {
        // 使onDraw得到执行
        setWillNotDraw(false)
        mNestedScrollingParentHelper = NestedScrollingParentHelper(this)
        mNestedScrollingChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
        // 查找xml中设置的是否enabled
        val typedArray = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS)
        isEnabled = typedArray.getBoolean(0, true)
        typedArray.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 0) {
            mTargetView = getChildAt(0)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // 当View大小改变的时候  修改这个最大回弹距离
        maxSpringBackDistance = h.toFloat()
    }

    /**
     * 判断targetView是否还能够继续向下滑动
     *
     * @return
     */
    private fun canChildScrollUp(): Boolean {
        return if (mTargetView is ListView) {
            ListViewCompat.canScrollList((mTargetView as ListView?)!!, -1)
        } else mTargetView.canScrollVertically(-1)
    }

    /**
     * 移动子View
     *
     * @param overScrollTop
     */
    private fun moveSpinner(overScrollTop: Float) {
        // 向下拉
        if (overScrollTop > 0) {
            // 记录一下Top值
            mLayoutTop = overScrollTop
            // 移动 y
            mTargetView.translationY = overScrollTop
        }
    }

    /**
     * 向上或者向下回弹动画
     *
     * @param isExpand true 动画将向上回弹   false  动画往下回弹
     * @return
     */
    private fun createSpringBackAnimator(isExpand: Boolean): Animator {
        // 结束位置
        val endPosition = if (isExpand) 0f else mTargetView.measuredHeight.toFloat()
        // 计算duration
        val duration = (MAX_DURATION * (Math.abs(endPosition - mLayoutTop) / maxSpringBackDistance)).toLong()
        val animator = ObjectAnimator.ofFloat(mTargetView, "translationY", mLayoutTop, endPosition)
        animator.interpolator = BezierInterpolator()
        animator.duration = duration
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 回调关闭事件
                if (!isExpand) {
                    onCollapseListener?.invoke()
                }
            }
        })
        return animator
    }

    /**
     * 松手会调用这个方法
     *
     * @param overScrollTop
     */
    private fun finishSpinner(overScrollTop: Float) {
        // 计算滑动速率
        val velocity = dip(overScrollTop.toInt()) / (System.currentTimeMillis() - startTouchTime)
        // 创建动画
        mSpringBackAnimator = if (velocity > 0.5f) {
            // 速率过大的情况  不需要判断当前的滑动距离是否超过了中线   直接关闭
            createSpringBackAnimator(false)
        } else {
            // 速率过小的情况  去判断滑动距离是否超过了中线
            createSpringBackAnimator(
                    overScrollTop < mTargetView.measuredHeight / 2.0)
        }
        // 启动回弹动画
        mSpringBackAnimator!!.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // view从屏幕移出的时候 取消正在执行的动画
        if (mSpringBackAnimator?.isRunning == true) {
            mSpringBackAnimator?.cancel()
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex: Int
        if (!isEnabled || canChildScrollUp() || mNestedScrollInProgress) {
            return false
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录开始滑动的时间戳
                startTouchTime = System.currentTimeMillis()
                mActivePointerId = event.getPointerId(0)
                mIsBeingDragged = false
                pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                // 记录触摸的起点Y
                mInitialDownY = event.getY(pointerIndex)
                // 当有手指按下时结束正在执行的动画
                if (mSpringBackAnimator?.isRunning == true) {
                    mSpringBackAnimator?.cancel()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId == INVALID_POINTER) {
                    return false
                }
                pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                val y = event.getY(pointerIndex)
                startDragging(y)
            }
            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }
            else -> {
            }
        }
        return mIsBeingDragged
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex: Int

        if (!isEnabled || canChildScrollUp() || mNestedScrollInProgress) {
            return false
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = event.getPointerId(0)
                mIsBeingDragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                val y = event.getY(pointerIndex)
                startDragging(y)
                if (mIsBeingDragged) {
                    val overScrollTop = (y - mInitialMotionY) * DRAG_RATE
                    // 直接拖拽this.View的时候
                    moveSpinner(overScrollTop)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                pointerIndex = event.actionIndex
                if (pointerIndex < 0) {
                    return false
                }
                mActivePointerId = event.getPointerId(pointerIndex)
            }
            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(event)
            MotionEvent.ACTION_UP -> {
                pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                if (mIsBeingDragged) {
                    val y = event.getY(pointerIndex)
                    val overScrollTop = (y - mInitialMotionY) * DRAG_RATE
                    mIsBeingDragged = false
                    if (overScrollTop > 0) {
                        finishSpinner(overScrollTop)
                    }
                }
                mActivePointerId = INVALID_POINTER
                return false
            }
            MotionEvent.ACTION_CANCEL -> return false
            else -> {
            }
        }
        return true
    }

    /**
     * 计算是否是在开始拖拽了(主要是为了区分点击事件)
     *
     * @param y
     */
    private fun startDragging(y: Float) {
        val yDiff = Math.abs(y - mInitialDownY)
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop
            mIsBeingDragged = true
        }
    }

    /**
     * 释放了一个多点触控的手指时计算当前正在活动的手指id
     *
     * @param ev
     */
    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    /**
     * 子View调用这个方法并传入true   那么本次touch事件之后的所有事件都不会再调用onInterceptTouchEvent
     *
     * @param b
     */
    override fun requestDisallowInterceptTouchEvent(b: Boolean) {
        val b1 = Build.VERSION.SDK_INT < 21 && mTargetView is AbsListView
        val b2 = !ViewCompat.isNestedScrollingEnabled(mTargetView)
        if (!b1 && !b2) {
            super.requestDisallowInterceptTouchEvent(b)
        }
    }

    // NestedScrollingParent
    override fun onStartNestedScroll(@NonNull child: View, @NonNull target: View, nestedScrollAxes: Int): Boolean {
        return isEnabled && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScrollAccepted(@NonNull child: View, @NonNull target: View, axes: Int) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
        startNestedScroll(axes and ViewCompat.SCROLL_AXIS_VERTICAL)
        mTotalUnconsumed = 0f
        mNestedScrollInProgress = true
    }

    override fun onNestedScroll(@NonNull target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow)
        val dy = dyUnconsumed + mParentOffsetInWindow[1]
        if (dy < 0 && !canChildScrollUp()) {
            mTotalUnconsumed += Math.abs(dy).toFloat()
            // 由子View回调过来的滑动距离
            moveSpinner(mTotalUnconsumed)
        }
    }

    override fun onNestedPreScroll(@NonNull target: View, dx: Int, dy: Int, @NonNull consumed: IntArray) {
        if (dy > 0 && mTotalUnconsumed > 0) {
            if (dy > mTotalUnconsumed) {
                consumed[1] = dy - mTotalUnconsumed.toInt()
                mTotalUnconsumed = 0f
            } else {
                mTotalUnconsumed -= dy.toFloat()
                consumed[1] = dy
            }
            moveSpinner(mTotalUnconsumed)
        }
        val parentConsumed = mParentScrollConsumed
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }
    }

    override fun getNestedScrollAxes(): Int {
        return mNestedScrollingParentHelper.nestedScrollAxes
    }

    override fun onStopNestedScroll(@NonNull target: View) {
        mNestedScrollingParentHelper.onStopNestedScroll(target)
        mNestedScrollInProgress = false
        if (mTotalUnconsumed > 0) {
            finishSpinner(mTotalUnconsumed)
            mTotalUnconsumed = 0f
        }
        stopNestedScroll()
    }

    // NestedScrollingChild
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mNestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mNestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mNestedScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int,
                                      dyConsumed: Int,
                                      dxUnconsumed: Int,
                                      dyUnconsumed: Int,
                                      offsetInWindow: IntArray?): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedScroll(
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int,
                                         dy: Int,
                                         consumed: IntArray?,
                                         offsetInWindow: IntArray?): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun onNestedPreFling(@NonNull target: View,
                                  velocityX: Float,
                                  velocityY: Float): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(@NonNull target: View,
                               velocityX: Float,
                               velocityY: Float,
                               consumed: Boolean): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedFling(velocityX: Float,
                                     velocityY: Float,
                                     consumed: Boolean): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    companion object {
        private const val INVALID_POINTER = -1
        /**
         * 这里不设置阻尼  滑动多少就移动多少
         */
        private const val DRAG_RATE = 1f
        /**
         * 动画最大持续时间
         */
        private val MAX_DURATION = 600L
        /**
         * 用于查找是否 enabled
         */
        private val LAYOUT_ATTRS = intArrayOf(android.R.attr.enabled)
    }
}
