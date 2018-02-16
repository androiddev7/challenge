package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class TransferAmount {
	
	@NotNull
	@NotEmpty
	private String accountIdFrom;
	
	@NotNull
	@NotEmpty
	private String accountIdTo;
	
	@NotNull
	@Min(value = 1, message = "Balance to transfer should be positive")
    private BigDecimal balance;
}
