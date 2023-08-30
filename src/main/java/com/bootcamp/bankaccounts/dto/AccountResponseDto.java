package com.bootcamp.bankaccounts.dto;

import com.bootcamp.bankaccounts.entity.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class AccountResponseDto {
	private Account account;
	private String message;
}
