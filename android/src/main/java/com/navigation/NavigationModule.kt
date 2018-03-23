package com.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

import java.util.HashMap

/**
 * Created by a.lunkov on 03.11.2017.
 */

class NavigationModule(internal val context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    private val activityEventListener: ActivityEventListener = object : ActivityEventListener {

        override fun onNewIntent(intent: Intent) {

        }

        override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent) {
            emitter.emit("ACTIVITY_RESULT", requestCode, resultCode, Converter.writeIntent(data))
        }
    }

    init {
        emitter = object : Emitter {
            override fun emit(eventName: String, vararg data: Any?) {
                context
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                        .emit(eventName, data)
            }
        }
        context.addActivityEventListener(activityEventListener)
    }

    override fun getName(): String {
        return "NavigationModule"
    }

    override fun getConstants(): Map<String, Any>? {
        return HashMap()
    }

    @ReactMethod
    fun currentActivityIsRunning(callback: Callback) {
        callback.invoke(currentActivity != null)
    }

    @ReactMethod
    fun getIntent(getter: Callback) {
        try {
            getter.invoke(Converter.writeIntent(currentActivity!!.intent))
        } catch (e: NullPointerException) {
            getter.invoke(Converter.writeError(e.message))
        }

    }

    @ReactMethod
    fun startActivity(intent: ReadableMap, callback: Callback) {
        navigate(
                intent,
                object : Starter {
                    override fun start(fromActivity: Boolean, result: Intent) {
                        if (fromActivity) {
                            currentActivity!!.startActivity(result)
                        } else {
                            context.startActivity(result)
                        }
                    }
                },
                callback
        )
    }

    @ReactMethod
    fun startActivityForResult(intent: ReadableMap, requestCode: Int, callback: Callback) {
        navigate(
                intent,
                object : Starter {
                    override fun start(fromActivity: Boolean, result: Intent) {
                        if (fromActivity) {
                            currentActivity!!.startActivityForResult(result, requestCode, null)
                        } else {
                            context.startActivityForResult(result, requestCode, null)
                        }
                    }
                },
                callback
        )
    }

    @ReactMethod
    fun startService(intent: ReadableMap, callback: Callback) {
        val result = Converter.readIntent(context, intent.toHashMap(), callback)
        if (result != null) {
            try {
                context.startService(result)
                callback.invoke()
            } catch (e: SecurityException) {
                callback.invoke(Converter.writeError("TARGET_CLASS_NOT_EXPORTED"))
            }

        }
    }

    private fun navigate(intent: ReadableMap, starter: Starter, callback: Callback) {
        val c: Context?
        var fromActivity = false
        var needNewTask = false
        if (currentActivity == null) {
            c = context
            if(!intent.getArray("flags").toArrayList().contains(Intent.FLAG_ACTIVITY_NEW_TASK.toDouble())) {
                needNewTask = true
            }
        } else {
            c = currentActivity
            fromActivity = true
        }
        try {
            val result = Converter.readIntent(c, intent.toHashMap(), callback)
            if (result != null) {
                if(needNewTask) {
                    result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    starter.start(fromActivity, result)
                    callback.invoke()
                } catch (e: SecurityException) {
                    callback.invoke(Converter.writeError("TARGET_CLASS_NOT_EXPORTED"))
                }

            }
        } catch (e: Exception) {
            callback.invoke(Converter.writeError(e.message))
        }

    }

    @ReactMethod
    fun setResult(resultCode: Int, data: ReadableMap?, callback: Callback) {
        try {
            currentActivity!!.setResult(resultCode, if (data == null) null else Converter.readIntent(context, data.toHashMap(), callback))
            callback.invoke()
        } catch (e: NullPointerException) {
            callback.invoke(Converter.writeError(e.message))
        }

    }

    @ReactMethod
    fun finish(callback: Callback) {
        try {
            currentActivity!!.finish()
            callback.invoke()
        } catch (e: NullPointerException) {
            callback.invoke(Converter.writeError(e.message))
        }

    }

    companion object {

        private lateinit var emitter: Emitter

        @JvmStatic
        fun onBackPressed(vararg data: Any?) {
            emitter.emit("BACK_PRESSED", data)
        }

    }

    private interface Starter {
        fun start(fromActivity: Boolean, result: Intent)
    }

    private interface Emitter {
        fun emit(eventName: String, vararg data: Any?)
    }

}

