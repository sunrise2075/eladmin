-- 删除免费图床表
# DROP TABLE tool_picture;

drop table if exists works_info;
create table works_info
(
    id               int auto_increment
        primary key,
    type             tinyint                             null comment '作品种类: 0. 文字类  1. 图片类  2. 视频类',
    wx_open_id       varchar(100)                        null comment '微信openId',
    author_name      varchar(100)                        null comment '作者姓名',
    author_mobile    varchar(50)                         null comment '作者手机号',
    self_description varchar(200)                        null comment '作品描述',
    star_index       int       default 0                 null comment '是否最佳 ： 默认值 0 表示该作品不是最佳作品, 数字越大代表当前作品越佳',
    win_flag         tinyint   default 0                 null comment '0 -  没有获奖    1 -  已经获奖',
    vote_count       int       default 0                 null comment '得票总数',
    life_status      tinyint                             null comment '作品状态：1. 提交  2. 审核中  3. 审核通过  4. 审核拒绝 5. 获奖',
    updated_date     timestamp default null null,
    created_by       varchar(50)                         null comment '创建人',
    updated_by       varchar(50)                         null comment '更新人',
    created_date     timestamp default CURRENT_TIMESTAMP null
)
    comment '作品信息';




drop table if exists works_files;
create table works_files
(
    id                 int auto_increment
        primary key,
    works_id           int          null comment '作品ID',
    relative_file_path varchar(500) null comment '文件相对路径'
) comment '作品和图片文件的对应关系';
# alter table works_files modify relative_file_path varchar(500) null comment '文件相对路径';

drop table if exists works_article;
create table works_article
(
    id              int auto_increment
        primary key,
    works_id        int  null,
    article_content text null comment '文章内容'
)
    comment '文字类作品的内容';


drop table if exists wx_works_author;
create table wx_works_author
(
    open_id varchar(60) not null comment '微信openId',
    nick_name varchar(50) null comment '昵称',
    head_img_url varchar(200) null comment '头像图片地址',
    sex char(5) null comment '性别',
    city varchar(50) null comment '城市',
    province varchar(50) null comment '省份',
    country varchar(50) null comment '国家',
    constraint wx_works_author_pk
        primary key (open_id)
)
    comment '作品参赛者的微信用户信息';

drop table if exists works_podcast;
create table works_podcast
(
    id           int auto_increment
        primary key,
    url          varchar(250)                        null comment '直播链接地址',
    image_path   varchar(200)                        null comment '海报相对路径',
    begin_time   datetime                            null comment '直播开始时间',
    created_time timestamp default CURRENT_TIMESTAMP null
)
    comment '直播列表';

# 给管理员新增菜单以及权限
INSERT INTO vote_db.sys_menu (menu_id, pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time) VALUES (118, 1, 3, 1, '作品管理', 'WorksInfo', 'system/works/index', 999, 'Steve-Jobs', 'works', false, false, false, 'worksInfo:list', 'admin', 'admin', '2021-07-24 10:36:35', '2021-07-24 11:31:21');
INSERT INTO vote_db.sys_menu (menu_id, pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time) VALUES (119, 118, 0, 2, '作品新增', null, '', 2, 'doc', 'works', false, false, false, 'worksInfo:add', 'admin', 'admin', '2021-07-24 11:09:52', '2021-07-24 11:38:49');
INSERT INTO vote_db.sys_menu (menu_id, pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time) VALUES (120, 118, 0, 2, '作品编辑', null, '', 3, null, 'works', false, false, false, 'worksInfo:edit', 'admin', 'admin', '2021-07-24 11:12:39', '2021-07-24 11:38:59');
INSERT INTO vote_db.sys_menu (menu_id, pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time) VALUES (121, 118, 0, 2, '作品删除', null, null, 4, null, 'works', false, false, false, 'worksInfo:edit', 'admin', 'admin', '2021-07-24 11:14:43', '2021-07-24 11:42:10');

INSERT INTO vote_db.sys_roles_menus (menu_id, role_id) VALUES (118, 1);
INSERT INTO vote_db.sys_roles_menus (menu_id, role_id) VALUES (119, 1);
INSERT INTO vote_db.sys_roles_menus (menu_id, role_id) VALUES (120, 1);
INSERT INTO vote_db.sys_roles_menus (menu_id, role_id) VALUES (121, 1);

