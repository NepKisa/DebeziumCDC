# Introduction
This project is based on debezium-1.9.5.Final from https://github.com/debezium/debezium

# Run Code In IDE
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
# Current Problem
## A Long Transaction Cause OOM
### 1. a long insert transaction with 30w rows, it will produce 1 start record + 30w insert records + 1 commit/rollback record
> This can be known by analyzing the heap dump file
> 
> * debezium will read records sequentially, 
> 
> * when read transaction type is start will execute handleStart() create cache,
> 
> * when read transaction type is commit will execute handleCommit() remove cache,
> 
> * when read transaction type is (insert、update、delete) , the parsed events(String) will add to  the list in this cache, oom will occured at this stage

### 2. lock all tables in capture list when snapshot stage 
> Beacuse when create the connector’s LogMiner user execute the following SQL satement
> ```sql
> GRANT CREATE TABLE TO CDCUSER
> ```

### 3. the performance is lower than expected
> method **emitChangeRecords()** in class **RelationalChangeRecordEmitter** execute tableSchema.valueFromColumnData() will reduce performance ? (other's publish)

### 4. debezium1.9.3+ fixed the bug oracle server logminer session occupies memory and does not release(this problem is not verified, other's publish) 

# TODO
### 1. Upgrade the debezium dependency from 1.6.4.Final to 1.9.5.Final+ in Flink CDC 2.3
**this would address the 2、4 issues above**

### 2. Use object stream, Serialize uncommited transactions into file
> * such as per 1000 records switch a file to avoid memory leakage
>
> * start dedicated write and read threads respectively, adopt produser/consumer mode,
>
> * construct a buffer queue to manage uncommited transactions

### 3. Write directly to kafka without formatting
> override method emitChangeRecords() in class LogMinerChangeRecordEmitter