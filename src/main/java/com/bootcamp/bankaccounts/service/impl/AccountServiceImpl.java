package com.bootcamp.bankaccounts.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootcamp.bankaccounts.clients.CustomerRestClient;
import com.bootcamp.bankaccounts.clients.TransactionsRestClient;
import com.bootcamp.bankaccounts.dto.AccountRequestDto;
import com.bootcamp.bankaccounts.dto.AccountResponseDto;
import com.bootcamp.bankaccounts.dto.Message;
import com.bootcamp.bankaccounts.dto.Transaction;
import com.bootcamp.bankaccounts.dto.TransferRequestDto;
import com.bootcamp.bankaccounts.dto.TransferResponseDto;
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
		Account account = new Account(null,accountRequestDto.getCustomerId(),accountRequestDto.getTypeAccount(),newAccount.getType(), accountRequestDto.getAmount()
				, accountRequestDto.getAmount(), newAccount.getMaintenance(), accountRequestDto.getTransaction(), newAccount.getDayOperation()
				, LocalDateTime.now(), accountRequestDto.getNumberAccount(), accountRequestDto.getTypeCustomer(), newAccount.getCommission(), 1);
		AccountResponseDto accountResponseDto = new AccountResponseDto(null, account);
		return validateTransactions(accountResponseDto)
				.flatMap(validate -> validate.getAccount() == null
					? Mono.just(validate)
					: customerRestClient.getPersonById(accountRequestDto.getCustomerId()).flatMap(c ->{
						account.setTypeCustomer(c.getTypeCustomer());
						account.setIndTransaction(1);
						if (account.getTransaction()==0) account.setIndTransaction(0);
						return getAccountByIdCustomerPerson(accountRequestDto.getCustomerId(),newAccount.getType()).flatMap(v -> {
							return Mono.just(new AccountResponseDto("Personal client already has a bank account: "+newAccount.getType(), null));
						}).switchIfEmpty(saveNewAccount(account));
					}).defaultIfEmpty(new AccountResponseDto("Client does not exist", null))
				);
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
		Account account = new Account(null,accountRequestDto.getCustomerId(),accountRequestDto.getTypeAccount(),newAccount.getType(), accountRequestDto.getAmount()
				, accountRequestDto.getAmount(), newAccount.getMaintenance(), accountRequestDto.getTransaction(), newAccount.getDayOperation()
				, LocalDateTime.now(), accountRequestDto.getNumberAccount(), accountRequestDto.getTypeCustomer(), newAccount.getCommission(), 1);
		AccountResponseDto accountResponseDto = new AccountResponseDto(null, account);
		return validateTransactions(accountResponseDto)
				.flatMap(validate -> validate.getAccount() == null
					? Mono.just(validate)
					: customerRestClient.getCompanyById(accountRequestDto.getCustomerId()).flatMap(c ->{
						account.setTypeCustomer(c.getTypeCustomer());
						account.setIndTransaction(1);
						if (account.getTransaction()==0) account.setIndTransaction(0);
						if(newAccount.getType().equals("C_CORRIENTE")) {
							return saveNewAccount(account);
						}
						return Mono.just(new AccountResponseDto("For company only type of account: C_CORRIENTE", null));
					}).defaultIfEmpty(new AccountResponseDto("Client does not exist", null))
				);
	}

	/**
	 * Actualización de una cuenta
	 * Se obtiene una cuenta por el id findById()
	 * Se guarda la cuenta save()
	 * @param accountRequestDto
	 * @return Mono<Account>
	 */
	@Override
	public Mono<AccountResponseDto> updateAccount(AccountRequestDto accountRequestDto) {
		return accountRepository.findById(accountRequestDto.getId())
                .flatMap(uAccount -> {
                	uAccount.setCustomerId(accountRequestDto.getCustomerId());
                	uAccount.setTypeAccount(accountRequestDto.getTypeAccount());
                	uAccount.setDescripTypeAccount(getTypeAccount(accountRequestDto.getTypeAccount()).getType());
                	uAccount.setAmount(accountRequestDto.getAmount());
                	uAccount.setMaintenance(accountRequestDto.getMaintenance());
                	uAccount.setTransaction(accountRequestDto.getTransaction());
                	uAccount.setOperationDay(accountRequestDto.getOperationDay());
                	uAccount.setNumberAccount(accountRequestDto.getNumberAccount());
                	uAccount.setTypeCustomer(accountRequestDto.getTypeCustomer());
                	uAccount.setIndTransaction(accountRequestDto.getTransaction() > 0 ? 1 : 0);
                	AccountResponseDto accountResponseDto = new AccountResponseDto(null, uAccount);
                	return validateTransactions(accountResponseDto)
            				.flatMap(validate -> validate.getAccount() == null
            				? Mono.just(validate)
            				: accountRepository.save(uAccount)
            					.map(savedAccount -> new AccountResponseDto(validate.getMessage(), savedAccount)));
        }).switchIfEmpty(Mono.just(new AccountResponseDto("Account does not exist", null)));
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
		return accountRepository.findById(accountRequestDto.getId()).flatMap(uAccount -> {
			Double amount = uAccount.getAmount() + accountRequestDto.getAmount();
			Double commission = uAccount.getTransaction() > 0 ? (0.0):(uAccount.getCommission());
			if(amount < uAccount.getCommission()) return Mono.just(new AccountResponseDto("The operation cannot be carried out for an account amount less than the commission", null));
			if(uAccount.getDescripTypeAccount().equals("PLAZO_FIJO") && uAccount.getOperationDay() != LocalDateTime.now().getDayOfMonth()) {
				return Mono.just(new AccountResponseDto("Day of the month not allowed for PLAZO_FIJO", null));
			}
			uAccount.setAmount(amount-commission);
			uAccount.setIndTransaction(uAccount.getTransaction() - 1 >= 0 ? 1 : 0);
			uAccount.setTransaction(uAccount.getTransaction() > 0 ? (uAccount.getTransaction() - 1) : (0));
            return accountRepository.save(uAccount).flatMap(account -> {
            	return registerTransaction(uAccount, accountRequestDto.getAmount(),"DEPOSITO", uAccount.getIndTransaction() == 1 ? 0:uAccount.getCommission());
            });
        }).defaultIfEmpty(new AccountResponseDto("Account does not exist", null));
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
		return accountRepository.findById(accountRequestDto.getId()).flatMap(uAccount -> {
			Double amount = uAccount.getAmount() - accountRequestDto.getAmount();
			Double commission = uAccount.getTransaction() > 0 ? (0.0):(uAccount.getCommission());
			if(amount >= 0) {
				if(uAccount.getDescripTypeAccount().equals("PLAZO_FIJO") && uAccount.getOperationDay()!=LocalDateTime.now().getDayOfMonth()) {
					return Mono.just(new AccountResponseDto("Day of the month not allowed for PLAZO_FIJO", null));
				}
				if(amount < uAccount.getCommission()) return Mono.just(new AccountResponseDto("The operation cannot be carried out for an account amount less than the commission", null));
				uAccount.setAmount(amount-commission);
				uAccount.setIndTransaction(uAccount.getTransaction() - 1 >= 0 ? 1 : 0);
				uAccount.setTransaction(uAccount.getTransaction()>0?(uAccount.getTransaction() - 1):(0));
	            return accountRepository.save(uAccount).flatMap(account -> {
	            	return registerTransaction(uAccount, accountRequestDto.getAmount(),"RETIRO", uAccount.getIndTransaction() == 1? 0 : uAccount.getCommission());
	            });
			}
			return Mono.just(new AccountResponseDto("You don't have enough balance", null));
        }).defaultIfEmpty(new AccountResponseDto("Account does not exist", null));
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
	 * Obtiene nombre, movimientos y mantenimiento según el tipo de cuenta
	 * @param idType
	 * @return TypeAccountDto
	 */
	private TypeAccountDto getTypeAccount(Integer idType) {
		Predicate<TypeAccountDto> p = f -> f.getId().equals(idType);
		return typeAccount.getAccounts().filter(p).toList().get(0);
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
			return registerTransaction(account, account.getAmount(),"APERTURA", 0.0).flatMap(t1 -> {
				return Mono.just(new AccountResponseDto("Account created successfully", x));
			});
		});
	}
	
	/**
	 * Registra una transacción
	 * @param uAccount
	 * @param amount
	 * @param typeTransaction
	 * @return Mono<AccountResponseDto>
	 */
	private Mono<AccountResponseDto> registerTransaction(Account uAccount, Double amount, String typeTransaction, Double commission){
		Transaction transaction = new Transaction();
		transaction.setCustomerId(uAccount.getCustomerId());
		transaction.setProductId(uAccount.getId());
		transaction.setProductType(uAccount.getDescripTypeAccount());
		transaction.setTransactionType(typeTransaction);
		transaction.setAmount(amount);
		transaction.setTransactionDate(LocalDateTime.now());
		transaction.setCustomerType(uAccount.getTypeCustomer());
		transaction.setBalance(uAccount.getAmount());
		return transactionRestClient.createTransaction(transaction).flatMap(t -> {
			if(commission>0) {
				transaction.setAmount(commission);
				transaction.setTransactionType("COMISION");
				return transactionRestClient.createTransaction(transaction).flatMap(d -> {
					return Mono.just(new AccountResponseDto("Successful transaction", uAccount));
				});
			}
			return Mono.just(new AccountResponseDto("Successful transaction", uAccount));
        });
	}
		
	@Override
	public Mono<TransferResponseDto> transferBetweenAccounts(TransferRequestDto transferRequestDto) {
		return accountRepository.findById(transferRequestDto.getOriginAccount()).flatMap(a -> {
			return accountRepository.findById(transferRequestDto.getDestinationAccount()).filter(c -> !c.getId().equals(a.getId())).flatMap(b -> {
				if(a.getCustomerId().equals(b.getCustomerId())) {
					if(a.getAmount() - transferRequestDto.getAmount() < 0)
						return Mono.just(new TransferResponseDto("You do not have a sufficient balance in your origin account", null));
					a.setAmount(a.getAmount() - transferRequestDto.getAmount());
					b.setAmount(b.getAmount() + transferRequestDto.getAmount());
		            return accountRepository.save(a).flatMap(account1 -> {
		            	return accountRepository.save(b).flatMap(account2 -> {
		            		return registerTransaction(account1, transferRequestDto.getAmount(),"TRANSFERENCIA ENTRE CUENTAS - RETIRO", 0.0).flatMap(t1 -> {
		            			return registerTransaction(account2, transferRequestDto.getAmount(),"TRANSFERENCIA ENTRE CUENTAS - DEPOSITO", 0.0).flatMap(t2 -> {
		            				return Mono.just(new TransferResponseDto("Successful transaction between accounts", Arrays.asList(a,b)));
		            			});
		            		});
		            	});
		            });
				}else {
					return Mono.just(new TransferResponseDto("Accounts are not from the same client", null));
				}
			}).defaultIfEmpty(new TransferResponseDto("Destination Account does not exist, enter another account", null));
		}).defaultIfEmpty(new TransferResponseDto("Origin Account does not exist", null));
	}

	@Override
	public Mono<TransferResponseDto> transferThirdParty(TransferRequestDto transferRequestDto) {
		return accountRepository.findById(transferRequestDto.getOriginAccount()).flatMap(a -> {
			return accountRepository.findById(transferRequestDto.getDestinationAccount()).filter(c -> !(c.getCustomerId().equals(a.getCustomerId()))).flatMap(b -> {
				if(a.getAmount() - transferRequestDto.getAmount() < 0)
					return Mono.just(new TransferResponseDto("You do not have a sufficient balance in your origin account", null));
				a.setAmount(a.getAmount() - transferRequestDto.getAmount());
				b.setAmount(b.getAmount() + transferRequestDto.getAmount());
	            return accountRepository.save(a).flatMap(account1 -> {
	            	return accountRepository.save(b).flatMap(account2 -> {
	            		return registerTransaction(account1, transferRequestDto.getAmount(),"TRANSFERENCIA A TERCEROS - RETIRO", 0.0).flatMap(t1 -> {
	            			return registerTransaction(account2, transferRequestDto.getAmount(),"TRANSFERENCIA A TERCEROS - DEPOSITO", 0.0).flatMap(t2 -> {
	            				return Mono.just(new TransferResponseDto("successful third-party account transaction", Arrays.asList(a,b)));
	            			});
	            		});
	            	});
	            });
			}).defaultIfEmpty(new TransferResponseDto("Destination Account does not exist, enter another account", null));
		}).defaultIfEmpty(new TransferResponseDto("Origin Account does not exist", null));
	}
	
	private Mono<AccountResponseDto> validateTransactions(AccountResponseDto accountResponseDto) {
        return Mono.just(accountResponseDto)
            .filter(account -> account.getAccount().getTransaction() != null && account.getAccount().getTransaction() <= 20)
            .switchIfEmpty(Mono.just(new AccountResponseDto("transaction: must have a maximum of 20 transactions",null)));
    }
	
}
