-- 删除免费图床表
# DROP TABLE tool_picture;

drop table if exists works_info;
create table works_info
(
    id               int auto_increment
        primary key,
    author_name      varchar(100)                       null comment '作者姓名',
    author_mobile    varchar(50)                        null comment '作者手机号',
    self_description varchar(200)                       null comment '作品描述',
    type             tinyint                            null comment '作品种类: 0. 文字类  1. 图片类  2. 视频类',
    life_status      tinyint                            null comment '作品状态：1. 提交  2. 审核中  3. 审核通过  4. 审核拒绝 5. 获奖',
    created_date     timestamp default CURRENT_TIMESTAMP null,
    created_by       varchar(50)                        null comment '创建人',
    updated_by       varchar(50)                        null comment '更新人',
    updated_date     timestamp default CURRENT_TIMESTAMP null
) comment '作品信息';

drop table if exists works_files;
create table works_files
(
    id                 int auto_increment
        primary key,
    works_id           int          null comment '作品ID',
    relative_file_path varchar(100) null comment '文件相对路径'
) comment '作品和图片文件的对应关系';

drop table if exists works_article;
create table works_article
(
    id              int auto_increment
        primary key,
    works_id        int  null,
    article_content text null comment '文章内容'
)
    comment '文字类作品的内容';

# 给管理员新增菜单以及权限
INSERT INTO vote_db.sys_menu (menu_id, pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time) VALUES (118, 1, 3, 1, '作品管理', 'WorksInfo', 'system/works/index', 999, 'Steve-Jobs', 'works', false, false, false, 'worksInfo:list', 'admin', 'admin', '2021-07-24 10:36:35', '2021-07-24 11:31:21');
INSERT INTO vote_db.sys_menu (menu_id, pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time) VALUES (119, 118, 0, 2, '作品新增', null, '', 2, 'doc', 'works', false, false, false, 'worksInfo:add', 'admin', 'admin', '2021-07-24 11:09:52', '2021-07-24 11:38:49');
INSERT INTO vote_db.sys_menu (menu_id, pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time) VALUES (120, 118, 0, 2, '作品编辑', null, '', 3, null, 'works', false, false, false, 'worksInfo:edit', 'admin', 'admin', '2021-07-24 11:12:39', '2021-07-24 11:38:59');
INSERT INTO vote_db.sys_menu (menu_id, pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time) VALUES (121, 118, 0, 2, '作品删除', null, null, 4, null, 'works', false, false, false, 'worksInfo:edit', 'admin', 'admin', '2021-07-24 11:14:43', '2021-07-24 11:42:10');

INSERT INTO vote_db.sys_roles_menus (menu_id, role_id) VALUES (118, 1);
INSERT INTO vote_db.sys_roles_menus (menu_id, role_id) VALUES (119, 1);
INSERT INTO vote_db.sys_roles_menus (menu_id, role_id) VALUES (120, 1);
INSERT INTO vote_db.sys_roles_menus (menu_id, role_id) VALUES (121, 1);
