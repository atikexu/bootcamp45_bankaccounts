package com.bootcamp.bankaccounts.service;

import com.bootcamp.bankaccounts.dto.AccountRequestDto;
import com.bootcamp.bankaccounts.dto.AccountResponseDto;
import com.bootcamp.bankaccounts.dto.Message;
import com.bootcamp.bankaccounts.entity.Account;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

	Flux<Account> getAll();

	Mono<Account> getAccountById(String accountId);

	Mono<AccountResponseDto> createAccountPerson(AccountRequestDto accountRequestDto);
	
	Mono<AccountResponseDto> createAccountCompany(AccountRequestDto accountRequestDto);

	Mono<Account> updateAccount(AccountRequestDto accountRequestDto);

	Mono<Message> deleteAccount(String accountId);
	
	Mono<AccountResponseDto> depositAccount(AccountRequestDto accountRequestDto);
	
	Mono<AccountResponseDto> withdrawalAccount(AccountRequestDto accountRequestDto);
	
	Flux<Account> getAllAccountXCustomerId(String customerId);
	
	Mono<Message> restartTransactions();

}
