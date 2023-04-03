package org.nepkisa;

import io.debezium.connector.oracle.OracleConnector;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import io.debezium.relational.history.FileDatabaseHistory;
import org.apache.kafka.connect.storage.FileOffsetBackingStore;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Nepkisa
 * @date 2023/04/03 09:24:44
 */
public class OracleConsumerInfinispan {
    public static void main(String[] args) {
        final Properties props = new Properties();
        props.setProperty("name", "oracle130-0401");
        props.setProperty("database.server.name", "oracle130-0401");
        props.setProperty("connector.class", OracleConnector.class.getName());
        props.setProperty("offset.storage", FileOffsetBackingStore.class.getName());
        //指定 offset 存储目录
        props.setProperty("offset.storage.file.filename", "I:\\Code\\Java\\DebeziumCDC\\data\\oracle4.txt");
        //指定 Topic offset 写入磁盘的间隔时间
        props.setProperty("offset.flush.interval.ms", "60000");
        props.setProperty("database.hostname", "192.168.10.130");
        props.setProperty("database.port", "1521");
        props.setProperty("database.user", "cdcuser");
        props.setProperty("log.mining.view.fetch.size", "1000");
        props.setProperty("log.mining.batch.size.min", "1000");
        props.setProperty("log.mining.batch.size.max", "1000");
        props.setProperty("database.password", "123456");
        //修改为infinispan方式
        props.setProperty("log.mining.buffer.type","infinispan_embedded");
        props.setProperty("log.mining.buffer.infinispan.cache.transactions","<local-cache name=\"transactions\"><persistence passivation=\"false\"><file-store fetch-state=\"true\" read-only=\"false\" preload=\"true\" shared=\"false\" segmented=\"false\" path=\"I:/Code/Java/DebeziumCDC/data\" /></persistence></local-cache>");
        props.setProperty("log.mining.buffer.infinispan.cache.events","<local-cache name=\"events\"><persistence passivation=\"false\"><file-store fetch-state=\"true\" read-only=\"false\" preload=\"true\" shared=\"false\" segmented=\"false\" path=\"I:/Code/Java/DebeziumCDC/data\" /></persistence></local-cache>");
        props.setProperty("log.mining.buffer.infinispan.cache.processed_transactions","<local-cache name=\"processed_transactions\"><persistence passivation=\"false\"><file-store fetch-state=\"true\" read-only=\"false\" preload=\"true\" shared=\"false\" segmented=\"false\" path=\"I:/Code/Java/DebeziumCDC/data\" /></persistence></local-cache>");
        props.setProperty("log.mining.buffer.infinispan.cache.schema_changes","<local-cache name=\"schema_changes\"><persistence passivation=\"false\"><file-store fetch-state=\"true\" read-only=\"false\" preload=\"true\" shared=\"false\" segmented=\"false\" path=\"I:/Code/Java/DebeziumCDC/data\" /></persistence></local-cache>");
        //关闭采集任务时删除持久化文件
        props.setProperty("log.mining.buffer.drop.on.stop","false");
        //要捕获的数据库名
        props.setProperty("database.dbname", "nep");
        //要捕获的数据表
        props.setProperty("table.include.list", "neptune.userx");
        //增量
        props.setProperty("snapshot.mode", "schema_only");
        props.setProperty("database.history", FileDatabaseHistory.class.getCanonicalName());
        props.setProperty("database.history.file.filename", "I:\\Code\\Java\\DebeziumCDC\\data\\oracle5.txt");
        //是否输出 schema 信息
        props.setProperty("key.converter.schemas.enable", "false");
        props.setProperty("value.converter.schemas.enable", "false");
        //时区
        props.setProperty("database.serverTimezone", "UTC");
        //模式
        props.setProperty("database.connection.adapter", "logminer");

        // 2. 构建 DebeziumEngine
        // 使用 Json 格式
        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine
                .create(Json.class)
                .using(props)
                .notifying(record -> {
                    // record中会有操作的类型（增、删、改）和具体的数据
                    System.out.println(record);
                    System.out.println("record.key() = " + record.key());
                    System.out.println("record.value() = " + record.value());
                })
                .using((success, message, error) -> {
                    //查看错误信息
                    if (!success && error != null) {
                        // 报错回调
                        System.out.println("----------error------");
                        System.out.println(message);
                        System.out.println(error);
                        error.printStackTrace();
                    }
                })
                .build();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);
        addShutdownHook(engine);
        awaitTermination(executor);

        System.out.println("------------main finished.");
    }

    private static void closeEngine(DebeziumEngine<ChangeEvent<String, String>> engine) {
        try {
            engine.close();
        } catch (IOException ignored) {
        }
    }

    private static void addShutdownHook(DebeziumEngine<ChangeEvent<String, String>> engine) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> closeEngine(engine)));
    }

    private static void awaitTermination(ExecutorService executor) {
        if (executor != null) {
            try {
                executor.shutdown();
                while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
