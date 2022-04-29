/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.gopayfast.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalsym.gopayfast.models.AccountValidationResponse;
import com.kalsym.gopayfast.models.BankValidationRequest;
import com.kalsym.gopayfast.models.BankQueryResponse;
import com.kalsym.gopayfast.models.BankTransactionRequest;
import com.kalsym.gopayfast.models.CardTransactionRequest;
import com.kalsym.gopayfast.models.CardValidationRequest;
import com.kalsym.gopayfast.models.ValidationRequest;
import com.kalsym.gopayfast.models.PaymentTypesResponse;
import com.kalsym.gopayfast.models.RefundTransactionRequest;
import com.kalsym.gopayfast.models.RefundTransactionResponse;
import com.kalsym.gopayfast.models.TokenResponse;
import com.kalsym.gopayfast.models.TransactionRequest;
import com.kalsym.gopayfast.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 *
 * @author imran
 */
@Service
public class GoPayFastClient {

    private static WebClient gpfClient;
    private static final Logger logger = LoggerFactory.getLogger(GoPayFastClient.class);

    private final String BASE_URL;
    private final String MERCHANT_ID;
    private final String API_KEY;

    private String accessToken;
    private String refreshToken;

    public GoPayFastClient(
            @Value("${gopayfast.baseurl}") final String BASE_URL,
            @Value("${gopayfast.merchantid}") final String MERCHANT_ID,
            @Value("${gopayfast.apikey}") final String API_KEY
    ) {
        this.BASE_URL = BASE_URL;
        this.MERCHANT_ID = MERCHANT_ID;
        this.API_KEY = API_KEY;

        initWebClient();
//        getAccessToken();

    }

    public TokenResponse getAccessToken() {
        initWebClient();
        TokenResponse response = new TokenResponse();

        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();

        bodyValues.add("merchant_id", MERCHANT_ID);
        bodyValues.add("grant_type", "client_credentials");
        bodyValues.add("secured_key", API_KEY);

        logger.info("Requesting access token with merchant_id: {} and secret api key", MERCHANT_ID);
        try {
            response = gpfClient.post()
                    .uri("/token")
                    .body(BodyInserters.fromFormData(bodyValues))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();
            logger.info("Token received successfully. {}", response);
        } catch (WebClientResponseException we) {
            logger.error("Error getting access token, Status Code: {}, {}, Response Body: {}",
                    we.getStatusCode(), we.getLocalizedMessage(), we.getResponseBodyAsString());
            ObjectMapper mapper = new ObjectMapper();
            try {
                response = mapper.readValue(we.getResponseBodyAsString(), TokenResponse.class);
                logger.error("Error body: {}, {}", response.getCode(), response.getMessage());
            } catch (JsonProcessingException jsonEx) {
                logger.error("Error parsing JSONString : {}", jsonEx.getLocalizedMessage());
            }
        }
        return response;
    }

    public TokenResponse refreshAccessToken() {
        initWebClient();
        TokenResponse response = new TokenResponse();
        if (refreshToken == null) {
            response = getAccessToken();

            if (response.getToken() != null) {
                updateTokens(response.getToken(), response.getRefreshToken());
            }
            return response;
        }

        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("grant_type", "refresh_token");
        bodyValues.add("refresh_token", refreshToken);

        logger.info("Refreshing access token using merchant_id: {}, secret api key, existing access token {} and refreshToken {}",
                MERCHANT_ID, accessToken, bodyValues.get("refresh_token"));
        try {
            response = gpfClient.post()
                    .uri("/refreshtoken")
                    .headers(h -> h.setBearerAuth(accessToken))
                    .body(BodyInserters.fromFormData(bodyValues))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();
            updateTokens(response.getToken(), response.getRefreshToken());
            logger.info("Token refreshed successfully. {}", response);
        } catch (WebClientResponseException we) {
            logger.error("Error refreshing token, Status Code: {}, {}, Response Body: {}",
                    we.getStatusCode(), we.getLocalizedMessage(), we.getResponseBodyAsString());
            ObjectMapper mapper = new ObjectMapper();
            try {
                response = mapper.readValue(we.getResponseBodyAsString(), TokenResponse.class);
                logger.error("Error body: {}, {}", response.getCode(), response.getMessage());
            } catch (JsonProcessingException jsonEx) {
                logger.error("Error parsing JSONString : {}", jsonEx.getLocalizedMessage());
            }
        }
        return response;
    }

