package com.rickenbazolo.paymux.pawapay.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatement;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatementRequest;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatementResponse;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayWalletBalance;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PawaPay finance endpoints: wallet balances and wallet statements.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayFinanceService extends PawapayApiSupport {

    static final String WALLET_BALANCES = "v2/wallet-balances";
    static final String STATEMENTS = "v2/statements";
    static final String STATEMENT_STATUS = "v2/statements/";

    public PawapayFinanceService(PawapayConfig config, PaymuxHttpClient httpClient) {
        super(config, httpClient);
    }

    /**
     * Fetches the wallet balances ({@code GET /v2/wallet-balances}).
     *
     * @param country optional ISO 3166-1 alpha-3 country filter
     * @return the balances
     */
    public List<PawapayWalletBalance> walletBalances(String country) {
        var params = new LinkedHashMap<String, String>();
        params.put("country", PawapayValidation.validateCountry(country));
        var query = queryString(params);
        log.debug("Fetching PawaPay wallet balances{}", query);
        return read(get(WALLET_BALANCES + query), WalletBalancesEnvelope.class).balances();
    }

    /**
     * Requests the generation of a wallet statement ({@code POST /v2/statements}).
     * PawaPay posts the result to the callback URL of the request once the file is ready.
     *
     * @param request the statement request
     * @return the initiation response ({@code ACCEPTED} with the statement id, or {@code REJECTED})
     */
    public PawapayStatementResponse initiateStatement(PawapayStatementRequest request) {
        Objects.requireNonNull(request, "request");
        log.debug("Initiating PawaPay statement for wallet {} from {} to {}",
            request.getWallet(), request.getStartDate(), request.getEndDate());
        var result = read(post(STATEMENTS, request), PawapayStatementResponse.class);
        log.debug("PawaPay statement initiation status: {} ({})", result.status(), result.statementId());
        return result;
    }

    /**
     * Checks a statement ({@code GET /v2/statements/{statementId}}).
     *
     * @param statementId the statement id
     * @return the statement, or empty if PawaPay does not know this id
     */
    public Optional<PawapayStatement> findStatement(String statementId) {
        String id = PawapayValidation.requireUuid(statementId, "statementId");
        log.debug("Checking PawaPay statement {}", id);
        return readSearchResult(get(STATEMENT_STATUS + encode(id)), PawapayStatement.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record WalletBalancesEnvelope(List<PawapayWalletBalance> balances) {
        WalletBalancesEnvelope {
            balances = balances == null ? List.of() : List.copyOf(balances);
        }
    }
}
