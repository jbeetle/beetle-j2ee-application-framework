package com.beetle.framework.business.common.tst;

import com.beetle.framework.business.command.CommandHelper;

public class ServiceMethodTransactionRollback {
	private ServiceMethodTransactionRollback() {

	}

	public static void rollbackByHand() {
		CommandHelper.setRollbackFlag();
	}
}
