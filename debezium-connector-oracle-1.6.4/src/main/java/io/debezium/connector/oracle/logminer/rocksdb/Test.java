package io.debezium.connector.oracle.logminer.rocksdb;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class Test {
    static {
        RocksDB.loadLibrary();
    }
    private static RocksDB rocksDB;
    private static String path = "/data";

    public static void main(String[] args) throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        rocksDB = RocksDB.open(options,path);
        rocksDB.put("aaa".getBytes(),"bbb".getBytes());
        System.out.println("-----");
        byte[] bytes = rocksDB.get("aaa".getBytes());
        System.out.println(new String(bytes));
        RocksIterator iter = rocksDB.newIterator();
        for (iter.seekToFirst();iter.isValid();iter.next()) {
            System.out.println("iter key: " + new String(iter.key()) + ",iter value: " +
                                new String(iter.value()));
        }
    }
}