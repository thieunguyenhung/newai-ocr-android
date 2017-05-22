package vn.newai.ocr;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import vn.newai.ocr.adapter.FileAdapter;
import vn.newai.ocr.navigation.ActivityNavigator;
import vn.newai.ocr.utility.LocalStorage;
import vn.newai.ocr.utility.UploadRequest;

public class FileActivity extends AppCompatActivity {
    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().toString();

    private DrawerLayout drawerLayout; /*-layout containt the navigation view*/
    private NavigationView navigationView; /*-navigation view contain header and list*/
    private ActionBarDrawerToggle actionBarDrawerToggle; /*-for animation hamburger button*/
    private static final int CUR_NAV_ITEM = R.id.nav_activity_file;/*-current activity int constant*/

    private CoordinatorLayout coordinatorLayoutContainer;/*-CoordinatorLayout container*/
    private TextView textViewGuide; /*-Text view guide*/
    private SwipeRefreshLayout fileRefreshList;/*-SwipeRefreshLayout fileRefreshList*/
    private ProgressBar fileProgressBar;/*-File Activity Progressbar*/
    private ArrayList<String> listPDFFilePath;/*-List file path*/
    private ListView listViewPDFFile; /*-List view all pdf file*/

    private String userEmail, userLang; /*-user email and language preferences*/

    /*-Request result code constant*/
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 11;
    private static boolean READ_EXTERNAL_STORAGE_PERMISSION = false;

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*-User setting info*/
        this.userLang = LocalStorage.getFromLocal(FileActivity.this, LocalStorage.KEY_OCR_LANG);
        this.userEmail = LocalStorage.getFromLocal(FileActivity.this, LocalStorage.KEY_USER_EMAIL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        Log.d("ROOT PATH", ROOT_PATH);

        addControls();
        addEvents();
        checkReadExternalPermission();
        if (READ_EXTERNAL_STORAGE_PERMISSION) {
            getListFileAsync(ROOT_PATH);
        }
    }

