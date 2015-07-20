/**
 * Copyright 2015 BitTechCenter Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coinj.litecoin;

import org.bitcoinj.core.*;
import org.bitcoinj.store.WalletProtobufSerializer;
import org.coinj.api.*;
import org.coinj.commons.*;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static org.coinj.commons.Util.impossibleNullCheck;

/**
 * Date: 4/30/15
 * Time: 3:00 PM
 *
 * @author Mikhail Kulikov
 */
public class LitecoinDefinition implements CoinDefinition, Serializable {

    public static final LitecoinDefinition INSTANCE = new LitecoinDefinition();

    private static final long serialVersionUID = 1L;

    public static final String NAME = "litecoin";
    public static final String SIGNING_NAME = "Litecoin";
    public static final String TICKER = "LTC";
    public static final String URI_SCHEME = "litecoin";
    public static final int PROTOCOL_VERSION = 70003;
    public static final boolean CHECKPOINTING_SUPPORT = true;
    public static final int CHECKPOINT_DAYS_BACK = 2;
    public static final int TARGET_TIMESPAN = (int) (3.5 * 24 * 60 * 60);  // // 3.5 days per difficulty cycle, on average.
    public static final int TARGET_SPACING = (int) (2.5 * 60);  // 2.5 minutes per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;
    public static final Integer MAIN_SUBSIDY_DECREASE_BLOCK_COUNT = 840000;
    public static final Integer TEST_SUBSIDY_DECREASE_BLOCK_COUNT = 210000;
    public static final long MAX_COINS = 84000000L;
    public static final BigInteger MAX_TARGET = Utils.decodeCompactBits(0x1e0fffffL);
    public static final BigInteger UNITTEST_MAX_TARGET = BigInteger.ONE.shiftLeft(256);
    public static final long REFERENCE_DEFAULT_MIN_TX_FEE = 100000;
    public static final int MIN_NONDUST_OUTPUT = 1000;
    public static final int MAX_BLOCK_SIZE = 250 * 1000;
    public static final Integer PORT = 9333;
    public static final Integer TEST_PORT = 19333;
    public static final Integer SPENDABLE_COINBASE_DEPTH = 100;
    public static final Integer PUBKEY_ADDRESS_HEADER = 48;
    public static final Integer DUMPED_PRIVATE_KEY_HEADER = 128 + PUBKEY_ADDRESS_HEADER;
    public static final Integer TEST_PUBKEY_ADDRESS_HEADER = 111;
    public static final Integer TEST_DUMPED_PRIVATE_KEY_HEADER = 128 + TEST_PUBKEY_ADDRESS_HEADER;
    public static final Integer P2SH_ADDRESS_HEADER = 5;
    public static final Integer TEST_P2SH_ADDRESS_HEADER = 196;
    public static final Long MAIN_PACKET_MAGIC = 0xfbc0b6dbL;
    public static final Long TEST_PACKET_MAGIC = 0xfcc1b7dcL;
    static final int ALLOWED_TIME_DRIFT = 2 * 60 * 60; // Same value as official client.

    /**
     * A services flag that denotes whether the peer has a copy of the block chain or not.
     */
    public static final int NODE_NETWORK = 1;

    private static final String MAIN_GENESIS_TX_IN_BYTES = "04b217bb4e022309";
    private static final String TEST_GENESIS_TX_IN_BYTES =
            "04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73";
    private static final String MAIN_GENESIS_TX_OUT_BYTES =
            "41044870341873accab7600d65e204bb4ae47c43d20c562ebfbf70cbcb188da98dec8b5ccf0526c8e4d954c6b47b898cc30adf1ff77c2e518ddc9785b87ccb90b8cdac";
    private static final String TEST_GENESIS_TX_OUT_BYTES =
            "04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f";
    private static final int GENESIS_BLOCK_VALUE = 50;
    private static final long GENESIS_BLOCK_DIFFICULTY_TARGET = 0x1e0ffff0L;
    private static final long MAIN_GENESIS_BLOCK_TIME = 1317972665L;
    private static final long MAIN_GENESIS_BLOCK_NONCE = 2084524493L;
    private static final String MAIN_GENESIS_MERKLE_ROOT = "97ddfbbae6be97fd6cdf3e7ca13232a3afff2353e29badfab7f73011edd4ced9";
    private static final String MAIN_GENESIS_HASH = "12a765e31ffd4059bada1e25190f6e98c99d9714d334efa41a195a7e7e04bfe2";
    private static final long TEST_GENESIS_BLOCK_TIME = 1317798646L;
    private static final long TEST_GENESIS_BLOCK_NONCE = 385270584L;
    private static final String TEST_GENESIS_HASH = "f5ae71e26c74beacc88382716aced69cddf3dffff24f384e1808905e0188f68f";
    private static final String TEST_GENESIS_MERKLE_ROOT = "97ddfbbae6be97fd6cdf3e7ca13232a3afff2353e29badfab7f73011edd4ced9";

