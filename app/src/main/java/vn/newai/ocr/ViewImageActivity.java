package vn.newai.ocr;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

import vn.newai.ocr.adapter.ViewImageAdapter;
import vn.newai.ocr.utility.ImageHelper;
import vn.newai.ocr.utility.LocalStorage;

public class ViewImageActivity extends AppCompatActivity {
    private Toolbar imageViewToolbar; //custom toolbar
    private ViewPager viewPager;
    private FloatingActionButton fabSend;
    private ArrayList<Integer> listImageId;

    private String userEmail, userLang; /*-user email and language preferences*/

    @Override
    protected void onResume() {
        super.onResume();
        /*-User setting info*/
        this.userLang = LocalStorage.getFromLocal(this, LocalStorage.KEY_OCR_LANG);
        this.userEmail = LocalStorage.getFromLocal(this, LocalStorage.KEY_USER_EMAIL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        // Hide the status bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        addControls();
        addEvents();
    }

    private void addControls() {
        /*-Action bar*/
        imageViewToolbar = (Toolbar) findViewById(R.id.toolbarImageView);
        imageViewToolbar.setNavigationIcon(R.drawable.ic_action_nav_back);
        setSupportActionBar(imageViewToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        /*-FAB Send*/
        this.fabSend = (FloatingActionButton) findViewById(R.id.viewImageFABSend);

        /*-View Pager*/
        this.viewPager = (ViewPager) findViewById(R.id.viewImageViewPager);
        if (null != this.getIntent()) {
            Bundle bundle = getIntent().getExtras();
            if (null != bundle) {
                this.listImageId = bundle.getIntegerArrayList("listImageId");
                ViewImageAdapter viewImageAdapter = new ViewImageAdapter(this.getApplicationContext(), listImageId, this.imageViewToolbar, this.fabSend);
                this.viewPager.setAdapter(viewImageAdapter);
                this.viewPager.setCurrentItem(listImageId.indexOf(bundle.getInt("imageId")));
            }
        }
    }

    private void addEvents() {
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listImageId && null != viewPager) {
                    String imagePath = ImageHelper.getRealPathFromURI(ViewImageActivity.this, listImageId.get(viewPager.getCurrentItem()));
                    String imageExtension = ImageHelper.getImageExtension(ViewImageActivity.this, listImageId.get(viewPager.getCurrentItem()));
                    if (!userEmail.isEmpty() && !userLang.isEmpty()) {
                        Intent data = new Intent();
                        data.putExtra("imagePath", imagePath);
                        data.putExtra("imageExtension", imageExtension);
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        Intent intent = new Intent(ViewImageActivity.this, SettingActivity.class);
                        ViewImageActivity.this.startActivity(intent);
                    }
                }
            }
        });

        imageViewToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewImageActivity.this.finish();
            }
        });

        /*this.layoutPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ViewImageActivity.this.toolbarStatus) {
                    imageViewToolbar.animate().translationY(-imageViewToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            //imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT);
                            p.addRule(RelativeLayout.CENTER_IN_PARENT);
                            imageView.setLayoutParams(p);
                        }
                    }).start();
                } else {
                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    p.addRule(RelativeLayout.BELOW, R.id.imageViewLinearToolbar);
                    p.addRule(RelativeLayout.CENTER_IN_PARENT);
                    imageView.setLayoutParams(p);
                    imageViewToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                        }
                    }).start();
                }
                ViewImageActivity.this.toolbarStatus = !ViewImageActivity.this.toolbarStatus;
            }
        });*/
    }
}
