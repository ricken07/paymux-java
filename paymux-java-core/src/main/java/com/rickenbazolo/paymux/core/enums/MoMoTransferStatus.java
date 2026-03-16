package com.rickenbazolo.paymux.core.enums;

/**
 * Enumeration of possible transfer statuses in Mobile Money operations.
 * These statuses represent the various states a transfer can be in during its lifecycle.
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public enum MoMoTransferStatus {
    PENDING,
    SUCCESSFUL,
    FAILED,
    CANCELLED,
    SUCCESS,
    UNKNOW
}
