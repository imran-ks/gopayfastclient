/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.gopayfast.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author imran
 */
@Getter
@Setter
@ToString
public class BankTransactionRequest extends TransactionRequest {
    
    String bankCode;
    String accountNumber;
    String cnicNumber;

    public BankTransactionRequest (
            String basketId,
            String transactionAmount,
            String orderDate,
            String customerMobileNo,
            String customerEmailAddress,
            String accountTypeId,
            String merCatCode,
            String transactionId,
            String eci,
            String bankCode,
            String accountNumber,
            String cnicNumber
    ) {
        super(basketId, transactionAmount, orderDate, customerMobileNo, customerEmailAddress, accountTypeId, merCatCode, transactionId, eci);

        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.cnicNumber = cnicNumber;
    }
}
