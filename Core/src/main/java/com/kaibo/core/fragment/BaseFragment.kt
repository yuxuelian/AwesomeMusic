package com.kaibo.core.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.bindToAutoDispose
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.AutoDisposeConverter
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment

/**
 * @author:Administrator
 * @date:2018/4/2 0002 上午 10:33
 * GitHub:
 * email:
 * description:
 */

abstract class BaseFragment : SwipeBackFragment() {

    private lateinit var fragmentActivity: FragmentActivity

    protected val mActivity
        get() = fragmentActivity

    private val rxPermissions by lazy {
        RxPermissions(activity!!)
    }

    /**
     * 需要滑动返回的Fragment  重新这个方法即可
     * 只有全屏的Fragment才能使用滑动返回   否则会有个bug
     */
    open val isCanSwipeBack = false

    /**
     * 封装一下权限申请后的处理逻辑
     */
    protected fun easyRequestPermission(permissionName: String, invoke: () -> Unit) {
        if (!rxPermissions.isGranted(permissionName)) {
            rxPermissions
                    .requestEach(permissionName)
                    .`as`(bindLifecycle())
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        fragmentActivity = context as FragmentActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(getLayoutRes(), container, false)
        return if (isCanSwipeBack) {
            attachToSwipeBack(rootView)
        } else {
            rootView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewCreated(savedInstanceState)
    }

    protected open fun initViewCreated(savedInstanceState: Bundle?) {

    }

    protected abstract fun getLayoutRes(): Int

    protected fun <T> bindLifecycle(): AutoDisposeConverter<T> = bindToAutoDispose(this)

}