package com.retailhub.ordermanagementservice.exception;

import java.io.Serializable;

public class ExceptionInfoModel implements Serializable {

    public final String url;
    public final String errorMessage;

    public ExceptionInfoModel(String url, Exception ex) {
        this.url = url;
        this.errorMessage = ex.getMessage();
    }
}
