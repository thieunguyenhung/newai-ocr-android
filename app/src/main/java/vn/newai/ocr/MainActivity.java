package vn.newai.ocr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import vn.newai.ocr.utility.LocalStorage;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*-Initialize setting, setup preference default values*/
        LocalStorage.initializeSetting(this);

        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    sleep(2000);
                } catch (Exception e) {
                    Log.d("Error", e.toString());
                } finally {
                    Intent i = new Intent(MainActivity.this, GalleryActivity.class);
                    //Intent i = new Intent(MainActivity.this, FileActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
        setContentView(R.layout.activity_main);
    }
}
