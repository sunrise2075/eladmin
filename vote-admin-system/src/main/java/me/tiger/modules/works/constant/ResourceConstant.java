package me.tiger.modules.works.constant;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceConstant {

    /**
     * 组成用于在网页上显示图片或者加载视频的URL相对路径
     */
    public static String getFileRelativeUrl(String relativeFilePath) {
        return String.format("%s%s%s", ResourceConstant.STATIC_FILE_PATH, File.separator, relativeFilePath);
    }

    public static final Path FILE_ROOT = Paths.get(ResourceConstant.UPLOAD_FOLDER_ROOT);

    public static final String UPLOAD_FOLDER_ROOT = "uploads";
    public static final String STATIC_FILE_PATH = "/view";
    public static final String STATIC_FILE_PATTERN = "/view/**";
    public static final String ADMIN_WEB_PATTERN = "/adminWeb/**";
    public static final String ADMIN_WEB_PATH = "/adminWeb";
}
