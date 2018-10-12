package com.kaibo.music.weight;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.kaibo.music.R;

/**
 * @author kaibo
 * @date 2018/10/12 14:31
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
public class ReboundEffectsView extends FrameLayout {

    private View mPrinceView;
    private int mInitTop, mInitBottom, mInitLeft, mInitRight;
    private boolean isEndwiseSlide;
    private float mVariableY;
    private float mVariableX;
    private int orientation;

    public ReboundEffectsView(Context context) {
        this(context, null);
    }

    public ReboundEffectsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReboundEffectsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setClickable(true);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ReboundEffectsView);
        orientation = ta.getInt(R.styleable.ReboundEffectsView_orientation, 1);
        ta.recycle();
    }

    private float x1;
    private float y1;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("ReboundEffectsView", "onInterceptTouchEvent");
//        RecyclerView.canScrollVertically(1)的值表示是否能向上滚动，false表示已经滚动到底部
//        RecyclerView.canScrollVertically(-1)的值表示是否能向下滚动，false表示已经滚动到顶部
        if (null != mPrinceView) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = ev.getX();
                    y1 = ev.getY();
                    onActionDown(ev);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (ev.getY() - y1 > 0) {
                        //向下滑动
                        if (mPrinceView.getTop() - mInitTop > 0) {
                            return false;
                        } else {
                            return !mPrinceView.canScrollVertically(-1);
                        }
                    } else if (ev.getY() - y1 < 0) {
                        if (mPrinceView.getBottom() - mInitBottom < 0) {
                            return false;
                        } else {
                            return !mPrinceView.canScrollVertically(-1);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * Touch事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Log.d("ReboundEffectsView", "onTouchEvent");
        if (null != mPrinceView) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    onActionMove(e);
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 执行回弹动画
                    onActionUp(e);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    /**
     * 手指按下事件
     */
    private void onActionDown(MotionEvent e) {
        mVariableY = e.getY();
        mVariableX = e.getX();
        // 保存mPrinceView的初始上下高度位置
        mInitTop = mPrinceView.getTop();
        mInitBottom = mPrinceView.getBottom();
        mInitLeft = mPrinceView.getLeft();
        mInitRight = mPrinceView.getRight();
    }

    /**
     * 手指滑动事件
     */
    private boolean onActionMove(MotionEvent e) {
        float nowY = e.getY();
        float diffY = (nowY - mVariableY) / 2;
        // 上下滑动
        if (orientation == 1 && Math.abs(diffY) > 0) {
            // 移动太子View的上下位置
            mPrinceView.layout(mPrinceView.getLeft(), mPrinceView.getTop() + (int) diffY, mPrinceView.getRight(), mPrinceView.getBottom() + (int) diffY);
            mVariableY = nowY;
            isEndwiseSlide = true;
            // 消费touch事件
            return true;
        }

        float nowX = e.getX();
        //除数越大可以滑动的距离越短
        float diffX = (nowX - mVariableX) / 5;
        // 左右滑动
        if (orientation == 2 && Math.abs(diffX) > 0) {
            // 移动太子View的左右位置
            mPrinceView.layout(mPrinceView.getLeft() + (int) diffX, mPrinceView.getTop(), mPrinceView.getRight() + (int) diffX, mPrinceView.getBottom());
            mVariableX = nowX;
            isEndwiseSlide = true;
            // 消费touch事件
            return true;
        }
        return super.onTouchEvent(e);
    }

    /**
     * 手指释放事件
     */
    private void onActionUp(MotionEvent e) {
        // 是否为纵向滑动事件
        if (isEndwiseSlide) {
            // 是纵向滑动事件，需要给太子View重置位置
            if (orientation == 1) {
                resetPrinceViewV();
            } else if (orientation == 2) {
                resetPrinceViewH();
            }
            isEndwiseSlide = false;
        }
    }

    /**
     * 回弹，重置太子View初始的位置
     */
    private void resetPrinceViewV() {
        TranslateAnimation ta = new TranslateAnimation(0, 0, mPrinceView.getTop() - mInitTop, 0);
        ta.setDuration(600);
        ta.setInterpolator(new BezierInterpolator(.6f, .4f, .4f, .6f));
        mPrinceView.startAnimation(ta);
        mPrinceView.layout(mPrinceView.getLeft(), mInitTop, mPrinceView.getRight(), mInitBottom);
    }

    private void resetPrinceViewH() {
        TranslateAnimation ta = new TranslateAnimation(mPrinceView.getLeft() - mInitLeft, 0, 0, 0);
        ta.setDuration(600);
        ta.setInterpolator(new BezierInterpolator(.6f, .4f, .4f, .6f));
        mPrinceView.startAnimation(ta);
        mPrinceView.layout(mInitLeft, mPrinceView.getTop(), mInitRight, mPrinceView.getBottom());
    }

    /**
     * XML布局完成加载
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            mPrinceView = getChildAt(0);
        }
    }
}
