package com.rickenbazolo.paymux.mtn.congo.collection.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponse;

/**
 * @author Ricken Bazolo
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MtnRequestToPayResponse implements TransferResponse {

    private String transactionId;
    private Integer statusCode;
    private String statusMessage;
    private String callback;
    private String ussdCode;
    private String message;

    @Override
    public String transactionId() {
        return transactionId;
    }

    @Override
    public String status() {
        return status();
    }

    @Override
    public String message() {
        return message;
    }
}
