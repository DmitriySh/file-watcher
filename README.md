file-watcher
=======

My pet project.  
This is a prototype of multithread service to watchs the directory, retrieves XML files (specific scheme) and persists contents to database. Powered by: `Java SE`, `Spring Data Framework`, `JPA / Hibernate`, `PostgreSQL` and `XML`. Project uses SEDA like highly customizable thread model: independent tasks for watching, parsing, persisting datas and queues between them.
  
  
## Rules:  

  * Choose the directory that needs to be watch. 
  * Successfully processed XML files move to `<dir>/success/` directory.
  * Files should stay in the same directory if they have processed with fail.
  * XML files should have similiar content:  
```xml  
<?xml version="1.0" encoding="utf-8" ?>
<Entry>
    <!-- length of string 1024 characters -->
    <content>Text</content>
    <!-- local date -->
    <creationDate>2014-01-01 00:00:00</creationDate>
</Entry>
```   

  
## Requirements:

  * Java SE Development Kit 7 (or newer)  
  * Gradle 2.x  
  * PostgreSQL 9.4 (older versions might be unsupported by JDBC driver)  
  * Git 1.7.x (or newer)  


## Project configuration:  

  * PostgreSQL should be installed and has user with password (`postgres` by default). 
    * Create database by ddl script [`test_db.ddl`](https://github.com/DmitriySh/file-watcher/blob/develop/src/main/resources/db/test_db.ddl) :
    ```sh  
    sudo -i -u postgres
    psql -U postgres -a -f <ddl_path>
    ```   
    ```sql  
    DROP DATABASE IF EXISTS  "test_db";

    CREATE DATABASE "test_db";
    \c "test_db";

    CREATE TABLE "entry" (
     "id"            BIGSERIAL                   NOT NULL,
     "version"       BIGINT                      NOT NULL,
     "content"       TEXT                        NOT NULL,
     "creation_date" TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT "entry_id_pk" PRIMARY KEY ("id")
    );
    ```  
    
  * Java SE should be installed and you need to set path variable for `JAVA_HOME`.
  * Gradle doesn't need to install because you might do this automatically thanks Gradle Wrapper.
  * There are two configuration files:
[`app.properties`](https://github.com/DmitriySh/file-watcher/blob/develop/src/main/resources/app.properties) and `app.local.properties`. Create and use `app.local.properties` file next to [`app.properties`](https://github.com/DmitriySh/file-watcher/blob/develop/src/main/resources/app.properties) to override main properties if you need it. You can tune it according to your choice.
```properties  
# Use the file 'app.local.properties' for overriding current properties.
                                                                      
###################################
# Configuration DB
###################################
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/test_db
db.username=postgres
db.password=postgres
db.poolsize.min=5
db.poolsize.max=30
db.poolsize.increment=5
db.statements=50

###################################
# Configuration File Watcher
###################################
# you can specify start path with {user.home} or {user.dir}
directory.path={user.home}/storage
# quantity of parallel tasks
parser.parallel.tasks=10
persist.parallel.tasks=5

```   


## Run
  *  Build project. Go to the root path `/file-watcher/` of the project and run:  
```sh
file-watcher$ ./gradlew clean build

:clean
:compileJava
:processResources
:classes
:jar
:assemble
:compileTestJava
:processTestResources
:testClasses
:test
:check
:build

BUILD SUCCESSFUL

Total time: 46.079 secs

```  
  *  Run server: 
```sh
file-watcher$ java -jar target/libs/file-watcher-all-0.1.jar

04.10.15 14:59:24 INFO  - Server               - Initialise server ... (Server.java:68)
04.10.15 14:59:24 DEBUG - DbUtil               - Check connection to DB ...  (DbUtil.java:18)
04.10.15 14:59:24 INFO  - Server               - Connected to DB on jdbc:postgresql://localhost:5432/test_db: driver: PostgreSQL 9.4 JDBC4.1 (build 1203) (Server.java:79)
04.10.15 14:59:24 DEBUG - Server               - Check directory ...  (Server.java:148)
04.10.15 14:59:24 DEBUG - SymlinkLoopUtil      - Check symlink ...  (SymlinkLoopUtil.java:29)
04.10.15 14:59:24 DEBUG - SymlinkLoopUtil      - Target of link '/home/dima/link' -> '/home/dima/programming/link_storage' (SymlinkLoopUtil.java:33)
04.10.15 14:59:24 INFO  - Server               - Directory path: /home/dima/link (Server.java:163)
04.10.15 14:59:24 INFO  - FileWatcher          - Initialise file watcher ... (FileWatcher.java:45)
04.10.15 14:59:24 INFO  - FileWatcher          - --> put file 'dd_link2.xml' : directoryQueue (FileWatcher.java:110)
04.10.15 14:59:24 INFO  - FileWatcher          - --> put file 'entry_valid.xml' : directoryQueue (FileWatcher.java:110)
04.10.15 14:59:24 INFO  - FileWatcher          - --> put file 'file3.xml' : directoryQueue (FileWatcher.java:110)
04.10.15 14:59:24 INFO  - FileParser           - Initialise file parser 1 ... (FileParser.java:58)
04.10.15 14:59:24 INFO  - FileParser           - Initialise file parser 2 ... (FileParser.java:58)
04.10.15 14:59:24 INFO  - FileParser           - Initialise file parser 3 ... (FileParser.java:58)
04.10.15 14:59:24 INFO  - FileParser           - Initialise file parser 6 ... (FileParser.java:58)
04.10.15 14:59:24 INFO  - FileParser           - Initialise file parser 4 ... (FileParser.java:58)
04.10.15 14:59:24 INFO  - FileParser           - Initialise file parser 5 ... (FileParser.java:58)
04.10.15 14:59:24 INFO  - FileParser           - <-- take file 'dd_link2.xml' : directoryQueue (FileParser.java:65)
04.10.15 14:59:24 INFO  - FileParser           - <-- take file 'file3.xml' : directoryQueue (FileParser.java:65)
04.10.15 14:59:24 INFO  - FileParser           - <-- take file 'entry_valid.xml' : directoryQueue (FileParser.java:65)
04.10.15 14:59:24 INFO  - FilePersist          - Initialise file persist 1 ... (FilePersist.java:48)
04.10.15 14:59:24 INFO  - FilePersist          - Initialise file persist 2 ... (FilePersist.java:48)
04.10.15 14:59:24 INFO  - FilePersist          - Initialise file persist 3 ... (FilePersist.java:48)
04.10.15 14:59:24 INFO  - FilePersist          - Initialise file persist 4 ... (FilePersist.java:48)
04.10.15 14:59:24 INFO  - FilePersist          - Initialise file persist 5 ... (FilePersist.java:48)
04.10.15 14:59:24 INFO  - Server               - Start the server: . Watch on: /home/dima/link (Server.java:90)

... <cut> ...

04.10.15 15:09:37 DEBUG - FileParser           - Start parse XML... (FileParser.java:144)
04.10.15 15:09:37 DEBUG - FileParser           - Stop parse XML... (FileParser.java:149)
04.10.15 15:09:37 INFO  - FileParser           - --> put transient entity and file '{file=file3.xml, entry=Entry{id=null, content='Text length 1024. Text le ...', creationDate=2014-01-01T00:00:00.000}}' : successQueue (FileParser.java:125)
04.10.15 15:09:37 INFO  - FilePersist          - <-- take transient entity 'Entry{id=null, content='Text length 1024. Text le ...', creationDate=2014-01-01T00:00:00.000}' : successQueue (FilePersist.java:55)

... <cut> ..

04.10.15 15:09:37 INFO  - FilePersist          - persist entity 'Entry{id=1, content='Text length 1024. Text le ...', creationDate=2014-01-01T00:00:00.000}' (FilePersist.java:58)
04.10.15 15:09:37 INFO  - FilePersist          - move file 'file3.xml' to directory '/home/dima/link/success' (FilePersist.java:78)


```  
  * Log into log.txt:
```sh  
04.10.15 13:38:04 INFO  - Server               - Initialise server ... (Server.java:68)
04.10.15 13:38:04 DEBUG - DbUtil               - Check connection to DB ...  (DbUtil.java:18)
04.10.15 13:38:04 INFO  - Server               - Connected to DB on jdbc:postgresql://localhost:5432/test_db: driver: PostgreSQL 9.4 JDBC4.1 (build 1203) (Server.java:79)
04.10.15 13:38:04 DEBUG - Server               - Check directory ...  (Server.java:148)
04.10.15 13:38:04 DEBUG - SymlinkLoopUtil      - Check symlink ...  (SymlinkLoopUtil.java:29)
04.10.15 13:38:04 DEBUG - SymlinkLoopUtil      - Target of link '/home/dima/link' -> '/home/dima/programming/link_storage' (SymlinkLoopUtil.java:33)
04.10.15 13:38:04 INFO  - Server               - Directory path: /home/dima/link (Server.java:163)
04.10.15 13:38:04 INFO  - FileWatcher          - Initialise file watcher ... (FileWatcher.java:45)

... <cut> ...
```  

## Stop
  * `File Watcher` is terminated in response to a user interrupt, such as typing `^C` (Ctrl + C), or a system-wide event of shutdown.  
```sh
04.10.15 15:09:45 DEBUG - Server               - Shutdown hook has been invoked (Server.java:211)
04.10.15 15:09:45 INFO - Server               - Finalization server ... (Server.java:98)
```
