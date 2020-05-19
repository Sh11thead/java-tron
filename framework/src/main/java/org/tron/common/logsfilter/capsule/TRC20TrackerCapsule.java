package org.tron.common.logsfilter.capsule;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.TRC20Utils;
import org.tron.common.logsfilter.trigger.TRC20TrackerTrigger;
import org.tron.common.logsfilter.trigger.TRC20TrackerTrigger.AssetStatusPojo;
import org.tron.common.runtime.vm.LogInfo;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;

@Slf4j
public class TRC20TrackerCapsule extends TriggerCapsule {


  @Getter
  @Setter
  TRC20TrackerTrigger trc20TrackerTrigger;

  public TRC20TrackerCapsule(BlockCapsule block) {
    List<TransactionCapsule> transactionCapsules = block.getTransactions();
    logger.info("There is {} in trc20 tracker block", transactionCapsules.size());
    List<LogInfo> logInfos = new ArrayList<>();
    for (TransactionCapsule transactionCapsule : transactionCapsules) {
      List<LogInfo> innerList = transactionCapsule.getTrxTrace().getTransactionContext()
          .getProgramResult().getLogInfoList();
      if (innerList != null && innerList.size() > 0) {
        logInfos.addAll(innerList);
      }
    }
    if (logInfos.size() > 0) {
      List<AssetStatusPojo> assetStatusPojos = TRC20Utils
          .parseTrc20AssetStatusPojo(block, logInfos);
      trc20TrackerTrigger = new TRC20TrackerTrigger();
      trc20TrackerTrigger.setBlockHash(block.getBlockId().toString());
      trc20TrackerTrigger.setBlockNumber(block.getNum());
      trc20TrackerTrigger.setTimeStamp(block.getTimeStamp());
      trc20TrackerTrigger.setAssetStatusList(assetStatusPojos);
      logger.info("---------------------trc20TrackerTrigger------------------------{}",
          JSONObject.toJSONString(trc20TrackerTrigger));
    }
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postTRC20TrackerTrigger(trc20TrackerTrigger);
  }


}
