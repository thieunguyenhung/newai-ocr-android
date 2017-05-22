package vn.newai.ocr.navigation;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import vn.newai.ocr.FileActivity;
import vn.newai.ocr.GalleryActivity;
import vn.newai.ocr.R;
import vn.newai.ocr.SettingActivity;

public class ActivityNavigator {
    /**
     * Switch between activities. Return <b>false</b> if target activity is the same with the current one, return <b>true</b> otherwise.
     *
     * @param menuItemTarget target activity item id
     * @param currentNavItem current activity item id
     * @param activity       current activity
     */
    public static boolean openActivity(MenuItem menuItemTarget, int currentNavItem, Activity activity) {
        if (menuItemTarget.getItemId() == currentNavItem)
            return false;
        Intent intent;
        switch (menuItemTarget.getItemId()) {
            case R.id.nav_activity_gallery:
                intent = new Intent(activity, GalleryActivity.class);
                activity.startActivity(intent);
                return true;
            case R.id.nav_activity_file:
                intent = new Intent(activity, FileActivity.class);
                activity.startActivity(intent);
                return true;
            case R.id.nav_setting:
                intent = new Intent(activity, SettingActivity.class);
                activity.startActivity(intent);
                return false;
        }
        return false;
    }
}
