package org.tron.common.logsfilter.capsule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.logsfilter.trigger.TRC20TrackerTrigger;
import org.tron.common.logsfilter.trigger.TRC20TrackerTrigger.AssetStatusPojo;
import org.tron.common.logsfilter.trigger.TRC20TrackerTrigger.ConcernTopics;
import org.tron.common.runtime.vm.LogInfo;
import org.tron.common.utils.WalletUtil;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.vm.utils.MUtil;

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

      List<AssetStatusPojo> assetStatusPojos = parseTrc20AssetStatusPojo(logInfos);


    }


  }

  public List<AssetStatusPojo> parseTrc20AssetStatusPojo(List<LogInfo> logInfos) {
    Map<String, BigInteger> incrementMap = new HashMap<>();
    Map<String, BigInteger> balanceMap = new HashMap<>();
    for (LogInfo logInfo : logInfos) {
      List<String> topics = logInfo.getHexTopics();
      if (topics == null) {
        continue;
      }
      if (topics.size() >= 3 && topics.get(0).equals(ConcernTopics.TRANSFER.getSignHash())) {
        //TransferCase : decrease sender, increase receiver
        String senderAddr = WalletUtil
            .encode58Check(MUtil.convertToTronAddress(logInfo.getTopics().get(1).getLast20Bytes()));
        String recAddr = WalletUtil
            .encode58Check(MUtil.convertToTronAddress(logInfo.getTopics().get(2).getLast20Bytes()));
        BigInteger increment = new BigInteger(logInfo.getHexData(), 16);

        adjustIncrement(incrementMap, recAddr, increment);
        adjustIncrement(incrementMap, senderAddr, increment.negate());
      }
    }
    logger.info("increment map: {}", incrementMap);
    return null;
  }

  private void adjustIncrement(Map<String, BigInteger> incrementMap, String address,
      BigInteger wad) {
    BigInteger previous = incrementMap.get(address);
    if (previous == null) {
      previous = new BigInteger("0");
    }
    previous = previous.add(wad);
    incrementMap.put(address, previous);
  }


}
