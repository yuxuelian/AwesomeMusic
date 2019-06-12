package com.kaibo.swipe_back

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.OverScroller
import androidx.customview.widget.ViewDragHelper


/**
 * @author: kaibo
 * @date: 2019/6/5 10:10
 * @GitHub: https://github.com/yuxuelian
 * @qq: 568966289
 * @description:
 */

internal class SwipeBackLayout
@JvmOverloads
constructor(private val mActivity: Activity, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(mActivity, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "SwipeBackLayout"
        // 触发返回的临界比率
        private const val BACK_APPROACH_RATE = .3f
        // 触发返回的最小速率
        private const val BACK_MIN_VEL = 5000f
    }

    private val mDragView: View

    private val mShadeView: View

    private val mImageView: ImageView

    private val mViewDragHelper = ViewDragHelper.create(this, 1.0f, MyCallback())

    private var mIsGetPreBitmap: Boolean = false

    init {
        // 修改一下 mScroller 主要是为了更改插值器
        val mScrollerField = ViewDragHelper::class.java.getDeclaredField("mScroller")
        mScrollerField.isAccessible = true
        val overScroller = OverScroller(mActivity, BezierInterpolator())
        mScrollerField.set(mViewDragHelper, overScroller)

        // 只能左侧边缘触发
//        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT)

        // 添加底层Activity截图显示
        mImageView = ImageView(mActivity)
        mImageView.scaleType = ImageView.ScaleType.FIT_XY
        addView(mImageView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        // 延迟一秒后获取截图
        postDelayed({
            // 获取底层Activity的截图(初始化的时候就获取一下截图,并设置到ImageView 防止拖拽时再获取的卡顿现象)
            getPreContentBitmap()
        }, 300)

        // 将这个阴影布局添加到最底层
        mShadeView = View(mActivity)
        mShadeView.setBackgroundResource(R.drawable.shadow)
        // 添加阴影布局到最底层
        addView(mShadeView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        // 将当前View绑定到Activity的根视图中去
        val decorView: ViewGroup = mActivity.window.decorView as ViewGroup
        val contentView = decorView.getChildAt(0)
        // 获取 window 的背景颜色
        val typedArray = mActivity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        val bgColor = typedArray.getResourceId(0, 0)
        typedArray.recycle()
        // 将 window 的颜色设置到 contentView 上去
        contentView.setBackgroundResource(bgColor)
        // 将 window 的背景颜色设置为白色
        mActivity.window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        decorView.removeView(contentView)
        addView(contentView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        // 将当前View添加到根View中去
        decorView.addView(
                this,
                0,
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        mDragView = contentView
    }

    /**
     * 获取前一个Activity的截图,并显示
     * 只会被执行一次
     */
    private fun getPreContentBitmap() {
        if (!mIsGetPreBitmap) {
            mIsGetPreBitmap = true
            SwipeBackManager.getPenultimateActivity(mActivity)?.let {
                val preContentView = (it.window.decorView as ViewGroup).getChildAt(0)
                val preContentBitmap = Bitmap.createBitmap(
                        preContentView.measuredWidth,
                        preContentView.measuredHeight,
                        Bitmap.Config.ARGB_8888
                )
                preContentView.draw(Canvas(preContentBitmap))
                // 显示截取到的图片
                mImageView.setImageBitmap(preContentBitmap)
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mViewDragHelper.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mViewDragHelper.processTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate()
        }
    }

    inner class MyCallback : ViewDragHelper.Callback() {
        private var mViewWidth: Int = 0

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child === mDragView
        }

        override fun onViewDragStateChanged(state: Int) {
            /*
            当状态改变的时候回调，返回相应的状态（这里有三种状态）
            STATE_IDLE 闲置状态
            STATE_DRAGGING 正在拖动
            STATE_SETTLING 放置到某个位置
             */

            if (state == ViewDragHelper.STATE_IDLE) {
                // 空闲状态(动画执行结束)
                if (mDragView.left == mDragView.measuredWidth) {
                    // 关闭当前Activity
                    mActivity.finish()
                    mActivity.overridePendingTransition(0, 0)
                } else {
                    // 拖拽结束后,恢复位置
                    mShadeView.translationX = 0f
                    mImageView.translationX = 0f
                }
            }
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            /*
            当你拖动的View位置发生改变的时候回调
            参数1：你当前拖动的这个View
            参数2：距离左边的距离
            参数3：距离右边的距离
            参数4：x轴的变化量
            参数5：y轴的变化量
             */
            val fl = -mViewWidth.toFloat() + left.toFloat()
            // 移动阴影位置
            mShadeView.translationX = fl
            mImageView.translationX = fl * BACK_APPROACH_RATE
        }

        override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            /*
            捕获View的时候调用的方法
            参数1：捕获的View（也就是你拖动的这个View）
            参数2：正在活动的手指的id
             */
            mViewWidth = capturedChild.measuredWidth
            if (mShadeView.translationX == 0f) {
                mShadeView.translationX = -mViewWidth.toFloat()
            }
            if (mImageView.translationX == 0f) {
                // 获取一次
                getPreContentBitmap()
                mImageView.translationX = -mViewWidth * BACK_APPROACH_RATE
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            /*
            手指离开屏幕调用这个方法
            当View停止拖拽的时候调用的方法，一般在这个方法中重置一些参数，比如回弹什么的。。。
            参数1：你拖拽的这个View
            参数2：x轴的速率
            参数3：y轴的速率
             */
            if (releasedChild.left > mViewWidth * BACK_APPROACH_RATE || xvel > BACK_MIN_VEL) {
                // 已经滑动到了屏幕的.3的位置   或者松手时速率过大  则滑动到最右侧去,然后触发返回逻辑
                mViewDragHelper.settleCapturedViewAt(mViewWidth, 0)
            } else {
                // 恢复到初始位置,什么也不做
                mViewDragHelper.settleCapturedViewAt(0, 0)
            }
            invalidate()
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            if (left < 0) {
                return 0
            }
            // 水平拖拽
            return left
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return mDragView.top
        }

        /**
         * 这个方法主要是为了按钮能够同时响应拖拽和点击
         */
        override fun getViewHorizontalDragRange(child: View): Int {
            return mViewDragHelper.touchSlop
        }

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            mViewDragHelper.captureChildView(mDragView, pointerId)
        }
    }

}