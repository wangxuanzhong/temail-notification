--liquibase formatted sql

--changeset liusen:1 context:dev,inter,prod
CREATE TABLE `unread` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `from` varchar(320) NOT NULL COMMENT '收件人',
  `to` varchar(320) NOT NULL COMMENT '发件人',
  `count` int(20) NOT NULL COMMENT '未读数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique` (`to`,`from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--rollback DROP TABLE `unread`;
