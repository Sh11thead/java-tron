package org.tron.common.runtime;

import lombok.Setter;
import org.tron.common.runtime.vm.program.InternalTransaction.TrxType;
import org.tron.common.runtime.vm.program.ProgramResult;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.exception.VMIllegalException;


public interface Runtime {

  void execute() throws ContractValidateException, ContractExeException, VMIllegalException;

  void go();

  TrxType getTrxType();

  void saveTrace();

  void finalization();

  ProgramResult getResult();

  String getRuntimeError();

  void setEnableEventLinstener(boolean enableEventLinstener);
}
