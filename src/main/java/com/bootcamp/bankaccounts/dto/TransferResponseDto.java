package com.bootcamp.bankaccounts.dto;

import com.bootcamp.bankaccounts.entity.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TransferResponseDto {
	private String message;
	private List<Account> account;
}