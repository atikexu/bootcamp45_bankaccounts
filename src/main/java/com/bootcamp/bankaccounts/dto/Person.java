package com.bootcamp.bankaccounts.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Person {
	private String id;
	private String name;
	private String lastName;
	private String dni;
	private String email;
	private String telephone;
	private String typeCustomer;
}
