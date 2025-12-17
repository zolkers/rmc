package com.riege.rmc.terminal;

import com.sun.jna.Callback;

public interface Terminal {

    interface NativeCallback extends Callback {
        void invoke(String data);
    }

    void start();

    void close();

    void logInfo(String msg);

    void logError(String msg);

    void logSuccess(String msg);

    void logWarning(String msg);

    void logDebug(String msg);

    void addCandidate(String candidate);

    void registerInputCallback(NativeCallback callback);

    void registerTabCallback(NativeCallback callback);
}