    private void addControls() {
        /*-Initialize list file*/
        listPDFFilePath = new ArrayList<>();

        /*-Text view guide*/
        textViewGuide = (TextView) findViewById(R.id.fileTextViewGuide);
        textViewGuide.setVisibility(View.INVISIBLE);

        /*-CoordinatorLayout container*/
        coordinatorLayoutContainer = (CoordinatorLayout) findViewById(R.id.fileCoordinatorLayout);
        /*-View snackbar guide*/
        final Snackbar snackbarGuide = Snackbar.make(coordinatorLayoutContainer, getString(R.string.guide_scroll_down), Snackbar.LENGTH_LONG);
        snackbarGuide.setAction(getString(R.string.btn_dismiss), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbarGuide.dismiss();
            }
        });
        snackbarGuide.show();

        /*Toolbar*/
        Toolbar galleryToolbar = (Toolbar) findViewById(R.id.toolbarFile);
        setSupportActionBar(galleryToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.activity_file));
        }

        /*DrawerLayout*/
        drawerLayout = (DrawerLayout) findViewById(R.id.activity_file);

        /*NavigationView*/
        navigationView = (NavigationView) findViewById(R.id.fileNavDrawer);
        navigationView.setItemIconTintList(null);
        setupDrawerContent(); //setup navigation contain list

        /*ActionBarDrawerToggle*/
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, galleryToolbar, R.string.drawer_open, R.string.drawer_close);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        /*-File Activity Progressbar*/
        fileProgressBar = (ProgressBar) findViewById(R.id.fileProgressBar);
        fileProgressBar.setVisibility(View.VISIBLE);
        /*-List view all pdf file*/
        listViewPDFFile = (ListView) findViewById(R.id.fileListViewPDFFile);
        listViewPDFFile.setVisibility(View.INVISIBLE);
        /*-SwipeRefreshLayout*/
        fileRefreshList = (SwipeRefreshLayout) findViewById(R.id.fileRefreshList);
    }

    private void addEvents() {
        fileRefreshList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getListFileAsync(ROOT_PATH);
            }
        });
        listViewPDFFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                File file = new File(listPDFFilePath.get(position));
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(FileActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(FileActivity.this);
                }
                builder.setMessage(getString(R.string.dialog_ocr_file) + " " + file.getName() + "?")
                        .setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("Button", "Yes clicked");
                                if (!userEmail.isEmpty() && !userLang.isEmpty()) {
                                    sendFile(listPDFFilePath.get(position));
                                } else {
                                    Intent intent = new Intent(FileActivity.this, SettingActivity.class);
                                    FileActivity.this.startActivity(intent);
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("Button", "No clicked");
                            }
                        });
                Dialog dialog = builder.create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                //dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
    }

    private void setupDrawerContent() {
        navigationView.getMenu().getItem(1).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        drawerLayout.closeDrawers();
                        if (ActivityNavigator.openActivity(menuItem, CUR_NAV_ITEM, FileActivity.this)) {
                            finish();
                        }
                        return true;
                    }
                });
    }

    private void checkReadExternalPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else READ_EXTERNAL_STORAGE_PERMISSION = true;
        } else READ_EXTERNAL_STORAGE_PERMISSION = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("SUCCESS", "READ_EXTERNAL_STORAGE_PERMISSION granted");
                    READ_EXTERNAL_STORAGE_PERMISSION = true;
                    getListFileAsync(ROOT_PATH);
                } else {
                    FileActivity.this.fileProgressBar.setVisibility(View.INVISIBLE);
                    FileActivity.this.listViewPDFFile.setVisibility(View.INVISIBLE);
                    FileActivity.this.textViewGuide.setVisibility(View.VISIBLE);
                    READ_EXTERNAL_STORAGE_PERMISSION = false;
                    Snackbar.make(this.coordinatorLayoutContainer, getString(R.string.err_permission_denied), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.btn_undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkReadExternalPermission();
                        }
                    }).show();
                }
                break;
            }
        }
    }

    private void sendFile(String filePath) {
        String fileName = String.valueOf(System.currentTimeMillis() / 1000) + "_" + userEmail + ".pdf";
        try {
            /*-Snack bar uploading progress*/
            final Snackbar snackbarLoading = Snackbar.make(coordinatorLayoutContainer, getString(R.string.info_sending_server), Snackbar.LENGTH_INDEFINITE);
            snackbarLoading.setAction(getString(R.string.btn_cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UploadService.stopAllUploads();
                }
            });
            snackbarLoading.show();

            /*-Snack bar alert when upload process done*/
            final Snackbar snackbarAlert = Snackbar.make(coordinatorLayoutContainer, getString(R.string.success_send_server), Snackbar.LENGTH_INDEFINITE);
            snackbarAlert.setAction(getString(R.string.btn_dismiss), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbarAlert.dismiss();
                }
            });
            UploadRequest.getUploadRequest(this, filePath, fileName).setDelegate(new UploadStatusDelegate() {

                @Override
                public void onProgress(Context context, UploadInfo uploadInfo) {
                }

                @Override
                public void onError(Context context, UploadInfo uploadInfo, Exception exception) {
                    snackbarLoading.dismiss();
                    snackbarAlert.setText(getString(R.string.err_send_server));
                    snackbarAlert.show();
                }

                @Override
                public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                    snackbarLoading.dismiss();
                    try {
                        String response = new String(serverResponse.getBody(), "UTF-8");
                        if (response.toLowerCase().startsWith("file saved")) {
                            snackbarAlert.setText(getString(R.string.success_send_server));
                            snackbarAlert.show();
                        } else {
                            snackbarAlert.setText(getString(R.string.err_send_server));
                            snackbarAlert.show();
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(Context context, UploadInfo uploadInfo) {
                    snackbarLoading.dismiss();
                    snackbarAlert.setText(getString(R.string.err_cancel_send_server));
                    snackbarAlert.show();
                }
            }).startUpload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Async function to get list pdf file
     *
     * @param path Input path to fetch all directory
     */
    private void getListFileAsync(final String path) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("Get list file", "Started");
                listPDFFilePath.clear();
            }

            @Override
            protected Void doInBackground(Void... params) {
                FileActivity.this.getListFileRecursive(path);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                FileAdapter adapter = new FileAdapter(FileActivity.this, listPDFFilePath);
                FileActivity.this.listViewPDFFile.setAdapter(adapter);
                FileActivity.this.fileProgressBar.setVisibility(View.INVISIBLE);
                FileActivity.this.listViewPDFFile.setVisibility(View.VISIBLE);
                if (fileRefreshList.isRefreshing())
                    fileRefreshList.setRefreshing(false);
                Log.d("Get list file", "DONE");
                Log.d("Size", String.valueOf(FileActivity.this.listPDFFilePath.size()));
                //Log.d("pdf File", FileActivity.this.listPDFFilePath.toString());
            }
        }.execute();
    }

    /**
     * Recursive function to fetch all available directory from input path.
     * <b>DO NOT</b> call this function directly, use getListFileAsync instead
     *
     * @param path Input path to fetch all directory
     */
    private void getListFileRecursive(String path) {
        File f = new File(path);
        File[] listFiles = f.listFiles();
        if (null != listFiles && listFiles.length > 0) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    this.getListFileRecursive(file.getAbsolutePath());
                } else {
                    this.addListFile(file);
                }
            }
        }
    }

    /**
     * Adds path of file to list pdf file if it is a PDF
     *
     * @param file File that will be added to list
     */
    private void addListFile(File file) {
        if (file.isFile()) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
            if (null != extension && !extension.isEmpty()) {
                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (null != type && type.toLowerCase().endsWith("pdf"))
                    this.listPDFFilePath.add(file.getAbsolutePath());
            } else if (file.getName().toLowerCase().endsWith("pdf")) {
                this.listPDFFilePath.add(file.getAbsolutePath());
            }
        }
    }
}
