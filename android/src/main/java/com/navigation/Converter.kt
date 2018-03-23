package com.navigation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import java.io.Serializable

/**
 * Created by a.lunkov on 25.10.2017.
 */

object Converter {

    internal fun readIntent(current: Context?, source: Map<*, *>, callback: Callback): Intent? {
        val result = createIntent(
                current,
                source["className"] as String?,
                source["packageName"] as String?,
                source["action"] as String?,
                source["customServiceEventName"] as String?,
                callback
        )
        if (result != null) {
            val extrasSource = source["extras"] as Map<*, *>
            if (!extrasSource.isEmpty()) {
                val extrasResult = Bundle()
                for (key in extrasSource.keys) {
                    readDefaultIntentExtra(key as String, extrasSource[key], extrasResult)
                }
                if (result.hasExtra("type")) {
                    result.putExtra("extras", extrasResult)
                } else {
                    result.putExtras(extrasResult)
                }
            }
            for (category in source["categories"] as List<*>) {
                result.addCategory(category as String)
            }
            for (flag in source["flags"] as List<*>) {
                result.addFlags(flag as? Int ?: (flag as Double).toInt())
            }
        }
        return result
    }

    private fun createIntent(current: Context?, className: String?, packageName: String?, action: String?, customServiceEventName: String?, callback: Callback): Intent? {
        var result: Intent? = null
        if (customServiceEventName != null) {
            result = Intent(current, EventEmissionService::class.java)
            result.putExtra("type", customServiceEventName)
        } else {
            val manager = current!!.packageManager
            if (className != null) {
                if (packageName != null) {
                    try {
                        manager.getPackageInfo(packageName, 0)
                        result = Intent()
                    } catch (e: PackageManager.NameNotFoundException) {
                        callback.invoke(writeError("TARGET_PACKAGE_NOT_FOUND"))
                    }

                    if (result != null) {
                        result.component = ComponentName(packageName, className)
                        val list = manager.queryIntentActivities(
                                result,
                                PackageManager.MATCH_DEFAULT_ONLY
                        )
                        if (list.size == 0) {
                            result = null
                            callback.invoke(writeError("TARGET_CLASS_NOT_FOUND"))
                        }
                    }
                } else {
                    try {
                        val dest = Class.forName(className)
                        result = Intent(current, dest)
                    } catch (e: ClassNotFoundException) {
                        callback.invoke(writeError("TARGET_CLASS_NOT_FOUND"))
                    }

                }
                if (result != null && action != null) {
                    result.action = action
                }
            } else if (packageName != null) {
                result = manager.getLaunchIntentForPackage(packageName)
                if (result == null) {
                    callback.invoke(writeError("TARGET_PACKAGE_NOT_FOUND"))
                } else if (action != null) {
                    result.action = action
                }
            } else if (action != null) {
                result = Intent(action)
            } else {
                result = Intent()
            }
        }
        return result
    }


    private fun readDefaultIntentExtra(key: String, item: Any?, extras: Bundle) {
        when (item) {
            is Boolean -> extras.putBoolean(key, item)
            is Int -> extras.putInt(key, item)
            is Double -> extras.putDouble(key, item)
            is String -> extras.putString(key, item)
            else -> readCustomIntentExtra(
                    item,
                    object : IntentExtraReadingFinisher {
                        override fun finishReading(itemItem: Any?) {
                            if (itemItem is Bundle) {
                                extras.putBundle(key, itemItem as Bundle?)
                            } else {
                                extras.putSerializable(key, itemItem as Serializable?)
                            }
                        }
                    })
        }
    }

    private fun readCustomIntentExtra(item: Any?, itemReader: IntentExtraReadingFinisher) {
        if (item is Map<*, *>) {
            val itemMutable = item.toMutableMap()
            for (itemKey in item.keys) {
                readCustomIntentExtra(
                        item[itemKey],
                        object : IntentExtraReadingFinisher {
                            override fun finishReading(itemItem: Any?) {
                                itemMutable[itemKey] = itemItem
                            }
                        }
                )
            }
            itemReader.finishReading(itemMutable)
        } else if (item is List<*>) {
            val itemMutable = item.toMutableList()
            for (i in item.indices) {
                readCustomIntentExtra(
                        item[i],
                        object : IntentExtraReadingFinisher {
                            override fun finishReading(itemItem: Any?) {
                                itemMutable[i] = itemItem
                            }
                        }
                )
            }
            itemReader.finishReading(itemMutable)
        }
    }

    internal fun writeIntent(source: Intent): WritableMap {
        val result = Arguments.createMap()
        result.putString("className", if (source.component == null) null else source.component.className)
        result.putString("packageName", source.`package`)
        result.putString("action", source.action)
        result.putMap("extras", if (source.extras == null) null else writeIntentExtras(source.extras))
        val categories = Arguments.createArray()
        if (source.categories != null) {
            for (item in source.categories) {
                categories.pushString(item)
            }
        }
        result.putArray("categories", categories)
        result.putInt("flags", source.flags)
        return result
    }

    internal fun writeIntentExtras(source: Bundle): WritableMap {
        val result = Arguments.createMap()
        for (key in source.keySet()) {
            writeIntentExtraObjectItem(key, source.get(key), result)
        }
        return result
    }

    private fun writeIntentExtraObjectItem(key: String, item: Any?, result: WritableMap) {
        when (item) {
            is Nothing -> result.putNull(key)
            is Boolean -> result.putBoolean(key, item)
            is Int -> result.putInt(key, item)
            is Double -> result.putDouble(key, item)
            is String -> result.putString(key, item)
            is Map<*, *> -> result.putMap(key, writeIntentExtraObject(item))
            is List<*> -> result.putArray(key, writeIntentExtraArray(item))
            is Bundle -> result.putMap(key, writeIntentExtras(item))
        }
    }

    private fun writeIntentExtraObject(source: Map<*, *>): WritableMap {
        val result = Arguments.createMap()
        for (key in source.keys) {
            writeIntentExtraObjectItem(key as String, source[key], result)
        }
        return result
    }

    private fun writeIntentExtraArray(source: List<*>): WritableArray {
        val result = Arguments.createArray()
        for (item in source) {
            when (item) {
                is Nothing -> result.pushNull()
                is Boolean -> result.pushBoolean(item)
                is Int -> result.pushInt(item)
                is Double -> result.pushDouble(item)
                is String -> result.pushString(item)
                is Map<*, *> -> result.pushMap(writeIntentExtraObject(item))
                is List<*> -> result.pushArray(writeIntentExtraArray(item))
                is Bundle -> result.pushMap(writeIntentExtras(item))
            }
        }
        return result
    }

    internal fun writeError(message: String?): WritableMap {
        val result = Arguments.createMap()
        result.putString("error", message)
        return result
    }

    private interface IntentExtraReadingFinisher {
        fun finishReading(itemItem: Any?)
    }

}
