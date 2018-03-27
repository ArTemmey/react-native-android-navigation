import React from 'react';
import {AppRegistry, DeviceEventEmitter, NativeModules} from 'react-native';
import {ActivityEventType} from "../Types/compilable";
import Converter from "./converter";

export default class EventHolder {

    static events = {};

    static addEventListener(type, listener) {
        if (!this.events.hasOwnProperty(type)) {
            this.events[type] = [];
        }
        if (!this.events[type].includes(listener)) {
            this.events[type].push(listener)
        }
    }

    static removeEventListener(type, listener) {
        if (this.events.hasOwnProperty(type)) {
            const removeIndex = this.events[type].indexOf(listener);
            if (removeIndex > -1) {
                this.events[type].splice(removeIndex, 1);
                if (this.events[type].length === 0) {
                    delete this.events[type];
                }
            }
        }
    }

}

const executeListeners = (eventData) => {
    if (EventHolder.events.hasOwnProperty(eventData.type)) {
        if (eventData.type === ActivityEventType.ACTIVITY_RESULT) {
            eventData.extras[2] = Converter.readIntent(eventData.extras[2]);
        }
        EventHolder.events[eventData.type].forEach(
            ActivityEventType.hasOwnProperty(eventData.type) ?
                listener => listener(...eventData.extras)
                : listener => listener(eventData.extras)
        );
    }
};

DeviceEventEmitter.addListener('LocalEventEmission', executeListeners);

AppRegistry.registerHeadlessTask(
    'GlobalEventEmission',
    () => async (eventData) => {
        if(eventData.hasOwnProperty('type')) {
            executeListeners(eventData);
        } else {
            NativeModules.NavigationModule.mountService();
        }
    }
);

