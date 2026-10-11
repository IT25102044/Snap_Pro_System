package com.event.snappro.SnapPro.util;

public final class PhotoUploadRules {

    // 1. Static instance variable
    private static PhotoUploadRules instance;

    // Existing maximum upload size: 10 MB
    private final long maxFileSize = 10L * 1024 * 1024;

    // 2. Private constructor
    private PhotoUploadRules() {
    }

    // 3. Public static method
    public static synchronized PhotoUploadRules getInstance() {

        if (instance == null) {
            instance = new PhotoUploadRules();
        }

        return instance;
    }

    // Return maximum photo size
    public long getMaxFileSize() {
        return maxFileSize;
    }
}
