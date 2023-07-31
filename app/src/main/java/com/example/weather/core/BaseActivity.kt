package com.example.weather.core

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


abstract class BaseActivity<V : ViewModel> : AppCompatActivity() {


    val TAG = javaClass.simpleName

    private var _viewModel: V? = null

    val vm: V
        get() = _viewModel as V


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        _viewModel = ViewModelProvider(this)[getViewModel()]


        addObserver()
    }

    override fun onPause() {
        super.onPause()
        dismissDialog()
    }


    override fun onDestroy() {

        super.onDestroy()
    }

    abstract fun getViewModel(): Class<V>
    abstract fun addObserver()

    /**
     * Use this to dismiss the dialog
     **/
    open fun dismissDialog() {

    }

    fun showErrorMessage(message: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    fun showSuccessMessage(message: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }


}
