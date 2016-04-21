CREATE DATABASE IF NOT EXISTS test_db;
USE test_db;

CREATE TABLE `users` (
  `user` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`user`,`password`),
  UNIQUE KEY `email` (`email`));

CREATE TABLE `friends` (
`user` varchar(255) NOT NULL,
`friend` varchar(255) NOT NULL,
`hunts_played` int(11) DEFAULT 0,
 `avg_hunt_score` double DEFAULT '0',
PRIMARY KEY (`user`,`friend`),
KEY `friend` (`friend`),
CONSTRAINT `friends_ibfk_1` FOREIGN KEY (`user`) REFERENCES `users` (`user`) ON DELETE CASCADE,
CONSTRAINT `friends_ibfk_2` FOREIGN KEY (`friend`) REFERENCES `users` (`user`) ON DELETE CASCADE);

CREATE TABLE `friend_request` (
`user` varchar(255) NOT NULL,
`request` varchar(255) NOT NULL,
PRIMARY KEY (`user`,`request`),
KEY `request` (`request`),
CONSTRAINT `friend_request_ibfk_1` FOREIGN KEY (`user`) REFERENCES `users` (`user`) ON DELETE CASCADE,
CONSTRAINT `friends_request_ibfk_2` FOREIGN KEY (`request`) REFERENCES `users` (`user`) ON DELETE CASCADE);

CREATE TABLE photos (
`user` varchar(255) not null,
`friend` varchar(255) not null,
`photo_location` varchar(255) not null,
`create_time` timestamp not null,
PRIMARY KEY (`user`,`friend`,`photo_location`),
CONSTRAINT `users_ibfk_1` foreign key (`user`) references users(`user`) on delete cascade,
CONSTRAINT `users_ibfk_2` foreign key (`friend`) references users(`user`) on delete cascade );

CREATE TABLE `current_hunts`(
`user` varchar(255) not null,
`friend` varchar(255) not null,
`rating` float DEFAULT NULL,
`topic` varchar(255) NOT NULL,
`round` int DEFAULT 0,
`updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (`user`, `friend`),
KEY `friend` (`friend`),
FOREIGN KEY (`user`) REFERENCES `users` (`user`) ON DELETE CASCADE,
FOREIGN KEY (`friend`) REFERENCES `users` (`user`) ON DELETE CASCADE);
