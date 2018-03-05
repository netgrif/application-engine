# WMS - App

Workflow Management System web application

## MySQL

Create NETGRIF user:
```mysql
CREATE USER 'netgrif_nae'@'localhost' IDENTIFIED BY 'netgrif_nae';
GRANT ALL PRIVILEGES ON * . * TO 'netgrif_nae'@'localhost';
```
Create NAE database
```mysql
CREATE DATABASE nae
  DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;
```