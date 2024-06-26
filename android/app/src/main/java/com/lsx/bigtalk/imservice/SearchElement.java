package com.lsx.bigtalk.imservice;

import androidx.annotation.NonNull;


public class SearchElement {
    public int startIndex = -1;
    public int endIndex = -1;

    @NonNull
    @Override
    public String toString() {
        return "SearchElement [startIndex="
                + startIndex
                + ", endIndex="
                + endIndex
                + "]";
    }

    public void reset() {
        startIndex = -1;
        endIndex = -1;
    }
}