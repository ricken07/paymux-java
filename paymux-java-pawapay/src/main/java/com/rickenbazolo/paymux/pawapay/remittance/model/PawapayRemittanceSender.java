package com.rickenbazolo.paymux.pawapay.remittance.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

/**
 * Sender of a remittance: the originating transaction and the KYC details of the person sending the money.
 *
 * @param transactionDetails the originating transaction (amount, currency, FX rate, fees, purpose and source of funds)
 * @param senderDetails      the identity of the sender
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PawapayRemittanceSender(TransactionDetails transactionDetails, SenderDetails senderDetails) {

    /**
     * Details of the originating transaction, all required by PawaPay.
     *
     * @param transactionReference your unique reference of the transaction (at most 64 characters)
     * @param originalAmount       the amount in the original currency
     * @param originalCurrency     the ISO 4217 code of the original currency
     * @param buyFxRate            the FX rate applied to the customer
     * @param senderFees           the fees paid by the customer
     * @param purposeOfFunds       the purpose of the funds
     * @param sourceOfFunds        the source of the funds
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TransactionDetails(String transactionReference, String originalAmount, String originalCurrency,
                                     String buyFxRate, String senderFees, PawapayPurposeOfFunds purposeOfFunds,
                                     PawapaySourceOfFunds sourceOfFunds) {
    }

    /**
     * Postal address of the sender, all fields required by PawaPay.
     *
     * @param addressLine the street address
     * @param postalCode  the postal / zip code
     * @param city        the city
     * @param country     the ISO 3166-1 alpha-3 country code
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Address(String addressLine, String postalCode, String city, String country) {
    }

    /**
     * Identification document used to KYC the sender.
     *
     * @param type   the document type
     * @param number the document number
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Identification(PawapayIdentificationType type, String number) {
    }

    /**
     * Identity of the sender. First name, last name, nationality, phone number, address and
     * identification are required; the other fields are optional.
     *
     * @param firstName             the first name
     * @param lastName              the last name
     * @param nationality           the ISO 3166-1 alpha-3 nationality
     * @param phoneNumber           the phone number
     * @param address               the postal address
     * @param identification        the identification document
     * @param gender                the gender (optional)
     * @param dateOfBirth           the date of birth, ISO 8601 date (optional)
     * @param placeOfBirth          the place of birth (optional)
     * @param occupation            the occupation (optional)
     * @param relationshipRecipient the relationship with the recipient (optional)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SenderDetails(String firstName, String lastName, String nationality, String phoneNumber,
                                Address address, Identification identification, PawapayGender gender,
                                String dateOfBirth, String placeOfBirth, String occupation,
                                PawapayRelationship relationshipRecipient) {

        /**
         * Creates a new builder.
         *
         * @return a new builder instance
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Builder for {@link SenderDetails}. Field values are validated when the remittance request is built.
         */
        public static final class Builder {
            private String firstName;
            private String lastName;
            private String nationality;
            private String phoneNumber;
            private Address address;
            private Identification identification;
            private PawapayGender gender;
            private String dateOfBirth;
            private String placeOfBirth;
            private String occupation;
            private PawapayRelationship relationshipRecipient;

            public Builder firstName(String firstName) {
                this.firstName = firstName;
                return this;
            }

            public Builder lastName(String lastName) {
                this.lastName = lastName;
                return this;
            }

            /**
             * @param nationality the ISO 3166-1 alpha-3 nationality (e.g. {@code USA})
             * @return this builder
             */
            public Builder nationality(String nationality) {
                this.nationality = nationality;
                return this;
            }

            public Builder phoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
                return this;
            }

            public Builder address(Address address) {
                this.address = address;
                return this;
            }

            public Builder address(String addressLine, String postalCode, String city, String country) {
                return address(new Address(addressLine, postalCode, city, country));
            }

            public Builder identification(Identification identification) {
                this.identification = identification;
                return this;
            }

            public Builder identification(PawapayIdentificationType type, String number) {
                return identification(new Identification(type, number));
            }

            public Builder gender(PawapayGender gender) {
                this.gender = gender;
                return this;
            }

            /**
             * @param dateOfBirth the date of birth as an ISO 8601 date (e.g. {@code 1977-12-31})
             * @return this builder
             */
            public Builder dateOfBirth(String dateOfBirth) {
                this.dateOfBirth = dateOfBirth;
                return this;
            }

            public Builder dateOfBirth(LocalDate dateOfBirth) {
                this.dateOfBirth = dateOfBirth != null ? dateOfBirth.toString() : null;
                return this;
            }

            public Builder placeOfBirth(String placeOfBirth) {
                this.placeOfBirth = placeOfBirth;
                return this;
            }

            public Builder occupation(String occupation) {
                this.occupation = occupation;
                return this;
            }

            public Builder relationshipRecipient(PawapayRelationship relationshipRecipient) {
                this.relationshipRecipient = relationshipRecipient;
                return this;
            }

            /**
             * Build the sender details.
             *
             * @return the sender details
             * @throws IllegalStateException if a required field is missing
             */
            public SenderDetails build() {
                requireSet(firstName, "Sender first name");
                requireSet(lastName, "Sender last name");
                requireSet(nationality, "Sender nationality");
                requireSet(phoneNumber, "Sender phone number");
                if (address == null) {
                    throw new IllegalStateException("Sender address is required");
                }
                if (identification == null) {
                    throw new IllegalStateException("Sender identification is required");
                }
                return new SenderDetails(firstName, lastName, nationality, phoneNumber, address, identification,
                    gender, dateOfBirth, placeOfBirth, occupation, relationshipRecipient);
            }

            private static void requireSet(String value, String field) {
                if (value == null || value.isBlank()) {
                    throw new IllegalStateException(field + " is required");
                }
            }
        }
    }
}
