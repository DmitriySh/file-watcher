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
    psql -U postgres -a -f <file_path>
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

