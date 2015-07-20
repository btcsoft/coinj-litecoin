/*
 * Copyright 2012 Google Inc.
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

import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.utils.Threading;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.bitcoinj.core.Coin.valueOf;
import static org.junit.Assert.assertEquals;

/**
 * Date: 7/17/15
 * Time: 5:46 PM
 *
 * @author Mikhail Kulikov
 */
public class ChainSplitTest {

    private NetworkParameters unitTestParams;
    private Wallet wallet;
    private BlockChain chain;
    private Address coinsTo;
    private Address someOtherGuy;

    @Before
    public void setUp() throws Exception {
        BriefLogFormatter.init();
        Utils.setMockClock(); // Use mock clock
        unitTestParams = UnitTestParams.get();
        Wallet.SendRequest.DEFAULT_FEE_PER_KB = Coin.zero(unitTestParams.getCoinDefinition());
        wallet = new Wallet(unitTestParams);
        ECKey key1 = wallet.freshReceiveKey();
        MemoryBlockStore blockStore = new MemoryBlockStore(unitTestParams);
        chain = new BlockChain(unitTestParams, wallet, blockStore);
        coinsTo = key1.toAddress(unitTestParams);
        someOtherGuy = new ECKey().toAddress(unitTestParams);
    }

    @Test
    public void testDoubleSpendOnForkPending() throws Exception {
        // Check what happens when a re-org happens and one of our unconfirmed transactions becomes invalidated by a
        // double spend on the new best chain.
        final Transaction[] eventDead = new Transaction[1];
        final Transaction[] eventReplacement = new Transaction[1];
        wallet.addEventListener(new AbstractWalletEventListener() {
            @Override
            public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
                super.onTransactionConfidenceChanged(wallet, tx);
                if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD)) {
                    eventDead[0] = tx;
                    eventReplacement[0] = tx.getConfidence().getOverridingTransaction();
                }
            }
        });

        // Start with 50 coins.
        Block b1 = unitTestParams.getGenesisBlock().createNextBlock(coinsTo);
        chain.add(b1);

        Transaction t1 = checkNotNull(wallet.createSend(someOtherGuy, valueOf(10, 0)));
        Address yetAnotherGuy = new ECKey().toAddress(unitTestParams);
        Transaction t2 = checkNotNull(wallet.createSend(yetAnotherGuy, valueOf(20, 0)));
        wallet.commitTx(t1);
        // t1 is still pending ...
        Block b2 = b1.createNextBlock(new ECKey().toAddress(unitTestParams));
        chain.add(b2);
        final Coin zero = Coin.zero(unitTestParams.getCoinDefinition());
        assertEquals(zero, wallet.getBalance());
        assertEquals(valueOf(40, 0), wallet.getBalance(Wallet.BalanceType.ESTIMATED));

        // Now we make a double spend become active after a re-org.
        // genesis -> b1 -> b2 [t1 pending]
        //              \-> b3 (t2) -> b4
        Block b3 = b1.createNextBlock(new ECKey().toAddress(unitTestParams));
        b3.addTransaction(t2);
        b3.solve();
        chain.add(roundtrip(b3));  // Side chain.
        Block b4 = b3.createNextBlock(new ECKey().toAddress(unitTestParams));
        chain.add(b4);  // New best chain.
        Threading.waitForUserCode();
        // Should have seen a double spend against the pending pool.
        // genesis -> b1 -> b2 [t1 dead and exited the miners mempools]
        //              \-> b3 (t2) -> b4
        assertEquals(t1, eventDead[0]);
        assertEquals(t2, eventReplacement[0]);
        assertEquals(valueOf(30, 0), wallet.getBalance());

        // ... and back to our own parallel universe.
        Block b5 = b2.createNextBlock(new ECKey().toAddress(unitTestParams));
        chain.add(b5);
        Block b6 = b5.createNextBlock(new ECKey().toAddress(unitTestParams));
        chain.add(b6);
        // genesis -> b1 -> b2 -> b5 -> b6 [t1 still dead]
        //              \-> b3 [t2 resurrected and now pending] -> b4
        assertEquals(zero, wallet.getBalance());
        // t2 is pending - resurrected double spends take precedence over our dead transactions (which are in nobodies
        // mempool by this point).
        t1 = checkNotNull(wallet.getTransaction(t1.getHash()));
        t2 = checkNotNull(wallet.getTransaction(t2.getHash()));
        assertEquals(TransactionConfidence.ConfidenceType.DEAD, t1.getConfidence().getConfidenceType());
        assertEquals(TransactionConfidence.ConfidenceType.PENDING, t2.getConfidence().getConfidenceType());
    }

    private Block roundtrip(Block b2) throws ProtocolException {
        return new Block(unitTestParams, b2.bitcoinSerialize());
    }

}
