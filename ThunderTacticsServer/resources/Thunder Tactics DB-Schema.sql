DROP TABLE IF EXISTS `account`;

CREATE TABLE IF NOT EXISTS `account` (
-- Account info
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `password` varchar(44) NOT NULL,
-- Character info
  `level` int(11) DEFAULT '1',
  `add_points` int(11) DEFAULT '15',
  `experience` double(11,2) NOT NULL DEFAULT '0',
  `life` double(11,5) NOT NULL DEFAULT '100.00000',
  `max_life` int(11) NOT NULL DEFAULT '100',
  `gold` int(11) NOT NULL DEFAULT '1000',
  `damage` int(11) DEFAULT '20',
  `defense` int(11) DEFAULT '15',
  `leadership` int(11) NOT NULL DEFAULT '100',
-- Location
  `xPos` int(11) DEFAULT '1000',
  `yPos` int(11) DEFAULT '1000',
-- Statistics
  `gold_captured` int(11) NOT NULL DEFAULT '0',
  `gold_lost` int(11) NOT NULL DEFAULT '0',
  `victories` int(11) DEFAULT '0',
  `defeats` int(11) DEFAULT '0',
-- Inventory - TODO: change sizes 
  `inventory` blob(1024) DEFAULT NULL,
  `weared_items` blob(1024) DEFAULT NULL,
-- Appearance
  `body` tinyint(2) NOT NULL DEFAULT 0,
  `clothes` tinyint(2) NOT NULL DEFAULT 0,
-- Units
  `units` blob(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
);

DROP TABLE IF EXISTS `ownable`;
CREATE TABLE `ownable`(`lastId` INT(11) DEFAULT 2001);