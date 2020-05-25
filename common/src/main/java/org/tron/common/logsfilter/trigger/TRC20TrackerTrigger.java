package org.tron.common.logsfilter.trigger;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = false)
public class TRC20TrackerTrigger extends Trigger {

  @Data
  public static class AssetStatusPojo {

    private String accountAddress;
    private String tokenAddress;
    private String balance;
    private String incrementBalance;
    private String decimals;
  }

  public static enum ConcernTopics {
    TRANSFER("Transfer(address,address,uint256)",
        "ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");

    @Getter
    private String sign;
    @Getter
    private String signHash;


    ConcernTopics(String sign, String signHash) {
      this.sign = sign;
      this.signHash = signHash;
    }

  }


  private long blockNumber;

  private String parentHash;

  private String blockHash;

  private Boolean solidity = false;

  private List<AssetStatusPojo> assetStatusList = new ArrayList<>();

  public TRC20TrackerTrigger() {
    super();
    setTriggerName(Trigger.TRC20TRACKER_TRIGGER_NAME);
  }

  public void solidityType() {
    setTriggerName(Trigger.TRC20TRACKER_SOLIDITY_TRIGGER_NAME);
  }


}


