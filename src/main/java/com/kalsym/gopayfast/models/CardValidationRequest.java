/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.gopayfast.models;

import lombok.AllArgsConstructor;
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
public class CardValidationRequest extends ValidationRequest {

    String cardNumber;
    String expiryMonth;
    String expiryYear;
    String cvv;

    public CardValidationRequest(
            String basketId,
            String transactionAmount,
            String orderDate,
            String customerMobileNo,
            String customerEmailAddress,
            String accountTypeId,
            String merCatCode,
            String cardNumber,
            String expiryMonth,
            String expiryYear,
            String cvv
    ) {
        super(basketId, transactionAmount, orderDate, customerMobileNo, customerEmailAddress, accountTypeId, merCatCode);

        this.cardNumber = cardNumber;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
    }
}
