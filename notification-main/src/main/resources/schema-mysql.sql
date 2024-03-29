CREATE TABLE `event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `x_packet_id` varchar(255) DEFAULT '' COMMENT 'x_packet_id',
  `event_seq_id` bigint(20) NOT NULL COMMENT '事件序列号',
  `event_type` tinyint(2) NOT NULL COMMENT '事件类型',
  `msg_id` varchar(255) DEFAULT '' COMMENT '消息ID',
  `parent_msg_id` varchar(255) DEFAULT '' COMMENT '父消息ID',
  `from` varchar(320) NOT NULL COMMENT '发件人',
  `to` varchar(320) NOT NULL COMMENT '收件人',
  `group_temail` varchar(320) DEFAULT '' COMMENT '群聊邮箱',
  `temail` varchar(320) DEFAULT '' COMMENT '当事人邮箱',
  `extend_param` mediumtext COMMENT '扩展字段',
  `timestamp` bigint(20) DEFAULT '0' COMMENT '客户端消息发送时间',
  `create_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `zip_packet` blob COMMENT '全报文信息',
  PRIMARY KEY (`id`),
  KEY `event_to` (`to`),
  KEY `event_xPacketId` (`x_packet_id`),
  KEY `event_msgId` (`msg_id`),
  KEY `event_parentMsgId` (`parent_msg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_temail` varchar(320) NOT NULL COMMENT '群邮件',
  `temail` varchar(320) NOT NULL COMMENT '用户邮件',
  `role` tinyint(1) NOT NULL DEFAULT '0' COMMENT '群角色',
  `user_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '个人状态',
  PRIMARY KEY (`id`),
  UNIQUE KEY `member_unique` (`group_temail`,`temail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `topic_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `x_packet_id` varchar(255) NOT NULL DEFAULT '' COMMENT 'x_packet_id',
  `event_seq_id` bigint(20) NOT NULL COMMENT '事件序列号',
  `event_type` tinyint(2) NOT NULL COMMENT '事件类型',
  `topic_id` varchar(255) NOT NULL DEFAULT '' COMMENT '话题ID',
  `msg_id` varchar(255) DEFAULT '' COMMENT '消息ID',
  `from` varchar(320) NOT NULL COMMENT '发件人',
  `to` varchar(320) NOT NULL COMMENT '收件人',
  `extend_param` mediumtext COMMENT '扩展字段',
  `timestamp` bigint(20) NOT NULL DEFAULT '0' COMMENT '客户端消息发送时间',
  `create_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `topic_event_unique` (`x_packet_id`,`event_type`,`to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `unread` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `from` varchar(320) NOT NULL COMMENT '收件人',
  `to` varchar(320) NOT NULL COMMENT '发件人',
  `count` int(20) NOT NULL COMMENT '未读数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unread_unique` (`to`,`from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;