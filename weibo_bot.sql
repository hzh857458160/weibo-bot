/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50724
 Source Host           : localhost:3306
 Source Schema         : weibo_bot

 Target Server Type    : MySQL
 Target Server Version : 50724
 File Encoding         : 65001

 Date: 30/05/2019 12:54:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bot_info
-- ----------------------------
DROP TABLE IF EXISTS `bot_info`;
CREATE TABLE `bot_info`  (
  `bot_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NULL DEFAULT NULL,
  `birth_date` date NULL DEFAULT NULL,
  `bot_level` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `gender` int(11) NOT NULL,
  `img_src` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `interests` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `location` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `nick_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`bot_id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 21 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bot_info
-- ----------------------------
INSERT INTO `bot_info` VALUES (1, 1, '1994-12-23', 'H', 1, 'http://p1.music.126.net/ed0eTPxvjipYiWEF9VYCIw==/109951164011475120.jpg?param=180y180', '烘焙#读书#动漫#', '河南南阳', '路边的世界9', 1);
INSERT INTO `bot_info` VALUES (2, 2, '1990-09-15', 'VH', 1, 'http://p1.music.126.net/TQMsyaeNJz-fR_8JJWOYhQ==/109951163303214369.jpg?param=180y180', '综艺#综艺#新闻#', '山西吕梁', '唉取个名字怎么这么难q', 1);
INSERT INTO `bot_info` VALUES (4, 4, '1995-10-29', 'VH', 1, 'http://p1.music.126.net/0eLoSDRlz1h0ptMGoiIHaw==/109951164009897422.jpg?param=180y180', '烘焙#美食#摄影#', '广东茂名', 'W_kkkkk-6', 1);
INSERT INTO `bot_info` VALUES (3, 3, '1984-03-07', 'VH', 0, 'http://p2.music.126.net/xSDAOXzHYl5CPdF5LRXztQ==/18769762999512883.jpg?param=180y180', '美食#嘻哈#电视剧#', '陕西咸阳', '你给他的w', 1);
INSERT INTO `bot_info` VALUES (5, 5, '1999-11-14', 'VH', 0, 'http://p2.music.126.net/x-VKno-oM-XlnRhLfUHGXA==/109951164082155795.jpg?param=180y180', '篮球#电影#嘻哈#', '重庆石柱土家族自治县', '拘禁令j', 1);
INSERT INTO `bot_info` VALUES (6, 6, '1989-07-29', 'H', 0, 'http://p2.music.126.net/BZ7jho6WjoERzvve5KdgBw==/3387595327353400.jpg?param=180y180', '嘻哈#小说#电视剧#', '内蒙古呼伦贝尔', 'Resign-Ag', 1);
INSERT INTO `bot_info` VALUES (7, 7, '1995-05-22', 'H', 0, 'http://p2.music.126.net/_MDb3PpvxDKMQxWLl5VZjw==/109951163407575030.jpg?param=180y180', '运动#绘画#嘻哈#', '陕西咸阳', 'Ari_sweeneyl', 1);
INSERT INTO `bot_info` VALUES (8, 8, '1985-02-10', 'H', 0, 'http://p1.music.126.net/Ot-p6V5PlkyNYMJp6mSeGA==/109951163305304258.jpg?param=180y180', '篮球#唱歌#家居#', '江西景德镇', '萧和猫h', 1);
INSERT INTO `bot_info` VALUES (9, 9, '1995-11-14', 'VH', 0, 'http://p2.music.126.net/Y1JiMrsU37wuVEta6UOxBQ==/109951163818507062.jpg?param=180y180', '足球#围棋#综艺#', '湖南株洲', '筠余m', 1);
INSERT INTO `bot_info` VALUES (10, 10, '1992-06-16', 'H', 1, 'http://p1.music.126.net/QxzO3eRIoYUhPY9ekXnzCQ==/109951163451688287.jpg?param=180y180', '音乐#家居#电视剧#', '辽宁营口', '余昧180430a', 1);
INSERT INTO `bot_info` VALUES (11, 11, '1988-12-25', 'VH', 1, 'http://p2.music.126.net/IBY8lw6l7jHSPWgbTSH73g==/109951163961915650.jpg?param=180y180', '烘焙#新闻#动漫#', '陕西渭南', '高飞呃2', 1);
INSERT INTO `bot_info` VALUES (12, 12, '1981-12-30', 'N', 1, 'http://p2.music.126.net/KjqB540nhQ3kvPQoWHcsYg==/3426078263476160.jpg?param=180y180', '瑜伽#化妆#八卦#', '福建福州', 'LovelyCatOhh', 1);
INSERT INTO `bot_info` VALUES (13, 13, '1982-04-06', 'H', 1, 'http://p1.music.126.net/zWBG_jZihbmtQ9cB73YKcQ==/109951163942568166.jpg?param=180y180', '设计#投资#经济#', '广东中山', '银河系战士598号q', 1);
INSERT INTO `bot_info` VALUES (14, 14, '1989-11-15', 'H', 0, 'http://p2.music.126.net/lz31WN8JS6ptEBL4NpoOeA==/109951163955010558.jpg?param=180y180', '健身#综艺#新闻#', '辽宁葫芦岛', '我乃抖腿之神s', 1);
INSERT INTO `bot_info` VALUES (19, 19, '1983-06-23', 'H', 1, 'http://p1.music.126.net/oWSfx3KQs0MIjcENZT-oww==/109951163713588989.jpg?param=180y180', '投资#艺术#读书#', '江苏苏州', '少来喔m', 1);
INSERT INTO `bot_info` VALUES (15, 15, '1996-11-01', 'VH', 0, 'http://p1.music.126.net/ZEbnlzUuT5SaRn4PkPXaww==/19149094509533039.jpg?param=180y180', '旅行#音乐#电视剧#', '天津河西区', 'LCXDN7', 1);
INSERT INTO `bot_info` VALUES (16, 16, '1992-01-04', 'H', 0, 'http://p1.music.126.net/Ch-nm0q4261z63_IqCQ45g==/109951164073223543.jpg?param=180y180', '读书#体育#街拍#', '陕西渭南', '我不想洗头阿3', 1);
INSERT INTO `bot_info` VALUES (17, 17, '1997-10-21', 'H', 0, 'http://p1.music.126.net/lEYmZXS-tbClas4lf7mgfQ==/19061133579396893.jpg?param=180y180', '运动#搏击#家居#', '福建宁德', '我也不会再对谁满怀期待l-', 1);
INSERT INTO `bot_info` VALUES (18, 18, '1998-07-02', 'H', 0, 'http://p1.music.126.net/GOYRuW0bo0WTAA4c3pfEMg==/3286440260918613.jpg?param=180y180', '读书#篮球#综艺#', '宁夏中卫', '丿殿下_b', 1);
INSERT INTO `bot_info` VALUES (20, 20, '1986-07-12', 'VH', 0, 'http://p1.music.126.net/ak1gIohY4K0WmFVNyPvrVQ==/109951163910237747.jpg?param=180y180', '动漫#数码#综艺#', '重庆双桥区', '__血舞s', 1);

-- ----------------------------
-- Table structure for weibo_account
-- ----------------------------
DROP TABLE IF EXISTS `weibo_account`;
CREATE TABLE `weibo_account`  (
  `account_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`account_id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 21 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of weibo_account
-- ----------------------------
INSERT INTO `weibo_account` VALUES (1, 'xh666666', '18216836345');
INSERT INTO `weibo_account` VALUES (2, 'xh666666', '18275056938');
INSERT INTO `weibo_account` VALUES (3, '68147531kk', '16506304987');
INSERT INTO `weibo_account` VALUES (4, 'Psfcuikg1', 'vldcaayqcchnku-unz@yahoo.com');
INSERT INTO `weibo_account` VALUES (5, 'TyuLxj9nU3HC2X3', '17716144642');
INSERT INTO `weibo_account` VALUES (6, 'xh666666', '13595207399');
INSERT INTO `weibo_account` VALUES (7, 'xh666666', '18848607307');
INSERT INTO `weibo_account` VALUES (8, 'Ybtdjmjzs9', 'bmmxjpstywonta-gwk259@yahoo.com');
INSERT INTO `weibo_account` VALUES (9, '8huv6h7wg7', '17819281434');
INSERT INTO `weibo_account` VALUES (10, 'xh666666', '18311922708');
INSERT INTO `weibo_account` VALUES (11, 'xh666666', '15085105751');
INSERT INTO `weibo_account` VALUES (12, 'xh666666', '18808571559');
INSERT INTO `weibo_account` VALUES (13, 'xh666666', '15108528686');
INSERT INTO `weibo_account` VALUES (14, 'xh666666', '18808533019');
INSERT INTO `weibo_account` VALUES (15, 'xh666666', '18798646655');
INSERT INTO `weibo_account` VALUES (16, 'xh666666', '18386753755');
INSERT INTO `weibo_account` VALUES (17, 'xh666666', '15208510458');
INSERT INTO `weibo_account` VALUES (18, 'xh666666', '18386868852');
INSERT INTO `weibo_account` VALUES (19, 'xh666666', '18224945888');
INSERT INTO `weibo_account` VALUES (20, 'xh666666', '18892369011');

SET FOREIGN_KEY_CHECKS = 1;
