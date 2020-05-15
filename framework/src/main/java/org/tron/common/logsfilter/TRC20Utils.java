package org.tron.common.logsfilter;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.springframework.util.StringUtils;
import org.tron.common.logsfilter.trigger.TRC20TrackerTrigger.AssetStatusPojo;
import org.tron.common.logsfilter.trigger.TRC20TrackerTrigger.ConcernTopics;
import org.tron.common.runtime.ProgramResult;
import org.tron.common.runtime.vm.LogInfo;
import org.tron.common.utils.Commons;
import org.tron.common.utils.WalletUtil;
import org.tron.core.actuator.VMActuator;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.db.TransactionContext;
import org.tron.core.store.StoreFactory;
import org.tron.core.vm.utils.MUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.contract.SmartContractOuterClass.TriggerSmartContract;

@Slf4j
public class TRC20Utils {


  public static BigInteger getTRC20Decimal(String contractAddress, BlockCapsule baseBlockCap) {
    byte[] data = Hex.decode("313ce567");
    ProgramResult result = triggerFromVM(contractAddress, data, baseBlockCap);
    if (!result.isRevert() && StringUtils.isEmpty(result.getRuntimeError())
        && result.getHReturn() != null) {
      try {
        BigInteger ret = new BigInteger(Hex.toHexString(result.getHReturn()), 16);
        return ret;
      } catch (Exception e) {
      }
    }
    return null;

  }

  public static BigInteger getTRC20Balance(String ownerAddress, String contractAddress,
      BlockCapsule baseBlockCap) {
    byte[] data = Bytes.concat(Hex.decode("70a082310000000000000000000000"),
        Commons.decodeFromBase58Check(ownerAddress));
    ProgramResult result = triggerFromVM(contractAddress, data, baseBlockCap);
    if (!result.isRevert() && StringUtils.isEmpty(result.getRuntimeError())
        && result.getHReturn() != null) {
      try {
        BigInteger ret = new BigInteger(Hex.toHexString(result.getHReturn()), 16);
        return ret;
      } catch (Exception e) {
      }
    }
    return null;

  }

  public static List<AssetStatusPojo> parseTrc20AssetStatusPojo(BlockCapsule block,
      List<LogInfo> logInfos) {
    Set<String> tokenSet = new HashSet<>();

    Map<String, BigInteger> incrementMap = new LinkedHashMap<>();
    Map<String, BigInteger> balanceMap = new LinkedHashMap<>();
    Map<String, BigInteger> decimalMap = new LinkedHashMap<>();
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
        String tokenAddress = WalletUtil
            .encode58Check(MUtil.convertToTronAddress(logInfo.getAddress()));
        BigInteger increment = new BigInteger(logInfo.getHexData(), 16);

        adjustIncrement(incrementMap, recAddr, tokenAddress, increment);
        adjustIncrement(incrementMap, senderAddr, tokenAddress, increment.negate());
        tokenSet.add(tokenAddress);

      }
    }
    for (String keys : incrementMap.keySet()) {
      // foreach address try to get it's balance.
      String[] key = keys.split(",");
      BigInteger balance = TRC20Utils.getTRC20Balance(key[0], key[1], block);
      if (balance != null) {
        balanceMap.put(keys, balance);
      }
    }
    for (String token : tokenSet) {
      BigInteger decimals = TRC20Utils.getTRC20Decimal(token, block);
      if (decimals != null) {
        decimalMap.put(token, decimals);
      }
    }

    logger.info("incrementMap: {}", incrementMap);
    logger.info("balanceMap: {}", balanceMap);
    logger.info("decimalsMap: {}", balanceMap);

    return null;
  }

  private static void adjustIncrement(Map<String, BigInteger> incrementMap, String address,
      String token,
      BigInteger wad) {
    BigInteger previous = incrementMap.get(address + "," + token);
    if (previous == null) {
      previous = new BigInteger("0");
    }
    previous = previous.add(wad);
    incrementMap.put(address + "," + token, previous);
  }


  private static ProgramResult triggerFromVM(String contractAddress, byte[] data,
      BlockCapsule baseBlockCap) {
    TriggerSmartContract.Builder build = TriggerSmartContract.newBuilder();
    build.setData(
        ByteString.copyFrom(data));
    build.setOwnerAddress(ByteString.EMPTY);
    build.setCallValue(0);
    build.setCallTokenValue(0);
    build.setTokenId(0);
    build.setContractAddress(ByteString.copyFrom(Commons.decodeFromBase58Check(contractAddress)));
    TransactionCapsule trx = new TransactionCapsule(build.build(),
        ContractType.TriggerSmartContract);
    Transaction.Builder txBuilder = trx.getInstance().toBuilder();
    Transaction.raw.Builder rawBuilder = trx.getInstance().getRawData().toBuilder();
    rawBuilder.setFeeLimit(1000000000L);
    txBuilder.setRawData(rawBuilder);

    TransactionContext context = new TransactionContext(baseBlockCap,
        new TransactionCapsule(txBuilder.build()),
        StoreFactory.getInstance(), true,
        false);
    VMActuator vmActuator = new VMActuator(true);

    try {
      vmActuator.validate(context);
      vmActuator.execute(context);
    } catch (Exception e) {
      logger.warn("{} trigger failed!", contractAddress);
    }

    ProgramResult result = context.getProgramResult();
    return result;
  }
}
