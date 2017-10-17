/*
SQLyog Ultimate v11.33 (64 bit)
MySQL - 5.5.55-0+deb8u1 : Database - data_db
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`data_db` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `data_db`;

/*Table structure for table `iot_nodes` */

DROP TABLE IF EXISTS `iot_nodes`;

CREATE TABLE `iot_nodes` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `node_name` varchar(30) NOT NULL DEFAULT 'Unknown',
  `node_state_current` varchar(30) NOT NULL DEFAULT 'not_important',
  `node_state_desired` varchar(30) NOT NULL DEFAULT 'no_change',
  `node_type` varchar(30) NOT NULL DEFAULT 'onoff',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;

/*Data for the table `iot_nodes` */

insert  into `iot_nodes`(`id`,`node_name`,`node_state_current`,`node_state_desired`,`node_type`) values (1,'reset','not_important','no_change','pulse'),(2,'light_dominik','on','no_change','onoff'),(3,'gate2','open','no_change','onoff'),(4,'test_led','not_important','no_change','pulse'),(5,'rgbmode_dominik','10','no_change','mode'),(6,'rgbspeed_dominik','19','no_change','analog'),(7,'rgbbrightness_dominik','127','no_change','analog'),(8,'rgbcolor_dominik','0:0:0','no_change','analog'),(9,'rgboverride_dominik','false','no_change','pulse'),(10,'door_dominik','unlocked','no_change','onoff'),(11,'door_main','locked','no_change','onoff'),(12,'bell','not_important','no_change','pulse'),(13,'gate1','not_important','no_change','pulse'),(15,'debug','','a','a'),(16,'lightbrightness_dominik','127','no_change','analog');

/*Table structure for table `mobile_users` */

DROP TABLE IF EXISTS `mobile_users`;

CREATE TABLE `mobile_users` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Id of a user',
  `name` varchar(50) NOT NULL COMMENT 'Username for logs/login/access',
  `md5_password` varchar(50) NOT NULL COMMENT 'encrypted pass',
  `email` varchar(50) NOT NULL COMMENT 'login mail',
  `access_level` int(10) NOT NULL DEFAULT '0' COMMENT 'to be implemented',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

/*Data for the table `mobile_users` */

insert  into `mobile_users`(`id`,`name`,`md5_password`,`email`,`access_level`) values (1,'dominik','acc20210913a67ac805d12a3668a81be','dominik.polic@gmail.com',10),(2,'igor','5f4dcc3b5aa765d61d8327deb882cf99','igor.polic@ericsson.com',1),(3,'kristina','5f4dcc3b5aa765d61d8327deb882cf99','krpolic@gmail.com',0),(4,'marko','5f4dcc3b5aa765d61d8327deb882cf99','marko.polic007@gmail.com',0),(5,'hrvoje','5f4dcc3b5aa765d61d8327deb882cf99','hrvoje.polic007@gmail.com',0);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
