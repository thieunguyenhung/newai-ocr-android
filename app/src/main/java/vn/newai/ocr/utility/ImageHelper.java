package vn.newai.ocr.utility;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;

/**
 * Utility class for Image
 */
public class ImageHelper {
    /**
     * Get image path in sd card from image ID
     *
     * @param context Activity that calls this function
     * @param imageId Image id from MediaStore.Images.Media._ID
     * @return String sd card path
     */
    public static String getRealPathFromURI(Context context, int imageId) {
        String filePath = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] column = {MediaStore.Images.Media.DATA};

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{String.valueOf(imageId)}, null);
            if (null != cursor) {
                int columnIndex = cursor.getColumnIndex(column[0]);

                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }
        return filePath;
    }

    /**
     * Get image file extension from image ID
     *
     * @param context Activity that calls this function
     * @param imageId Image id from MediaStore.Images.Media._ID
     * @return String image file extension in sd card
     */
    public static String getImageExtension(Context context, int imageId) {
        String fileExtension = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] column = {MediaStore.Images.Media.MIME_TYPE};
            String selection = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, selection, new String[]{String.valueOf(imageId)}, null);
            if (null != cursor) {
                int columnMimeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);
                if (cursor.moveToFirst()) {
                    String[] fileMimeType = cursor.getString(columnMimeIndex).split("/");
                    if (fileMimeType.length > 0)
                        fileExtension = fileMimeType[fileMimeType.length - 1];
                }
                cursor.close();
            }
        }
        return fileExtension;
    }
}
