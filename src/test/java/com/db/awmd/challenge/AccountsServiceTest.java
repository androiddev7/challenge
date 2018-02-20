package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {
  
  @Autowired
  private AccountsService accountsService;
  
  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);
    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }
  
  @Test
  public void testTransferAmount() throws Exception {
	  
	    //created two accounts
	    Account accountFrom= new Account("Id-AccFrom"); 
	    accountFrom.setBalance(new BigDecimal(1000));
	    this.accountsService.createAccount(accountFrom);
	    Account accountTo= new Account("Id-AccTo"); 
	    accountTo.setBalance(new BigDecimal(1000));
	    this.accountsService.createAccount(accountTo);
	    BigDecimal balance = new BigDecimal(500);
	    
	    //assert account balance before transfer
	    assertThat(accountFrom.getBalance()).isEqualTo(new BigDecimal(1000));
	    assertThat(accountTo.getBalance()).isEqualTo(new BigDecimal(1000));
	    
	    //transfer amount
	    this.accountsService.transferAmount(accountFrom, accountTo, balance);
	    
	    //assert account balance after transfer
	    assertThat(accountFrom.getBalance()).isEqualTo(new BigDecimal(500));
	    assertThat(accountTo.getBalance()).isEqualTo(new BigDecimal(1500));
  }
  
  @Test
  public void testWithdrawAmount() throws Exception{
	  Account account= new Account("Id-AccWithDraw"); 
	  account.setBalance(new BigDecimal(1000));
	  this.accountsService.createAccount(account);
	  
	  //assert account balance before withdraw
	  assertThat(account.getBalance()).isEqualTo(new BigDecimal(1000));
	  
	  account.withdraw(new BigDecimal(500));
	  
	  //assert account balance after withdraw
	  assertThat(account.getBalance()).isEqualTo(new BigDecimal(500));
  }
  
  @Test
  public void testDepositAmount() throws Exception{
	  Account account= new Account("Id-AccDeposit"); 
	  account.setBalance(new BigDecimal(1000));
	  this.accountsService.createAccount(account);
	  //assert account balance before deposit
	  assertThat(account.getBalance()).isEqualTo(new BigDecimal(1000));
	  account.deposit(new BigDecimal(500));
	  //assert account balance after deposit
	  assertThat(account.getBalance()).isEqualTo(new BigDecimal(1500));
  }
  
  @Test
  public void testConcurrentOperations() throws Exception{
	  Account account= new Account("Id-ConcAcc"); 
	  account.setBalance(new BigDecimal(1000));
	  this.accountsService.createAccount(account);
	  Runnable r_withdraw = new Runnable() {
		@Override
		public void run() {
			account.withdraw(new BigDecimal(500));
			assertThat(account.getBalance()).isEqualTo(new BigDecimal(500));
		}
	  };
	  
	  Runnable r_deposit = new Runnable() {
		@Override
		public void run() {
			account.deposit(new BigDecimal(1500));
		    assertThat(account.getBalance()).isEqualTo(new BigDecimal(2000));
		}	
	   };
	   
	  Thread tWithdraw = new Thread(r_withdraw);
	  Thread tDeposit = new Thread(r_deposit);
	  tWithdraw.start();
	  tDeposit.start();
  }
  
  @Test
  public void testNotificationService() throws Exception{
	  AccountsRepository accountRepository = mock(AccountsRepository.class);
	  EmailNotificationService notificationService = mock(EmailNotificationService.class);
	  AccountsService accService = new AccountsService(accountRepository, notificationService);
      Account accountFrom = new Account("accFrom", new BigDecimal(1000));
      Account accountTo = new Account("accTo", new BigDecimal(500));	  
	  accService.transferAmount(accountFrom,accountTo,new BigDecimal(500));
	  verify(notificationService, times(2)).notifyAboutTransfer(any(), any());
  }
  
}
