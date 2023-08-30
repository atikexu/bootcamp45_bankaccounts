package com.bootcamp.bankaccounts.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.bootcamp.bankaccounts.dto.TypeAccountDto;


public class TypeAccount {
	//ID, Type, Maintenance, Transactions, DayOperation
	public Stream<TypeAccountDto> getAccounts() {
        List<TypeAccountDto> types = new ArrayList<>();
        types.add(new TypeAccountDto(0, "AHORRO", 0.00, 3, 0));
        types.add(new TypeAccountDto(1, "C_CORRIENTE", 12.00, 99999, 0));
        types.add(new TypeAccountDto(2, "PLAZO_FIJO", 0.00, 1, 15));
        types.add(new TypeAccountDto(3, "CRED_PERSONAL", 0.00, 0, 0));
        types.add(new TypeAccountDto(4, "CRED_EMPRESARIAL", 0.00, 0, 0));
        types.add(new TypeAccountDto(5, "TAR_CRED_PERSONAL", 0.00, 0, 0));
        types.add(new TypeAccountDto(6, "TAR_CRED_EMPRESARIAL", 0.00, 0, 0));
        return types.stream();
    }
}
