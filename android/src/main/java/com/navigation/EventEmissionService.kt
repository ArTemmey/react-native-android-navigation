package com.navigation

import android.content.Intent

import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig

/**
 * Created by a.lunkov on 21.12.2017.
 */

class EventEmissionService : HeadlessJsTaskService() {

    override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig {
        return HeadlessJsTaskConfig(
                "CustomEventEmission",
                if (intent.extras == null) Arguments.createMap() else Converter.writeIntentExtras(intent.extras!!),
                0,
                true
        )
    }

}
