CREATE TABLE IF NOT EXISTS `test_entities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `active` BOOLEAN DEFAULT 0,
  `created_at` date DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `test_entities_un` (`id`)
);