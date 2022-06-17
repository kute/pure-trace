-- ----------------------------
-- Table structure for tb_item
-- ----------------------------
DROP TABLE IF EXISTS `tb_item`;
CREATE TABLE `tb_item` (
                           `id` int(11) NOT NULL,
                           `title` varchar(200) NOT NULL COMMENT '商品名字',
                           `price` int(11) NOT NULL,
                           `count` int(11) NOT NULL,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of tb_item
-- ----------------------------
INSERT INTO `tb_item` VALUES ('1', '华为P40', '8000', '10');
INSERT INTO `tb_item` VALUES ('2', '荣耀30S', '3500', '100');


-- ----------------------------
-- Table structure for tb_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order` (
                            `id` int(11) NOT NULL AUTO_INCREMENT,
                            `item_id` int(11) NOT NULL COMMENT '商品ID',
                            `count` int(11) NOT NULL COMMENT '购买数量',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;