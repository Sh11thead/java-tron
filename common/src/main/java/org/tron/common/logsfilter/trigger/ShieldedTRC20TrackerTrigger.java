package org.tron.common.logsfilter.trigger;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ShieldedTRC20TrackerTrigger extends Trigger {

  @Data
  public static class LogPojo {

    private int type;
    private String address;
    private List<String> topics = new ArrayList<>();
    private String data;

  }

  @Data
  public static class TransactionPojo {

    private String txId;
    private String contractAddress;

    private long energyFee;
    private long energyUsage;
    private long originEnergyUsage;
    private long energyUsageTotal;
    private long netFee;
    private long netUsage;
    private List<LogPojo> LogList = new ArrayList<>();

  }

  private long blockNumber;

  private String parentHash;

  private String blockHash;

  private List<TransactionPojo> transactionList = new ArrayList<>();


  public ShieldedTRC20TrackerTrigger() {
    super();
    setTriggerName(Trigger.SHIELDED_TRC20SOLIDITYTRACKER_TRIGGER_NAME);
  }


}


