/*
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

import org.bitcoinj.crypto.BIP38PrivateKey;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Date: 7/19/15
 * Time: 12:28 AM
 *
 * @author Mikhail Kulikov
 */
public class BIP38PrivateKeyTest {

    private static final MainNetParams MAINNET = MainNetParams.get();

    @Test
    public void bip38TestVector() throws Exception {
        testEncryptedKey("6PfM1h6cZSCh7RXD3tG44cDhcVcPAmfGDGbuKX5zyrafejKiiamaTYat3a", "LWFV3MfjJV6D2tPz5gP8qnhfFpgiuBztTJ", "hello");
        testEncryptedKey("6PfPk46EPA5ynCwa22hyQrsjsEYvybm2G6bmCydPgumTY1EhuHG5aQ7Nfy", "LSgT5i4AaNjoWosWtGL5fT5toySs5CFzWt", "hello");
        testEncryptedKey("6PfRGtaRNme92T7b8V5kTu2ib2y5Mjwgj8ehPcYsvfxXAfuQ4qGhWo7vR6", "LLynr1GLHpY2Mdd1TCt2ZyhNum8B3yU756", "hello");
    }

    private void testEncryptedKey(String encKey, String addr, String pass) throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MAINNET, encKey);
        ECKey key = encryptedKey.decrypt(pass);
        assertEquals(addr, new Address(MAINNET, key.getPrivateKeyEncoded(MAINNET).getKey().getPubKeyHash()).toString());
    }

}
