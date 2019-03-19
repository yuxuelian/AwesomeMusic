package com.kaibo.core.util

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.kaibo.core.R
import java.io.File

/**
 * @author kaibo
 * @date 2018/11/15 9:52
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 * 使用原生的方式选择图片和调用相机拍照
 */

private const val TAG = "PictureSelectUtilsKt"

private const val REQUEST_SELECT_PIC = 0x01
private const val REQUEST_TAKE_PHOTO = 0x02

private var mSelectCallBack: ((res: Uri) -> Unit)? = null

/**
 * 相机拍照的临时存储文件
 */
private val takePhotoFile by lazy {
    // 系统相机目录
    val photoPath = Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DCIM + File.separator
    // 创建需要保存的图片文件
    File(photoPath, "take-photo-file.jpg")
}

/**
 * 获取选择的图片的路径
 */
private fun Context.handleSelectIntent(data: Intent): String {
    return data.data?.let { uri: Uri ->
        when {
            DocumentsContract.isDocumentUri(this, uri) -> {
                val docId = DocumentsContract.getDocumentId(uri)
                //如果是document类型的Uri,则通过document id处理
                when {
                    "com.android.providers.media.documents" == uri.authority -> {
                        val id = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                        val selection = MediaStore.Images.Media._ID + "=" + id
                        getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
                    }
                    "com.android.providers.downloads.documents" == uri.authority -> {
                        val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), docId.toLong())
                        getImagePath(contentUri, null)
                    }
                    else -> ""
                }
            }
            //如果是content类型的Uri，则使用普通方式处理
            "content".equals(uri.scheme, ignoreCase = true) -> getImagePath(uri, null)
            //如果是file类型的Uri，直接获取图片路径即可
            "file".equals(uri.scheme, ignoreCase = true) -> uri.path
            else -> ""
        }
    } ?: ""
}

/**
 * 获取选择图片的路径
 */
private fun Context.getImagePath(uri: Uri, selection: String?): String {
    var path = ""
    val cursor: Cursor? = this.contentResolver.query(uri, null, selection, null, null)
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        }
        cursor.close()
    }
    return path
}

/**
 * 启动原生的图片选择器
 */
fun Fragment.openPhotoAlbum(callBack: ((res: Uri) -> Unit)? = null) {
    mSelectCallBack = callBack
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "image/*"
    //打开系统相册
    startActivityForResult(intent, REQUEST_SELECT_PIC)
}

/**
 * 打开相机拍照
 */
fun Fragment.openCamera(callBack: ((res: Uri) -> Unit)? = null) {
    mSelectCallBack = callBack
    // 启动相机的Intent
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    // 是否可以启动相机
    val mTempContext = context
    if (mTempContext != null && takePictureIntent.resolveActivity(mTempContext.packageManager) != null) {
        // 兼容android 7.0
        val photoURI: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(mTempContext, "${mTempContext.packageName}.fileProvider", takePhotoFile)
        } else {
            Uri.fromFile(takePhotoFile)
        }
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
    } else {
        Log.e(TAG, "没有相机,无法启动")
    }
}

/**
 * 处理拍照和选择图片的返回值
 */
fun Fragment.handleSelectPicResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode == Activity.RESULT_OK) {
        when (requestCode) {
            REQUEST_SELECT_PIC -> {
                // 相册返回
                data?.let {
                    // 获取选择的图片的路径
                    val sourceFilePath = requireContext().handleSelectIntent(it)
                    if (sourceFilePath.isNotEmpty()) {
                        // 回调选择成功
                        mSelectCallBack?.invoke(Uri.fromFile(File(sourceFilePath)))
                        return
                    }
                } ?: Log.e(TAG, "选择的图片无效")
                mSelectCallBack = null
            }
            REQUEST_TAKE_PHOTO -> {
                // 拍照返回
                mSelectCallBack?.invoke(Uri.fromFile(takePhotoFile))
                mSelectCallBack = null
            }
        }
    }
}

/**
 * 启动原生的图片选择器
 */
fun Activity.openPhotoAlbum(callBack: ((res: Uri) -> Unit)? = null) {
    mSelectCallBack = callBack
    val intent = Intent("android.intent.action.GET_CONTENT")
    intent.type = "image/*"
    //打开系统相册
    startActivityForResult(intent, REQUEST_SELECT_PIC)
    overridePendingTransition(R.anim.translation_right_in, R.anim.translation_right_out)
}

/**
 * 打开相机拍照
 */
fun Activity.openCamera(callBack: ((res: Uri) -> Unit)? = null) {
    mSelectCallBack = callBack
    // 启动相机的Intent
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    // 是否可以启动相机
    if (takePictureIntent.resolveActivity(this.packageManager) != null) {
        // 兼容android 7.0
        val photoURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "${this.packageName}.fileProvider", takePhotoFile)
        } else {
            Uri.fromFile(takePhotoFile)
        }
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
    } else {
        Log.e(TAG, "没有相机,无法启动")
    }
}

/**
 * 处理选择图片的返回值
 */
fun Activity.handleSelectPicResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode == Activity.RESULT_OK) {
        when (requestCode) {
            REQUEST_SELECT_PIC -> {
                data?.let {
                    // 获取选择的图片的路径
                    val sourceFilePath = this.handleSelectIntent(it)
                    if (sourceFilePath.isNotEmpty()) {
                        // 回调选择成功
                        mSelectCallBack?.invoke(Uri.fromFile(File(sourceFilePath)))
                        return
                    }
                } ?: Log.e(TAG, "选择的图片无效")
                mSelectCallBack = null
            }
            REQUEST_TAKE_PHOTO -> {
                // 拍照返回
                mSelectCallBack?.invoke(Uri.fromFile(takePhotoFile))
                mSelectCallBack = null
            }
        }
    }
}
