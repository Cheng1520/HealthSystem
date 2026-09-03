-- ============================================================
-- 健康管理系统 数据库初始化脚本
-- 使用方法：用 MySQL 客户端（Navicat / 命令行 / Workbench）复制运行即可
-- 默认账号（密码均为 123456，共 17 个用户，角色见下方 INSERT）：
--   管理员   admin         / 123456
--   医生     doctor        / 123456 （王慧敏）
--   医生     ligg          / 123456 （李国伟）
--   医生     zhangyq       / 123456 （张雅琴）
--   管理员   zhouzheng     / 123456 （周正）
--   普通用户 zhangsan      / 123456 （张三）
--   普通用户 chengshaohao  / 123456 （程绍豪）
--   普通用户 guoyingmeng   / 123456 （过颖萌）
--   普通用户 gongqi        / 123456 （龚琦）
--   普通用户 chengdahao    / 123456 （程大豪）
--   普通用户 chengxiaohao  / 123456 （程小豪）
--   其余账号见下方 INSERT
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

-- 用户（密码均为 123456 的 MD5；手机号均为合法格式）
INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `age`, `phone`, `role`) VALUES
('admin',        'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '男', 30, '13800000001', '管理员'),
('doctor',       'e10adc3949ba59abbe56e057f20f883e', '王慧敏',   '女', 35, '13800000004', '医生'),
('zhangsan',     'e10adc3949ba59abbe56e057f20f883e', '张三',     '男', 25, '13800000002', '普通用户'),
('lisi',         'e10adc3949ba59abbe56e057f20f883e', '李四',     '女', 40, '13800000003', '普通用户'),
('chengshaohao', 'e10adc3949ba59abbe56e057f20f883e', '程绍豪',   '男', 33, '13912345001', '普通用户'),
('guoyingmeng',  'e10adc3949ba59abbe56e057f20f883e', '过颖萌',   '女', 27, '13912345002', '普通用户'),
('gongqi',       'e10adc3949ba59abbe56e057f20f883e', '龚琦',     '女', 31, '13912345003', '普通用户'),
('chengdahao',   'e10adc3949ba59abbe56e057f20f883e', '程大豪',   '男', 45, '13712345001', '普通用户'),
('chengxiaohao', 'e10adc3949ba59abbe56e057f20f883e', '程小豪',   '男', 21, '13712345002', '普通用户'),
('wangxiaoming', 'e10adc3949ba59abbe56e057f20f883e', '王小明',   '男', 19, '13712345003', '普通用户'),
('liumei',       'e10adc3949ba59abbe56e057f20f883e', '刘梅',     '女', 52, '13612345001', '普通用户'),
('zhaolei',      'e10adc3949ba59abbe56e057f20f883e', '赵磊',     '男', 38, '13612345002', '普通用户'),
('chenjing',     'e10adc3949ba59abbe56e057f20f883e', '陈静',     '女', 24, '13612345003', '普通用户'),
('sunqiang',     'e10adc3949ba59abbe56e057f20f883e', '孙强',     '男', 56, '13512345001', '普通用户'),
('ligg',         'e10adc3949ba59abbe56e057f20f883e', '李国伟',   '男', 42, '15912345001', '医生'),
('zhangyq',      'e10adc3949ba59abbe56e057f20f883e', '张雅琴',   '女', 39, '15912345002', '医生'),
('zhouzheng',    'e10adc3949ba59abbe56e057f20f883e', '周正',     '男', 30, '15812345001', '管理员');

-- 检查项（覆盖血常规 / 血糖血脂 / 心血管 / 肝功能 / 代谢 / 基础生命体征）
INSERT INTO `check_item` (`item_name`, `unit`, `ref_min`, `ref_max`, `remark`) VALUES
('血红蛋白',        'g/L',       115,   150,   '判断是否贫血'),
('白细胞计数',      '10^9/L',    3.5,   9.5,   '反映免疫功能'),
('红细胞计数',      '10^12/L',   3.5,   5.5,   '运输氧气'),
('血小板计数',      '10^9/L',    125,   350,   '参与凝血'),
('空腹血糖',        'mmol/L',    3.9,   6.1,   '空腹血糖'),
('总胆固醇',        'mmol/L',    2.8,   5.2,   '血脂指标'),
('甘油三酯',        'mmol/L',    0.45,  1.7,   '血脂指标'),
('低密度脂蛋白',    'mmol/L',    0,     3.4,   '坏胆固醇'),
('高密度脂蛋白',    'mmol/L',    1.0,   2.0,   '好胆固醇'),
('尿酸',            'umol/L',    208,   428,   '痛风风险'),
('肌酐',            'umol/L',    44,    133,   '肾功能'),
('收缩压',          'mmHg',      90,    140,   '高压'),
('舒张压',          'mmHg',      60,    90,    '低压'),
('心率',            '次/分',     60,    100,   '安静状态'),
('体温',            '℃',         36.0,  37.2,  '基础生命体征'),
('体重指数(BMI)',   'kg/m²',     18.5,  23.9,  '体重评估'),
('谷丙转氨酶(ALT)', 'U/L',       0,     40,    '肝功能'),
('总胆红素',        'umol/L',    3.4,   20.5,  '肝胆代谢'),
('血氧饱和度',      '%',         95,    100,   '血氧');

-- 检查组
INSERT INTO `check_group` (`group_name`, `remark`) VALUES
('基础体检套餐', '常规基础检查'),
('血液检查套餐', '血常规+血糖血脂'),
('心血管套餐',   '血压心率血氧血脂'),
('肝功能套餐',   '转氨酶胆红素肾功尿酸'),
('代谢套餐',     '血糖血脂尿酸体重');

-- 检查组-检查项关联
INSERT INTO `check_group_item` (`group_id`, `item_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 15), (1, 16),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7),
(3, 12), (3, 13), (3, 14), (3, 19), (3, 6), (3, 8),
(4, 17), (4, 18), (4, 11), (4, 10),
(5, 5), (5, 6), (5, 7), (5, 8), (5, 9), (5, 10), (5, 16);

-- 预约（覆盖：未来预约 / 今日预约 / 已完成 / 用于病史对比的多时间点历史预约）
INSERT INTO `appointment` (`id`, `user_id`, `method`, `check_group_id`, `check_item_id`, `appoint_date`, `status`, `remark`) VALUES
-- 待检预约（未来/今日，测试“标记完成”和“录入结果”）
(1,  5,  '套餐', 1,    NULL, '2026-09-10', '已预约', '程绍豪 基础体检（待检）'),
(2,  6,  '单项', NULL, 5,    '2026-09-12', '已预约', '过颖萌 测空腹血糖（待检）'),
(3,  7,  '套餐', 3,    NULL, '2026-09-15', '已预约', '龚琦 心血管套餐（待检）'),
(4,  8,  '单项', NULL, 10,   '2026-09-20', '已预约', '程大豪 复查尿酸（待检）'),
(5,  3,  '套餐', 2,    NULL, CURDATE(),    '已预约', '张三 今日血液检查'),
(6,  9,  '套餐', 5,    NULL, CURDATE(),    '已预约', '程小豪 今日代谢检查'),
(7,  10, '单项', NULL, 12,   CURDATE(),    '已预约', '王小明 今日测血压'),
(8,  11, '套餐', 4,    NULL, '2026-09-25', '已预约', '刘梅 肝功能套餐（待检）'),
(9,  12, '单项', NULL, 6,    '2026-09-30', '已预约', '赵磊 测总胆固醇（待检）'),
-- 张三 血糖 5 次复查（病史对比折线图：呈上升趋势）
(10, 3,  '单项', NULL, 5,    '2026-04-10', '已完成', '张三 血糖复查 第1次'),
(11, 3,  '单项', NULL, 5,    '2026-05-10', '已完成', '张三 血糖复查 第2次'),
(12, 3,  '单项', NULL, 5,    '2026-06-10', '已完成', '张三 血糖复查 第3次'),
(13, 3,  '单项', NULL, 5,    '2026-07-10', '已完成', '张三 血糖复查 第4次'),
(14, 3,  '单项', NULL, 5,    '2026-08-10', '已完成', '张三 血糖复查 第5次'),
-- 李四 血压心率 3 次复查（病史对比）
(15, 4,  '套餐', 3,    NULL, '2026-05-15', '已完成', '李四 血压心率复查 第1次'),
(16, 4,  '套餐', 3,    NULL, '2026-06-15', '已完成', '李四 血压心率复查 第2次'),
(17, 4,  '套餐', 3,    NULL, '2026-07-15', '已完成', '李四 血压心率复查 第3次'),
-- 程大豪 尿酸 3 次复查（病史对比）
(18, 8,  '单项', NULL, 10,   '2026-05-20', '已完成', '程大豪 尿酸复查 第1次'),
(19, 8,  '单项', NULL, 10,   '2026-06-20', '已完成', '程大豪 尿酸复查 第2次'),
(20, 8,  '单项', NULL, 10,   '2026-07-20', '已完成', '程大豪 尿酸复查 第3次'),
-- 其余已完成报告（丰富“历史检查报告”）
(21, 5,  '套餐', 1,    NULL, '2026-06-01', '已完成', '程绍豪 基础体检'),
(22, 6,  '套餐', 2,    NULL, '2026-06-05', '已完成', '过颖萌 血液检查'),
(23, 7,  '套餐', 3,    NULL, '2026-06-08', '已完成', '龚琦 心血管检查'),
(24, 10, '套餐', 1,    NULL, '2026-07-01', '已完成', '王小明 基础体检'),
(25, 11, '套餐', 4,    NULL, '2026-07-05', '已完成', '刘梅 肝功能检查'),
(26, 12, '套餐', 5,    NULL, '2026-07-10', '已完成', '赵磊 代谢检查'),
(27, 13, '单项', NULL, 16,   '2026-07-15', '已完成', '陈静 测体重指数'),
(28, 14, '套餐', 3,    NULL, '2026-07-20', '已完成', '孙强 心血管检查');

-- 检查结果
INSERT INTO `check_result` (`appointment_id`, `item_id`, `value`, `analysis`, `check_date`) VALUES
-- 张三 血糖趋势（正常→偏高）
(10, 5, 5.0, '正常', '2026-04-10'),
(11, 5, 5.3, '正常', '2026-05-10'),
(12, 5, 5.8, '正常', '2026-06-10'),
(13, 5, 6.2, '偏高', '2026-07-10'),
(14, 5, 6.8, '偏高', '2026-08-10'),
-- 李四 血压心率趋势
(15, 12, 128, '正常', '2026-05-15'), (15, 13, 82, '正常', '2026-05-15'), (15, 14, 78, '正常', '2026-05-15'),
(16, 12, 135, '正常', '2026-06-15'), (16, 13, 85, '正常', '2026-06-15'), (16, 14, 84, '正常', '2026-06-15'),
(17, 12, 145, '偏高', '2026-07-15'), (17, 13, 92, '偏高', '2026-07-15'), (17, 14, 88, '正常', '2026-07-15'),
-- 程大豪 尿酸趋势
(18, 10, 350, '正常', '2026-05-20'),
(19, 10, 415, '正常', '2026-06-20'),
(20, 10, 486, '偏高', '2026-07-20'),
-- 程绍豪 基础体检
(21, 1, 142, '正常', '2026-06-01'), (21, 2, 6.8, '正常', '2026-06-01'), (21, 3, 4.9, '正常', '2026-06-01'),
(21, 4, 265, '正常', '2026-06-01'), (21, 15, 36.5, '正常', '2026-06-01'), (21, 16, 24.6, '偏高', '2026-06-01'),
-- 过颖萌 血液检查
(22, 1, 118, '正常', '2026-06-05'), (22, 2, 8.9, '正常', '2026-06-05'), (22, 3, 4.1, '正常', '2026-06-05'),
(22, 4, 230, '正常', '2026-06-05'), (22, 5, 4.9, '正常', '2026-06-05'), (22, 6, 5.6, '偏高', '2026-06-05'),
(22, 7, 1.9, '偏高', '2026-06-05'),
-- 龚琦 心血管检查
(23, 12, 138, '正常', '2026-06-08'), (23, 13, 88, '正常', '2026-06-08'), (23, 14, 92, '正常', '2026-06-08'),
(23, 19, 98, '正常', '2026-06-08'), (23, 6, 5.0, '正常', '2026-06-08'), (23, 8, 3.1, '正常', '2026-06-08'),
-- 王小明 基础体检
(24, 1, 150, '正常', '2026-07-01'), (24, 2, 9.6, '偏高', '2026-07-01'), (24, 3, 5.3, '正常', '2026-07-01'),
(24, 4, 180, '正常', '2026-07-01'), (24, 15, 36.8, '正常', '2026-07-01'), (24, 16, 20.1, '正常', '2026-07-01'),
-- 刘梅 肝功能检查
(25, 17, 52, '偏高', '2026-07-05'), (25, 18, 18.2, '正常', '2026-07-05'), (25, 11, 90, '正常', '2026-07-05'),
(25, 10, 400, '正常', '2026-07-05'),
-- 赵磊 代谢检查（多项异常，演示代谢综合征）
(26, 5, 7.2, '偏高', '2026-07-10'), (26, 6, 6.1, '偏高', '2026-07-10'), (26, 7, 2.6, '偏高', '2026-07-10'),
(26, 8, 4.2, '偏高', '2026-07-10'), (26, 9, 0.9, '偏低', '2026-07-10'), (26, 10, 460, '偏高', '2026-07-10'),
(26, 16, 27.5, '偏高', '2026-07-10'),
-- 陈静 体重指数
(27, 16, 17.8, '偏低', '2026-07-15'),
-- 孙强 心血管检查
(28, 12, 152, '偏高', '2026-07-20'), (28, 13, 96, '偏高', '2026-07-20'), (28, 14, 76, '正常', '2026-07-20'),
(28, 19, 96, '正常', '2026-07-20'), (28, 6, 5.4, '偏高', '2026-07-20'), (28, 8, 3.5, '偏高', '2026-07-20');

-- 医生诊断建议（演示“历史检查报告 / 医生诊断建议”）
UPDATE `appointment` SET `suggestion` = '空腹血糖连续多次复查呈上升趋势，已达偏高范围，建议控制含糖饮食、加强运动，两周后复测空腹血糖与糖化血红蛋白。' WHERE `id` = 14;
UPDATE `appointment` SET `suggestion` = '血压近期呈上升趋势，本次收缩压/舒张压均偏高，建议低盐饮食、规律作息，连续监测一周血压。' WHERE `id` = 17;
UPDATE `appointment` SET `suggestion` = '尿酸复查偏高，注意控制高嘌呤饮食（海鲜、动物内脏、啤酒），多饮水，一个月后复查。' WHERE `id` = 20;
UPDATE `appointment` SET `suggestion` = '血糖、血脂多项指标异常，存在代谢综合征风险，建议内分泌科就诊并制定综合干预方案。' WHERE `id` = 26;
UPDATE `appointment` SET `suggestion` = '血压明显偏高，建议尽快到心内科就诊，必要时启动降压治疗。' WHERE `id` = 28;
