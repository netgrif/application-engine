# Netgrif Application Engine

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


## DOCKER
```
sudo docker build -t netgrif/4.1.2 .
sudo docker image ls
sudo docker run --publish 8000:8080 netgrif/4.1.2
```