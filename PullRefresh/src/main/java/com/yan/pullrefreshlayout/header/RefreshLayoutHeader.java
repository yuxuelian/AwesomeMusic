package com.yan.pullrefreshlayout.header;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.pullrefreshlayout.R;
import com.yan.pullrefreshlayout.pathview.ProgressDrawable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.yan.pullrefreshlayout.DimensionsKt.dip;

/**
 * @author kaibo
 * @date 2018/7/6 15:15
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
public class RefreshLayoutHeader extends NestedRelativeLayout implements PullRefreshLayout.OnPullListener {

    public static String REFRESH_HEADER_PULLDOWN = "下拉可以刷新";
    public static String REFRESH_HEADER_REFRESHING = "正在刷新...";
    public static String REFRESH_HEADER_RELEASE = "释放立即刷新";
    public static String REFRESH_HEADER_FINISH = "刷新完成";
    public static String REFRESH_HEADER_FAILED = "刷新失败";

    private String lastUpdateTime = "LAST_UPDATE_TIME";

    private Date mLastTime;
    protected TextView mHeaderText;
    protected TextView mLastUpdateText;
    protected ImageView mArrowView;
    protected ImageView mProgressView;

    protected Drawable mArrowDrawable;

    protected ProgressDrawable mProgressDrawable;
    private DateFormat mFormat = new SimpleDateFormat("上次更新 M-d HH:mm", Locale.CHINESE);
    private SharedPreferences mShared;

    public RefreshLayoutHeader(Context context) {
        super(context);
        this.initView(context);
    }

    protected void initView(Context context) {
        setMinimumHeight(dip(getContext(), 80));

        LinearLayout layout = new LinearLayout(context);
        layout.setId(android.R.id.widget_frame);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setOrientation(LinearLayout.VERTICAL);
        // 文字提示信息
        mHeaderText = new TextView(context);
        mHeaderText.setText(REFRESH_HEADER_PULLDOWN);
        mHeaderText.setTextColor(ContextCompat.getColor(getContext(), R.color.color_303030));
        mHeaderText.setTextSize(16);

        // 上次刷新的刷新时间
        mLastUpdateText = new TextView(context);
        mLastUpdateText.setTextColor(ContextCompat.getColor(getContext(), R.color.color_303030));
        mLastUpdateText.setTextSize(12);
        LinearLayout.LayoutParams lpHeaderText = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layout.addView(mHeaderText, lpHeaderText);
        LinearLayout.LayoutParams lpUpdateText = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layout.addView(mLastUpdateText, lpUpdateText);

        LayoutParams lpHeaderLayout = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lpHeaderLayout.addRule(CENTER_IN_PARENT);
        addView(layout, lpHeaderLayout);

        mProgressView = new ImageView(context);
        mProgressView.animate().setInterpolator(new LinearInterpolator());
        LayoutParams lpProgress = new LayoutParams(dip(getContext(), 20), dip(getContext(), 20));
        lpProgress.rightMargin = dip(getContext(), 20);
        lpProgress.addRule(CENTER_VERTICAL);
        lpProgress.addRule(LEFT_OF, android.R.id.widget_frame);
        addView(mProgressView, lpProgress);

        mArrowView = new ImageView(context);
        addView(mArrowView, lpProgress);

        if (isInEditMode()) {
            mArrowView.setVisibility(GONE);
            mHeaderText.setText(REFRESH_HEADER_REFRESHING);
        } else {
            mProgressView.setVisibility(GONE);
        }

        mArrowDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_downward_black_24dp);
        mArrowView.setImageDrawable(mArrowDrawable);
        mProgressDrawable = new ProgressDrawable();
        mProgressDrawable.setColor(ContextCompat.getColor(getContext(), R.color.color_303030));
        mProgressView.setImageDrawable(mProgressDrawable);

        try {//try 不能删除-否则会出现兼容性问题
            if (context instanceof FragmentActivity) {
                FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
                if (manager != null) {
                    List<Fragment> fragments = manager.getFragments();
                    if (fragments.size() > 0) {
                        setLastUpdateTime(new Date());
                        return;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        lastUpdateTime += context.getClass().getName();
        mShared = context.getSharedPreferences("ClassicsHeader", Context.MODE_PRIVATE);
        setLastUpdateTime(new Date(mShared.getLong(lastUpdateTime, System.currentTimeMillis())));
    }

    public RefreshLayoutHeader setProgressBitmap(Bitmap bitmap) {
        mProgressDrawable = null;
        mProgressView.setImageBitmap(bitmap);
        return this;
    }

    public RefreshLayoutHeader setProgressDrawable(Drawable drawable) {
        mProgressDrawable = null;
        mProgressView.setImageDrawable(drawable);
        return this;
    }

    public RefreshLayoutHeader setProgressResource(@DrawableRes int resId) {
        mProgressDrawable = null;
        mProgressView.setImageResource(resId);
        return this;
    }

    public RefreshLayoutHeader setArrowBitmap(Bitmap bitmap) {
        mArrowDrawable = null;
        mArrowView.setImageBitmap(bitmap);
        return this;
    }

    public RefreshLayoutHeader setArrowDrawable(Drawable drawable) {
        mArrowDrawable = null;
        mArrowView.setImageDrawable(drawable);
        return this;
    }

    public RefreshLayoutHeader setArrowResource(@DrawableRes int resId) {
        mArrowDrawable = null;
        mArrowView.setImageResource(resId);
        return this;
    }

    public RefreshLayoutHeader setLastUpdateTime(Date time) {
        mLastTime = time;
        mLastUpdateText.setText(mFormat.format(time));
        if (mShared != null && !isInEditMode()) {
            mShared.edit().putLong(lastUpdateTime, time.getTime()).apply();
        }
        return this;
    }

    public RefreshLayoutHeader setTimeFormat(DateFormat format) {
        mFormat = format;
        mLastUpdateText.setText(mFormat.format(mLastTime));
        return this;
    }

    @Override
    public void onPullChange(float percent) {

    }

    @Override
    public void onPullHoldTrigger() {
        mHeaderText.setText(REFRESH_HEADER_RELEASE);
        mArrowView.animate().rotation(180).start();
    }

    @Override
    public void onPullHoldUnTrigger() {
        mHeaderText.setText(REFRESH_HEADER_PULLDOWN);
        mArrowView.setVisibility(VISIBLE);
        mProgressView.setVisibility(GONE);
        mArrowView.animate().rotation(0).start();
    }

    @Override
    public void onPullHolding() {
        mHeaderText.setText(REFRESH_HEADER_REFRESHING);
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
        mHeaderText.setText(REFRESH_HEADER_FAILED);
    }

    @Override
    public void onPullFinish(boolean flag) {
        if (mProgressDrawable != null) {
            mProgressDrawable.stop();
        } else {
            mProgressView.animate().rotation(0).setDuration(300).start();
        }
        if (!mHeaderText.getText().toString().equals(REFRESH_HEADER_FAILED)) {
            mHeaderText.setText(REFRESH_HEADER_FINISH);
        }
        mProgressView.setVisibility(GONE);
        setLastUpdateTime(new Date());
    }

    @Override
    public void onPullReset() {
        onPullHoldUnTrigger();
    }
}
