package com.rickenbazolo.paymux.pawapay.toolkit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rickenbazolo.paymux.pawapay.model.PawapayOperationType;
import com.rickenbazolo.paymux.pawapay.model.PawapayProviderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Availability of the providers of one country ({@code GET /v2/availability}).
 *
 * @param country   the ISO 3166-1 alpha-3 code
 * @param providers the providers of the country (never null)
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayAvailability(String country, List<Provider> providers) {

    public PawapayAvailability {
        providers = providers == null ? List.of() : List.copyOf(providers);
    }

    /**
     * Looks up the status of an operation type for a provider.
     *
     * @param providerCode  the provider code
     * @param operationType the operation type
     * @return the status, if reported
     */
    public Optional<PawapayProviderStatus> statusOf(String providerCode, PawapayOperationType operationType) {
        return providers.stream()
            .filter(p -> providerCode != null && providerCode.equalsIgnoreCase(p.provider()))
            .flatMap(p -> p.operationTypes().stream())
            .filter(o -> o.operationType() == operationType)
            .map(OperationTypeStatus::status)
            .findFirst();
    }

    /**
     * Availability of one provider.
     *
     * @param provider       the provider code
     * @param operationTypes the status per operation type (never null)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Provider(String provider, List<OperationTypeStatus> operationTypes) {

        public Provider {
            operationTypes = operationTypes == null ? List.of() : List.copyOf(operationTypes);
        }
    }

    /**
     * Status of one operation type.
     *
     * @param operationType the operation type
     * @param status        the current status
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OperationTypeStatus(PawapayOperationType operationType, PawapayProviderStatus status) {
    }
}
