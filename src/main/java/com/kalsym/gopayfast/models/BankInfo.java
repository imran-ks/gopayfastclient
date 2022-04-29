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
public class BankInfo {

    @JsonProperty("bank_code")
    String bankCode;
    String name;
    @JsonProperty("is_slab")
    boolean isSlab;
    @JsonProperty("allow_non_islamic")
    boolean allowNonIslamic;
}
