package com.kaibo.music.dialog

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaibo.core.adapter.withItems
import com.kaibo.core.dialog.BaseSheetDialog
import com.kaibo.core.util.dip
import com.kaibo.music.R
import com.kaibo.music.item.recommend.SongTitleItem
import com.kaibo.music.weight.BottomSheetLayout
import kotlinx.android.synthetic.main.dialog_being_play_list.*

/**
 * @author kaibo
 * @date 2018/11/1 17:30
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class BeingPlayListDialog : BaseSheetDialog() {

    override fun getLayoutRes() = R.layout.dialog_being_play_list

    override fun getSize() = Pair(WindowManager.LayoutParams.MATCH_PARENT, dip(350))

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        testRecyclerView.layoutManager = LinearLayoutManager(mActivity)
        testRecyclerView.withItems {
            (0..30).forEach { _ ->
                add(SongTitleItem())
            }
        }

        /**
         * 完全关闭了
         */
        bottomSheetLayout.setOnCollapseListener {
            dismiss()
        }
    }

    fun show(manager: FragmentManager) {
        super.show(manager, "beingPlayListDialog")
    }
}