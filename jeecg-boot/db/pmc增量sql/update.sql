-- auto-generated definition
create table xd_schedule
(
    id          varchar(50)  not null comment 'id'
        primary key,
    day         date         not null,
    week        varchar(100) null,
    isworking   tinyint(1)   null comment '是否工作日',
    weeknum     int(65)      null comment '周序号',
    create_by   varchar(32)  null comment '创建人',
    create_time datetime     null comment '创建时间',
    update_by   varchar(32)  null comment '修改人',
    update_time datetime     null comment '修改时间',
    del_flag    tinyint(1)   null comment '删除标识0-正常,1-已删除'
)
    comment '时间表';
INSERT INTO `sys_permission` VALUES ('xd_schedule', 'd7d6e2e4e2934f2c9385a623fd98c6f3', '时间表', '/xd_schedule', 'append/XdSchedule/XdScheduleList', null, null, '1', null, '1', '99.00', '0', 'hourglass', '1', '1', '0', '0', null, 'admin', '2019-10-29 09:53:51', 'admin', '2019-12-02 16:41:48', '0', '0', '1', '0');
