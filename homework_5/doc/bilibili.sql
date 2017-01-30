SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `bilibili`
-- ----------------------------
DROP TABLE IF EXISTS `bilibili`;
CREATE TABLE `bilibili` (
  `aid` int(14) unsigned NOT NULL,
  `tid` int(14) unsigned NOT NULL,
  `title` varchar(100) NOT NULL,
  `tname` varchar(30) NOT NULL,
  `favorite` int(14) signed NOT NULL,
  `coin` int(14) signed NOT NULL,
  `author` varchar(40) NOT NULL
  -- PRIMARY KEY (`aid`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
