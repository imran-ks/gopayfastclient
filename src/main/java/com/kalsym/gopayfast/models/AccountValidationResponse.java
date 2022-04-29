/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.gopayfast.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author imran
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountValidationResponse {

    @JsonProperty("code")
    String code;
    @JsonProperty("message")
    String message;
    @JsonProperty("data_3ds_acsurl")
    String data3dsAcsurl;
    @JsonProperty("data_3ds_pareq")
    String data3dsPareq;
    @JsonProperty("data_3ds_html")
    String data3dsHtml;
    @JsonProperty("data_3ds_secureid")
    String data3dsSecureid;
    @JsonProperty("data_3ds_gatewayrecommendation")
    String data3dsGatewayRecommendation;
    @JsonProperty("transaction_id")
    String transactionId;
}
