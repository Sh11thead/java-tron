package org.tron.common.logsfilter.trigger;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false)
public class TRC20TrackerTrigger extends Trigger {

  @Data
  public static class AssetStatusPojo{
    private String accountAddress;
    private String tokenAddress;
    private String balance;
    private String increment_balance;
  }

  private long blockNumber;

  private String blockHash;

  private List<AssetStatusPojo> assetStatusList = new ArrayList<>();

}


