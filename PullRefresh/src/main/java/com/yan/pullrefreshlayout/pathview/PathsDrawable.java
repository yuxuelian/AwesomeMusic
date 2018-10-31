package com.yan.pullrefreshlayout.pathview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * @author 56896
 * @date 2018/10/18 23:54
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
public class PathsDrawable extends Drawable {

    private Paint mPaint;
    private List<Path> mPaths;
    private List<Integer> mColors;
    private int mWidth = 1, mHeight = 1;
    private int mStartX = 0, mStartY = 0;
    private int mOrginWidth;
    private int mOrginHeight;
    private static final Region REGION = new Region();
    private static final Region MAX_CLIP = new Region(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private List<Path> mOriginPaths;
    private List<String> mOriginSvg;
    private Bitmap mCachedBitmap;
    private boolean mCacheDirty;

    public PathsDrawable() {
        mPaint = new Paint();
        mPaint.setColor(0xFF303030);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    private void onMeasure() {
        Integer top = null, left = null, right = null, bottom = null;
        if (mPaths != null) {
            for (Path path : mPaths) {
                REGION.setPath(path, MAX_CLIP);
                Rect bounds = REGION.getBounds();
                top = Math.min(top == null ? bounds.top : top, bounds.top);
                left = Math.min(left == null ? bounds.left : left, bounds.left);
                right = Math.max(right == null ? bounds.right : right, bounds.right);
                bottom = Math.max(bottom == null ? bounds.bottom : bottom, bounds.bottom);
            }
        }
        mStartX = left == null ? 0 : left;
        mStartY = top == null ? 0 : top;
        mWidth = right == null ? 0 : right - mStartX;
        mHeight = bottom == null ? 0 : bottom - mStartY;
        if (mOrginWidth == 0) {
            mOrginWidth = mWidth;
        }
        if (mOrginHeight == 0) {
            mOrginHeight = mHeight;
        }
        Rect bounds = getBounds();
        super.setBounds(bounds.left, bounds.top, bounds.left + mWidth, bounds.top + mHeight);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        final int width = right - left;
        final int height = bottom - top;
        if (mOriginPaths != null && mOriginPaths.size() > 0 &&
                (width != mWidth || height != mHeight)) {
            float ratioWidth = 1f * width / mOrginWidth;
            float ratioHeight = 1f * height / mOrginHeight;
            mPaths = PathParser.transformScale(ratioWidth, ratioHeight, mOriginPaths, mOriginSvg);
            onMeasure();
        } else {
            super.setBounds(left, top, right, bottom);
        }
    }

    @Override
    public void setBounds(@NonNull Rect bounds) {
        setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    public void parserPaths(String... paths) {
        mOrginWidth = mOrginHeight = 0;
        mOriginSvg = new ArrayList<>();
        mPaths = mOriginPaths = new ArrayList<>();
        for (String path : paths) {
            mOriginSvg.add(path);
            mOriginPaths.add(PathParser.createPathFromPathData(path));
        }
        onMeasure();
    }

    public void parserColors(int... colors) {
        mColors = new ArrayList<>();
        for (int color : colors) {
            mColors.add(color);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        int width = bounds.width();
        int height = bounds.height();
        if (mPaint.getAlpha() == 0xFF) {
            canvas.save();
            canvas.translate(bounds.left - mStartX, bounds.top - mStartY);
            if (mPaths != null) {
                for (int i = 0; i < mPaths.size(); i++) {
                    if (mColors != null && i < mColors.size()) {
                        mPaint.setColor(mColors.get(i));
                    }
                    canvas.drawPath(mPaths.get(i), mPaint);
                }
                mPaint.setAlpha(0xFF);
            }
            canvas.restore();
        } else {
            createCachedBitmapIfNeeded(width, height);
            if (!canReuseCache()) {
                updateCachedBitmap(width, height);
                updateCacheStates();
            }
            canvas.drawBitmap(mCachedBitmap, bounds.left, bounds.top, mPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public int width() {
        return getBounds().width();
    }

    public int height() {
        return getBounds().height();
    }

    public void setGeometricWidth(int width) {
        Rect bounds = getBounds();
        float rate = 1f * width / bounds.width();
        setBounds(
                (int) (bounds.left * rate),
                (int) (bounds.top * rate),
                (int) (bounds.right * rate),
                (int) (bounds.bottom * rate)
        );

    }

    public void setGeometricHeight(int height) {
        Rect bounds = getBounds();
        float rate = 1f * height / bounds.height();
        setBounds(
                (int) (bounds.left * rate),
                (int) (bounds.top * rate),
                (int) (bounds.right * rate),
                (int) (bounds.bottom * rate)
        );
    }

    public Paint getPaint() {
        return mPaint;
    }

    private void updateCachedBitmap(int width, int height) {
        mCachedBitmap.eraseColor(Color.TRANSPARENT);
        Canvas tmpCanvas = new Canvas(mCachedBitmap);
        drawCachedBitmap(tmpCanvas);
    }

    private void drawCachedBitmap(Canvas canvas) {
        canvas.translate(-mStartX, -mStartY);
        if (mPaths != null) {
            for (int i = 0; i < mPaths.size(); i++) {
                if (mColors != null && i < mColors.size()) {
                    mPaint.setColor(mColors.get(i));
                }
                canvas.drawPath(mPaths.get(i), mPaint);
            }
        }
    }

    private void createCachedBitmapIfNeeded(int width, int height) {
        if (mCachedBitmap == null || !canReuseBitmap(width, height)) {
            mCachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCacheDirty = true;
        }
    }

    private boolean canReuseBitmap(int width, int height) {
        if (width == mCachedBitmap.getWidth()
                && height == mCachedBitmap.getHeight()) {
            return true;
        }
        return false;
    }

    private boolean canReuseCache() {
        return !mCacheDirty;
    }

    private void updateCacheStates() {
        mCacheDirty = false;
    }
}
