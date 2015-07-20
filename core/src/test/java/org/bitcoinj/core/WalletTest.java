/**
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
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

import org.bitcoinj.testing.TestWithWallet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.bitcoinj.testing.FakeTxBuilder.createFakeTxWithChangeAddress;
import static org.bitcoinj.testing.FakeTxBuilder.makeSolvedTestBlock;
import static org.junit.Assert.assertEquals;

/**
 * Date: 7/18/15
 * Time: 10:46 PM
 *
 * @author Mikhail Kulikov
 */
public class WalletTest extends TestWithWallet {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void basicCategoryStepTest() throws Exception {
        // Creates spends that step through the possible fee solver categories
        Wallet.SendRequest.DEFAULT_FEE_PER_KB = zero;
        // Make sure TestWithWallet isnt doing anything crazy.
        assertEquals(0, wallet.getTransactions(true).size());

        Address notMyAddr = new ECKey().toAddress(params);

        // Generate a ton of small outputs
        StoredBlock block = new StoredBlock(makeSolvedTestBlock(blockStore, notMyAddr), BigInteger.ONE, 1);
        int i = 0;
        Coin tenThousand = Coin.valueOf(10000);
        while (i <= 100) {
            Transaction tx = createFakeTxWithChangeAddress(params, tenThousand, myAddress, notMyAddr);
            tx.getInput(0).setSequenceNumber(i++); // Keep every transaction unique
            wallet.receiveFromBlock(tx, block, AbstractBlockChain.NewBlockType.BEST_CHAIN, i);
        }
        Coin balance = wallet.getBalance();

        // Create a spend that will throw away change (category 3 type 2 in which the change causes fee which is worth more than change)
        Wallet.SendRequest request1 = Wallet.SendRequest.to(notMyAddr, balance.subtract(satoshi));
        wallet.completeTx(request1);
        assertEquals(satoshi, request1.tx.getFee());
        assertEquals(request1.tx.getInputs().size(), i); // We should have spent all inputs

        // Give us one more input...
        Transaction tx1 = createFakeTxWithChangeAddress(params, tenThousand, myAddress, notMyAddr);
        tx1.getInput(0).setSequenceNumber(i++); // Keep every transaction unique
        wallet.receiveFromBlock(tx1, block, AbstractBlockChain.NewBlockType.BEST_CHAIN, i);

        // ... and create a spend that will throw away change (category 3 type 1 in which the change causes dust output)
        Wallet.SendRequest request2 = Wallet.SendRequest.to(notMyAddr, balance.subtract(satoshi));
        wallet.completeTx(request2);
        assertEquals(satoshi, request2.tx.getFee());
        assertEquals(request2.tx.getInputs().size(), i - 1); // We should have spent all inputs - 1

        // Give us one more input...
        Transaction tx2 = createFakeTxWithChangeAddress(params, tenThousand, myAddress, notMyAddr);
        tx2.getInput(0).setSequenceNumber(i++); // Keep every transaction unique
        wallet.receiveFromBlock(tx2, block, AbstractBlockChain.NewBlockType.BEST_CHAIN, i);

        // ... and create a spend that will throw away change (category 3 type 1 in which the change causes dust output)
        // but that also could have been category 2 if it wanted
        Wallet.SendRequest request3 = Wallet.SendRequest.to(notMyAddr, cent.add(tenThousand).subtract(satoshi));
        wallet.completeTx(request3);
        assertEquals(satoshi, request3.tx.getFee());
        assertEquals(request3.tx.getInputs().size(), i - 2); // We should have spent all inputs - 2

        //
        Wallet.SendRequest request4 = Wallet.SendRequest.to(notMyAddr, balance.subtract(satoshi));
        request4.feePerKb = minTxFee.divide(request3.tx.bitcoinSerialize().length);
        wallet.completeTx(request4);
        System.out.println(request4.tx.bitcoinSerialize().length);
        assertEquals(Coin.valueOf(10001, params), request4.tx.getFee());
        assertEquals(request4.tx.getInputs().size(), i - 1); // We should have spent all inputs - 1

        // Give us a few more inputs...
        while (wallet.getBalance().compareTo(cent.multiply(2)) < 0) {
            Transaction tx3 = createFakeTxWithChangeAddress(params, tenThousand, myAddress, notMyAddr);
            tx3.getInput(0).setSequenceNumber(i++); // Keep every transaction unique
            wallet.receiveFromBlock(tx3, block, AbstractBlockChain.NewBlockType.BEST_CHAIN, i);
        }

        // ...that is just slightly less than is needed for category 1
        Wallet.SendRequest request5 = Wallet.SendRequest.to(notMyAddr, cent.add(tenThousand).subtract(satoshi));
        wallet.completeTx(request5);
        assertEquals(satoshi, request5.tx.getFee());
        assertEquals(1, request5.tx.getOutputs().size()); // We should have no change output

        // Give us one more input...
        Transaction tx4 = createFakeTxWithChangeAddress(params, tenThousand, myAddress, notMyAddr);
        tx4.getInput(0).setSequenceNumber(i); // Keep every transaction unique
        wallet.receiveFromBlock(tx4, block, AbstractBlockChain.NewBlockType.BEST_CHAIN, i);

        // ... that puts us in category 1 (no fee!)
        Wallet.SendRequest request6 = Wallet.SendRequest.to(notMyAddr, cent.add(tenThousand).subtract(satoshi));
        wallet.completeTx(request6);
        assertEquals(zero, request6.tx.getFee());
        assertEquals(2, request6.tx.getOutputs().size()); // We should have a change output

        Wallet.SendRequest.DEFAULT_FEE_PER_KB = minTxFee;
    }

}
