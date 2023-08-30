package com.bootcamp.bankaccounts.dto;

import java.util.Date;

import lombok.Data;

@Data
public class Transaction {
	private String id;
	private String productType; //AHORRO, C_CORRIENTE, PLAZO_FIJO, CRE_PERSONAL, CRED_EMPRESARIAL, TAR_CRED_PERSONAL, TAR_CRED_EMPRESARIAL
	private String productId;
	private String customerId;
	private String transactionType; //DEPOSITO, RETIRO, PAGO, CONSUMO
	private Double amount;
	private Date transactionDate;
	private String customerType;
}
