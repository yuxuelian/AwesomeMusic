package com.kaibo.core.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.kaibo.core.R
import com.kaibo.core.utl.installApk
import org.jetbrains.anko.sp


/**
 * @author kaibo
 * @date 2018/8/9 10:11
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class AppInstallApkActivity : BaseActivity() {

    companion object {
        const val GET_UNKNOWN_APP_SOURCES = 0x01
    }

    private lateinit var apkPath: String

    override fun getLayoutRes() = R.layout.activity_app_install_apk

    override fun initOnCreate(savedInstanceState: Bundle?) {
        apkPath = intent.getStringExtra("apkPath")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //8.0
            //判断是否可以直接安装
            val canInstall = packageManager.canRequestPackageInstalls()
            if (!canInstall) {
                rxPermissions
                        .request(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                        .subscribe { granted: Boolean ->
                            when {
                                granted -> {
                                    //安装apk
                                    installApk(apkPath)
                                }
                                shouldShowRequestPermissionRationale(Manifest.permission.REQUEST_INSTALL_PACKAGES) -> {
                                    //引导用户去打开权限
                                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES)
                                }
                                else -> {
                                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES)
                                }
                            }
                        }
            } else {
                installApk(apkPath)
            }
        } else {
            installApk(apkPath)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GET_UNKNOWN_APP_SOURCES -> {
                installApk(apkPath)
            }
        }
    }
}