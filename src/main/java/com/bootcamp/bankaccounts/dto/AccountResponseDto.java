package com.bootcamp.bankaccounts.dto;

import com.bootcamp.bankaccounts.entity.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class AccountResponseDto {
	private String message;
	private Account account;
}
