/*
 * Copyright 2012 Google Inc.
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

import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.FullPrunedBlockStore;
import org.bitcoinj.store.H2FullPrunedBlockStore;
import org.junit.After;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Date: 7/18/15
 * Time: 8:53 PM
 *
 * @author Mikhail Kulikov
 *
 * An H2 implementation of the FullPrunedBlockStoreTest
 */
public class H2FullPrunedBlockChainTest extends AbstractFullPrunedBlockChainTest {

    @After
    public void tearDown() throws Exception {
        deleteFiles();
    }

    @Override
    public FullPrunedBlockStore createStore(NetworkParameters params, int blockCount) throws BlockStoreException {
        deleteFiles();
        try {
            final Connection connection = DriverManager.getConnection("jdbc:h2:test;create=true;LOCK_TIMEOUT=60000");
            Statement s = connection.createStatement();
            s.executeUpdate("DROP TABLE IF EXISTS settings");
            s.executeUpdate("DROP TABLE IF EXISTS headers");
            s.executeUpdate("DROP TABLE IF EXISTS undoableBlocks");
            s.executeUpdate("DROP TABLE IF EXISTS openOutputs");
            s.close();
            connection.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        return new H2FullPrunedBlockStore(params, "test", blockCount);
    }

    private void deleteFiles() {
        maybeDelete("test.h2.db");
        maybeDelete("test.trace.db");
    }

    private void maybeDelete(String s) {
        new File(s).delete();
    }

    @Override
    public void resetStore(FullPrunedBlockStore store) throws BlockStoreException {
        ((H2FullPrunedBlockStore)store).resetStore();
    }

}
