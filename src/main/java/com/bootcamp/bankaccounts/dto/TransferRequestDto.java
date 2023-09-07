package com.bootcamp.bankaccounts.dto;

import lombok.Data;

@Data
public class TransferRequestDto {
	private String originAccount;
	private String destinationAccount;
	private Double amount;
}