    public BankQueryResponse getBanksOrIssuers() {
        BankQueryResponse response = new BankQueryResponse();
        if (refreshAccessToken().getToken() == null) {
            return response;
        }

        logger.info("Querying issuers and banks");
        try {
            response = gpfClient.get()
                    .uri("/list/banks")
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(BankQueryResponse.class)
                    .block();
            logger.info("Banks received successfully. {}", response);
        } catch (WebClientResponseException we) {
            logger.error("Error querying banks, Status Code: {}, {}, Response Body: {}",
                    we.getStatusCode(), we.getLocalizedMessage(), we.getResponseBodyAsString());
            ObjectMapper mapper = new ObjectMapper();
            try {
                response = mapper.readValue(we.getResponseBodyAsString(), BankQueryResponse.class);
                logger.error("Error body: {}, {}", response.getCode(), response.getMessage());
            } catch (JsonProcessingException jsonEx) {
                logger.error("Error parsing JSONString : {}", jsonEx.getLocalizedMessage());
            }
        }
        return response;
    }

    public PaymentTypesResponse getPaymentTypesByBank(String bankCode) {
        PaymentTypesResponse response = new PaymentTypesResponse();

        if (refreshAccessToken().getToken() == null) {
            return response;
        }

        logger.info("Querying payment types for bank code " + bankCode);
        try {
            response = gpfClient.get()
                    .uri(uriBuilder
                            -> uriBuilder.path("/list/instruments")
                            .queryParam("bank_code", bankCode)
                            .build())
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(PaymentTypesResponse.class)
                    .block();
            logger.info("Payment types received successfully. {}", response);
        } catch (WebClientResponseException we) {
            logger.error("Error querying payment types, Status Code: {}, {}, Response Body: {}",
                    we.getStatusCode(), we.getLocalizedMessage(), we.getResponseBodyAsString());
            ObjectMapper mapper = new ObjectMapper();
            try {
                response = mapper.readValue(we.getResponseBodyAsString(), PaymentTypesResponse.class);
                logger.error("Error body: {}, {}", response.getCode(), response.getMessage());
            } catch (JsonProcessingException jsonEx) {
                logger.error("Error parsing JSONString : {}", jsonEx.getLocalizedMessage());
            }
        }
        return response;
    }

    public PaymentTypesResponse getBanksByPaymentType(String instrumentId) {
        PaymentTypesResponse response = new PaymentTypesResponse();
        if (refreshAccessToken().getToken() == null) {
            return response;
        }

        logger.info("Querying banks with payment type id" + instrumentId);
        try {
            response = gpfClient.get()
                    .uri(uriBuilder
                            -> uriBuilder.path("/list/instrumentbanks")
                            .queryParam("instrument_id", instrumentId)
                            .build())
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(PaymentTypesResponse.class)
                    .block();
            logger.info("Payment types received successfully. {}", response);
        } catch (WebClientResponseException we) {
            logger.error("Error querying payment types, Status Code: {}, {}, Response Body: {}",
                    we.getStatusCode(), we.getLocalizedMessage(), we.getResponseBodyAsString());
            ObjectMapper mapper = new ObjectMapper();
            try {
                response = mapper.readValue(we.getResponseBodyAsString(), PaymentTypesResponse.class);
                logger.error("Error body: {}, {}", response.getCode(), response.getMessage());
            } catch (JsonProcessingException jsonEx) {
                logger.error("Error parsing JSONString : {}", jsonEx.getLocalizedMessage());
            }
        }
        return response;
    }

    public AccountValidationResponse validateCustomerAccountByCard(CardValidationRequest request) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("card_number", request.getCardNumber());
        bodyValues.add("expiry_month", request.getExpiryMonth());
        bodyValues.add("expiry_year", request.getExpiryYear());
        bodyValues.add("cvv", request.getCvv());

