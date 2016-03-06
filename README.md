# ScavengerHuntServer

Simple server for ScavengerHuntApp learning purposes

How to setup the standalone server:

SW Requirements:
	mariadb (mysql) - databases to hold most of the information on the server
	maven - only needed to edit the server
	IntelliJ - To develop Java server
	JDK 1.8

1. git clone https://github.com/Dhsieh/ScavengerHuntServer
2. log in to mariadb and create the android_db database
3. use android_db
4. create tables

CREATE TABLE `users` (
  `user` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  PRIMARY KEY (`user`,`password`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `friend_requests` (
  `user` varchar(255) NOT NULL DEFAULT '',
  `requests` blob,
  PRIMARY KEY (`user`),
  CONSTRAINT `friend_requests_ibfk_1` FOREIGN KEY (`user`) REFERENCES `users` (`user`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `friends` (
  `user` varchar(255) NOT NULL,
  `friend` varchar(255) NOT NULL,
  PRIMARY KEY (`user`,`friend`),
  KEY `friend` (`friend`),
  CONSTRAINT `friends_ibfk_1` FOREIGN KEY (`user`) REFERENCES `users` (`user`) ON DELETE CASCADE,
  CONSTRAINT `friends_ibfk_2` FOREIGN KEY (`friend`) REFERENCES `users` (`email`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
 
5. insert users values('test','tester','McTester@Tester.com','tester','McTester');
6. make a user with a password
	create user 'readwrite' identified by 'reader123'
7. change permissions
	grant all on android_db.* to 'readwrite'@'%';
8. make a sql.properties file with the following
	username=readwrite
	password=reader123
	url=jdbc:mariadb://localhost:3306/android_db
9. Change the file path in DBUtil/DBConnector of the ScavengerHuntServer to localhost
10. Change the IP address of the async adapters in the ScavengerHuntApp to the local machine IP