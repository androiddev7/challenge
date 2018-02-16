package com.db.awmd.challenge.web;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferAmount;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.service.AccountsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }
  
  @PutMapping(path = "/transfer",consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> transferMoney(@RequestBody @Valid TransferAmount transferBalance){
	  ResponseEntity<Object> response = null;
	  try{
		 ///validating request body
		final Account accountFrom = getAccountIfExists(transferBalance.getAccountIdFrom());
		final Account accountTo = getAccountIfExists(transferBalance.getAccountIdTo());
		validateRequest(accountFrom, accountTo, transferBalance);
		
		//transferring amount if the request is valid 
		log.info("{} amount transferring from {} to {}",transferBalance.getBalance(),transferBalance.getAccountIdFrom(),transferBalance.getAccountIdTo());
		this.accountsService.transferAmount(accountFrom, accountTo, transferBalance.getBalance());
		
		//if amount gets transferred successfully
		response= new ResponseEntity<Object>("Amount successfully transferred", HttpStatus.OK);
	  }catch(InvalidAmountException ibe){
		response = new ResponseEntity<Object>(ibe.getMessage(), HttpStatus.BAD_REQUEST);
	  }catch(InvalidAccountException iae){
		response = new ResponseEntity<Object>(iae.getMessage(), HttpStatus.BAD_REQUEST);
	  }
	 return response;
  }
  
	private void validateRequest(Account accountFrom, Account accountTo, TransferAmount transferBalance) {
		if (transferBalance.getAccountIdFrom().equals(transferBalance.getAccountIdTo())) {
			throw new InvalidAccountException("Both accounts cannot be same");
		} else if (transferBalance.getBalance().compareTo(accountFrom.getBalance()) > 0) {
			throw new InvalidAmountException("Account id " + accountFrom.getAccountId() + " does not have sufficient balance to transfer");
		}
	}

	private Account getAccountIfExists(String accountId) {
		final Account account = this.accountsService.getAccount(accountId);
		if (null == account) {
			throw new InvalidAccountException("Account id " + accountId + " does not exist"); //if the account id in request does not exist
		}
		return account;
	}

}
