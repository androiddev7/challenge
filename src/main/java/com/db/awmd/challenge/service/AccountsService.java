package com.db.awmd.challenge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferAmount;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  
  @Getter
  private final EmailNotificationService emailNotificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository,EmailNotificationService emailNotificationService) {
    this.accountsRepository = accountsRepository;
    this.emailNotificationService=emailNotificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  
  public void transferAmount(TransferAmount transferBalance) {
	final Account accountFrom = getAccountIfExists(transferBalance.getAccountIdFrom());
	final Account accountTo = getAccountIfExists(transferBalance.getAccountIdTo());
	validateRequest(accountFrom, accountTo, transferBalance);
	BigDecimal balance=transferBalance.getBalance();
	this.accountsRepository.updateAccountBalance(accountFrom, accountTo, balance);
	emailNotificationService.notifyAboutTransfer(accountFrom,balance+" debited from the account" );
	emailNotificationService.notifyAboutTransfer(accountTo,balance+" credited into the account" );
  }

  private void validateRequest(Account accountFrom, Account accountTo, TransferAmount transferBalance) {
	 if (transferBalance.getBalance().intValue() == 0) {
		throw new InvalidAccountException("Amount to transfer cannot be 0");
	} else if (transferBalance.getAccountIdFrom().equals(transferBalance.getAccountIdTo())) {
		throw new InvalidAccountException("Both accounts cannot be same");
	} else if (transferBalance.getBalance().compareTo(accountFrom.getBalance()) > 0) {
		throw new InvalidAmountException("Account id " + accountFrom.getAccountId() + " does not have sufficient balance");
	}
  }

  private Account getAccountIfExists(String accountId) {
	final Account account = getAccount(accountId);
	if (null == account) {
		throw new InvalidAccountException("Account id " + accountId + " does not exist");
	}
	return account;
  }	
}