        return validateCustomerAccount(request, bodyValues);
    }

    public AccountValidationResponse validateCustomerAccountByBank(BankValidationRequest request) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("bank_code", request.getBankCode());
        bodyValues.add("account_number", request.getAccountNumber());
        bodyValues.add("cnic_number", request.getCnicNumber());

        return validateCustomerAccount(request, bodyValues);
    }

    private AccountValidationResponse validateCustomerAccount(ValidationRequest request,
            MultiValueMap<String, String> bodyValues) {
        AccountValidationResponse response = new AccountValidationResponse();
        if (refreshAccessToken().getToken() == null) {
            return response;
        }

        bodyValues.add("basket_id", request.getBasketId());
        bodyValues.add("txnamt", request.getTransactionAmount());
        bodyValues.add("order_date", request.getOrderDate());
        bodyValues.add("customer_mobile_no", request.getCustomerMobileNo());
        bodyValues.add("customer_email_address", request.getCustomerEmailAddress());
        bodyValues.add("account_type_id", request.getAccountTypeId());
        bodyValues.add("merCatCode", request.getMerCatCode());

        logger.info("Validating customer account");
        try {
            response = gpfClient.post()
                    .uri("/customer/validate")
                    .headers(h -> h.setBearerAuth(accessToken))
                    .body(BodyInserters.fromFormData(bodyValues))
                    .retrieve()
                    .bodyToMono(AccountValidationResponse.class)
                    .block();
            logger.info("Token received successfully. {}", response);
        } catch (WebClientResponseException we) {
            logger.error("Error getting access token, Status Code: {}, {}, Response Body: {}",
                    we.getStatusCode(), we.getLocalizedMessage(), we.getResponseBodyAsString());
            ObjectMapper mapper = new ObjectMapper();
            try {
                response = mapper.readValue(we.getResponseBodyAsString(), AccountValidationResponse.class);
                logger.error("Error body: {}, {}", response.getCode(), response.getMessage());
            } catch (JsonProcessingException jsonEx) {
                logger.error("Error parsing JSONString : {}", jsonEx.getLocalizedMessage());
            }
        }
        return response;
    }

    public Transaction initiateBankTransaction(BankTransactionRequest request) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("bank_code", request.getBankCode());
        bodyValues.add("account_number", request.getAccountNumber());
        bodyValues.add("cnic_number", request.getCnicNumber());

        return initiateTransaction(request, bodyValues);
    }

    public Transaction initiateCardTransaction(CardTransactionRequest request) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("card_number", request.getCardNumber());
        bodyValues.add("expiry_month", request.getExpiryMonth());
        bodyValues.add("expiry_year", request.getExpiryYear());
        bodyValues.add("cvv", request.getCvv());

        return initiateTransaction(request, bodyValues);
    }

    private Transaction initiateTransaction(TransactionRequest request,
            MultiValueMap<String, String> bodyValues) {
        Transaction response = new Transaction();
        if (refreshAccessToken().getToken() == null) {
            return response;
        }

        bodyValues.add("basket_id", request.getBasketId());
        bodyValues.add("txnamt", request.getTransactionAmount());
        bodyValues.add("order_date", request.getOrderDate());
        bodyValues.add("customer_mobile_no", request.getCustomerMobileNo());
        bodyValues.add("customer_email_address", request.getCustomerEmailAddress());
        bodyValues.add("account_type_id", request.getAccountTypeId());
        bodyValues.add("merCatCode", request.getMerCatCode());
        bodyValues.add("transaction_id", request.getTransactionId());
        bodyValues.add("eci", request.getEci());

        logger.info("Validating customer account");
        try {
            response = gpfClient.post()
                    .uri("/transaction")
                    .headers(h -> h.setBearerAuth(accessToken))
                    .body(BodyInserters.fromFormData(bodyValues))
                    .retrieve()
                    .bodyToMono(Transaction.class)
                    .block();
            logger.info("Transaction completed successfully. {}", response);
        } catch (WebClientResponseException we) {
            logger.error("Error initiating transaction, Status Code: {}, {}, Response Body: {}",
                    we.getStatusCode(), we.getLocalizedMessage(), we.getResponseBodyAsString());
            ObjectMapper mapper = new ObjectMapper();
            try {
                response = mapper.readValue(we.getResponseBodyAsString(), Transaction.class);
            } catch (JsonProcessingException jsonEx) {
                logger.error("Error parsing JSONString : {}", jsonEx.getLocalizedMessage());
            }
        }
        return response;
    }

    public RefundTransactionResponse refundTransaction(RefundTransactionRequest request) {
        RefundTransactionResponse response = new RefundTransactionResponse();
        if (refreshAccessToken().getToken() == null) {
            return response;
        }

        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("txnamt", request.getTransactionAmount());
        bodyValues.add("refund_reason", request.getRefundReason());

        logger.info("Requesting refund for transaction {}", request.getTransactionId());
        try {
            response = gpfClient.post()
                    .uri("/transaction/refund/" + request.getTransactionId())
                    .headers(h -> h.setBearerAuth(accessToken))
                    .body(BodyInserters.fromFormData(bodyValues))
                    .retrieve()
                    .bodyToMono(RefundTransactionResponse.class)
                    .block();
            logger.info("Transaction refunded successfully. Response body: {}", response);
        } catch (WebClientResponseException we) {
            logger.error("Error refunding transaction, Status Code: {}, {}, Response Body: {}",
                    we.getStatusCode(), we.getLocalizedMessage(), we.getResponseBodyAsString());
            ObjectMapper mapper = new ObjectMapper();
            try {
                response = mapper.readValue(we.getResponseBodyAsString(), RefundTransactionResponse.class);
            } catch (JsonProcessingException jsonEx) {
                logger.error("Error parsing JSONString : {}", jsonEx.getLocalizedMessage());
            }
        }
        return response;
    }

    public Transaction getTransactionById(String transactionId) {
        logger.info("Getting transaction status using transactionId {}", transactionId);
        return getTransactionStatus("/transaction/" + transactionId);
    }

    public Transaction getTransactionByBasketId(String basketId) {
        logger.info("Getting transaction status using basketId {}", basketId);
        return getTransactionStatus("/transaction/basket_id/" + basketId);
    }

    private Transaction getTransactionStatus(String endpoint) {
        Transaction response = new Transaction();
        if (refreshAccessToken().getToken() == null) {
            return response;
        }

        try {
            response = gpfClient.get()
                    .uri(endpoint)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(Transaction.class)
                    .block();
            logger.info("Received transaction status. {}", response);
        } catch (WebClientResponseException we) {
            logger.error("Error getting transaction status, Status Code: {}, {}, Response Body: {}",
                    we.getStatusCode(), we.getLocalizedMessage(), we.getResponseBodyAsString());
            ObjectMapper mapper = new ObjectMapper();
            try {
                response = mapper.readValue(we.getResponseBodyAsString(), Transaction.class);
            } catch (JsonProcessingException jsonEx) {
                logger.error("Error parsing JSONString : {}", jsonEx.getLocalizedMessage());
            }
        }
        return response;
    }

    private void initWebClient() {
        if (gpfClient == null) {
            logger.info("Building GoPayFast WebClient object");
            gpfClient = WebClient.builder()
                    .baseUrl(BASE_URL)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .filters(exchangeFilterFunctions -> {
                        exchangeFilterFunctions.add(logRequest());
                        exchangeFilterFunctions.add(logResponse());
                    })
                    .build();
        }
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Authorization: ")
                    .append(clientRequest.headers().get(HttpHeaders.AUTHORIZATION)).append("\n");
            sb.append(clientRequest.method())
                    .append(" ").append(clientRequest.url()).append("\n");
            logger.info("Request info: {}", sb.toString());
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.info("Response statusCode: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    private void updateTokens(String token, String refreshToken) {
        this.accessToken = token;
        this.refreshToken = refreshToken;
    }
}
