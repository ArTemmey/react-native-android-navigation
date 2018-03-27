import React from 'react';
import {NativeModules} from 'react-native';
import Intent from './Intent';
import {AndroidNavigatorEventType, AndroidNavigatorEventListener} from './Types/inbuilt';
import Converter from "./utils/converter";
import ErrorHandler from "./utils/errorHandler";
import EventHolder from "./utils/eventHolder";

const NavigationModule = NativeModules.NavigationModule;

export default class AndroidNavigator {

    static RESULT_CANCELED = 0;
    static RESULT_FIRST_USER = 1;
    static RESULT_OK = -1;

    static currentActivityIsRunning(): Promise<boolean> {
        return new Promise(resolve => NavigationModule.currentActivityIsRunning(resolve));
    }

    static getIntent(): Promise<Intent> {
        return new Promise((resolve, reject) => NavigationModule.getIntent(ErrorHandler.getExecutor(Converter.getIntentReader(resolve), reject)));
    }

    static startActivity(intent: Intent): Promise<void> {
        return new Promise((resolve, reject) => NavigationModule.startActivity(Converter.writeIntent(intent), ErrorHandler.getExecutor(resolve, reject)));
    }

    static startActivityForResult(intent: Intent, requestCode: number): Promise<void> {
        return new Promise((resolve, reject) => NavigationModule.startActivityForResult(Converter.writeIntent(intent), requestCode, ErrorHandler.getExecutor(resolve, reject)));
    }

    static startService(intent: Intent): Promise<void> {
        return new Promise((resolve, reject) => NavigationModule.startService(Converter.writeIntent(intent), ErrorHandler.getExecutor(resolve, reject)));
    }

    static setResult(resultCode: number, data?: Intent): Promise<void> {
        return new Promise((resolve, reject) => NavigationModule.setResult(resultCode, data ? Converter.writeIntent(data) : null, ErrorHandler.getExecutor(resolve, reject)));
    }

    static finish(): Promise<void> {
        return new Promise((resolve, reject) => NavigationModule.finish(ErrorHandler.getExecutor(resolve, reject)));
    }

    static addEventListener(type: AndroidNavigatorEventType, listener: AndroidNavigatorEventListener): void {
        EventHolder.addEventListener(type, listener);
    }

    static removeEventListener(type: AndroidNavigatorEventType, listener: AndroidNavigatorEventListener): void {
        EventHolder.removeEventListener(type, listener);
    }

}