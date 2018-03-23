import {NavigationErrorMessage} from "../Types";

export class NavigationError extends Error {
    constructor(message: NavigationErrorMessage) {
        super(message);
        this.name = 'NavigationError';
    }
}

export class NoActivityError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'NoActivityError';
    }
}

export default class ErrorHandler {

    static getExecutor(resolve, reject) {
        return (result) => {
            if (typeof result === 'object' && result.hasOwnProperty('error')) {
                if(typeof result.error === NavigationErrorMessage) {
                    reject(new NavigationError(result.error));
                } else {
                    reject(new NoActivityError(result.error));
                }
            } else {
                resolve(result);
            }
        };
    }

}