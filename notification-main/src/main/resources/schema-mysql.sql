CREATE TABLE `event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sequence_no` bigint(20) NOT NULL COMMENT '事件序列号',
  `event_type` tinyint(1) NOT NULL COMMENT '消息类型',
  `from` varchar(320) NOT NULL COMMENT '发件人',
  `to` varchar(320) NOT NULL COMMENT '收件人',
  `message_id` bigint(20) NOT NULL COMMENT '消息id',
  `message_seq_no` bigint(20) NOT NULL COMMENT '消息序列号',
  `message` varchar(500) DEFAULT NULL COMMENT '消息内容',
  `client_sent_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '客户端消息发送时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;