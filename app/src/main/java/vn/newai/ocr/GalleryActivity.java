package vn.newai.ocr;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;

import vn.newai.ocr.adapter.GalleryImageAdapter;
import vn.newai.ocr.navigation.ActivityNavigator;
import vn.newai.ocr.utility.LocalStorage;
import vn.newai.ocr.utility.SystemBroadcaster;
import vn.newai.ocr.utility.UploadRequest;

public class GalleryActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout; /*-layout containt the navigation view*/
    private NavigationView navigationView; /*-navigation view contain header and list*/
    private ActionBarDrawerToggle actionBarDrawerToggle; /*-for animation hamburger button*/
    private static final int CUR_NAV_ITEM = R.id.nav_activity_gallery;/*-current activity int constant*/

    private CoordinatorLayout coordinatorLayoutContainer;/*-CoordinatorLayout container*/
    private TextView textViewGuide; /*-Text view guide*/
    private SwipeRefreshLayout galleryRefreshGrid;/*-SwipeRefreshLayout galleryRefreshGrid*/
    private FloatingActionButton fabCamera; /*-FAB Camera*/

    /*-Grid view*/
    private ArrayList<Integer> listImageId;
    private GridView gridImageGallery;
    private int numColumn = 4;
    private int imageViewWidth = 88;
    private int imageViewHeight = 88;

    /*-Activity open result code constant*/
    private static final int VIEW_IMAGE_ACTIVITY = 50;

    /*-Request result code constant*/
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 11;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 12;

    private static boolean READ_EXTERNAL_STORAGE_PERMISSION = false;
    private static boolean CAMERA_PERMISSION = false;
    private static boolean WRITE_EXTERNAL_STORAGE_PERMISSION = false;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        /*-Setup namespace for upload notification*/
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

        /*-Calculate size for image item in grid view*/
        setImageItemSize();

        addControls();
        addEvents();
        checkReadExternalPermission();
        if (READ_EXTERNAL_STORAGE_PERMISSION) {
            SystemBroadcaster.requestMediaScanner(this);
            this.loadGridImage();
        }
    }

    private void setImageItemSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        double dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        double dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.d("Size", dpWidth + "x" + dpHeight);
        this.numColumn = (int) Math.floor(dpWidth / 88);
        this.imageViewWidth += dpWidth / 88;
        this.imageViewHeight += dpWidth / 88;

        Log.d("NumCol", String.valueOf(this.numColumn));

        Resources r = getResources();
        this.imageViewWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.imageViewWidth, r.getDisplayMetrics());
        this.imageViewHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.imageViewHeight, r.getDisplayMetrics());
        Log.d("ImageView", imageViewWidth + "x" + imageViewHeight);
    }

    private void addControls() {
        /*-CoordinatorLayout container*/
        coordinatorLayoutContainer = (CoordinatorLayout) findViewById(R.id.galleryCoordinatorLayout);
        /*-View snackbar guide*/
        final Snackbar snackbarGuide = Snackbar.make(coordinatorLayoutContainer, getString(R.string.guide_scroll_down), Snackbar.LENGTH_SHORT);
        snackbarGuide.setAction(getString(R.string.btn_dismiss), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbarGuide.dismiss();
            }
        });
        snackbarGuide.show();

        /*-Text view guide*/
        textViewGuide = (TextView) findViewById(R.id.galleryTextViewGuide);
        textViewGuide.setVisibility(View.INVISIBLE);

         /*-Toolbar*/
        Toolbar galleryToolbar = (Toolbar) findViewById(R.id.toolbarGallery);
        setSupportActionBar(galleryToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.activity_gallery));
        }

        /*-DrawerLayout*/
        drawerLayout = (DrawerLayout) findViewById(R.id.activity_gallery);

        /*-NavigationView*/
        navigationView = (NavigationView) findViewById(R.id.galleryNavDrawer);
        navigationView.setItemIconTintList(null);
        setupDrawerContent(); //setup navigation contain list

        /*-ActionBarDrawerToggle*/
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, galleryToolbar, R.string.drawer_open, R.string.drawer_close);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        /*-SwipeRefreshLayout*/
        galleryRefreshGrid = (SwipeRefreshLayout) findViewById(R.id.galleryRefreshGrid);

        /*-Grid Thumb*/
        gridImageGallery = (GridView) findViewById(R.id.galleryGridThumb);
        gridImageGallery.setNumColumns(this.numColumn);
        gridImageGallery.setVisibility(View.VISIBLE);

        /*-FAB Camera*/
        fabCamera = (FloatingActionButton) findViewById(R.id.galleryFABCamera);
    }

    private void addEvents() {
        galleryRefreshGrid.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (galleryRefreshGrid.isRefreshing())
                    galleryRefreshGrid.setRefreshing(false);
                if (!READ_EXTERNAL_STORAGE_PERMISSION)
                    checkReadExternalPermission();
                else GalleryActivity.this.loadGridImage();
            }
        });
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!WRITE_EXTERNAL_STORAGE_PERMISSION)
                    checkWriteExternalPermission();
                if (!CAMERA_PERMISSION)
                    checkCameraPermission();
                if (WRITE_EXTERNAL_STORAGE_PERMISSION && CAMERA_PERMISSION) {
                    GalleryActivity.this.startCameraIntent();
                }
            }
        });
        gridImageGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(GalleryActivity.this, ViewImageActivity.class);
                intent.putExtra("imageId", GalleryActivity.this.listImageId.get(position));
                intent.putIntegerArrayListExtra("listImageId", GalleryActivity.this.listImageId);
                startActivityForResult(intent, VIEW_IMAGE_ACTIVITY);
            }
        });
    }

    private void setupDrawerContent() {
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        drawerLayout.closeDrawers();
                        if (ActivityNavigator.openActivity(menuItem, CUR_NAV_ITEM, GalleryActivity.this)) {
                            finish();
                        }
                        return true;
                    }
                });
    }

    private void checkReadExternalPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (GalleryActivity.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else READ_EXTERNAL_STORAGE_PERMISSION = true;
        } else READ_EXTERNAL_STORAGE_PERMISSION = true;
    }

    private void checkWriteExternalPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (GalleryActivity.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else WRITE_EXTERNAL_STORAGE_PERMISSION = true;
        } else WRITE_EXTERNAL_STORAGE_PERMISSION = true;
    }

    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (GalleryActivity.this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else CAMERA_PERMISSION = true;
        } else CAMERA_PERMISSION = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("SUCCESS", "READ_EXTERNAL_STORAGE_PERMISSION granted");
                    READ_EXTERNAL_STORAGE_PERMISSION = true;
                    this.loadGridImage();
                } else {
                    /*-User just denied*/
                    if (ActivityCompat.shouldShowRequestPermissionRationale(GalleryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        gridImageGallery.setVisibility(View.INVISIBLE);
                        textViewGuide.setVisibility(View.VISIBLE);
                        READ_EXTERNAL_STORAGE_PERMISSION = false;
                        Snackbar.make(this.coordinatorLayoutContainer, getString(R.string.err_permission_denied), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.btn_undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkReadExternalPermission();
                            }
                        }).show();
                    } else { /*-User denied and check never ask again*/
                        this.showRequestPermissionDialog(getString(R.string.permission_rationale_read_external_storage));
                    }
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("SUCCESS", "CAMERA_PERMISSION granted");
                    CAMERA_PERMISSION = true;
                    GalleryActivity.this.startCameraIntent();
                } else {
                    /*-User just denied*/
                    if (ActivityCompat.shouldShowRequestPermissionRationale(GalleryActivity.this, Manifest.permission.CAMERA)) {
                        CAMERA_PERMISSION = false;
                        Snackbar.make(this.coordinatorLayoutContainer, getString(R.string.err_permission_denied), Snackbar.LENGTH_LONG).setAction(getString(R.string.btn_undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkCameraPermission();
                            }
                        }).show();
                    } else { /*-User denied and check never ask again*/
                        this.showRequestPermissionDialog(getString(R.string.permission_rationale_camera));
                    }
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("SUCCESS", "WRITE_EXTERNAL_STORAGE_PERMISSION granted");
                    WRITE_EXTERNAL_STORAGE_PERMISSION = true;
                } else {
                     /*-User just denied*/
                    if (ActivityCompat.shouldShowRequestPermissionRationale(GalleryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        WRITE_EXTERNAL_STORAGE_PERMISSION = false;
                        Snackbar.make(this.coordinatorLayoutContainer, getString(R.string.err_permission_denied), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.btn_undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkWriteExternalPermission();
                            }
                        }).show();
                    } else { /*-User denied and check never ask again*/
                        this.showRequestPermissionDialog(getString(R.string.permission_rationale_read_external_storage));
                    }
                }
                break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (resultCode == RESULT_OK) {
                    SystemBroadcaster.requestMediaScanner(this);
                }
                break;
            }
            case VIEW_IMAGE_ACTIVITY: {
                if (resultCode == RESULT_OK) {
                    String imagePath = data.getStringExtra("imagePath");
                    String imageExtension = data.getStringExtra("imageExtension");
                    String fileName = String.valueOf(System.currentTimeMillis() / 1000) + "_" + LocalStorage.getFromLocal(this, LocalStorage.KEY_USER_EMAIL);
                    if (null != imageExtension && !imageExtension.isEmpty())
                        fileName += "." + imageExtension;
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
                        UploadRequest.getUploadRequest(this, imagePath, fileName).setDelegate(new UploadStatusDelegate() {
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
                                    Log.d("SERVER RESPONSE", response);
                                    if (response.toLowerCase().startsWith("ocr result will send to")) {
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
                break;
            }
        }
    }

    /**
     * Fetch all image id from MediaStore
     */
    private void getListImageId() {
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.MIME_TYPE};
        Cursor cur = getContentResolver().query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                MediaStore.Images.Media.DATE_TAKEN + " DESC"        // Order by date taken descending
        );

        if (null != cur && cur.getCount() > 0) {
            this.listImageId = new ArrayList<>();
            Log.d("Image count", String.valueOf(cur.getCount()));
            if (cur.moveToFirst()) {
                int id;
                String mimeType;
                do {
                    mimeType = cur.getString(cur.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                    if (!"image/gif".equals(mimeType)) {
                        id = cur.getInt(cur.getColumnIndex(MediaStore.Images.Media._ID));
                        this.listImageId.add(id);
                    }
                } while (cur.moveToNext());
            }
            cur.close();
        } else {
            Log.d("ERROR", "Cursor image null");
        }
    }

    /**
     * Reload grid images by create new instance of GalleryImageAdapter
     */
    private void loadGridImage() {
        this.getListImageId();
        if (null != this.listImageId && this.listImageId.size() > 0) {
            GalleryImageAdapter adapter = new GalleryImageAdapter(GalleryActivity.this, this.listImageId, this.imageViewWidth, this.imageViewHeight);
            gridImageGallery.setAdapter(adapter);
            gridImageGallery.setVisibility(View.VISIBLE);
            textViewGuide.setVisibility(View.INVISIBLE);
        } else {
            gridImageGallery.setVisibility(View.INVISIBLE);
            textViewGuide.setVisibility(View.VISIBLE);
            Snackbar.make(this.coordinatorLayoutContainer, getString(R.string.err_list_image_null), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Start camera
     */
    private void startCameraIntent() {
        File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "NewAI");
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "NewAI/" + (Calendar.getInstance().getTimeInMillis() + ".jpg"));
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(i, MY_PERMISSIONS_REQUEST_CAMERA);
    }

    /**
     * Show rationale for asking a permission
     *
     * @param rationale message to show on dialog, use <b>string.xml</b> to get rationale
     */
    private void showRequestPermissionDialog(String rationale) {
        /*-Check if rationale is null or empty*/
        if (null == rationale || rationale.isEmpty())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(rationale);
        builder.setPositiveButton(getString(R.string.btn_system_setting),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*-Go to system setting*/
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        GalleryActivity.this.startActivity(intent);
                    }
                });
        builder.setNegativeButton(getString(R.string.btn_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       /*-Dismiss dialog*/
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
