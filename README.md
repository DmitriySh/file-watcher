file-watcher
=======

My pet project.  
This is a prototype of multithread service to watchs the directory, retrieves XML files (specific scheme) and persists contents to database. Powered by: `Java SE`, `Spring Data Framework`, `JPA / Hibernate`, `PostgreSQL` and `XML`. Project uses SEDA like highly customizable thread model: independent tasks for watching, parsing, persisting datas and queues between them.
  
  
## Rules:  

  * Choose the directory which needs to be watch. 
  * Configure the database.
  * Successfully processed XML files move to `./success/` directory.
  * Files have processed with fail should stay in the same directory.

