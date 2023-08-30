package com.bootcamp.bankaccounts.clients;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.bootcamp.bankaccounts.clients.config.RestConfig;
import com.bootcamp.bankaccounts.dto.Transaction;

import reactor.core.publisher.Mono;

@Service
public class TransactionsRestClient {
    RestConfig restConfig = new RestConfig();
	
	public Mono<Transaction> createTransaction(Transaction transaction) {
		WebClient webClient = WebClient.create("http://localhost:8086");
        return  webClient.post()
                .uri("/transaction")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(transaction), Transaction.class)
                .retrieve()
                .bodyToMono(Transaction.class);
	}
}
