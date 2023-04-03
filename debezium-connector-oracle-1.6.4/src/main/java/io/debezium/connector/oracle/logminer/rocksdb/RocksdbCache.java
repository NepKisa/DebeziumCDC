package io.debezium.connector.oracle.logminer.rocksdb;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

/**
 * @author Nepkisa
 * @date 2023/04/03 14:22:59
 */
public class RocksdbCache {
    public static void main(String[] args) {
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();

        // the Options class contains a set of configurable DB options
        // that determines the behaviour of the database.
        try (final Options options = new Options().setCreateIfMissing(true)) {

            // a factory method that returns a RocksDB instance
            try (final RocksDB db = RocksDB.open(options, "/data")) {

                // do something
                byte[] key1 = new byte[0];
                byte[] key2 = new byte[0];
// some initialization for key1 and key2
                final byte[] value = db.get(key1);
                if (value != null) {  // value == null if key1 does not exist in db.
                    db.put(key2, value);
                }
                db.delete(key1);
            } catch (RocksDBException e) {
                // do some error handling
            }
        }
    }
}