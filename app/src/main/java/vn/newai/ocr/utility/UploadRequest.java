package vn.newai.ocr.utility;

import android.content.Context;
import android.content.Intent;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import vn.newai.ocr.GalleryActivity;
import vn.newai.ocr.R;

public class UploadRequest {
    private static final String UPLOAD_URL = "http://35.194.137.113:8080/newai-ocr-api/rest/upload";

    /**
     * Get net.gotev.uploadservice.MultipartUploadRequest to upload file
     *
     * @param context  Activity that calls the upload
     * @param filePath File path in sd card
     * @param fileName New file name that replaces name in sd card
     * @throws Exception Upload exception
     */
    public static MultipartUploadRequest getUploadRequest(Context context, String filePath, String fileName) throws Exception {
        String userLang = LocalStorage.getFromLocal(context, LocalStorage.KEY_OCR_LANG);
        String outputFormat = LocalStorage.getFromLocal(context, LocalStorage.KEY_OUTPUT_FORMAT);
        String userEmail = LocalStorage.getFromLocal(context, LocalStorage.KEY_USER_EMAIL);
        return new MultipartUploadRequest(context, UPLOAD_URL)
                .addFileToUpload(filePath, "file", fileName)
                .addParameter("lang", userLang)
                .addParameter("outputFormat", outputFormat)
                .addParameter("email", userEmail)
                .setNotificationConfig(getUploadNotificationConfig(context))
                .setMaxRetries(1)
                .setUtf8Charset();
    }

    /**
     * Get net.gotev.uploadservice.UploadNotificationConfig for upload request
     *
     * @param context Activity to open finish Intent
     */
    private static UploadNotificationConfig getUploadNotificationConfig(Context context) {
        UploadNotificationConfig notificationConfig = new UploadNotificationConfig();
        notificationConfig.setTitle(context.getString(R.string.upload_notification_title));
        notificationConfig.setErrorMessage(context.getString(R.string.upload_notification_error));
        notificationConfig.setCancelledMessage(context.getString(R.string.upload_notification_cancelled));
        notificationConfig.setCompletedMessage(context.getString(R.string.upload_notification_completed));
        notificationConfig.setClickIntent(new Intent(context, GalleryActivity.class));
        return notificationConfig;
    }
}
