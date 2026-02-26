package com.kdichter.security.constant;

public class Constant {
    public static final String PHOTO_DIRECTORY = System.getenv().getOrDefault("UPLOAD_DIR", System.getProperty("user.home") + "/Downloads/uploads/");
    public static final String WEB_URL = System.getenv().getOrDefault("WEB_URL", "http://localhost:3000");
    public static final String X_REQUESTED_WITH = "X-Requested-With";
}
