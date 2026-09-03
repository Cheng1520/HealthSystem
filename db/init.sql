-- ============================================================
-- 健康管理系统 数据库初始化脚本
-- 使用方法：用 MySQL 客户端（Navicat / 命令行 / Workbench）复制运行即可
-- 默认账号（密码均为 123456）：
--   管理员    admin     / 123456
--   医生      doctor    / 123456
--   普通用户  zhangsan  / 123456
--   普通用户  lisi      / 123456
-- ============================================================

DROP DATABASE IF EXISTS healthsystem;
CREATE DATABASE healthsystem DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE healthsystem;

-- ------------------------------------------------------------
-- 1. 用户表（登录注册模块）
-- ------------------------------------------------------------
CREATE TABLE `user` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`   VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`   VARCHAR(64)  NOT NULL                COMMENT '密码(MD5)',
    `real_name`  VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `gender`     VARCHAR(10)  DEFAULT NULL            COMMENT '性别',
    `age`        INT          DEFAULT NULL            COMMENT '年龄',
    `phone`      VARCHAR(20)  DEFAULT NULL            COMMENT '电话',
    `role`       VARCHAR(20)  NOT NULL DEFAULT '普通用户' COMMENT '角色: 管理员/医生/普通用户',
    `avatar`     VARCHAR(255) DEFAULT NULL            COMMENT '头像文件名(本地avatars目录)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. 检查项表（检查项管理模块）
