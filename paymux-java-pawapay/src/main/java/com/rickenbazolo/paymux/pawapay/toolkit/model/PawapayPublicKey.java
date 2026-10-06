package com.rickenbazolo.paymux.pawapay.toolkit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A PawaPay public key used to verify signed callbacks ({@code GET /v2/public-key/http}).
 *
 * @param id  the key identifier, matched against the {@code keyid} parameter of the {@code Signature-Input} header
 * @param key the PEM encoded public key
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayPublicKey(String id, String key) {
}
