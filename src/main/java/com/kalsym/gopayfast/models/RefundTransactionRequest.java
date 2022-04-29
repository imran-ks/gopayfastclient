/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.gopayfast.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author imran
 */
@Data
@AllArgsConstructor
public class RefundTransactionRequest {
    String transactionId;
    String transactionAmount;
    String refundReason;
}
