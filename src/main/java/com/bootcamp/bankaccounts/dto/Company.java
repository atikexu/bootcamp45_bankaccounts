package com.bootcamp.bankaccounts.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Company {
	private String id;
	private String businessName;
	private String ruc;
	private String email;
	private String telephone;
	private List<AuthorizedDto> headlines;
	private List<AuthorizedDto> signatories;
	private String typeCustomer;
}
