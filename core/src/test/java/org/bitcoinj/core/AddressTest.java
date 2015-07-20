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
import org.bitcoinj.params.Networks;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.coinj.api.CoinDefinition;
import org.coinj.api.CoinLocator;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.bitcoinj.core.Utils.HEX;
import static org.junit.Assert.*;

/**
 * Date: 7/15/15
 * Time: 4:52 PM
 *
 * @author Mikhail Kulikov
 */
public class AddressTest {

    static final NetworkParameters testParams = TestNet3Params.get();
    static final NetworkParameters mainParams = MainNetParams.get();

    @Test
    public void p2shAddress() throws Exception {
        // Test that we can construct P2SH addresses
        Address mainNetP2SHAddress = new Address(MainNetParams.get(), "35b9vsyH1KoFT5a5KtrKusaCcPLkiSo1tU");
        assertEquals(mainNetP2SHAddress.version, MainNetParams.get().p2shHeader);
        assertTrue(mainNetP2SHAddress.isP2SHAddress());
        Address testNetP2SHAddress = new Address(TestNet3Params.get(), "2MuVSxtfivPKJe93EC1Tb9UhJtGhsoWEHCe");
        assertEquals(testNetP2SHAddress.version, TestNet3Params.get().p2shHeader);
        assertTrue(testNetP2SHAddress.isP2SHAddress());

        // Test that we can determine what network a P2SH address belongs to
        NetworkParameters mainNetParams = Address.getParametersFromAddress("35b9vsyH1KoFT5a5KtrKusaCcPLkiSo1tU");
        assertEquals(MainNetParams.get().getId(), mainNetParams.getId());
        NetworkParameters testNetParams = Address.getParametersFromAddress("2MuVSxtfivPKJe93EC1Tb9UhJtGhsoWEHCe");
        assertEquals(TestNet3Params.get().getId(), testNetParams.getId());

        // Test that we can convert them from hashes
        byte[] hex = HEX.decode("2ac4b0b501117cc8119c5797b519538d4942e90e");
        Address a = Address.fromP2SHHash(mainParams, hex);
        assertEquals("35b9vsyH1KoFT5a5KtrKusaCcPLkiSo1tU", a.toString());
        Address b = Address.fromP2SHHash(testParams, HEX.decode("18a0e827269b5211eb51a4af1b2fa69333efa722"));
        assertEquals("2MuVSxtfivPKJe93EC1Tb9UhJtGhsoWEHCe", b.toString());
        Address c = Address.fromP2SHScript(mainParams, ScriptBuilder.createP2SHOutputScript(hex));
        assertEquals("35b9vsyH1KoFT5a5KtrKusaCcPLkiSo1tU", c.toString());
    }

    @Test
    public void stringification() throws Exception {
        // Test a testnet address.
        Address a = new Address(testParams, HEX.decode("fda79a24e50ff70ff42f7d89585da5bd19d9e5cc"));
        assertEquals("n4eA2nbYqErp7H6jebchxAN59DmNpksexv", a.toString());
        assertFalse(a.isP2SHAddress());

        Address b = new Address(mainParams, HEX.decode("4a22c3c4cbb31e4d03b15550636762bda0baf85a"));
        assertEquals("LRywuuNCDLJCX4cnQ8RxQQoLbtM5FRsgto", b.toString());
        assertFalse(b.isP2SHAddress());
    }

    @Test
    public void getNetwork() throws Exception {
        NetworkParameters params = Address.getParametersFromAddress("LQEytfirB1xVh3hJNzG4jCfb8L39kLSs7H");
        assertEquals(MainNetParams.get().getId(), params.getId());
        params = Address.getParametersFromAddress("moBDdygugKavtVdkyb6aow6wemga7zm5cF");
        assertEquals(TestNet3Params.get().getId(), params.getId());
    }

