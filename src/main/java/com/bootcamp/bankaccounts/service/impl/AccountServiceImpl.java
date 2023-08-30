package com.bootcamp.bankaccounts.service.impl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootcamp.bankaccounts.clients.CustomerRestClient;
import com.bootcamp.bankaccounts.clients.TransactionsRestClient;
import com.bootcamp.bankaccounts.dto.AccountRequestDto;
import com.bootcamp.bankaccounts.dto.AccountResponseDto;
import com.bootcamp.bankaccounts.dto.Message;
import com.bootcamp.bankaccounts.dto.Transaction;
import com.bootcamp.bankaccounts.dto.TypeAccountDto;
import com.bootcamp.bankaccounts.entity.Account;
import com.bootcamp.bankaccounts.repository.AccountRepository;
import com.bootcamp.bankaccounts.service.AccountService;
import com.bootcamp.bankaccounts.util.TypeAccount;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class AccountServiceImpl implements AccountService{
	
	private final TypeAccount typeAccount = new TypeAccount();

	@Autowired
    private AccountRepository accountRepository;
	
	@Autowired
    CustomerRestClient customerRestClient;
	
	@Autowired
	TransactionsRestClient transactionRestClient;
	
	/**
	 * Obtiene todas las cuentas
	 * @return Flux<Account>
	 */
	@Override
	public Flux<Account> getAll() {
		return accountRepository.findAll();
	}

	/**
	 * Obtiene la cuenta por su id
	 * @param accountId
	 * @return Mono<Account>
	 */
	@Override
	public Mono<Account> getAccountById(String accountId) {
		return accountRepository.findById(accountId);
	}

	/**
	 * Registro de una cuenta para una persona
	 * Se obtiene los movimientos y mantenimioento según el tipo de cuenta en getTypeAccount()
	 * Se busca al cliente getPersonById() y se valida si ya tiene una cuenta en getAccountByIdCustomerPerson()
	 * Si no tiene cuenta se crea la cuenta saveNewAccount()
	 * @param accountRequestDto
	 * @return Mono<AccountResponseDto>
	 */
	@Override
	public Mono<AccountResponseDto> createAccountPerson(AccountRequestDto accountRequestDto) {
		TypeAccountDto newAccount = getTypeAccount(accountRequestDto.getTypeAccount());
		Account account = new Account(null,accountRequestDto.getCustomerId(),accountRequestDto.getTypeAccount(),newAccount.getType(), 0.00
				, newAccount.getMaintenance(), newAccount.getTransactions(), newAccount.getDayOperation()
				, accountRequestDto.getDateAccount(), accountRequestDto.getNumberAccount(), accountRequestDto.getTypeCustomer());
		return customerRestClient.getPersonById(accountRequestDto.getCustomerId()).flatMap(c ->{
			account.setTypeCustomer(c.getTypeCustomer());
			return getAccountByIdCustomerPerson(accountRequestDto.getCustomerId(),newAccount.getType()).flatMap(v -> {
				return Mono.just(new AccountResponseDto(null, "Personal client already has a bank account: "+newAccount.getType()));
			}).switchIfEmpty(saveNewAccount(account));
		}).defaultIfEmpty(new AccountResponseDto(null, "Client does not exist"));
	}
	
	/**
	 * Registro de una cuenta para una empresa
	 * Se busca al cliente getCompanyById()
	 * Se valida el tipo de cuenta y se crea la cuenta saveNewAccount()
	 * @param accountRequestDto
	 * @return Mono<AccountResponseDto>
	 */
	@Override
	public Mono<AccountResponseDto> createAccountCompany(AccountRequestDto accountRequestDto) {
		TypeAccountDto newAccount = getTypeAccount(accountRequestDto.getTypeAccount());
		Account account = new Account(null,accountRequestDto.getCustomerId(),accountRequestDto.getTypeAccount(),newAccount.getType(), 0.00
				, newAccount.getMaintenance(), newAccount.getTransactions(), newAccount.getDayOperation()
				, accountRequestDto.getDateAccount(), accountRequestDto.getNumberAccount(), accountRequestDto.getTypeCustomer());	
		return customerRestClient.getCompanyById(accountRequestDto.getCustomerId()).flatMap(c ->{
			account.setTypeCustomer(c.getTypeCustomer());
			if(newAccount.getType().equals("C_CORRIENTE")) {
				return saveNewAccount(account);
			}
			return Mono.just(new AccountResponseDto(null, "For company only type of account: C_CORRIENTE"));
		}).defaultIfEmpty(new AccountResponseDto(null, "Client does not exist"));
	}

	/**
	 * Actualización de una cuenta
	 * Se obtiene una cuenta por el id findById()
	 * Se guarda la cuenta save()
	 * @param accountRequestDto
	 * @return Mono<Account>
	 */
	@Override
	public Mono<Account> updateAccount(AccountRequestDto accountRequestDto) {
		return accountRepository.findById(accountRequestDto.getId())
                .flatMap(uAccount -> {
                	uAccount.setCustomerId(accountRequestDto.getCustomerId());
                	uAccount.setTypeAccount(accountRequestDto.getTypeAccount());
                	uAccount.setDescripTypeAccount(getTypeAccount(accountRequestDto.getTypeAccount()).getType());
                	uAccount.setAmount(accountRequestDto.getAmount());
                	uAccount.setMaintenance(accountRequestDto.getMaintenance());
                	uAccount.setTransaction(accountRequestDto.getTransaction());
                	uAccount.setOperationDay(accountRequestDto.getOperationDay());
                	uAccount.setDateAccount(accountRequestDto.getDateAccount());
                	uAccount.setNumberAccount(accountRequestDto.getNumberAccount());
                	uAccount.setTypeCustomer(accountRequestDto.getTypeCustomer());
                    return accountRepository.save(uAccount);
        });
	}

	/**
	 * Eliminación de una cuenta
	 * Se obtiene una cuenta por el id findById()
	 * Se elimina la cuenta deleteById()
	 * @param accountId
	 * @return Mono<Message>
	 */
	@Override
	public Mono<Message> deleteAccount(String accountId) {
		Message message = new Message("Account does not exist");
		return accountRepository.findById(accountId)
                .flatMap(dAccount -> {
                	message.setMessage("Account deleted successfully");
                	return accountRepository.deleteById(dAccount.getId()).thenReturn(message);
        }).defaultIfEmpty(message);
	}
	
	/**
	 * Depósito de una cuenta
	 * Se obtiene una cuenta por su id findById() y valida el numero de movimientos
	 * Se actualiza la cuenta con el deposito realizado save()
	 * Se registra la transacción registerTransaction()
	 * @param accountRequestDto
	 * @return Mono<AccountResponseDto>
	 */
	@Override
	public Mono<AccountResponseDto> depositAccount(AccountRequestDto accountRequestDto) {
		LocalDateTime myDateObj = LocalDateTime.now();
		return accountRepository.findById(accountRequestDto.getId()).filter(a -> a.getTransaction()>=0).flatMap(uAccount -> {
			if(uAccount.getTransaction() - 1 >= 0) {
				if(uAccount.getDescripTypeAccount().equals("PLAZO_FIJO") && uAccount.getOperationDay()!=myDateObj.getDayOfMonth()) {
					return Mono.just(new AccountResponseDto(null, "Day of the month not allowed for PLAZO_FIJO"));
				}
				uAccount.setAmount(uAccount.getAmount() + accountRequestDto.getAmount());
				uAccount.setTransaction(uAccount.getTransaction() - 1);
	            return accountRepository.save(uAccount).flatMap(account -> {
	            	return registerTransaction(uAccount, accountRequestDto.getAmount(),"DEPOSITO");
	            });
			}
			return Mono.just(new AccountResponseDto(null, "Exhausted monthly movements limit"));
        });
	}

	/**
	 * Retiro de una cuenta
	 * Se obtiene una cuenta por su id findById() y valida el numero de movimientos
	 * Se valida el monto a retirar y actualiza la cuenta con el retiro realizado save()
	 * Se registra la transacción registerTransaction()
	 * @param accountRequestDto
	 * @return Mono<AccountResponseDto>
	 */
	@Override
	public Mono<AccountResponseDto> withdrawalAccount(AccountRequestDto accountRequestDto) {
		LocalDateTime myDateObj = LocalDateTime.now();
		return accountRepository.findById(accountRequestDto.getId()).flatMap(uAccount -> {
			if(uAccount.getTransaction() - 1 >= 0) {
				Double amount = uAccount.getAmount() - accountRequestDto.getAmount();
				if(amount >= 0) {
					if(uAccount.getDescripTypeAccount().equals("PLAZO_FIJO") && uAccount.getOperationDay()!=myDateObj.getDayOfMonth()) {
						return Mono.just(new AccountResponseDto(null, "Day of the month not allowed for PLAZO_FIJO"));
					}
					uAccount.setAmount(amount);
					uAccount.setTransaction(uAccount.getTransaction() - 1);
		            return accountRepository.save(uAccount).flatMap(account -> {
		            	return registerTransaction(uAccount, accountRequestDto.getAmount(),"RETIRO");
		            });
				}
				return Mono.just(new AccountResponseDto(null, "You don't have enough balance"));
			}
			return Mono.just(new AccountResponseDto(null, "Exhausted monthly movements limit"));
        });
	}

	/**
	 * Obtiene todas la cuentas por el id del cliente
	 * @param customerId
	 * @return Flux<Account>
	 */
	@Override
	public Flux<Account> getAllAccountXCustomerId(String customerId) {
		return accountRepository.findAll()
				.filter(c -> c.getCustomerId().equals(customerId));
	}
	
	/**
	 * Reiniciar el numero de movimientos de las cuentas
	 * Actualiza los movimientos permitidos a todas las cuentas en updateTransaction(); 
	 * @return Mono<Message>
	 */
	@Override
	public Mono<Message> restartTransactions() {
		return updateTransaction().collectList().flatMap(c -> {
			return Mono.just(new Message("The number of transactions of the accounts was satisfactorily restarted"));
		});
	}
	
	/**
	 * Obtiene nombre, movimientos y mantenimiento según el tipo de cuenta
	 * @param idType
	 * @return TypeAccountDto
	 */
	private TypeAccountDto getTypeAccount(Integer idType) {
		Predicate<TypeAccountDto> p = f -> f.getId()==idType;
		TypeAccountDto type = typeAccount.getAccounts().filter(p).collect(Collectors.toList()).get(0);
		return type;
    }

	/**
	 * Obtiene las cuentas según el id del cliente y tipo de cuenta
	 * @param customerId
	 * @param type
	 * @return Mono<Account>
	 */
	private Mono<Account> getAccountByIdCustomerPerson(String customerId, String type) {
		return  accountRepository.findAll()
				.filter(c -> c.getCustomerId().equals(customerId))
				.filter(c -> c.getDescripTypeAccount().equals(type))
				.next();
	}
	
	/**
	 * Guarda una cuenta
	 * @param account
	 * @return Mono<AccountResponseDto>
	 */
	private Mono<AccountResponseDto> saveNewAccount(Account account) {
		return accountRepository.save(account).flatMap(x -> {
			return Mono.just(new AccountResponseDto(account, "Account created successfully"));
		});
	}
	
	/**
	 * Registra una transacción
	 * @param uAccount
	 * @param amount
	 * @param typeTransaction
	 * @return Mono<AccountResponseDto>
	 */
	private Mono<AccountResponseDto> registerTransaction(Account uAccount, Double amount, String typeTransaction){
		Transaction transaction = new Transaction();
		transaction.setCustomerId(uAccount.getCustomerId());
		transaction.setProductId(uAccount.getId());
		transaction.setProductType(uAccount.getDescripTypeAccount());
		transaction.setTransactionType(typeTransaction);
		transaction.setAmount(amount);
		transaction.setTransactionDate(new Date());
		transaction.setCustomerType(uAccount.getTypeCustomer());
		return transactionRestClient.createTransaction(transaction).flatMap(t -> {
			return Mono.just(new AccountResponseDto(uAccount, "Successful transaction"));
        });
	}
	
	/**
	 * Obtiene todas las transacciónes y actualiza el numero de transacciones permitidas
	 * @return Flux<Account>
	 */
	private Flux<Account> updateTransaction(){
		return  accountRepository.findAll()
				.flatMap(c -> {
					c.setTransaction(getTypeAccount(c.getTypeAccount()).getTransactions());
					return accountRepository.save(c);
				});
	}
	
}
