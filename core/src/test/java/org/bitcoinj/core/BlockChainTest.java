/**
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
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

package org.bitcoinj.core;

import com.google.common.util.concurrent.ListenableFuture;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.coinj.api.CoinDefinition;
import org.coinj.api.CoinLocator;
import org.coinj.litecoin.LitecoinDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Date: 7/16/15
 * Time: 9:17 PM
 *
 * @author Mikhail Kulikov
 */
public class BlockChainTest {
    private static final Logger logger = LoggerFactory.getLogger(BlockChainTest.class);
    private BlockChain testNetChain;

    private BlockChain chain;
    private BlockStore blockStore;
    private Address coinbaseTo;
    private NetworkParameters unitTestParams;
    private final StoredBlock[] block = new StoredBlock[1];

    private static class TweakableTestNet2Params extends TestNet3Params {
        private static final long serialVersionUID = 1;

        public TweakableTestNet2Params(CoinDefinition coinDefinition) {
            super(coinDefinition);
        }

        public void setMaxTarget(BigInteger limit) {
            maxTarget = limit;
        }
    }
    private static final TweakableTestNet2Params testNet = new TweakableTestNet2Params(CoinLocator.discoverCoinDefinition());

    private void resetBlockStore() {
        blockStore = new MemoryBlockStore(unitTestParams);
    }

    @Before
    public void setUp() throws Exception {
        BriefLogFormatter.initVerbose();
        testNetChain = new BlockChain(testNet, new Wallet(testNet), new MemoryBlockStore(testNet));

        unitTestParams = UnitTestParams.get();
        Wallet.SendRequest.DEFAULT_FEE_PER_KB = Coin.zero(unitTestParams.getCoinDefinition());

        final Wallet wallet = new Wallet(unitTestParams) {
            @Override
            public void receiveFromBlock(Transaction tx, StoredBlock block, BlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
                super.receiveFromBlock(tx, block, blockType, relativityOffset);
                BlockChainTest.this.block[0] = block;
            }
        };

        wallet.freshReceiveKey();

        resetBlockStore();
        chain = new BlockChain(unitTestParams, wallet, blockStore);

        coinbaseTo = wallet.currentReceiveKey().toAddress(unitTestParams);
    }

    @After
    public void tearDown() {
        Wallet.SendRequest.DEFAULT_FEE_PER_KB = Wallet.SendRequest.defaultMinTransactionFee();
    }

    @Test
    public void difficultyTransitions() throws Exception {
        // Add a bunch of blocks in a loop until we reach a difficulty transition point. The unit test params have an
        // artificially shortened period.
        Block prev = unitTestParams.getGenesisBlock();
        int height = 0;
        Utils.setMockClock(System.currentTimeMillis()/1000);
        for (int i = 0; i < unitTestParams.getInterval(prev, height++) - 1; i++) {
            Block newBlock = prev.createNextBlock(coinbaseTo, Utils.currentTimeSeconds());
            assertTrue(chain.add(newBlock));
            prev = newBlock;
            // The fake chain should seem to be "fast" for the purposes of difficulty calculations.
            Utils.rollMockClock(unitTestParams.getTargetSpacing(prev, height) + 2);
        }
        // Now add another block that has no difficulty adjustment, it should be rejected.
        try {
            chain.add(prev.createNextBlock(coinbaseTo, Utils.currentTimeSeconds()));
            fail();
        } catch (VerificationException ignore) {}

        // Create a new block with the right difficulty target given our blistering speed relative to the huge amount
        // of time it's supposed to take (set in the unit test network parameters).
        Block b = prev.createNextBlock(coinbaseTo, Utils.currentTimeSeconds());
        b.setDifficultyTarget(0x2000ccccL);
        b.solve();
        assertTrue(chain.add(b));
        // Successfully traversed a difficulty transition period.
    }

    @Test
    public void duplicates() throws Exception {
        // Adding a block twice should not have any effect, in particular it should not send the block to the wallet.
        Block b1 = unitTestParams.getGenesisBlock().createNextBlock(coinbaseTo);
        Block b2 = b1.createNextBlock(coinbaseTo);
        Block b3 = b2.createNextBlock(coinbaseTo);
        assertTrue(chain.add(b1));
        assertEquals(b1, block[0].getHeader());
        assertTrue(chain.add(b2));
        assertEquals(b2, block[0].getHeader());
        assertTrue(chain.add(b3));
        assertEquals(b3, block[0].getHeader());
        assertEquals(b3, chain.getChainHead().getHeader());
        assertTrue(chain.add(b2));
        assertEquals(b3, chain.getChainHead().getHeader());
        // Wallet was NOT called with the new block because the duplicate add was spotted.
        assertEquals(b3, block[0].getHeader());
    }

