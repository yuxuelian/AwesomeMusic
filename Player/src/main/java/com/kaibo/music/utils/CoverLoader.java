package com.kaibo.music.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kaibo.core.glide.GlideApp;
import com.kaibo.music.bean.SongBean;
import com.kaibo.music.player.R;

import androidx.annotation.NonNull;


/**
 * 专辑封面图片加载器
 * Glide加载异常处理
 */
public class CoverLoader {
    private static final String TAG = "CoverLoader";

    public interface BitmapCallBack {
        void showBitmap(Bitmap bitmap);
    }

    public static String getCoverUri(Context context, String albumId) {
        if ("-1".equals(albumId)) {
            return null;
        }
        String uri = null;
        try {
            Cursor cursor = context.getContentResolver().query(Uri.parse("content://media/external/audio/albums/" + albumId), new String[]{"album_art"}, null, null, null);
            if (cursor != null) {
                cursor.moveToNext();
                uri = cursor.getString(0);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    public static int getCoverUriByRandom() {
        int[] bitmaps = {R.drawable.music_one, R.drawable.music_two, R.drawable.music_three,
                R.drawable.music_four, R.drawable.music_five, R.drawable.music_six,
                R.drawable.music_seven, R.drawable.music_eight, R.drawable.music_nine,
                R.drawable.music_ten, R.drawable.music_eleven, R.drawable.music_twelve};
        int random = (int) (Math.random() * bitmaps.length);
        return bitmaps[random];
    }

    /**
     * 显示小图
     *
     * @param mContext
     * @param music
     * @param callBack
     */
    public static void loadImageViewByMusic(Context mContext, SongBean music, BitmapCallBack callBack) {
        if (music == null) {
            return;
        }
        loadBitmap(mContext, music.getImage(), callBack);

    }

    /**
     * 显示播放页大图
     *
     * @param mContext
     */
    public static void loadBigImageView(Context mContext, SongBean music, BitmapCallBack callBack) {
        GlideApp.with(mContext)
                .asBitmap()
                .load(music.getImage())
                .error(getCoverUriByRandom())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        if (callBack != null && resource != null) {
                            callBack.showBitmap(resource);
                        }
                    }
                });
    }

    public static void loadBigImageView(Context mContext, SongBean songBean, ImageView imageView) {
        GlideApp.with(mContext)
                .asBitmap()
                .load(songBean.getImage())
                .error(getCoverUriByRandom())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * 显示图片
     *
     * @param mContext
     * @param url
     * @param imageView
     */
    public static void loadImageView(Context mContext, String url, ImageView imageView) {
        if (mContext == null) return;
        GlideApp.with(mContext)
                .load(url)
                .error(R.drawable.ic_account_circle)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    public static void loadImageView(Context mContext, String url, int defaultUrl, ImageView imageView) {
        if (mContext == null) return;
        GlideApp.with(mContext)
                .load(url)
                .error(defaultUrl)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * 根据id显示
     *
     * @param mContext
     * @param albumId
     * @param callBack
     */
    public static void loadBitmapById(Context mContext, String albumId, BitmapCallBack callBack) {
        loadBitmap(mContext, getCoverUri(mContext, albumId), callBack);
    }

    /**
     * 返回bitmap
     *
     * @param mContext
     * @param url
     * @param callBack
     */
    public static void loadBitmap(Context mContext, String url, BitmapCallBack callBack) {
        GlideApp.with(mContext)
                .asBitmap()
                .load(url == null ? getCoverUriByRandom() : url)
                .error(getCoverUriByRandom())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        if (callBack != null) {
                            callBack.showBitmap(resource);
                        }
                    }
                });
    }

    public static Drawable createBlurredImageFromBitmap(Bitmap bitmap) {
        return ImageUtils.createBlurredImageFromBitmap(bitmap, 4);
    }
}
