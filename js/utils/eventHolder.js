import React from 'react';
import {AppRegistry, DeviceEventEmitter, NativeModules} from 'react-native';
import {ActivityEventType} from "../Types/compilable";
import Converter from "./converter";

const NavigationModule = NativeModules.NavigationModule;

export default class EventHolder {

    static events = {};

    static addEventListener(type, listener) {
        if (!this.events.hasOwnProperty(type)) {
            this.events[type] = [];
            if (type === `ACTIVITY_RESULT`) {
                NavigationModule.setListenerEnabled(true);
            }
        }
        if (!this.events[type].includes(listener)) {
            this.events[type].push(listener)
        }
    }

    static removeEventListener(type, listener) {
        if (!this.events.hasOwnProperty(type)) {
            return false;
        }
        const prevLength = this.events[type].length;
        if (listener) {
            const removeIndex = this.events[type].indexOf(listener);
            if (removeIndex > -1) {
                this.events[type].splice(removeIndex, 1);
            }
        }
        if (!listener || this.events[type].length === 0) {
            if (type === `ACTIVITY_RESULT`) {
                NavigationModule.setListenerEnabled(false);
            }
            return delete this.events[type];
        }
        return prevLength > this.events[type].length;
    }

}

const executeListeners = (eventData) => {
    if (eventData.hasOwnProperty('type') && EventHolder.events.hasOwnProperty(eventData.type)) {
        if (eventData.type === `ACTIVITY_RESULT`) {
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

AppRegistry.registerHeadlessTask('GlobalEventEmission', () => async (eventData) => executeListeners(eventData));