    @Test
    public void p2shAddressCreationFromKeys() throws Exception {
        // import some keys from this example: https://gist.github.com/gavinandresen/3966071
        ECKey key1 = new DumpedPrivateKey(mainParams, "6uZUEQLcANQ2HWanTsvnxvzUKeYtMdDJr7SHrmyPbgJ18zco4ez").getKey();
        key1 = ECKey.fromPrivate(key1.getPrivKeyBytes());
        ECKey key2 = new DumpedPrivateKey(mainParams, "6utr8LCDuwMdYM53Bsd1JzEG8YaYuoZnGPNDdYDAtHz2n38APap").getKey();
        key2 = ECKey.fromPrivate(key2.getPrivKeyBytes());
        ECKey key3 = new DumpedPrivateKey(mainParams, "6uZUEQLcANQ2HWanTsvnxvzUKeYtMdDJr7SHrmyPbgJ18zco4ez").getKey();
        key3 = ECKey.fromPrivate(key3.getPrivKeyBytes());

        List<ECKey> keys = Arrays.asList(key1, key2, key3);
        Script p2shScript = ScriptBuilder.createP2SHOutputScript(2, keys);
        Address address = Address.fromP2SHScript(mainParams, p2shScript);
        assertEquals("3F1ZKmboZANCfanWZLwfQTbfVPhkuw7YT3", address.toString());
    }

    @Test
    public void getAltNetwork() throws Exception {
        // An alternative network
        class AltNetwork extends MainNetParams {
            private static final long serialVersionUID = 1;

            AltNetwork(CoinDefinition def) {
                super(def);
                id = "alt.network";
                addressHeader = 0;
                p2shHeader = 5;
                acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
            }
        }
        final CoinDefinition def = CoinLocator.discoverCoinDefinition();
        AltNetwork altNetwork = new AltNetwork(def);
        // Add new network params
        Networks.register(def, altNetwork);
        // Check if can parse address
        NetworkParameters params = Address.getParametersFromAddress("12jVX52XwYJvjNPVLKRr38nfwh9cV8BiNT");
        assertEquals(altNetwork.getId(), params.getId());
        // Check if main network works as before
        params = Address.getParametersFromAddress("LRywuuNCDLJCX4cnQ8RxQQoLbtM5FRsgto");
        assertEquals(MainNetParams.get().getId(), params.getId());
        // Unregister network
        Networks.unregister(def, altNetwork);
        try {
            Address.getParametersFromAddress("12jVX52XwYJvjNPVLKRr38nfwh9cV8BiNT");
            fail();
        } catch (AddressFormatException ignore) { }
    }

    @Test
    public void decoding() throws Exception {
        Address a = new Address(testParams, "n4eA2nbYqErp7H6jebchxAN59DmNpksexv");
        assertEquals("fda79a24e50ff70ff42f7d89585da5bd19d9e5cc", Utils.HEX.encode(a.getHash160()));

        Address b = new Address(mainParams, "LRywuuNCDLJCX4cnQ8RxQQoLbtM5FRsgto");
        assertEquals("4a22c3c4cbb31e4d03b15550636762bda0baf85a", Utils.HEX.encode(b.getHash160()));
    }

    @Test
    public void errorPaths() {
        // Check what happens if we try and decode garbage.
        try {
            new Address(testParams, "this is not a valid address!");
            fail();
        } catch (WrongNetworkException e) {
            fail();
        } catch (AddressFormatException e) {
            // Success.
        }

        // Check the empty case.
        try {
            new Address(testParams, "");
            fail();
        } catch (WrongNetworkException e) {
            fail();
        } catch (AddressFormatException e) {
            // Success.
        }

        // Check the case of a mismatched network.
        try {
            new Address(testParams, "LRywuuNCDLJCX4cnQ8RxQQoLbtM5FRsgto");
            fail();
        } catch (WrongNetworkException e) {
            // Success.
            assertEquals(e.verCode, MainNetParams.get().getAddressHeader());
            assertTrue(Arrays.equals(e.acceptableVersions, TestNet3Params.get().getAcceptableAddressCodes()));
        } catch (AddressFormatException e) {
            fail();
        }
    }

}