-- ------------------------------------------------------------
CREATE TABLE `check_item` (
    `id`        INT          NOT NULL AUTO_INCREMENT COMMENT '检查项ID',
    `item_name` VARCHAR(100) NOT NULL                COMMENT '检查项名称',
    `unit`      VARCHAR(20)  DEFAULT NULL            COMMENT '单位',
    `ref_min`   DOUBLE       DEFAULT NULL            COMMENT '参考下限',
    `ref_max`   DOUBLE       DEFAULT NULL            COMMENT '参考上限',
    `remark`    VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查项表';

-- ------------------------------------------------------------
-- 3. 检查组表（检查组管理模块）
-- ------------------------------------------------------------
CREATE TABLE `check_group` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '检查组ID',
    `group_name` VARCHAR(100) NOT NULL                COMMENT '检查组名称',
    `remark`     VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查组表';

-- ------------------------------------------------------------
-- 4. 检查组-检查项 关联表
-- ------------------------------------------------------------
CREATE TABLE `check_group_item` (
    `group_id` INT NOT NULL COMMENT '检查组ID',
    `item_id`  INT NOT NULL COMMENT '检查项ID',
    PRIMARY KEY (`group_id`, `item_id`),
    CONSTRAINT `fk_group_item_group` FOREIGN KEY (`group_id`) REFERENCES `check_group` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_group_item_item`  FOREIGN KEY (`item_id`)  REFERENCES `check_item` (`id`)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查组-检查项关联表';

-- ------------------------------------------------------------
-- 5. 预约表（预约与跟踪模块）
-- ------------------------------------------------------------
CREATE TABLE `appointment` (
    `id`             INT          NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    `user_id`        INT          NOT NULL                COMMENT '预约用户ID',
    `method`         VARCHAR(10)  NOT NULL                COMMENT '体检方式: 套餐/单项',
    `check_group_id` INT          DEFAULT NULL            COMMENT '套餐时选的检查组ID',
    `check_item_id`  INT          DEFAULT NULL            COMMENT '单项时选的检查项ID',
    `appoint_date`   VARCHAR(20)  DEFAULT NULL            COMMENT '预约日期',
    `status`         VARCHAR(10)  DEFAULT '已预约'         COMMENT '状态: 已预约/已完成',
    `remark`         VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    `suggestion`     VARCHAR(500) DEFAULT NULL            COMMENT '医生诊断建议',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_appt_user`  FOREIGN KEY (`user_id`)        REFERENCES `user` (`id`)        ON DELETE CASCADE,
    CONSTRAINT `fk_appt_group` FOREIGN KEY (`check_group_id`) REFERENCES `check_group` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_appt_item`  FOREIGN KEY (`check_item_id`)  REFERENCES `check_item` (`id`)  ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ------------------------------------------------------------
-- 6. 检查结果表（结果分析与病史对比）
-- ------------------------------------------------------------
CREATE TABLE `check_result` (
    `id`             INT          NOT NULL AUTO_INCREMENT COMMENT '结果ID',
    `appointment_id` INT          NOT NULL                COMMENT '所属预约ID',
    `item_id`        INT          NOT NULL                COMMENT '检查项ID',
    `value`          DOUBLE       NOT NULL                COMMENT '检测数值',
    `analysis`       VARCHAR(10)  DEFAULT NULL            COMMENT '分析: 正常/偏高/偏低',
    `check_date`     VARCHAR(20)  DEFAULT NULL            COMMENT '检测日期',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_result_appt` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_result_item` FOREIGN KEY (`item_id`)        REFERENCES `check_item` (`id`)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查结果表';

-- ============================================================
-- 初始化种子数据
-- ============================================================

-- 用户（密码均为 123456 的 MD5）
INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `age`, `phone`, `role`) VALUES
('admin',    'e10adc3949ba59abbe56e057f20f883e', '管理员', '男', 30, '13800000001', '管理员'),
('doctor',   'e10adc3949ba59abbe56e057f20f883e', '王医生', '女', 35, '13800000004', '医生'),
('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三',   '男', 25, '13800000002', '普通用户'),
('lisi',     'e10adc3949ba59abbe56e057f20f883e', '李四',   '女', 40, '13800000003', '普通用户');

-- 检查项
INSERT INTO `check_item` (`item_name`, `unit`, `ref_min`, `ref_max`, `remark`) VALUES
('血红蛋白', 'g/L',     115, 150, '判断是否贫血'),
('白细胞',   '10^9/L',  3.5, 9.5, '反映免疫功能'),
('红细胞',   '10^12/L', 3.5, 5.5, '运输氧气'),
('血糖',     'mmol/L',  3.9, 6.1, '空腹血糖'),
('总胆固醇', 'mmol/L',  2.8, 5.2, '血脂指标'),
('收缩压',   'mmHg',    90,  140, '高压'),
('舒张压',   'mmHg',    60,  90,  '低压'),
('心率',     '次/分',   60,  100, '安静状态');

-- 检查组
INSERT INTO `check_group` (`group_name`, `remark`) VALUES
('基础体检套餐', '常规基础检查'),
('血液检查套餐', '血常规相关指标'),
('心血管套餐',   '血压心率相关');

-- 检查组-检查项关联
INSERT INTO `check_group_item` (`group_id`, `item_id`) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5),
(3, 6), (3, 7), (3, 8);

-- 预约（覆盖：未来预约 / 已完成 / 今日预约 / 用于病史对比的历史预约）
INSERT INTO `appointment` (`user_id`, `method`, `check_group_id`, `check_item_id`, `appoint_date`, `status`, `remark`) VALUES
(2, '套餐', 1,    NULL, '2026-09-03', '已预约', '张三基础体检（未来预约）'),
(3, '单项', NULL, 4,    '2026-09-04', '已完成', '李四测血糖（已完成）'),
(3, '套餐', 2,    NULL, CURDATE(),    '已预约', '李四今日血液检查（今日预约演示）'),
(2, '单项', NULL, 4,    '2026-06-10', '已完成', '张三血糖复查 第1次'),
(2, '单项', NULL, 4,    '2026-07-10', '已完成', '张三血糖复查 第2次'),
(2, '单项', NULL, 4,    '2026-08-10', '已完成', '张三血糖复查 第3次');

-- 检查结果（张三的血糖三次复查呈上升趋势，用于演示“病史对比与跟踪”）
INSERT INTO `check_result` (`appointment_id`, `item_id`, `value`, `analysis`, `check_date`) VALUES
(2, 4, 6.8, '偏高', '2026-09-04'),
(4, 4, 5.1, '正常', '2026-06-10'),
(5, 4, 5.6, '正常', '2026-07-10'),
(6, 4, 6.5, '偏高', '2026-08-10');

-- 医生对某次检查报告的诊断建议（演示“历史检查报告 / 医生诊断建议”）
UPDATE `appointment` SET `suggestion` = '血糖三次复查呈上升趋势，建议控制饮食、增加运动，一周后复测空腹血糖。' WHERE `id` = 6;
