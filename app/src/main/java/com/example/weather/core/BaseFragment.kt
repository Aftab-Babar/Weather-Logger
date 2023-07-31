package com.example.weather.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.weather.models.WeatherReport
import com.example.weather.utils.SessionManager
import com.example.weather.utils.Tools.isOnBackStack
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


abstract class BaseFragment<VB : ViewBinding>(private val bindingInflater: (inflater: LayoutInflater) -> VB) :
    Fragment() {


    private var _binding: VB? = null


    val TAG = javaClass.simpleName


    val binding: VB
        get() = _binding as VB

/*
    @Inject
    lateinit var sessionManager: SessionManager

    private var _report: WeatherReport? = null
    val report: WeatherReport?
        get() {
            if (_report == null) {
                _report = runBlocking {
                    Gson().fromJson(
                        context?.let { sessionManager.getReport().first() },
                        WeatherReport::class.java
                    )
                }
            }
            return _report
        }
*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO initialize stored pref if needed.

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater)
        if (_binding == null) {
            throw IllegalArgumentException("Binding cannot be null")
        }
        showStatusBar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListener()
        addObserver()

    }

    override fun onPause() {
        super.onPause()
        dismissDialog()
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null

    }

    abstract fun initView()

    /**
     * This abstract method is used to initialize all listener
     */
    abstract fun initListener()

    /**
     * This abstract method is used to initialize all observer
     */
    abstract fun addObserver()

    fun navigate(@IdRes resId: Int, bundle: Bundle? = null, navOptions: NavOptions? = null) {
        if (isAdded) {
            if (findNavController().isOnBackStack(resId)) {
                findNavController().popBackStack(resId, false)
            } else {
                findNavController().navigate(resId, bundle, navOptions)
            }
        }


    }

    fun popBackStack() {
        if (isAdded) {
            findNavController().popBackStack()
        }
    }

    fun showErrorMessage(message: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    fun showSuccessMessage(message: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    /**
     * Use this to dismiss the dialog
     **/
    open fun dismissDialog() {

    }


    fun hideStatusBar() {
        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    fun showStatusBar() {
        activity?.window?.decorView?.systemUiVisibility = View.STATUS_BAR_VISIBLE
    }



}