package com.lsx.bigtalk.service.callback;

public interface IMListener<T> {
    void onSuccess(T response);

    void onFailed();

    void onTimeout();
}
