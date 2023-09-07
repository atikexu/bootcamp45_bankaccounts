package com.bootcamp.bankaccounts.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="account")
public class Account {
	@Id
	private String id;
	private String customerId;
	private Integer typeAccount;
	private String descripTypeAccount;
	private Double amount;
	private Double startAmount;
	private Double maintenance;
	private Integer transaction;
	private Integer operationDay;
	private LocalDateTime dateAccount;
	private String numberAccount;
	private String typeCustomer;
	private Double commission;
	private Integer indTransaction;
}
