package com.yan.pullrefreshlayout.footer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.pullrefreshlayout.R;
import com.yan.pullrefreshlayout.pathview.ProgressDrawable;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.yan.pullrefreshlayout.DimensionsKt.dip;

/**
 * @author kaibo
 * @date 2018/7/6 15:15
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
public class RefreshLayoutFooter extends NestedLinearLayout implements PullRefreshLayout.OnPullListener {

    public static String LOAD_FOOTER_PUSH_DOWN = "上拉加载更多";
    public static String LOAD_FOOTER_LOADING = "正在加载...";
    public static String LOAD_FOOTER_RELEASE = "释放立即加载";
    public static String LOAD_FOOTER_FINISH = "加载完成";
    public static String LOAD_FOOTER_FAILED = "加载失败";
    public static String LOAD_FOOTER_NO_MORE = "没有更多数据";

    private TextView mHeaderText;
    private ImageView mArrowView;
    private ImageView mProgressView;
    private FrameLayout mImageContainer;
    private Drawable mArrowDrawable;
    private ProgressDrawable mProgressDrawable;

    public RefreshLayoutFooter(Context context) {
        super(context);
        this.initView(context);
    }

    protected void initView(Context context) {
        setMinimumHeight(dip(getContext(), 80));

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        mProgressView = new ImageView(context);
        mProgressView.animate().setInterpolator(new LinearInterpolator());
        mProgressDrawable = new ProgressDrawable();
        mProgressDrawable.setColor(ContextCompat.getColor(getContext(), R.color.color_303030));
        mProgressView.setImageDrawable(mProgressDrawable);

        mArrowView = new ImageView(context);
        mArrowView.setRotation(180f);

        mArrowDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_downward_black_24dp);
        mArrowView.setImageDrawable(mArrowDrawable);

        if (isInEditMode()) {
            mArrowView.setVisibility(GONE);
            mHeaderText.setText(LOAD_FOOTER_LOADING);
        } else {
            mProgressView.setVisibility(GONE);
        }

        mImageContainer = new FrameLayout(context);
        // 将两个ImageView添加到Container
        FrameLayout.LayoutParams lpArrow = new FrameLayout.LayoutParams(dip(getContext(), 20), dip(getContext(), 20));
        mImageContainer.addView(mArrowView, lpArrow);
        FrameLayout.LayoutParams lpProgress = new FrameLayout.LayoutParams(dip(getContext(), 20), dip(getContext(), 20));
        mImageContainer.addView(mProgressView, lpProgress);

        LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams.rightMargin = dip(getContext(), 20);
        // 将Container添加到Footer
        addView(mImageContainer, layoutParams);

        // 文字提示信息
        mHeaderText = new TextView(context);
        mHeaderText.setText(LOAD_FOOTER_PUSH_DOWN);
        mHeaderText.setTextColor(ContextCompat.getColor(getContext(), R.color.color_303030));
        mHeaderText.setTextSize(16);
        LayoutParams layoutParams2 = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        // 将提示文字添加到Footer
        addView(mHeaderText, layoutParams2);
    }

    public RefreshLayoutFooter setProgressBitmap(Bitmap bitmap) {
        mProgressDrawable = null;
        mProgressView.setImageBitmap(bitmap);
        return this;
    }

    public RefreshLayoutFooter setProgressDrawable(Drawable drawable) {
        mProgressDrawable = null;
        mProgressView.setImageDrawable(drawable);
        return this;
    }

    public RefreshLayoutFooter setProgressResource(@DrawableRes int resId) {
        mProgressDrawable = null;
        mProgressView.setImageResource(resId);
        return this;
    }

    public RefreshLayoutFooter setArrowBitmap(Bitmap bitmap) {
        mArrowDrawable = null;
        mArrowView.setImageBitmap(bitmap);
        return this;
    }

    public RefreshLayoutFooter setArrowDrawable(Drawable drawable) {
        mArrowDrawable = null;
        mArrowView.setImageDrawable(drawable);
        return this;
    }

    public RefreshLayoutFooter setArrowResource(@DrawableRes int resId) {
        mArrowDrawable = null;
        mArrowView.setImageResource(resId);
        return this;
    }

    private boolean isLoadMoreEnd = false;

    public void loadMoreEnd() {
        isLoadMoreEnd = true;
        mHeaderText.setText(LOAD_FOOTER_NO_MORE);
        mProgressView.setVisibility(GONE);
    }

    public void loadMoreReset() {
        isLoadMoreEnd = false;
        onPullHoldUnTrigger();
    }

    @Override
    public void onPullChange(float percent) {

    }

    @Override
    public void onPullHoldTrigger() {
        if (isLoadMoreEnd) {
            mImageContainer.setVisibility(GONE);
            return;
        }
        mImageContainer.setVisibility(VISIBLE);
        mHeaderText.setText(LOAD_FOOTER_RELEASE);
        mArrowView.animate().rotation(360).start();
    }

    @Override
    public void onPullHoldUnTrigger() {
        if (isLoadMoreEnd) {
            mImageContainer.setVisibility(GONE);
            return;
        }
        mImageContainer.setVisibility(VISIBLE);
        mHeaderText.setText(LOAD_FOOTER_PUSH_DOWN);
        mArrowView.setVisibility(VISIBLE);
        mProgressView.setVisibility(GONE);
        mArrowView.animate().rotation(180).start();
    }

    @Override
    public void onPullHolding() {
        if (isLoadMoreEnd) {
            mImageContainer.setVisibility(GONE);
            return;
        }
        mImageContainer.setVisibility(VISIBLE);
        mHeaderText.setText(LOAD_FOOTER_LOADING);
        mProgressView.setVisibility(VISIBLE);
        mArrowView.setVisibility(GONE);

        if (mProgressDrawable != null) {
            mProgressDrawable.start();
        } else {
            mProgressView.animate().rotation(36000).setDuration(100000).start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mProgressDrawable != null) {
            mProgressDrawable.stop();
        }
    }

    public void setRefreshError() {
        mHeaderText.setText(LOAD_FOOTER_FAILED);
    }

    @Override
    public void onPullFinish(boolean flag) {
        if (isLoadMoreEnd) {
            mImageContainer.setVisibility(GONE);
            return;
        }
        mImageContainer.setVisibility(VISIBLE);
        if (mProgressDrawable != null) {
            mProgressDrawable.stop();
        } else {
            mProgressView.animate().rotation(0).setDuration(300).start();
        }
        if (!mHeaderText.getText().toString().equals(LOAD_FOOTER_FAILED)) {
            mHeaderText.setText(LOAD_FOOTER_FINISH);
        }
        mProgressView.setVisibility(GONE);
    }

    @Override
    public void onPullReset() {
        onPullHoldUnTrigger();
    }
}
