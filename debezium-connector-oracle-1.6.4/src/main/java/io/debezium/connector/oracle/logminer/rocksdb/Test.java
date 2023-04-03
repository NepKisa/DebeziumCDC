package io.debezium.connector.oracle.logminer.rocksdb;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class Test {

    public static void main(String[] args) throws RocksDBException {
        String id = "123456-aaa";
        Student student = new Student("unicorn", "female", 17);

        RocksDbCache.put(id, student);
        Student student1 = (Student) RocksDbCache.get(id);
        System.out.println(student1);
    }

}