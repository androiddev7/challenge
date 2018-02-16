package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }
  
  @Test
  public void transferAmountSuccessfully() throws Exception{
	 prepareTestData();
	 this.mockMvc.perform(put("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .content("{\"accountIdFrom\":\"1001\",\"accountIdTo\":\"1002\",\"balance\": 5000}"))
	 		  .andExpect(status().isOk())
	 		  .andExpect(content().string("Amount successfully transferred"));
  }

  @Test
  public void testInvalidAccount() throws Exception{
	 prepareTestData();
	 this.mockMvc.perform(put("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .content("{\"accountIdFrom\":\"1003\",\"accountIdTo\":\"1002\",\"balance\": 50000}"))
	 		  .andExpect(status().isBadRequest())
	 		  .andExpect(content().string("Account id 1003 does not exist"));
	 
  }
  
  @Test
  public void testInsufficientBalance() throws Exception{
	 prepareTestData();
	 this.mockMvc.perform(put("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .content("{\"accountIdFrom\":\"1001\",\"accountIdTo\":\"1002\",\"balance\": 50000}"))
	          .andExpect(status().isBadRequest())
	          .andExpect(content().string("Account id 1001 does not have sufficient balance"));
	 
  }
  
  @Test
  public void testAccountsShouldNotBeSame() throws Exception{
	 prepareTestData();
	 this.mockMvc.perform(put("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .content("{\"accountIdFrom\":\"1001\",\"accountIdTo\":\"1001\",\"balance\": 50000}"))
	 		  .andExpect(status().isBadRequest())
	 		  .andExpect(content().string("Both accounts cannot be same"));
  }
  
  private void prepareTestData() {
		Account account = new Account("1001", new BigDecimal("10000"));
		this.accountsService.createAccount(account);

		account = new Account("1002", new BigDecimal("20000"));
		this.accountsService.createAccount(account);	
	}	
	  
	  
}
