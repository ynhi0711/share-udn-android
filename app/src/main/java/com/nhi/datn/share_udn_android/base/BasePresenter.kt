package com.nhi.datn.share_udn_android.base

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BasePresenter {

    private var compositeDisposables: CompositeDisposable? = null

    protected fun addDisposable(disposable: Disposable) {
        if (compositeDisposables == null)
            compositeDisposables = CompositeDisposable()
        compositeDisposables?.add(disposable)
    }

    fun onCleared() {
        compositeDisposables?.dispose()
    }

}