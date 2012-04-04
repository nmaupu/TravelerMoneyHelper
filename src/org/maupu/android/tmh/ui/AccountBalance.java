package org.maupu.android.tmh.ui;

import java.util.HashMap;

import org.maupu.android.tmh.database.object.Account;

public class AccountBalance extends HashMap<Integer, Double> {
	private static final long serialVersionUID = 1L;
	private Account account;
	private Double balanceRate = 0d;
	
	public AccountBalance(Account account) {
		this.account = account;
	}
	
	public void setBalanceRate(Double balanceRate) {
		this.balanceRate = balanceRate;
	}
	
	public Account getAccount() {
		return account;
	}
	
	public Double getBalanceRate() {
		return balanceRate;
	}
}
