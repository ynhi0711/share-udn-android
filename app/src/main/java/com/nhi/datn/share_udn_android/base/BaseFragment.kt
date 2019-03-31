package com.nhi.datn.share_udn_android.base

import android.content.Context
import android.net.Uri
import android.support.v4.app.Fragment
import com.nhi.datn.share_udn_android.view.DialogMessage

/**
 * Created by thaivuvo on 2018/04/24
 */

abstract class BaseFragment<BP : BasePresenter> : Fragment {
    var mContext: Context? = null

    protected var mPresenter: BP? = null

    constructor()

    constructor(context: Context) {
        this.mContext = context
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
        mPresenter = getPresenter()
    }

    fun showLoading() {
        if (mContext != null && activity != null && !activity!!.isFinishing) {
            (mContext as IBaseActivity).showLoading()
        }
    }

    fun dismissLoading() {
        if (mContext != null && activity != null && !activity!!.isFinishing) {
            (mContext as IBaseActivity).dismissLoading()
        }
    }

    fun showError(throwable: Throwable) {
        if (mContext != null && activity != null && !activity!!.isFinishing) {
            (mContext as IBaseActivity).showError(throwable)
        }
    }


    fun showDialogError(msg: String): DialogMessage? {
        return if (mContext == null || activity == null || activity!!.isFinishing) {
            null
        } else (mContext as IBaseActivity).showDialogError(msg)
    }

    fun forceDismissLoading() {
        if (mContext == null) return
        (mContext as IBaseActivity).forceDismissLoading()
    }

    fun showDialogSuccess(msg: String): DialogMessage? {
        return if (mContext == null || activity == null || activity!!.isFinishing) {
            null
        } else (mContext as IBaseActivity).showDialogSuccess(msg)
    }

    protected abstract fun getPresenter(): BP?

    open fun onCapturedImage(path: String) {}

    open fun onChoseImage(uri: Uri) {}

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onCleared()
    }
}
