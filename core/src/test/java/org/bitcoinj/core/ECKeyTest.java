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

import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Date: 7/17/15
 * Time: 10:40 PM
 *
 * @author Mikhail Kulikov
 */
public class ECKeyTest {

    private NetworkParameters params = MainNetParams.get();

    @Test
    public void verifyMessage() throws Exception {
        // Test vector generated by Bitcoin-Qt.
        String message = "hello";
        String sigBase64 = "HxNZdo6ggZ41hd3mM3gfJRqOQPZYcO8z8qdX2BwmpbF11CaOQV+QiZGGQxaYOncKoNW61oRuSMMF8udfK54XqI8=";
        Address expectedAddress = new Address(MainNetParams.get(), "LZ6wJiEro3QL6fJAkBJA1Gdt2TTDf1sj3U");
        ECKey key = ECKey.signedMessageToKey(message, sigBase64, params);
        Address gotAddress = key.toAddress(MainNetParams.get());
        assertEquals(expectedAddress, gotAddress);
    }

}
