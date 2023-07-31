package com.example.weather.utils


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.weather.R
import com.google.android.material.color.MaterialColors
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.joda.time.format.DateTimeFormat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


object Tools {

    /** Possible UTC formats
    1). "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
     */

    const val commonUTCFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    const val commonOutputFormat = "dd-MMM-yyyy h:mm a"

    //Size Value
    const val UNIT_KB = 1
    const val UNIT_MB = 2
    const val UNIT_GB = 3

    private const val TAG = "Tools"


    fun String.dateTimeFormatter(
        outputFormat: String = commonOutputFormat,
        inputFormat: String = commonUTCFormat
    ): String {


        val inputDate = DateTimeFormat.forPattern(inputFormat)
        val outputDate = DateTimeFormat.forPattern(outputFormat)

        return try {
            val dateTime = inputDate.parseDateTime(this)
            outputDate.print(dateTime)
        } catch (e: Exception) {
            "-"
        }

    }

    fun Context.isInternetAvailable(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    fun String.isJsonValid(): Boolean {
        try {
            JSONObject(this)
        } catch (je: JSONException) {
            try {
                JSONArray(this)
            } catch (je: JSONException) {
                return false
            }
        }
        return true
    }

    fun Any.convertToJsonString(serializeNulls: Boolean = true): String {
        val gson = if (serializeNulls) {
            GsonBuilder().serializeNulls().create()
        } else {
            Gson()
        }
        return gson.toJson(this)
    }

    fun String.utcToLocal(
        outputFormat: String = commonOutputFormat,
        inputFormat: String = commonUTCFormat
    ): String {

        val sdfInput = SimpleDateFormat(inputFormat, Locale.US)
        sdfInput.timeZone = TimeZone.getTimeZone("UTC")

        return try {
            val date = sdfInput.parse(this)

            val sdfOutput = SimpleDateFormat(outputFormat, Locale.US)
            if (date != null) {
                sdfOutput.format(date)
            } else {
                "-"
            }
        } catch (pe: ParseException) {
            pe.printStackTrace()
            this
        }
    }

    fun String.isDateInFuture(inputFormat: String = commonUTCFormat): Boolean {
        val formatToCompare = "dd-MM-yyyy HH:mm"
        val sdf = SimpleDateFormat(formatToCompare, Locale.US)
        val currentDateFormattedDate = sdf.format(Date())

        return try {
            val dateToCompare =
                sdf.parse(
                    this.utcToLocal(
                        inputFormat = inputFormat,
                        outputFormat = formatToCompare
                    )
                )

            sdf.parse(currentDateFormattedDate)?.after(dateToCompare) == true

        } catch (e: Exception) {
            Log.e(TAG, "Error in parsing the dates.")
            false
        }
    }

    fun String.addDaysToDate(
        inputFormat: String = commonUTCFormat,
        outputFormat: String = commonOutputFormat,
        numberOfDays: Int
    ): String {
        val sdfInput = SimpleDateFormat(inputFormat, Locale.US)
        sdfInput.timeZone = TimeZone.getDefault()
        val sdfOutput = SimpleDateFormat(outputFormat, Locale.US)
        sdfOutput.timeZone = TimeZone.getDefault()

        val date: Date?


        return try {
            date = sdfInput.parse(this)
            val calendar = Calendar.getInstance()
            calendar.time = date!!
            calendar.add(Calendar.DAY_OF_MONTH, numberOfDays)
            sdfOutput.format(calendar.time)
        } catch (pe: ParseException) {
            pe.printStackTrace()
            "-"
        }

    }

    fun currentTimeToUtc(
        datesToConvert: String,
        numberDays: Int,
        inputFormat: String = "dd-MM-yyyy"
    ): String {

        val outputFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        var dateToReturn = datesToConvert
        val inputDateFormat = SimpleDateFormat(inputFormat, Locale.US)
        inputDateFormat.timeZone = TimeZone.getDefault()
        val gmt: Date
        val outputDataFormat = SimpleDateFormat(outputFormat, Locale.US)
        outputDataFormat.timeZone = TimeZone.getDefault()
        try {
            gmt = inputDateFormat.parse(datesToConvert) ?: Date()

            val cal = Calendar.getInstance()
            cal.time = gmt
            cal.add(Calendar.DAY_OF_MONTH, numberDays)
            dateToReturn = outputDataFormat.format(cal.time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return dateToReturn
    }

    fun String.localToGMT(
        inputFormat: String = "dd/MM/yyyy hh:mm a",
        outputFormat: String = commonUTCFormat
    ): String? {

        val sdfOutput = SimpleDateFormat(outputFormat, Locale.US)
        val calendar = Calendar.getInstance()


        return try {
            calendar.time =
                SimpleDateFormat(inputFormat, Locale.US).apply {
                    timeZone = TimeZone.getDefault()
                }.parse(this)!!
            sdfOutput.timeZone = TimeZone.getTimeZone("GMT")
            sdfOutput.format(calendar.time)
        } catch (e: Exception) {
            null
        }


    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getCurrentDate(outputFormat: String = commonOutputFormat): String {
        val sdf = SimpleDateFormat(outputFormat, Locale.US)
        //sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Calendar.getInstance().time)
    }

    fun checkInternet(context: Context?): Boolean {
        return if (context != null) {
            val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            (activeNetwork != null
                    && activeNetwork.isConnected)
        } else {
            false
        }
    }

    fun NavController.isOnBackStack(@IdRes id: Int): Boolean = try {
        getBackStackEntry(id); true
    } catch (e: Throwable) {
        false
    }

    fun String.getInitials(): String {
        var initials = ""

        val splitted: Array<String> = this.split("\\s+").toTypedArray()
        initials = if (splitted.size == 1) {
            val midPoint = splitted[0].length / 2
            splitted[0][0].toString() + splitted[0][midPoint]
        } else {
            splitted[0][0].toString() + splitted[1][0]
        }
        return initials.uppercase()
    }

    fun renderBlock(
        block: String,
        bodyColor: String? = null,
        textColor: String = "#000000"
    ): String {
        var body = "<body style=\"color:$textColor\";>$block</body>"
        if (bodyColor != null) {
            Log.d(TAG, "renderBlock: $bodyColor")
            body = "<body style=\"background:$bodyColor;color:$textColor\";>$block</body>"
        }

        return """<!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width">
                  <title>MathJax example</title>
                 <script type="text/x-mathjax-config">
                  MathJax.Hub.Config({
                    tex2jax: {
                      inlineMath: [ ['$','$'], ["\\(","\\)"] ],
                      processEscapes: true
                    }
                  });
                </script>
                <script type="text/javascript"
                        src="https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML">
                </script>
                </head>
                $body
                </html>"""
    }


    fun String.isUrl(): Boolean {
        val regex = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$"
        val p = Pattern.compile(regex)
        val m = p.matcher(this)
        return m.find()
    }

    fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun createImageFile(context: Context): File {
        val fileName =
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}"

        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(fileName, ".jpg", storageDir)

    }

    /** Use this method to get the real path of a file and store it in internal storage.
     * @param context
     * @param uri of file
     */
    fun Uri.copyFileToInternalStorage(context: Context): String? {
        val directoryName = "files"
        val returnCursor = context.contentResolver.query(
            this, arrayOf(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        )

        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val name = nameIndex?.let { returnCursor.getString(it) }
        returnCursor?.close()
        Log.d(TAG, "copyFileToInternalStorage: $name")
        val output = name?.let { File(context.filesDir.toString() + "/" + it) }
        try {
            val inputStream = context.contentResolver.openInputStream(this)
            val outputStream = FileOutputStream(output)
            var read = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.e("Exception", e.message.toString())
        }
        return output?.path
    }

    fun Long.getFileSize(unit: Int = UNIT_MB): Long {

        val fileSizeInKB = this / 1024
        val fileSizeInMB = fileSizeInKB / 1024
        val fileSizeInGB = fileSizeInMB / 1000
        return when (unit) {
            UNIT_KB -> {
                fileSizeInKB
            }
            UNIT_MB -> {
                fileSizeInMB
            }
            UNIT_GB -> {
                fileSizeInGB
            }
            else -> {
                this
            }
        }

    }

    fun String.removeQueryParams(): String {
        return try {
            val uri = URI(this)
            return URI(
                uri.scheme,
                uri.authority,
                uri.path,
                null, // Ignore the query part of the input url
                uri.fragment
            ).toString()
        } catch (ue: URISyntaxException) {
            this
        }


    }

    fun isImg(file: String): Boolean {
        return (file.endsWith(".png", ignoreCase = true) || file.endsWith(
            ".jpg",
            ignoreCase = true
        ) || file.endsWith(
            ".jpeg", ignoreCase = true
        )
                || file.endsWith(
            ".gif", ignoreCase = true
        ))
    }

    fun isVideo(file: String): Boolean {
        return file.endsWith(".avi", ignoreCase = true) || file.endsWith(
            ".mp4",
            ignoreCase = true
        ) || file.endsWith(".flv", ignoreCase = true) || file.endsWith(
            ".mov", ignoreCase = true
        )
    }

    fun isAudio(file: String): Boolean {
        return file.endsWith(".mp3", ignoreCase = true)
    }

    fun View.makeViewVisibleWithAnimation() {
        this.alpha = 0.3f
        this.visibility = View.VISIBLE
        this.animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(null)
    }

    fun View.expandLayout(rootView: ViewGroup) {
        this.isVisible = !this.isVisible
        TransitionManager.beginDelayedTransition(
            rootView,
            AutoTransition()
        )
    }

    fun View.rotateMe(isDown: Boolean) {
        val animSet = AnimationSet(true)
        animSet.interpolator = DecelerateInterpolator()
        animSet.fillAfter = true
        animSet.isFillEnabled = true

        val animRotate: RotateAnimation = if (isDown) {
            RotateAnimation(
                0.0f, 180.0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f
            )

        } else {
            RotateAnimation(
                180.0f, 0.0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f
            )
        }

        animRotate.duration = 500
        animRotate.fillAfter = true
        animSet.addAnimation(animRotate)
        this.startAnimation(animSet)

    }


    fun createExitWarningDialog(view: View, message: String): AlertDialog {
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle("Are you sure?")
        builder.setMessage(message)
        builder.setPositiveButton(
            "Yes"
        ) { _: DialogInterface?, _: Int ->
            Navigation.findNavController(view)
                .popBackStack()
        }

        builder.setNegativeButton(
            "No"
        ) { p0, p1 -> p0.dismiss() }
        return builder.create()
    }


    @SuppressLint("SetJavaScriptEnabled")
    fun setWebViewSetting(webView: WebView) {
        webView.settings.setSupportZoom(false)
        webView.settings.loadsImagesAutomatically = true
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webView.settings.javaScriptEnabled = true
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
    }

    fun setStatement(
        webView: WebView,
        questionStatement: String,
        backgroundColorHex: String? = null
    ) {
        webView.loadDataWithBaseURL(
            null,
            renderBlock(
                questionStatement, backgroundColorHex
                    ?: ("#" + Integer.toHexString(
                        MaterialColors.getColor(
                            webView,
                            android.R.attr.windowBackground
                        ) and 0x00ffffff and 0x00ffffff
                    ))
            ), "text/html", "UTF-8", null
        )
    }


    fun String.addQueryParamsToUrl(params: HashMap<String, String>): String {
        val builder = Uri.parse(this).buildUpon()

        for (key in params.keys) {
            builder.appendQueryParameter(key, params[key])
        }

        return try {
            URL(builder.build().toString()).toString()
        } catch (mue: MalformedURLException) {
            Log.e(TAG, "MalformedURLException while adding param, Please check the url.")
            ""
        }


    }

    fun String.appendPathToUrl(vararg paths: String): String {
        val builder = Uri.parse(this).buildUpon()
        for (path in paths) {
            builder.appendEncodedPath("$path/")
        }
        return try {
            URL(builder.build().toString()).toString()
        } catch (mue: MalformedURLException) {
            Log.e(TAG, "MalformedURLException while adding path, Please check the url.")
            ""
        }


    }

    fun getFileExtension(url: String?): String {

        if (url.isNullOrEmpty()) {
            return "-"
        }

        val file = File(url.removeQueryParams())
        return file.name.substring(file.name.lastIndexOf("."));
    }

    fun getFileName(url: String?): String {

        if (url.isNullOrEmpty()) {
            return "-"
        }

        val file = File(url.removeQueryParams())
        return file.name
    }

    fun downloadFile(url: String, context: Context) {

        val file = File(url)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        try {
            val uri = Uri.parse(url)
            val storagePath = "/${context.getString(R.string.appName)}/"
            val request = DownloadManager.Request(uri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(true)
            request.setTitle("Downloading ${file.name}")
            request.setDescription("Downloading...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                storagePath + file.name.removeQueryParams().replace("%", "")
            )
            val refId = downloadManager.enqueue(request)
            Log.i(TAG, "Downloaded file ref id is $refId")
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }


    fun Int.toColorHexString(context: Context, transparency: Int = 0x00ffffff): String {
        return String.format(
            "#%06x",
            ContextCompat.getColor(
                context,
                this
            ) and transparency
        )
    }

    fun TextView.startCountAnimation(duration: Int, range: Int) {
        val animator = ValueAnimator.ofInt(0, range)
        animator.duration = duration.toLong()
        animator.addUpdateListener { animation: ValueAnimator ->
            this.text = animation.animatedValue.toString()
        }
        animator.start()
    }

    fun flipCards(originCard: CardView, destinationCard: CardView) {
        val oa1 = ObjectAnimator.ofFloat(originCard, "scaleX", 1f, 0f)
        val oa2 = ObjectAnimator.ofFloat(destinationCard, "scaleX", 0f, 1f)
        oa1.interpolator = DecelerateInterpolator()
        oa2.interpolator = AccelerateDecelerateInterpolator()
        //oa1.setDuration((long)0.9);
        //oa2.setDuration((long)0.7);
        oa1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                originCard.isVisible = false
                destinationCard.isVisible = true
                oa2.start()
            }
        })
        oa1.start()
    }

    fun setColorOrAppColorAccent(view: View, colorHex: String?): String {

        return if (colorHex.isNullOrEmpty()) {
            ("#" + Integer.toHexString(
                MaterialColors.getColor(
                    view,
                    androidx.appcompat.R.attr.colorAccent
                )
            ))
        } else {
            colorHex
        }


    }

    @SuppressLint("MissingPermission")
    fun Context.getLastKnownLocation(): Location? {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    fun Context.isLocationEnabled(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


}

