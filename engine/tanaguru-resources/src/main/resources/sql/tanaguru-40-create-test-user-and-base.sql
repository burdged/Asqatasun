CREATE USER 'test'@'localhost' IDENTIFIED BY 'test';

CREATE DATABASE IF NOT EXISTS `myTestDb` CHARACTER SET utf8;

GRANT ALL PRIVILEGES ON `myTestDb` . * TO 'test'@'localhost';

FLUSH PRIVILEGES;