# React Native Android Navigation [![npm version](https://badge.fury.io/js/react-native-android-navigation.svg)](https://badge.fury.io/js/react-native-android-navigation)

*Let your application feel at ease.*

React Native Android Navigation is the new way to integrate JavaScript and Java/Kotlin inside React Native. 
With it's help you can easily handle native window or background task in your project and also interact with other applications.

* [Getting started](#getting-started)
* [Installation](#installation)
  * [Adding the dependency](#adding-the-dependency)
  * [Linking](#linking)
    * [Automatic](#automatic)
    * [Manual](#manual)
* [Usage](#usage)
  * [Intent methods](#intent-methods)
    * [Initialization](#initialization)
    * [Extras](#extras)
      * [Putting](#putting)
      * [Getting](#getting)
      * [Other extras methods](#other-extras-methods)
    * [Categories and flags](#categories-and-flags)
  * [AndroidNavigator methods](#androidnavigator-methods)
    * [Activity processing](#activity-processing)
      * [Starting Activity](#starting-activity)
      * [Retrieving the result](#retrieving-the-result)
      * [Handling the back press](#handling-the-back-press)
      * [Other Activity methods](#other-activity-methods)
    * [Service processing](#activity-processing)
      * [Starting Service](#starting-service)
      * [Adding custom tasks](#adding-custom-tasks)
    * [Catching errors](#catching-errors)
	
## Getting started

Native Android navigation is made up of two main parts:

1. Navigation methods, available inside the native window — `Activity` — and inside the background task worker — `Service`;
 
2. Object with the navigation data — `Intent`, which is passed into the navigation methods to direct the router. It's also transferred to the destination.

Based on this, React Native Android Navigation has two classes for usage:
  
1. `AndroidNavigator` — class that wraps all the navigation methods as static properties;
 
2. `Intent` — native Intent wrapper.

Simple example:
```javascript
import {
    AndroidNavigator,
    Intent
} from 'react-native-android-navigation';

const intent = new Intent();
intent.setClassName('your.package.name.YourActivity');
AndroidNavigator.startActivity(intent);
```
*"your.package.name" is the path from `<YourProject>/android/app/src/main/java` to the directory where your Activity class is located.*

## Installation

### Adding the dependency

  ```bash
  yarn add react-native-android-navigation
  ```

  or

  ```bash
  npm install --save react-native-android-navigation
  ```

### Linking

#### Automatic

  ```bash
  react-native link react-native-android-navigation
  ```

#### Manual

1. Open `<YourProject>/android/settings.gradle` and add the following:
  ```
    include ':react-native-android-navigation'
    project(':react-native-android-navigation').projectDir = new File(rootProject.projectDir,   '../node_modules/react-native-android-navigation/android')
  ```
2. Open `<YourProject>/android/app/build.gradle` and add inside the dependencies block:
  ```
    compile project(':react-native-android-navigation')
  ```
3. Open `<YourProject>/android/app/src/main/java/package.name/MainApplication.java`:
  - Add `import com.navigation.NavigationPackage;`
  - Add `new NavigationPackage()` to the list returned by `getPackages()` method

## Usage

### Intent methods

#### Initialization

Intent has four methods for initialization. Call them separately or in combination with each other:

1. `setClassName(className: string): Intent`
  
	Use it separately if the target Activity is inside your application. Combine with `setPackageName` if the target Activity is inside another application.
  
2. `setPackageName(packageName: string): Intent`
  
	Use it separately to get the launcher Intent for the target package. Combine with `setClassName` if you need to start a certain Activity from the target package.

3. `setAction(action: string): Intent`

	Represents Android Intent method `setAction`. Note that JS Intent class has all the default Android Intent actions as static properties, so use them them the same way as in Android: `intent.setAction(Intent.ACTION_MAIN)`.

4. `setCustomServiceEventName(eventName: string): Intent`

	Use it separately to start [your custom Service](#adding-custom-tasks). Combine with `setPackageName` if the target Service is inside another application.

#### Extras

Intent allows you to pass extra data of any type.

##### Putting

Putting extras is done in the same way as in Android, with the following methods:
  - `putExtra(key: string, extra: any): Intent`
  - `putExtras(extras: Object): Intent`

Example:
```javascript
intent
    .putExtra("key1", "Hello world!")
    .putExtras(	//New data will be added to the existing
        {
            key2: 25.001,
            key3: ["pass array", true, 12345],
            key4: {key1: "object as well", key2: 69.999}
        }
    );
```

##### Getting

On Java side, extras can be obtained as usual.
For primitives and strings use `getBooleanExtra`, `getIntExtra`, `getDoubleExtra`, `getStringExtra`.
For arrays and objects use `getSerializableExtra` and cast the result to `List` or `Map` respectively. Then if needed cast each their child to its class.

Example:
```java
String helloWorld = intent.getStringExtra("key1"));
List list = (List) intent.getSerializableExtra("key3");
boolean b = (Boolean) list.get(1);
Map map = (Map) intent.getSerializableExtra("key4");
double d = (Double) map.get("key2");
```

On JS side, just use the following methods:
  - `getExtra(key: string): any`
  - `getExtras(): Object`

##### Other extras methods 

  - `removeExtra(key: string): void`
  - `replaceExtras(extras: Object): Intent`
  - `hasExtra(key: string): boolean`

#### Categories and flags

To deal with categories and flags the same methods are used as in Android:
  - `addCategory(category: string): Intent`
  - `removeCategory(category: string): void`
  - `getCategories(): string[]`
  - `hasCategory(category: string): boolean`
  - `addFlags(flags: number): Intent`
  - `setFlags(flags: number): Intent`
  - `getFlags(): number[]`
  - `removeFlags(flags: number): void`
  
Like the actions, all the default Android Intent categories and flags are declared in JS Intent class as static properties, so use them the same way as in Android.

Example:
```javascript
intent.addCategory(Intent.CATEGORY_DEFAULT).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
```

### AndroidNavigator methods

#### Activity processing

##### Starting Activity

To start new Activity, use the same methods as in Android:
 
  - `static startActivity(intent: Intent): Promise<void>`
  
  	[May trow error](#catching-errors)
  	
  - `static startActivityForResult(intent: Intent, requestCode: number): Promise<void>`
  
  	Use it to get the data you need from the target Activity after it's finished. `requestCode` can be any integer.
  	
  	[May trow error](#catching-errors)

##### Retrieving the result 

To get the result, add event listener for the corresponding `ActivityEventType`:

`export type ActivityResultListener = (requestCode: number, resultCode: number, data: Intent | null) => void`

Example:
```javascript
const myOnResult = (requestCode, resultCode, data) => {
    console.log(data.getExtras().key1);
};
AndroidNavigator.addEventListener(ActivityEventType.ACTIVITY_RESULT, myOnResult); //myOnResult will be called each time Activity result is received
```

##### Handling the back press

React Native Android Navigation allows you to pass any data from native side on back press event.

On Java side, open file `<YourProject>/android/app/src/main/java/your.package.name/MainActivity.java`:

  - Add `import com.navigation.NavigationModule;`
  - Add inside the Activity class:
  ```java
  @Override
  public void onBackPressed() {
      NavigationModule.onBackPressed("My data", 123); //pass rest parameters
  }
  ```

On JS side, add event listener for the corresponding `ActivityEventType`:

`export type BackPressListener = (...data: any) => void`

Example:
```javascript
const myOnBackPressed = (myData, oneTwoThree) => {
    console.log(myData);
};
AndroidNavigator.addEventListener(ActivityEventType.BACK_PRESSED, myOnBackPressed); //myOnBackPressed will be called each time back button is pressed
```

##### Other Activity methods

  - `static currentActivityIsRunning(): Promise<boolean>`
  
    Checks if your app has any running Activity.
    
    [May trow error](#catching-errors)
    
  - `static getIntent(): Promise<Intent>`
  
    Gets the Intent with which Activity was started.
    
    [May trow error](#catching-errors)
    
  - `setResult(resultCode: number, data?: Intent): Promise<void>`
  
    Use it if Activity was started for result. 
    Possible values of `resultCode` are included in AndroidNavigator as static properties: `RESULT_CANCELED = 0`, `RESULT_FIRST_USER = 1`, `RESULT_OK = -1`
    
    [May trow error](#catching-errors)
    
  - `finish(): Promise<void>`
  
    Closes current Activity.
    
    [May trow error](#catching-errors)

#### Service processing

##### Starting Service

To start Service, use the same method as in Android:

  - `startService(intent: Intent): Promise<void>`
  
  	 [May trow error](#catching-errors)

##### Adding custom tasks

To add your custom task, add it's event listener:

`export type CustomServiceEventListener = (extras: Object) => void`

and then start it with `startService` method, passing Intent, initialized with `setCustomServiceEventName`.

Example:
```javascript
const myServiceListener = (eventExtras) => {
    console.log(eventExtras.myData);
};
AndroidNavigator.addEventListener("myService", myServiceListener);
const intent = new Intent();
intent.setCustomServiceEventName("myService");
intent.putExtra("myData", "Hello world!");
AndroidNavigator.startService(intent);
```

#### Catching errors

The following errors can be thrown while using AndroidNavigator methods:

```javascript
export class NavigationError extends Error {
    constructor(message: NavigationErrorMessage) {}
}
```
```typescript
export enum NavigationErrorMessage {
    TARGET_CLASS_NOT_FOUND = "TARGET_CLASS_NOT_FOUND",
    TARGET_PACKAGE_NOT_FOUND = "TARGET_PACKAGE_NOT_FOUND",
    TARGET_CLASS_NOT_EXPORTED = "TARGET_CLASS_NOT_EXPORTED"
}
```
```javascript
export class NoActivityError extends Error {
    constructor(message: string) {}
}
```

To catch them, add `catch` block on any Promise returned by AndroidNavigator method.

Example:
```javascript
AndroidNavigator
    .startActivity(intent)
    .catch(
        (e) => {
            console.log(e.message);
        }
    );
```