    private static final String[] DNS_SEEDS = new String[] {
            "dnsseed.litecointools.com",
            "dnsseed.litecoinpool.org",
            "dnsseed.ltc.xurious.com",
            "dnsseed.koin-project.com",
            "dnsseed.weminemnc.com",
    };
    private static final String[] TEST_DNS_SEEDS = new String[] {
            "testnet-seed.litecointools.com",
            "testnet-seed.ltc.xurious.com",
            "dnsseed.wemine-testnet.com"
    };

    private static final long EASIEST_DIFFICULTY_TARGET = 0x20010000L;

    public static final String ID_MAINNET = "org.litecoin.production";
    public static final String ID_TESTNET = "org.litecoin.test";
    public static final String ID_UNITTESTNET = "org.litecoin.unittest";

    private static final String MAIN_ALERT_KEY = "04fc9702847840aaf195de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284";
    private static final int MIN_BROADCAST_CONNECTIONS = 0;
    public static final int MIN_BLOOM_PROTOCOL_VERSION = 70000;
    public static final int MIN_PONG_PROTOCOL_VERSION = 60001;
    private static final String UNIT_TEST_STANDARD_NETWORK_ID = "unitTest";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSignedMessageName() {
        return SIGNING_NAME;
    }

    @Override
    public String getTicker() {
        return TICKER;
    }

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public int getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    @Override
    public boolean isCheckpointingSupported() {
        return CHECKPOINTING_SUPPORT;
    }

    @Override
    public int getCheckpointDaysBack() {
        return CHECKPOINT_DAYS_BACK;
    }

    @Override
    public void checkpointsSanityCheck(CheckpointManager checkpointStore, Map checkpoints, StandardNetworkId networkId) {
        checkState(checkpointStore.numCheckpoints() == checkpoints.size());

        if (MAIN_NETWORK_STANDARD.equals(networkId)) {
            StoredBlock test = checkpointStore.getCheckpointBefore(1348310800);
            checkState(test.getHeight() == 211680);
            checkState(test.getHeader().getHashAsString()
                    .equals("d8c4452c530b0a2f5c7b712b0503704f0956c5ec1878cd372035311fedcd2d9c"));
        }
    }

    @Override
    public long getEasiestDifficultyTarget() {
        return EASIEST_DIFFICULTY_TARGET;
    }

    @Override
    public int getTargetTimespan(Block block, int height, @Nullable StandardNetworkId networkId) {
        return TARGET_TIMESPAN;
    }

    @Override
    public int getTargetSpacing(Block block, int height, @Nullable StandardNetworkId networkId) {
        if (networkId != null && networkId.str().equals(UNIT_TEST_STANDARD_NETWORK_ID)) {
            return 20000000;
        }
        return TARGET_SPACING;
    }

    @Override
    public int getInterval(Block block, int height, @Nullable StandardNetworkId networkId) {
        return INTERVAL;
    }

    @Override
    public int getAllowedBlockTimeDrift(StandardNetworkId networkId) {
        if (networkId != null && networkId.str().equals(UNIT_TEST_STANDARD_NETWORK_ID)) {
            return ALLOWED_TIME_DRIFT * 10000;
        }
        return ALLOWED_TIME_DRIFT;
    }

    @Override
    public int getIntervalCheckpoints(Block block, int height, @Nullable StandardNetworkId networkId) {
        return INTERVAL;
    }

    @Override
    public long getBlockReward(Block block, Block prevBlock, int prevHeight, StandardNetworkId networkId) {
        return Constants.FIFTY_COINS.shiftRight((prevHeight + 1) / getSubsidyDecreaseBlockCount(networkId != null ? networkId : MAIN_NETWORK_STANDARD)).longValue();
    }

    @Override
    public int getSubsidyDecreaseBlockCount(StandardNetworkId networkId) {
        if (networkId.str().equals(UNIT_TEST_STANDARD_NETWORK_ID)) {
            return MAIN_SUBSIDY_DECREASE_BLOCK_COUNT;
        }
        return (Integer) impossibleNullCheck(networkCheck(MAIN_SUBSIDY_DECREASE_BLOCK_COUNT, TEST_SUBSIDY_DECREASE_BLOCK_COUNT, networkId));
    }

