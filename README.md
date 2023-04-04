# Introduction
The project is based on debezium-1.9.5.Final from https://github.com/debezium/debezium

# Environment

## Software

| Software | Version     |
| -------- | ----------- |
| java     | 1.8.0_321   |
| maven    | 3.8.5       |
| oracle   | 19c         |
| debezium | 1.9.5.Final |

## jvm options

```perl
-ea -Xmx100m -Xms100m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:\Users\hakuou\Desktop\error2.log -Dlog4j.skipJansi=false
```

# Build In Your IDE
First you need download oracle client from https://www.oracle.com/cn/database/technologies/instant-client/downloads.html

If you don't want to download, then you can use the jar in directory lib

Install the oracle xstreams.jar to local maven repository

```perl
mvn install:install-file -Dfile=C:\Users\hakuou\Downloads\instantclient-basic-windows.x64-21.7.0.0.0dbru\instantclient_21_7\xstreams.jar -DgroupId=com.oracle.instantclient -DartifactId=xstreams -Dversion=21.7.0.0.0 -Dpackaging=jar
```
The depenedency in pom.xml like this

```xml
        <dependency>
            <groupId>com.oracle.instantclient</groupId>
            <artifactId>xstreams</artifactId>
            <version>21.7.0.0.0</version>
            <scope>provided</scope>
        </dependency>
```
# Issues
### 1. A Long Transaction Cause OOM

> These can be known by analyzing the heap dump file and read debezium-connector-oracle
> 
> * debezium will read records sequentially
> * a long insert transaction with 30w rows, it will produce 1 start record + 30w insert records + 1 commit/rollback record
> * when read transaction type is start will execute handleStart() create cache
> * when read transaction type is commit will execute handleCommit() remove cache
> * when read transaction type is (insert、update、delete) , the parsed events(String) will add to  the list in this cache, oom will occured at this stage

### 2. lock all tables in capture list when snapshot stage 
> Beacuse when create the connector’s LogMiner user execute the following SQL satement
> ```sql
> GRANT LOCK ANY TABLE TO CDCUSER;
> ```

### 3. the performance is lower than expected
> method **emitChangeRecords()** in class **RelationalChangeRecordEmitter** execute tableSchema.valueFromColumnData() will reduce performance ? (other's publish)

### 4. debezium-1.9.3 fixed the bug oracle server logminer session occupies memory and does not release(this issue is not verified, other's publish) 

# TODO
### 1. Upgrade the debezium dependency from 1.6.4.Final to 1.9.5.Final in Flink CDC 2.3
> this would address the 2、4 issues above

### 2. Use object stream, Serialize uncommited transactions into RocksDB
> use rocksDB save events replace memory

### 3. Write directly to kafka without formatting
> override method emitChangeRecords() in class LogMinerChangeRecordEmitter

