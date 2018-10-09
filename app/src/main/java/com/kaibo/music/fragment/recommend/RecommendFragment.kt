package com.kaibo.music.fragment.recommend

import android.os.Bundle
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.music.R
import kotlinx.android.synthetic.main.fragment_recommend_layout.*


/**
 * @author kaibo
 * @date 2018/10/9 10:31
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class RecommendFragment : BaseFragment() {

    override fun getLayoutRes() = R.layout.fragment_recommend_layout

    override fun initViewCreated(savedInstanceState: Bundle?) {
        //获取控件
        val imgesUrl: MutableList<String> = ArrayList()
        imgesUrl.add("http://img3.fengniao.com/forum/attachpics/913/114/36502745.jpg")
        imgesUrl.add("http://imageprocess.yitos.net/images/public/20160910/99381473502384338.jpg")
        imgesUrl.add("http://imageprocess.yitos.net/images/public/20160910/77991473496077677.jpg")
        imgesUrl.add("http://imageprocess.yitos.net/images/public/20160906/1291473163104906.jpg")
        //添加轮播图片数据（图片数据不局限于网络图片、本地资源文件、View 都可以）,刷新数据也是调用该方法
        xbanner.setData(imgesUrl, null)//第二个参数为提示文字资源集合
    }

    override fun onResume() {
        super.onResume()
        xbanner.startAutoPlay()
    }

    override fun onPause() {
        xbanner.stopAutoPlay()
        super.onPause()
    }

}