    @Override
    public int getSpendableDepth(StandardNetworkId networkId) {
        return (Integer) impossibleNullCheck(networkCheck(SPENDABLE_COINBASE_DEPTH, SPENDABLE_COINBASE_DEPTH, networkId));
    }

    @Override
    public long getMaxCoins() {
        return MAX_COINS;
    }

    @Override
    public BigInteger getProofOfWorkLimit(StandardNetworkId networkId) {
        if (networkId.str().equals(UNIT_TEST_STANDARD_NETWORK_ID)) {
            return UNITTEST_MAX_TARGET;
        }
        return (BigInteger) networkCheck(MAX_TARGET, MAX_TARGET, networkId);
    }

    @Override
    public long getDefaultMinTransactionFee() {
        return REFERENCE_DEFAULT_MIN_TX_FEE;
    }

    @Override
    public long getDustLimit() {
        return MIN_NONDUST_OUTPUT;
    }

    @Override
    public int getMaxBlockSize() {
        return MAX_BLOCK_SIZE;
    }

    @Override
    public int getPort(StandardNetworkId networkId) {
        return (Integer) impossibleNullCheck(networkCheck(PORT, TEST_PORT, networkId));
    }

    @Override
    public int getPubkeyAddressHeader(StandardNetworkId networkId) {
        return (Integer) impossibleNullCheck(networkCheck(PUBKEY_ADDRESS_HEADER, TEST_PUBKEY_ADDRESS_HEADER, networkId));
    }

    @Override
    public int getDumpedPrivateKeyHeader(StandardNetworkId networkId) {
        return (Integer) impossibleNullCheck(networkCheck(DUMPED_PRIVATE_KEY_HEADER, TEST_DUMPED_PRIVATE_KEY_HEADER, networkId));
    }

    @Override
    public int getP2shAddressHeader(StandardNetworkId networkId) {
        return (Integer) impossibleNullCheck(networkCheck(P2SH_ADDRESS_HEADER, TEST_P2SH_ADDRESS_HEADER, networkId));
    }

    @Override
    public void initCheckpoints(CheckpointsContainer checkpointsContainer) {}

    @Override
    public long getPacketMagic(StandardNetworkId networkId) {
        return (Long) impossibleNullCheck(networkCheck(MAIN_PACKET_MAGIC, TEST_PACKET_MAGIC, networkId));
    }

    @Override
    public GenesisBlockInfo getGenesisBlockInfo(StandardNetworkId networkId) {
        final GenesisBlockInfo.GenesisBlockInfoBuilder builder = new GenesisBlockInfo.GenesisBlockInfoBuilder();
        builder.setGenesisBlockValue(GENESIS_BLOCK_VALUE);

        if (MAIN_NETWORK_STANDARD.equals(networkId) || networkId.str().equals(UNIT_TEST_STANDARD_NETWORK_ID)) {
            builder.setGenesisTxInBytes(MAIN_GENESIS_TX_IN_BYTES);
            builder.setGenesisTxOutBytes(MAIN_GENESIS_TX_OUT_BYTES);
            builder.setGenesisBlockDifficultyTarget(GENESIS_BLOCK_DIFFICULTY_TARGET);
            builder.setGenesisBlockTime(MAIN_GENESIS_BLOCK_TIME);
            builder.setGenesisBlockNonce(MAIN_GENESIS_BLOCK_NONCE);
            builder.setGenesisMerkleRoot(MAIN_GENESIS_MERKLE_ROOT);
            builder.setGenesisHash(MAIN_GENESIS_HASH);
        } else if (TEST_NETWORK_STANDARD.equals(networkId)) {
            builder.setGenesisTxInBytes(TEST_GENESIS_TX_IN_BYTES);
            builder.setGenesisTxOutBytes(TEST_GENESIS_TX_OUT_BYTES);
            builder.setGenesisBlockDifficultyTarget(GENESIS_BLOCK_DIFFICULTY_TARGET);
            builder.setGenesisBlockTime(TEST_GENESIS_BLOCK_TIME);
            builder.setGenesisBlockNonce(TEST_GENESIS_BLOCK_NONCE);
            builder.setGenesisMerkleRoot(TEST_GENESIS_MERKLE_ROOT);
            builder.setGenesisHash(TEST_GENESIS_HASH);
        } else {
            throw new NonStandardNetworkException(networkId.str(), NAME);
        }

        return builder.build();
    }

    @Override
    public String[] getDnsSeeds(StandardNetworkId networkId) {
        return (String[]) networkCheck(DNS_SEEDS, TEST_DNS_SEEDS, networkId);
    }

    @Override
    public String getAlertKey(StandardNetworkId networkId) {
        return MAIN_ALERT_KEY;
    }

