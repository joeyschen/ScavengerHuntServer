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

CREATE TABLE topics (
`user` varchar(255) NOT NULL,
`friend` varchar(255) NOT NULL,
`topic` varchar(255) NOT NULL,
`updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY(`user`, `friend`),
KEY friend (`friend`),
CONSTRAINT `topics_ibfk_1` FOREIGN KEY (`user`) REFERENCES users(`user`),
CONSTRAINT `topics_ibfk_2` FOREIGN KEY (`friend`) REFERENCES users(`user`));

CREATE TABLE `ranks`(
`user` varchar(255) not null,
`friend` varchar(255) not null,
`rank` smallint not null,
`updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (`user`, `friend`),
KEY `friend` (`friend`),
CONSTRAINT `ranks_ibfk_1` FOREIGN KEY (`user`) REFERENCES `users` (`user`) ON DELETE CASCADE,
CONSTRAINT  `ranks_ibfk_2` FOREIGN KEY (`friend`) REFERENCES `users` (`user`) ON DELETE CASCADE);

CREATE TABLE `avg_rank_score` (
`user` varchar(255) NOT NULL,
`friend` varchar(255) NOT NULL,
`avg_rank` int(11) DEFAULT '0',
`num_of_hunts` int(11) DEFAULT '0',
PRIMARY KEY (`user`, `friend`),
KEY `friend` (`friend`),
CONSTRAINT `avg_rank_score_ibfk_1` FOREIGN KEY (`user`) REFERENCES `users` (`user`) ON DELETE CASCADE,
CONSTRAINT `avg_rank_score_ibfk_2` FOREIGN KEY (`friend`) REFERENCES `users` (`user`) ON DELETE CASCADE
);