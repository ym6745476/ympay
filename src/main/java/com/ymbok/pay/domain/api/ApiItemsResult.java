package com.ymbok.pay.domain.api;

import java.util.List;


public class ApiItemsResult<T> extends ApiResult {

    private List<T> items;

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
