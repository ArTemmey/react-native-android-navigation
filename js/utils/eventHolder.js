import React from 'react';
import {AppRegistry, DeviceEventEmitter} from 'react-native';
import Converter from "./converter";
import {ActivityEventType} from "../Types";

export default class EventHolder {

    static customEvents = {};

    static addEventListener(type, listener) {
        if (typeof type === ActivityEventType) {
            DeviceEventEmitter.addListener(type, listener);
        } else {
            if (!this.customEvents.hasOwnProperty(type)) {
                this.customEvents[type] = [];
            }
            if (!this.customEvents[type].includes(listener)) {
                this.customEvents[type].push(listener)
            }
        }
    }

    static removeEventListener(type, listener) {
        if (typeof type === ActivityEventType) {
            DeviceEventEmitter.removeListener(type, listener);
        } else if (this.customEvents.hasOwnProperty(type)) {
            this.customEvents[type].filter(l => l !== listener);
        }
    }

}

AppRegistry.registerHeadlessTask(
    'CustomEventEmission',
    () => async (eventData) => {
        if (EventHolder.customEvents.hasOwnProperty(eventData.type)) {
            EventHolder.customEvents[eventData.type].forEach(listener => listener(eventData.extras));
        }
    }
);