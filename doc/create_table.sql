CREATE database if not exists `qingyu_picture`;
use qingyu_picture;

# 用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`           BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    `userAccount`  VARCHAR(256)                           NOT NULL COMMENT '账号',
    `userPassword` VARCHAR(512)                           NOT NULL COMMENT '密码',
    `userName`     VARCHAR(256)                           NULL COMMENT '用户昵称',
    `userAvatar`   VARCHAR(1024)                          NULL COMMENT '用户头像',
    `userProfile`  VARCHAR(512)                           NULL COMMENT '用户简介',
    `userRole`     VARCHAR(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin',
    `editTime`     DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    `createTime`   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `updateTime`   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     TINYINT      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    UNIQUE KEY `uk_userAccount` (`userAccount`),
    INDEX `idx_userName` (`userName`)
) COMMENT ='用户表' collate = utf8mb4_unicode_ci;

# 图片表
CREATE TABLE IF NOT EXISTS `picture`
(
    `id`            BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    `url`           VARCHAR(512)                       NOT NULL COMMENT '图片 url',
    `name`          VARCHAR(128)                       NOT NULL COMMENT '图片名称',
    `introduction`  VARCHAR(512)                       NULL COMMENT '简介',
    `category`      VARCHAR(64)                        NULL COMMENT '分类',
    `tags`          VARCHAR(512)                       NULL COMMENT '标签（JSON 数组）',
    `picSize`       BIGINT                             NULL COMMENT '图片体积',
    `picWidth`      INT                                NULL COMMENT '图片宽度',
    `picHeight`     INT                                NULL COMMENT '图片高度',
    `picScale`      DOUBLE                             NULL COMMENT '图片宽高比例',
    `picFormat`     VARCHAR(32)                        NULL COMMENT '图片格式',
    `userId`        BIGINT                             NOT NULL COMMENT '创建用户 id',
    `createTime`    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `editTime`      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    `updateTime`    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`      TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    `reviewStatus`  TINYINT  DEFAULT 0                 NOT NULL COMMENT '审核状态:0-待审核,1-已审核,2-未过审被拒绝',
    `reviewMessage` VARCHAR(512)                       NULL COMMENT '审核信息',
    `reviewerId`    BIGINT                             NULL COMMENT '审核人 ID',
    `reviewTime`    DATETIME                           NULL COMMENT '审核时间',
    `thumbnailUrl`  VARCHAR(512)                       NULL COMMENT '缩略图 url',
    `originalUrl`   VARCHAR(512)                       NULL COMMENT '未压缩原图 url',
    `spaceId`       BIGINT                             NULL COMMENT '空间 id（为空表示公共空间）',
    INDEX `idx_spaceId` (`spaceId`),
    INDEX `idx_name` (`name`),                 -- 提升基于图片名称的查询性能
    INDEX `idx_introduction` (`introduction`), -- 用于模糊搜索图片简介
    INDEX `idx_category` (`category`),         -- 提升基于分类的查询性能
    INDEX `idx_tags` (`tags`),                 -- 提升基于标签的查询性能
    INDEX `idx_userId` (`userId`),             -- 提升基于用户 ID 的查询性能
    INDEX `idx_reviewStatus` (`reviewStatus`)  -- 创建基于 reviewStatus 列的索引
) COMMENT '图片' collate = utf8mb4_unicode_ci;

# 空间表
CREATE TABLE IF NOT EXISTS `space`
(
    `id`         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    `spaceName`  VARCHAR(128)                       NULL COMMENT '空间名称',
    `spaceLevel` INT      DEFAULT 0                 NULL COMMENT '空间级别：0-普通版 1-专业版 2-旗舰版',

    `maxSize`    BIGINT   DEFAULT 0                 NULL COMMENT '空间图片的最大总大小',
    `maxCount`   BIGINT   DEFAULT 0                 NULL COMMENT '空间图片的最大数量',
    `totalSize`  BIGINT   DEFAULT 0                 NULL COMMENT '当前空间下图片的总大小',
    `totalCount` BIGINT   DEFAULT 0                 NULL COMMENT '当前空间下的图片数量',
    `userId`     BIGINT                             NOT NULL COMMENT '创建用户 id',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `editTime`   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`   TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX `idx_userId` (`userId`),        -- 提升基于用户的查询效率
    INDEX `idx_spaceName` (`spaceName`),  -- 提升基于空间名称的查询效率
    INDEX `idx_spaceLevel` (`spaceLevel`) -- 提升按空间级别查询的效率
) COMMENT '空间' collate = utf8mb4_unicode_ci;

