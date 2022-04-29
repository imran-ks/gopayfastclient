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
public class Transaction {

    @JsonProperty("status_code")
    String statusCode;
    @JsonProperty("status_msg")
    String statusMessage;
    @JsonProperty("rdv_message_key")
    String rdvMessageKey;
    @JsonProperty("basket_id")
    String basketId;
    @JsonProperty("transaction_id")
    String transactionId;
    @JsonProperty("code")
    String code;
}
