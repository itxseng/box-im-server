-- 新旧表数据需要在同一个数据库中，方可迁移

-- 插入数据到 im_user 表
INSERT INTO im_user (
    user_name,
    nick_name,
    head_image,
    head_image_thumb,
    password,
    sex,
    phone,
    email,
    country,
    created_time,
    last_login_time,
    region_code
)
SELECT
    _uid,
    _display_name,
    _portrait,
    _portrait,
    _passwd_md5,
    _gender,
    -- 提取手机号
    CASE
        WHEN _mobile REGEXP '^\\([^)]+\\)' THEN SUBSTRING(_mobile, LOCATE(')', _mobile) + 1)
        ELSE _mobile
    END,
    _email,
    _company,
    _createTime,
    -- 从 JSON 中提取 statusTime 并转换为 datetime, check if _extra is valid JSON
    IF(
        JSON_VALID(_extra) AND JSON_EXTRACT(_extra, '$.statusTime') IS NOT NULL,
        FROM_UNIXTIME(JSON_EXTRACT(_extra, '$.statusTime') / 1000),
        NULL
    ),
    -- 提取国家区号
    CASE
        WHEN _mobile REGEXP '^\\([^)]+\\)' THEN SUBSTRING(_mobile, 2, LOCATE(')', _mobile) - 2)
        ELSE NULL
    END
FROM
    t_user o
-- 检查是否已存在相同的用户
left join im_user n on o._uid = n.user_name
WHERE
    _deleted = 0 and n.user_name is null; -- 只迁移未删除的数据


-- 插入数据到 im_friend 表
INSERT INTO im_friend (
    user_id,
    friend_id,
    friend_nick_name,
    friend_head_image,
    remark_nick_name,
    deleted
)
SELECT
    u1.id AS user_id,
    u2.id AS friend_id,
    u2.nick_name AS friend_nick_name,
    u2.head_image_thumb AS friend_head_image,
    tf._alias AS remark_nick_name,
    CASE
        WHEN tf._state = 1 THEN 1
        ELSE 0
    END AS deleted
FROM
    t_friend tf
-- 关联 im_user 表获取用户 ID
inner join im_user u1 on tf._uid = u1.user_name
-- 关联 im_user 表获取好友 ID、昵称和头像
inner join im_user u2 on tf._friend_uid = u2.user_name
-- 检查是否已存在相同的好友关系
left join im_friend n on u1.id = n.user_id and u2.id = n.friend_id
WHERE
    tf._state = 0 and n.user_id is null;


-- 插入数据到 im_friend_request 表
INSERT INTO im_friend_request (
    send_id,
    send_nick_name,
    send_head_image,
    recv_id,
    recv_nick_name,
    recv_head_image,
    remark,
    status,
    apply_time
)
SELECT
    u1.id AS send_id,
    u1.nick_name AS send_nick_name,
    u1.head_image_thumb AS send_head_image,
    u2.id AS recv_id,
    u2.nick_name AS recv_nick_name,
    u2.head_image_thumb AS recv_head_image,
    tfr._reason AS remark,
    -- 转换 _status 字段的值
    CASE tfr._status
        WHEN 0 THEN 1
        WHEN 1 THEN 2
        WHEN 2 THEN 3
        WHEN 3 THEN 4
        ELSE 1
    END AS status,
    -- 将 _dt 时间戳转换为 datetime 类型，假设时间戳单位为毫秒
 FROM_UNIXTIME(FLOOR(tfr._dt / 1000)) AS apply_time
FROM
    t_friend_request tfr
-- 关联 t_user 表获取发起方用户信息
inner join im_user u1 on tfr._uid = u1.user_name
-- 关联 t_user 表获取接收方用户信息
inner join im_user u2 on tfr._friend_uid = u2.user_name
-- 检查是否已存在相同的好友请求关系
left join im_friend_request n on u1.id = n.send_id and u2.id = n.recv_id
WHERE n.send_id is NULL OR FROM_UNIXTIME(FLOOR(tfr._dt / 1000))!=n.apply_time;


-- 插入数据到 im_group 表
INSERT INTO im_group (
    old_id,
    name,
    owner_id,
    head_image,
    head_image_thumb,
    created_time,
    add_group_perm,
    interim_perm,
    query_member_perm,
    is_muted,
    group_type
)
SELECT
    tg._gid,
    tg._name,
    tu.id AS owner_id,
    tg._portrait,
    tg._portrait,
    tg._createTime,
    tg._join_type,
    -- 优先使用 _extra 中的 isGroupCall，若不存在则使用 _private_chat
    CAST(
        IF(
            JSON_VALID(tg._extra) AND JSON_EXTRACT(tg._extra, '$.isGroupCall') IS NOT NULL,
            JSON_UNQUOTE(JSON_EXTRACT(tg._extra, '$.isGroupCall')),
            tg._private_chat
        ) AS SIGNED
    ),
    -- 从 _extra 中提取 isGroupLookMember
    CAST(
        IF(
            JSON_VALID(tg._extra) AND JSON_EXTRACT(tg._extra, '$.isGroupLookMember') IS NOT NULL,
            JSON_UNQUOTE(JSON_EXTRACT(tg._extra, '$.isGroupLookMember')),
            NULL
        ) AS SIGNED
    ),
    tg._mute,
    tg._super_group
FROM
    t_group tg
-- 关联 t_user 表获取群主的 id
JOIN
    t_user tu ON tg._owner = tu._uid
left join im_group n on tg._gid = n.old_id
WHERE
    n.id IS NULL AND tg._deleted = 0; -- 只迁移未删除的数据


-- 插入数据到 im_group_member 表
INSERT INTO im_group_member (
    old_group_id,
    group_id,
    user_id,
    user_nick_name,
    remark_nick_name,
    head_image,
    is_manager,
    is_muted,
    created_time
)
SELECT
    tgm._gid,
    tg.id AS group_id,
    tu.id AS user_id,
    tu._display_name AS user_nick_name,
    tgm._alias AS remark_nick_name,
    tu._portrait AS head_image,
    CASE
        WHEN tgm._type = 0 THEN 0
        WHEN tgm._type IN (1, 2) THEN 1
        ELSE 0
    END AS is_manager,
    -- 从 _extra 中提取 isMute，处理空或无效 JSON 情况
    IF(
        JSON_VALID(tgm._extra) AND JSON_EXTRACT(tgm._extra, '$.isMute') IS NOT NULL,
        CAST(JSON_UNQUOTE(JSON_EXTRACT(tgm._extra, '$.isMute')) AS SIGNED),
        0
    ) AS is_muted,
    -- 将 _create_dt 时间戳转换为 datetime 类型
    FROM_UNIXTIME(tgm._dt / 1000) AS created_time
FROM
    t_group_member tgm
-- 关联 t_group 表获取群 id
JOIN
    t_group tg ON tgm._gid = tg._gid
-- 关联 t_user 表获取用户信息
JOIN
    t_user tu ON tgm._mid = tu._uid
    -- 检查是否已存在相同的群成员关系
    left join im_group_member n on tgm._gid = n.old_group_id and tu.id = n.user_id
WHERE
     n.id is null;


