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

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.coinj.commons.AbstractComplexBlockHasher;
import org.coinj.scrypt.ScryptAlg;

/**
* Date: 5/17/15
* Time: 1:27 PM
*
* @author Mikhail Kulikov
*/
public final class LitecoinBlockHasher extends AbstractComplexBlockHasher {

    LitecoinBlockHasher() {}

    @Override
    protected Sha256Hash calculatePofHash(Block block) {
        return new Sha256Hash(Utils.reverseBytes(ScryptAlg.scryptDigest(block.headerAsBytes())));
    }

    @Override
    protected LitecoinBlockHasher construct() {
        return new LitecoinBlockHasher();
    }

    @Override
    protected Sha256Hash calculateHash(Block block) {
        return new Sha256Hash(Utils.dSha256Hash(block.headerAsBytes()));
    }

}
