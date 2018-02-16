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
  
  public void transferAmount(Account accountFrom, Account accountTo, BigDecimal balance) {
	this.accountsRepository.updateAccountBalance(accountFrom, accountTo, balance);
	emailNotificationService.notifyAboutTransfer(accountFrom,balance+" debited and credited to account id "+accountTo.getAccountId() );
	emailNotificationService.notifyAboutTransfer(accountTo,balance+" credited into the account from account id "+accountFrom.getAccountId() );
  }
}
