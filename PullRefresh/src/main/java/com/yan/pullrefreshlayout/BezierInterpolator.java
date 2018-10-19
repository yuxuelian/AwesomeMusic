package com.yan.pullrefreshlayout;

/**
 * @author kaibo
 * @date 2018/10/19 14:34
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

import android.graphics.PointF;
import android.view.animation.Interpolator;

/**
 * @author 56896
 * @date 2018/10/18 23:54
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
public class BezierInterpolator implements Interpolator {

    private static final float STEP_SIZE = 1.0f / 4096;
    private final PointF point1 = new PointF();
    private final PointF point2 = new PointF();

    private int mLastI = 0;

    public BezierInterpolator(float x1, float y1, float x2, float y2) {
        point1.x = x1;
        point1.y = y1;
        point2.x = x2;
        point2.y = y2;
    }

    @Override
    public float getInterpolation(float input) {
        float t = input;
        //如果重新开始要重置缓存的i。
        if ((int) (input * 10) == 0) {
            mLastI = 0;
        }
        // 近似求解t
        double tempX;
        for (int i = mLastI; i < 4096; i++) {
            t = i * STEP_SIZE;
            tempX = cubicEquation(t, point1.x, point2.x);
            if (tempX >= input) {
                mLastI = i;
                break;
            }
        }
        double value = cubicEquation(t, point1.y, point2.y);
        return (float) value;
    }

    public static double cubicEquation(double t, double p1, double p2) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double ttt = tt * t;
        return 3 * uu * t * p1 + 3 * u * tt * p2 + ttt;
    }
}

