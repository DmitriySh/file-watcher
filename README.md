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


  
