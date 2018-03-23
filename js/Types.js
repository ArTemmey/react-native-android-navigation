import Intent from "./Intent";

export type CustomServiceEventType = string;
export type ActivityEventType =
    "ACTIVITY_RESULT" |
    "BACK_PRESSED";
export type AndroidNavigatorEventType = ActivityEventType | CustomServiceEventType;

export type ActivityResultListener = (requestCode: number, resultCode: number, data: Intent | null) => void;
export type BackPressListener = (...data: any) => void;
export type ActivityEventListener = ActivityResultListener | BackPressListener;
export type CustomServiceEventListener = (extras: Object) => void;
export type AndroidNavigatorEventListener = ActivityEventListener | CustomServiceEventListener;

export type NavigationErrorMessage =
    "TARGET_CLASS_NOT_FOUND" |
    "TARGET_PACKAGE_NOT_FOUND" |
    "TARGET_CLASS_IS_NOT_EXPORTED";