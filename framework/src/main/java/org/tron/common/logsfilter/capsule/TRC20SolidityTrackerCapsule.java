package org.tron.common.logsfilter.capsule;

import com.alibaba.fastjson.JSONObject;
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

@Slf4j
public class TRC20SolidityTrackerCapsule extends TriggerCapsule {


  @Getter
  @Setter
  TRC20TrackerTrigger trc20TrackerTrigger;

  public TRC20SolidityTrackerCapsule(BlockCapsule block, List<LogInfo> logInfos) {
    if (logInfos.size() > 0) {
      trc20TrackerTrigger = new TRC20TrackerTrigger();
      trc20TrackerTrigger.setBlockHash(block.getBlockId().toString());
      trc20TrackerTrigger.setParentHash(block.getParentHash().toString());
      trc20TrackerTrigger.setBlockNumber(block.getNum());
      trc20TrackerTrigger.setTimeStamp(block.getTimeStamp());
      trc20TrackerTrigger.solidityType();
      trc20TrackerTrigger.setSolidity(true);
      if (logInfos != null) {
        List<AssetStatusPojo> assetStatusPojos = TRC20Utils
            .parseTrc20AssetStatusPojo(block, logInfos);
        trc20TrackerTrigger.setAssetStatusList(assetStatusPojos);

      }
      logger.info("---------------------trc20SolidityTrigger------------------------{}",
          JSONObject.toJSONString(trc20TrackerTrigger));

    }
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postTRC20TrackerTrigger(trc20TrackerTrigger);
  }

}
