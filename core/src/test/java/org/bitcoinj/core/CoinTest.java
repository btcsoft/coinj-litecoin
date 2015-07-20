/**
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

import org.junit.Test;

import static org.bitcoinj.core.Coin.valueOf;
import static org.junit.Assert.assertEquals;

/**
 * Date: 7/17/15
 * Time: 10:20 PM
 *
 * @author Mikhail Kulikov
 */
public class CoinTest {

    private static final Coin COIN = Coin.coin();

    @Test
    public void testToFriendlyString() {
        assertEquals("1.00 LTC", COIN.toFriendlyString());
        assertEquals("1.23 LTC", valueOf(1, 23).toFriendlyString());
        assertEquals("0.001 LTC", COIN.divide(1000).toFriendlyString());
        assertEquals("-1.23 LTC", valueOf(1, 23).negate().toFriendlyString());
    }

}