    @Override
    public String getIdMainNet() {
        return ID_MAINNET;
    }

    @Override
    public String getIdTestNet() {
        return ID_TESTNET;
    }

    @Override
    @Nullable
    public String getIdRegTest() {
        return null;
    }

    @Override
    public String getIdUnitTestNet() {
        return ID_UNITTESTNET;
    }

    @Override
    @Nullable
    public String getPaymentProtocolId(StandardNetworkId networkId) {
        return null;
    }

    @Override
    public int getMinBroadcastConnections() {
        return MIN_BROADCAST_CONNECTIONS;
    }

    @Override
    public int getMinPongProtocolVersion() {
        return MIN_PONG_PROTOCOL_VERSION;
    }

    @Override
    public boolean isBitcoinPrivateKeyAllowed() {
        return false;
    }

    @Override
    public int getAllowedPrivateKey() {
        return 0;
    }

    @Override
    public boolean isBloomFilteringSupported(VersionMessage versionInfo) {
        return versionInfo.getClientVersion() >= MIN_BLOOM_PROTOCOL_VERSION;
    }

    @Override
    public boolean hasBlockChain(VersionMessage versionInfo) {
        return (versionInfo.getLocalServices() & NODE_NETWORK) == NODE_NETWORK;
    }

    @Override
    public boolean isGetUTXOsSupported(VersionMessage versionInfo) {
        return false;
    }

    @Override
    public boolean isPingPongSupported(VersionMessage versionInfo) {
        return versionInfo.getClientVersion() >= MIN_PONG_PROTOCOL_VERSION;
    }

    @Override
    public int getMinBloomProtocolVersion() {
        return MIN_BLOOM_PROTOCOL_VERSION;
    }

    @Nullable
    @Override
    public Integer getNodeBloomConstant() {
        return null;
    }

    @Nullable
    @Override
    public Integer getNodeNetworkConstant() {
        return NODE_NETWORK;
    }

    @Nullable
    @Override
    public Integer getNodeGetUtxosConstant() {
        return null;
    }

    @Nullable
    @Override
    public Integer getNodePongConstant() {
        return null;
    }

    @Override
    public BlockHasher createBlockHasher() {
        return new LitecoinBlockHasher();
    }

    @Override
    public BlockExtension createBlockExtension(Block block) {
        return EmptyBlockExtension.INSTANCE;
    }

    @Override
    public TransactionExtension createTransactionExtension(Transaction transaction) {
        return new EmptyTransactionExtension(transaction);
    }

    @Override
    public CoinSerializerExtension createCoinSerializerExtension() {
        return EmptyCoinSerializerExtension.INSTANCE;
    }

    @Override
    public PeerExtension createPeerExtension(Peer peer) {
        return EmptyPeerExtension.INSTANCE;
    }

    @Override
    public PeerGroupExtension createPeerGroupExtension(PeerGroup peerGroup) {
        return EmptyPeerGroupExtension.INSTANCE;
    }

    @Override
    public BlockChainExtension createBlockChainExtension(AbstractBlockChain blockChain) {
        return new LitecoinChainExtension(blockChain);
    }

    @Override
    public TransactionConfidenceExtension createTransactionConfidenceExtension(TransactionConfidence transactionConfidence) {
        return EmptyTransactionConfidenceExtension.INSTANCE;
    }

    @Override
    public WalletCoinSpecifics createWalletCoinSpecifics(Wallet wallet) {
        return EmptyWalletCoinSpecifics.INSTANCE;
    }

    @Override
    public WalletProtobufSerializerExtension createWalletProtobufSerializerExtension(WalletProtobufSerializer walletProtobufSerializer) {
        return EmptyWalletProtobufSerializerExtension.INSTANCE;
    }

    @Override
    public NetworkExtensionsContainer createNetworkExtensionsContainer(NetworkParameters params) {
        return EmptyNetworkExtensions.INSTANCE;
    }

    @Override
    public NetworkExtensionsContainer createNetworkExtensionsContainer(NetworkParameters params, @Nullable NetworkMode networkMode) {
        return EmptyNetworkExtensions.INSTANCE;
    }

    @Nullable
    @Override
    public String getInventoryTypeByCode(int typeCode) {
        return null;
    }

    @Nullable
    @Override
    public Integer getInventoryTypeOrdinal(String type) {
        return null;
    }

    static final long testnetDiffDate = 1329264000000L;

    @Nullable
    private static Object networkCheck(@Nullable Object first, @Nullable Object second, StandardNetworkId networkId) {
        return Util.networkCheck(first, second, Util.UNSUPPORTED_SIG, networkId, NAME);
    }

}
