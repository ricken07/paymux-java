package com.rickenbazolo.paymux.pawapay.exception;

import com.rickenbazolo.paymux.core.exception.PaymuxException;

/**
 * Exception thrown when a PawaPay callback signature cannot be verified.
 * <p>
 * Raised for a missing or malformed {@code Content-Digest} / {@code Signature} /
 * {@code Signature-Input} header, a digest mismatch, an expired signature, an unknown
 * signing key, an unsupported algorithm or an invalid signature.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapaySignatureException extends PaymuxException {

    public PawapaySignatureException(String message) {
        super(message);
    }

    public PawapaySignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