    @Test
    public void testBasicChaining() throws Exception {
        // Check that we can plug a few blocks together and the futures work.
        ListenableFuture<StoredBlock> future = testNetChain.getHeightFuture(2);
        // Block 1 from the testnet.
        Block b1 = getBlock1();
        assertTrue(testNetChain.add(b1));
        assertFalse(future.isDone());
        // Block 2 from the testnet.
        Block b2 = getBlock2();

        // Let's try adding an invalid block.
        long n = b2.getNonce();
        try {
            b2.setNonce(12345);
            testNetChain.add(b2);
            fail();
        } catch (VerificationException e) {
            b2.setNonce(n);
        }

        // Now it works because we reset the nonce.
        assertTrue(testNetChain.add(b2));
        assertTrue(future.isDone());
        assertEquals(2, future.get().getHeight());
    }

    @Test
    public void estimatedBlockTime() throws Exception {
        NetworkParameters params = MainNetParams.get();
        BlockChain prod = new BlockChain(params, new MemoryBlockStore(params));
        Date d = prod.estimateBlockTime(200000);
        // The actual date of block 200,000 was 2012-08-30 14:08:39
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse("2012-09-18T05:51:05.000-0700"), d);
    }

    @Test
    public void badDifficulty() throws Exception {
        assertTrue(testNetChain.add(getBlock1()));
        Block b2 = getBlock2();
        assertTrue(testNetChain.add(b2));
        Block bad = new Block(testNet);
        // Merkle root can be anything here, doesn't matter.
        bad.setMerkleRoot(new Sha256Hash("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        // Nonce was just some number that made the hash < difficulty limit set below, it can be anything.
        bad.setNonce(314);
        bad.setTime(1279242649);
        bad.setPrevBlockHash(b2.getHash());
        // We're going to make this block so easy 50% of solutions will pass, and check it gets rejected for having a
        // bad difficulty target. Unfortunately the encoding mechanism means we cannot make one that accepts all
        // solutions.
        bad.setDifficultyTarget(testNet.getCoinDefinition().getEasiestDifficultyTarget());
        try {
            testNetChain.add(bad);
            // The difficulty target above should be rejected on the grounds of being easier than the networks
            // allowable difficulty.
            fail();
        } catch (VerificationException e) {
            assertTrue(e.getMessage(), e.getCause().getMessage().contains("Difficulty target is bad"));
        }

        // Accept any level of difficulty now.
        BigInteger oldVal = testNet.getMaxTarget();
        testNet.setMaxTarget(LitecoinDefinition.UNITTEST_MAX_TARGET);
        try {
            testNetChain.add(bad);
            // We should not get here as the difficulty target should not be changing at this point.
            fail();
        } catch (VerificationException e) {
            logger.info("badDifficulty() last exception", e);
            assertTrue(e.getMessage(), e.getCause().getMessage().contains("Unexpected change in difficulty"));
        }
        testNet.setMaxTarget(oldVal);
    }

    // Some blocks from the test net.
    private static Block getBlock2() throws Exception {
        Block b2 = new Block(testNet);
        b2.setMerkleRoot(new Sha256Hash("5ee74271f3a13a3853c3b7a61f12d2ae4414437ac9ff6ac3746b9e34c1a90922"));
        b2.setNonce(2156988416L);
        b2.setTime(1365456256);
        b2.setDifficultyTarget(504365040);
        b2.setPrevBlockHash(new Sha256Hash("4daf3f5d54e7a2d448bd185b0f68fbc97d811ffa1a8bade3b4c1cdbe8a91c90c"));
        assertEquals("d6dc101d863b3cc575fbe117e2b32abaef931dfda26a690b50d087e72ba9f0ab", b2.getHashAsString());
        b2.verifyHeader();
        return b2;
    }

    private static Block getBlock1() throws Exception {
        Block b1 = new Block(testNet);
        b1.setMerkleRoot(new Sha256Hash("3090c24778a61763e1d060abf382ebcb9bf311a86dbe4d1dcc1218324e1f2ff8"));
        b1.setNonce(2790195456L);
        b1.setTime(1365456253);
        b1.setPrevBlockHash(new Sha256Hash("f5ae71e26c74beacc88382716aced69cddf3dffff24f384e1808905e0188f68f"));
        b1.setDifficultyTarget(504365055);
        assertEquals("4daf3f5d54e7a2d448bd185b0f68fbc97d811ffa1a8bade3b4c1cdbe8a91c90c", b1.getHashAsString());
        b1.verifyHeader();
        return b1;
    }

}
