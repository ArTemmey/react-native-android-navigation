import {ActivityEventType} from "./compilable";
import Intent from "../Intent";

export type CustomServiceEventType = string;
export type AndroidNavigatorEventType = ActivityEventType | CustomServiceEventType;

export type ActivityResultListener = (requestCode: number, resultCode: number, data: Intent | null) => void;
export type BackPressListener = (...data: any) => void;
export type ActivityEventListener = ActivityResultListener | BackPressListener;
export type CustomServiceEventListener = (extras: Object) => void;
export type AndroidNavigatorEventListener = ActivityEventListener | CustomServiceEventListener;