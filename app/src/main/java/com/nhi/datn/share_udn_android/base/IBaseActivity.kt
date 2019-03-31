package com.nhi.datn.share_udn_android.base

import android.arch.lifecycle.LifecycleOwner
import com.nhi.datn.share_udn_android.view.DialogMessage

/**
 * Created by thaivuvo on 2018/04/24
 */

interface IBaseActivity : LifecycleOwner {

    fun showDialogSuccess(msg: String): DialogMessage

    fun forceDismissLoading()

    fun showDialogError(msg: String): DialogMessage

    fun dismissLoading()

    fun showLoading()

    fun showDialogPermission(msg: String)

    fun addFragment(layout: Int, fragment: BaseFragment<*>, name: String)

    fun replaceFragment(layout: Int, fragment: BaseFragment<*>, name: String)

    fun showToastSuccess(message: String)

    fun showErrorMessage(message: String)

    fun showError(throwable: Throwable)

    fun hideKeyBoard()

}
