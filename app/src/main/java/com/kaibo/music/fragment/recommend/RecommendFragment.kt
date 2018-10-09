package com.kaibo.music.fragment.recommend

import android.os.Bundle
import android.widget.ImageView
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.bean.BannerDataBean
import com.kaibo.music.net.Api
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

        Api.instance.getBannerList().toMainThread().`as`(bindLifecycle()).subscribe({
            xbanner.setData(it.data, null)
            xbanner.loadImage { banner, model, view, position ->
                GlideApp
                        .with(this)
                        .load((model as BannerDataBean).picUrl)
                        .placeholder(R.drawable.logo).error(R.drawable.logo)
                        .into(view as ImageView)
            }
        }) {
            it.printStackTrace()
        }

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