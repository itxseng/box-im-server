package com.bx.implatform.contant;

public final class Constant {

    /**
     * 系统用户id
     */
    public static final Long SYS_USER_ID = 0L;
    /**
     * 最大图片上传大小
     */
    public static final Long MAX_IMAGE_SIZE = 200 * 1024 * 1024L;
    /**
     * 最大上传文件大小
     */
    public static final Long MAX_FILE_SIZE = 200 * 1024 * 1024L;

    /**
     * 最大上传视频大小
     */
    public static final Long MAX_VIDEO_SIZE = 500 * 1024 * 1024L;
    /**
     * 大群人数上限
     */
    public static final Long MAX_LARGE_GROUP_MEMBER = 10000L;

    /**
     * 普通群人数上限
     */
    public static final Long MAX_NORMAL_GROUP_MEMBER = 500L;

    /**
     * 好友申请列表上线
     */
    public static final Long MAX_PRIEND_APPLY = 20L;

    /**
     * 默认群头像
     */
    public static final String DEFAULT_GROUP_HEAD_IMAGE = "https://cbxsss8.s3.ap-southeast-1.amazonaws.com/profile/group.jpg";
    public static final String DEFAULT_GROUP_HEAD_IMAGE_THUMB = "https://cbxsss8.s3.ap-southeast-1.amazonaws.com/profile/group.jpg";
}
