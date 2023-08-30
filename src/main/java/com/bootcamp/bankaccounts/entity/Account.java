package com.bootcamp.bankaccounts.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	@JsonIgnore
	private Integer typeAccount;
	private String descripTypeAccount;
	private Double amount;
	private Double maintenance;
	private Integer transaction;
	private Integer operationDay;
	private Date dateAccount;
	private String NumberAccount;
	private String typeCustomer;
	
}
