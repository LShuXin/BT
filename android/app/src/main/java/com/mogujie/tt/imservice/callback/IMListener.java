package com.mogujie.tt.imservice.callback;

/**
 * @author : yingmu on 15-1-7.
 * @email : yingmu@mogujie.com.
 */
public interface IMListener<T> {
    void onSuccess(T response);

    void onFaild();

    void onTimeout();
}
