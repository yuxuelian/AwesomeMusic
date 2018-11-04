package com.kaibo.music.weight;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.yan.pullrefreshlayout.BezierInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ListViewCompat;

import static org.jetbrains.anko.DimensionsKt.px2dip;

public class BottomSheetLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private static final String TAG = "BottomSheetLayout";

    private static final int INVALID_POINTER = -1;

    /**
     * 这里不设置阻尼  滑动多少就移动多少
     */
    private static final float DRAG_RATE = 1f;

    /**
     * 动画最大持续时间
     */
    private static final long MAX_DURATION = 600L;

    private int mTouchSlop;
    private float mTotalUnconsumed;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    private boolean mNestedScrollInProgress;
    private float mInitialMotionY;
    private float mInitialDownY;

    /**
     * 标记是否正在拖拽
     */
    private boolean mIsBeingDragged;

    /**
     * 多点触控  标记触摸手指id
     */
    private int mActivePointerId = INVALID_POINTER;

    /**
     * 用于查找是否 enabled
     */
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };

    /**
     * 回弹View
     */
    private View mTargetView;

    /**
     * 执行动画
     */
    private Animator mSpringBackAnimator;

    /**
     * targetView最大动画移动距离
     */
    private float maxSpringBackDistance;

    private OnCollapseListener onCollapseListener;

    /**
     * 记录开始滑动的时间戳
     */
    private long startTouchTime;

    public void setOnCollapseListener(OnCollapseListener onCollapseListener) {
        this.onCollapseListener = onCollapseListener;
    }

    public interface OnCollapseListener {
        /**
         * 完全关闭了
         */
        void onCollapse();
    }

    public BottomSheetLayout(@NonNull Context context) {
        this(context, null);
    }

    public BottomSheetLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomSheetLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        // 使onDraw得到执行
        setWillNotDraw(false);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        // 查找xml中设置的是否enabled
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(typedArray.getBoolean(0, true));
        typedArray.recycle();
    }

    private void ensureTarget() {
        if (mTargetView == null && getChildCount() > 0) {
            mTargetView = getChildAt(0);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            mTargetView = getChildAt(0);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureTarget();
        if (mTargetView == null) {
            return;
        }
        int measureChildWidth = MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
        int measureChildHeight = MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        // 测量子View
        mTargetView.measure(measureChildWidth, measureChildHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // 当View大小改变的时候  修改这个最大回弹距离
        maxSpringBackDistance = h;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            targetViewLayout();
        }
    }

    private void targetViewLayout() {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTargetView == null) {
            return;
        }
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        mTargetView.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
    }

    /**
     * 判断targetView是否还能够继续向下滑动
     *
     * @return
     */
    public boolean canChildScrollUp() {
        if (mTargetView instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) mTargetView, -1);
        }
        return mTargetView.canScrollVertically(-1);
    }

    /**
     * 移动子View
     *
     * @param overScrollTop
     */
    private void moveSpinner(float overScrollTop) {
        // 向下拉
        if (overScrollTop > 0) {
            // 记录一下Top值
            mLayoutTop = (int) overScrollTop;
            mTargetView.layout(0, (int) overScrollTop, mTargetView.getMeasuredWidth(), (int) (mTargetView.getMeasuredHeight() + overScrollTop));
        } else {
            // 向下拉

        }
    }

    /**
     * 向上或者向下回弹动画
     *
     * @param isExpand true 动画将向上回弹   false  动画往下回弹
     * @return
     */
    private Animator createSpringBackAnimator(final boolean isExpand) {
        // 结束位置
        final int endPosition = isExpand ? 0 : mTargetView.getMeasuredHeight();
        // 计算duration
        long duration = (long) (MAX_DURATION * (Math.abs(endPosition - mLayoutTop) / maxSpringBackDistance));
        Animator animator = ObjectAnimator.ofInt(this, "layoutTop", mLayoutTop, endPosition);
        animator.setInterpolator(new BezierInterpolator());
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 回调关闭事件
                if (!isExpand && onCollapseListener != null) {
                    onCollapseListener.onCollapse();
                }
            }
        });
        return animator;
    }

    /**
     * 当前TargetView的Top值
     */
    private int mLayoutTop;

    /**
     * 提供给属性动画使用
     *
     * @param value
     */
    public void setLayoutTop(int value) {
        // 获取到值
        mLayoutTop = value;
        // 改变布局位置
        mTargetView.layout(0, mLayoutTop, mTargetView.getMeasuredWidth(), mTargetView.getMeasuredHeight() + mLayoutTop);
    }

    /**
     * 松手会调用这个方法
     *
     * @param overScrollTop
     */
    private void finishSpinner(float overScrollTop) {
        // 计算滑动速率
        float velocity = px2dip(getContext(), (int) overScrollTop) / (System.currentTimeMillis() - startTouchTime);
        if (velocity > 0.5f) {
            // 速率过大的情况  不需要判断当前的滑动距离是否超过了中线   直接关闭
            mSpringBackAnimator = createSpringBackAnimator(false);
        } else {
            // 速率过小的情况  去判断滑动距离是否超过了中线
            mSpringBackAnimator = createSpringBackAnimator(overScrollTop < mTargetView.getMeasuredHeight() / 2.0);
        }
        // 启动回弹动画
        mSpringBackAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // view从屏幕移出的时候 取消正在执行的动画
        if (mSpringBackAnimator != null && mSpringBackAnimator.isRunning()) {
            mSpringBackAnimator.cancel();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        ensureTarget();
        final int action = event.getActionMasked();
        int pointerIndex;

        if (!isEnabled() || canChildScrollUp() || mNestedScrollInProgress) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 记录开始滑动的时间戳
                startTouchTime = System.currentTimeMillis();
                mActivePointerId = event.getPointerId(0);
                mIsBeingDragged = false;
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                // 记录触摸的起点Y
                mInitialDownY = event.getY(pointerIndex);

                int layoutTop = this.mLayoutTop;
                // 当有手指按下时结束正在执行的动画
                if (mSpringBackAnimator != null && mSpringBackAnimator.isRunning()) {
                    mSpringBackAnimator.cancel();
                }
                this.setLayoutTop(layoutTop);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = event.getY(pointerIndex);
                startDragging(y);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
            default:
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        int pointerIndex;

        if (!isEnabled() || canChildScrollUp() || mNestedScrollInProgress) {
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(0);
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = event.getY(pointerIndex);
                startDragging(y);
                if (mIsBeingDragged) {
                    final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    // 直接拖拽this.View的时候
                    moveSpinner(overScrollTop);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                pointerIndex = event.getActionIndex();
                if (pointerIndex < 0) {
                    return false;
                }
                mActivePointerId = event.getPointerId(pointerIndex);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_UP: {
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                if (mIsBeingDragged) {
                    final float y = event.getY(pointerIndex);
                    final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    finishSpinner(overScrollTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
            default:
                break;
        }
        return true;
    }

    /**
     * 计算是否是在开始拖拽了(主要是为了区分点击事件)
     *
     * @param y
     */
    private void startDragging(float y) {
        final float yDiff = Math.abs(y - mInitialDownY);
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
        }
    }

    /**
     * 释放了一个多点触控的手指时计算当前正在活动的手指id
     *
     * @param ev
     */
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    /**
     * 子View调用这个方法并传入true   那么本次touch事件之后的所有事件都不会再调用onInterceptTouchEvent
     *
     * @param b
     */
    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        boolean b1 = Build.VERSION.SDK_INT < 21 && mTargetView instanceof AbsListView;
        boolean b2 = mTargetView != null && !ViewCompat.isNestedScrollingEnabled(mTargetView);
        if (!b1 && !b2) {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    // NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mTotalUnconsumed = 0;
        mNestedScrollInProgress = true;
    }

    @Override
    public void onNestedScroll(@NonNull final View target, final int dxConsumed, final int dyConsumed, final int dxUnconsumed, final int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow);
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !canChildScrollUp()) {
            mTotalUnconsumed += Math.abs(dy);
            // 由子View回调过来的滑动距离
            moveSpinner(mTotalUnconsumed);
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        if (dy > 0 && mTotalUnconsumed > 0) {
            if (dy > mTotalUnconsumed) {
                consumed[1] = dy - (int) mTotalUnconsumed;
                mTotalUnconsumed = 0;
            } else {
                mTotalUnconsumed -= dy;
                consumed[1] = dy;
            }
            moveSpinner(mTotalUnconsumed);
        }
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        if (mTotalUnconsumed > 0) {
            finishSpinner(mTotalUnconsumed);
            mTotalUnconsumed = 0;
        }
        stopNestedScroll();
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}
