package com.bootcamp.bankaccounts.clients;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.bootcamp.bankaccounts.clients.config.RestConfig;
import com.bootcamp.bankaccounts.dto.Company;
import com.bootcamp.bankaccounts.dto.Person;

import reactor.core.publisher.Mono;

@Service
public class CustomerRestClient {
    RestConfig restConfig = new RestConfig();
	
	public Mono<Person> getPersonById(String id) {
		WebClient webClient = WebClient.create("http://localhost:8083");
        return  webClient.get()
                .uri("/person/"+id)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(Person.class);
	}
	
	public Mono<Company> getCompanyById(String id) {
		WebClient webClient = WebClient.create("http://localhost:8083");
        return  webClient.get()
                .uri("/company/"+id)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(Company.class);
	}
}
