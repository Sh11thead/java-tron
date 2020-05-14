package org.tron.common.logsfilter.capsule;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.trigger.BlockLogTrigger;
import org.tron.common.logsfilter.trigger.ContractTrigger;
import org.tron.common.logsfilter.trigger.TRC20TrackerTrigger;
import org.tron.common.utils.FIFOCache;
import org.tron.core.Wallet;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.capsule.TransactionInfoCapsule;
import org.tron.core.db.Manager;
import org.tron.protos.Protocol.TransactionInfo;

public class TRC20TrackerCapsule extends TriggerCapsule {


  @Getter
  @Setter
  TRC20TrackerTrigger trc20TrackerTrigger;

  public TRC20TrackerCapsule(BlockCapsule block, List<TransactionInfo> transactionInfos) {

    for( TransactionInfo transactionInfo:transactionInfos){

    }



  }


  @Override
  public void processTrigger() {
  //  EventPluginLoader.getInstance().postBlockTrigger(trc20TrackerTrigger);
  }
}
