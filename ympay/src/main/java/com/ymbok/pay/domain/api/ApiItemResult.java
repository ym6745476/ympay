package com.ymbok.pay.domain.api;

public class ApiItemResult<T> extends ApiResult {

    private T item;

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }
}
