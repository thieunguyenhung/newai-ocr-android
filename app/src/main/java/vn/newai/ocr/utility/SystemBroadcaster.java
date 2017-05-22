package vn.newai.ocr.utility;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import vn.newai.ocr.GalleryActivity;
import vn.newai.ocr.R;

/**
 * Request broadcast on system
 */
public class SystemBroadcaster {
    /**
     * Broadcast on system in order to scan media files
     *
     * @param context Activity that requests broadcast and shows finishing toast
     */
    public static void requestMediaScanner(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Log.d("Media Scanner " + Build.VERSION_CODES.KITKAT, "started");
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(Environment.getExternalStorageDirectory().toString())));
        } else {
            Log.d("Media Scanner", "started");
            MediaScannerConnection.scanFile(context, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Log.d("Media Scanner", "Completed");
                }
            });
        }
    }
}
