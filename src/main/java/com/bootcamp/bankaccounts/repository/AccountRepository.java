package com.bootcamp.bankaccounts.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.bootcamp.bankaccounts.entity.Account;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

}
