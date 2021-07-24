-- 删除免费图床表
# DROP TABLE tool_picture;

drop table if exists works_info;
create table works_info
(
    id               int auto_increment,
    author_name      varchar(100) null comment '作者姓名',
    author_mobile    varchar(50)  null comment '作者手机号',
    self_description varchar(200) null comment '作品描述',
    type             tinyint      null comment '作品种类: 0. 文字类  1. 图片类  2. 视频类',
    life_status      tinyint      null comment '作品状态：1. 提交  2. 审核中  3. 审核通过  4. 审核拒绝 5. 获奖',
    created_by       varchar(50)  null comment '创建人',
    created_date     datetime     null default current_timestamp,
    updated_by       varchar(50)  null comment '更新人',
    updated_date     datetime     null default current_timestamp on update current_timestamp,
    constraint works_info_pk
        primary key (id)
)
    comment '作品信息';