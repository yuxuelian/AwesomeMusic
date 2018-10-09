package com.kaibo.core.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.AutoDisposeConverter
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.bindToAutoDispose

/**
 * @author:Administrator
 * @date:2018/4/2 0002 上午 10:33
 * GitHub:
 * email:
 * description:
 */

abstract class BaseFragment : Fragment() {

    private val rxPermissions by lazy {
        RxPermissions(activity!!)
    }

    /**
     * 封装一下权限申请后的处理逻辑
     */
    protected fun easyRequestPermission(permissionName: String, invoke: () -> Unit) {
        if (!rxPermissions.isGranted(permissionName)) {
            rxPermissions
                    .requestEach(permissionName)
                    .subscribe { permission: Permission ->
                        if (permission.granted) {
                            invoke.invoke()
                        } else {
                            if (!permission.shouldShowRequestPermissionRationale) {
                                ToastUtils.showError("所需权限被拒绝,无法进行相关操作")
                            } else {
                                ToastUtils.showError("所需权限被永久拒绝,请到安全中心开启")
                            }
                        }
                    }
        } else {
            invoke.invoke()
        }
    }


    protected lateinit var mAttachActivity: FragmentActivity

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mAttachActivity = context as FragmentActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutRes(), container, false)
//        return container?.inflate(getLayoutRes())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewCreated(savedInstanceState)
    }

    protected open fun initViewCreated(savedInstanceState: Bundle?) {

    }

    protected abstract fun getLayoutRes(): Int

    protected fun <T> bindLifecycle(): AutoDisposeConverter<T> = bindToAutoDispose(this)

}