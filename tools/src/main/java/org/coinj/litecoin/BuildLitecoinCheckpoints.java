package org.coinj.litecoin;

import org.bitcoinj.tools.BuildCheckpoints;
import org.coinj.api.CoinLocator;

/**
 * Date: 7/11/15
 * Time: 9:33 PM
 *
 * @author Mikhail Kulikov
 */
public final class BuildLitecoinCheckpoints {

    public static void main(String[] args) {
        CoinLocator.registerCoin(LitecoinDefinition.INSTANCE);
        try {
            //args = new String[5];
            //args[0] = "useDiscovery";
            //args[1] = "networkId";
            //args[2] = "org.litecoin.test";
            //args[3] = "fastCatchupTimeSecs";
            //args[4] = "1317798646";
            BuildCheckpoints.main(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private BuildLitecoinCheckpoints() {}

}
