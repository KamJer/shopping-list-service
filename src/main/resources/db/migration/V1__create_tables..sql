CREATE TABLE `amount_type` (
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `amount_type_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `saved_time` datetime(6) DEFAULT NULL,
  `type_name` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`amount_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
CREATE TABLE `category` (
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `saved_time` datetime(6) DEFAULT NULL,
  `category_name` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
CREATE TABLE `shopping_item` (
  `amount` double DEFAULT NULL,
  `bought` bit(1) NOT NULL DEFAULT b'0',
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `amount_type_id` bigint(20) DEFAULT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `saved_time` datetime(6) DEFAULT NULL,
  `shopping_item_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_name` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`shopping_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;