package com.nhi.datn.share_udn_android.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.nhi.datn.share_udn_android.R
import com.nhi.datn.share_udn_android.models.ErrorResponse
import com.nhi.datn.share_udn_android.network.DefineMessageException
import com.nhi.datn.share_udn_android.network.NoConnectionException
import com.nhi.datn.share_udn_android.view.DialogMessage
import com.nhi.datn.share_udn_android.view.ErrorDialog
import com.nhi.datn.share_udn_android.view.LoadingProgress
import com.tbruyelle.rxpermissions2.RxPermissions
import retrofit2.HttpException
import java.io.File


abstract class BaseActivity<BP : BasePresenter> : AppCompatActivity(), IBaseActivity {

    private val CAMERA_REQUEST_CODE = 1011
    private val GALLERY_REQUEST_CODE = 1012
    private var mCapturedPath: String? = null
    protected var mPresenter: BP? = null
    lateinit var mRxPermissions: RxPermissions

    companion object {
        const val CROP_PICTURE_REQUEST_CODE = 1013
    }

    override fun showErrorMessage(message: String) {
        val dialogError = ErrorDialog()
        dialogError.apply {
            setTitle(this@BaseActivity.getString(R.string.error))
            setMessage(message)
        }.show(supportFragmentManager, "")
    }

    var loadingProgress: LoadingProgress? = null

    var hasUpdate: Byte = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter = getPresenter()
        loadingProgress = LoadingProgress(this)
        mRxPermissions = RxPermissions(this)
    }

    override fun showLoading() {
        if (!loadingProgress?.isShowing!!)
            loadingProgress?.show()
    }

    override fun onDestroy() {
        try {
            if (loadingProgress?.isShowing!!) {
                loadingProgress!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onDestroy()
        mPresenter?.onCleared()
    }

    protected abstract fun getPresenter(): BP?

    override fun dismissLoading() {
        if (!isFinishing) {
            if (loadingProgress?.isShowing!!)
                loadingProgress!!.dismiss()
        }
    }

    override fun showDialogError(msg: String): DialogMessage {
        val dialogMessage = DialogMessage(this, msg)
        dialogMessage.show()
        dialogMessage.setOnDismissListener {
            //            if (msg == getString(R.string.unauthorized)) {
//                authenticationFailed()
//            }
        }
        return dialogMessage
    }

    override fun forceDismissLoading() {
        loadingProgress?.forceDismiss()
    }

    override fun showDialogSuccess(msg: String): DialogMessage {
        val dialogMessage = DialogMessage(this, msg)
        dialogMessage.show()
        return dialogMessage
    }

    override fun onResume() {
        super.onResume()
        hasUpdate = 0
    }

    override fun startActivity(intent: Intent) {
        if (hasUpdate.toInt() == 1) return
        super.startActivity(intent)
        hasUpdate = 2
    }

    override fun showDialogPermission(msg: String) {
        showDialogSuccess(msg)
    }

    override fun addFragment(layout: Int, fragment: BaseFragment<*>, name: String) {
        supportFragmentManager.beginTransaction().add(layout, fragment).addToBackStack(name).commitAllowingStateLoss()
    }

    override fun replaceFragment(layout: Int, fragment: BaseFragment<*>, name: String) {
        supportFragmentManager.beginTransaction().replace(layout, fragment).addToBackStack(name).commitAllowingStateLoss()
    }

    @SuppressLint("InflateParams")
    override fun showToastSuccess(message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.view_dialog_add_favorite, null)
        layout.findViewById<TextView>(R.id.tvMessage).text = message
        Toast(applicationContext).apply {
            setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (mCapturedPath != null)
                    onCapturedImage(mCapturedPath!!)
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                if (data != null) {
                    val selectedImage = data.data
                    onChoseImage(selectedImage)
                }
            } else if (requestCode == CROP_PICTURE_REQUEST_CODE) {
                data?.getStringExtra("result")?.let {
                    onCropImage(it)
                }
            }
        } else if (requestCode == GALLERY_REQUEST_CODE) {
            onChoseNoImage()
        }
    }

    protected open fun onCapturedImage(path: String) {}

    protected open fun onChoseImage(uri: Uri) {}

    protected open fun onCropImage(path: String) {}

    protected open fun onChoseNoImage() {}

    @SuppressLint("CheckResult")
    fun openCamera(fileName: String? = null) {
//        val tempFile = FileUtil.getOutputMediaFile(applicationContext, fileName)
//        tempFile?.let {
//            mCapturedPath = tempFile.absolutePath
//            camera(tempFile)
//        }
    }

    @SuppressLint("CheckResult")
    fun openGallery() {
        mRxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted)
                        gallery()
                }
    }


    private fun camera(tempFile: File) {
        val capturedFileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this,
                    applicationContext.packageName + ".provider", tempFile)
        } else {
            Uri.fromFile(tempFile)
        }
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePhotoIntent.resolveActivity(packageManager) != null) {
            takePhotoIntent.putExtra("return-data", true)
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedFileUri)
            val chooserIntent = Intent.createChooser(takePhotoIntent, "Selection Photo")
            if (chooserIntent != null)
                startActivityForResult(chooserIntent, CAMERA_REQUEST_CODE)
        }
    }

    private fun gallery() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        if (photoPickerIntent.resolveActivity(packageManager) != null) {
            photoPickerIntent.type = "image/*"
            val chooserIntent = Intent.createChooser(photoPickerIntent, "Selection Photo")
            if (chooserIntent != null)
                startActivityForResult(chooserIntent, GALLERY_REQUEST_CODE)
        }
    }

    override fun showError(throwable: Throwable) {
        var msg: String? = null
        try {
            if (throwable is HttpException) {
                val response = throwable.response()
                if (response != null) {
                    val responseBody = response.errorBody()
                    if (responseBody?.charStream() != null) {
                        val error = Gson().fromJson<ErrorResponse>(responseBody.charStream(), ErrorResponse::class.java)
                        when {
//                            error.statusCode == 401 -> authenticationFailed()
                            error.statusCode == 400 -> msg = error.messages
                            else -> Log.e("ERROR", error.messages)
                        }
                    }
                }
            } else if (throwable is NoConnectionException) {
                TODO("show toast")

                return
            } else if (throwable is DefineMessageException) {
                msg = throwable.message
            } else {
                if (throwable !is IndexOutOfBoundsException) {
                    msg = throwable.message
                }
            }
        } catch (e: Exception) {
            //fix crash from fabric
            //Caused by retrofit2.adapter.rxjava2.HttpException: HTTP 503 Service Unavailable
            //msg = getString(R.string.server_error)
        }
        if (msg != null) {
            showDialogError(msg)

        }
    }
    override fun hideKeyBoard() {
        runOnUiThread {
            this@BaseActivity.currentFocus?.let {
                try {
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                            it.applicationWindowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                } catch (e: IllegalStateException) {
                } catch (e: Exception) {
                }
            }

        }
    }

}
