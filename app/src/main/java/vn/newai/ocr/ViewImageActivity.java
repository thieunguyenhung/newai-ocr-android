package vn.newai.ocr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import vn.newai.ocr.utility.ImageHelper;
import vn.newai.ocr.utility.LocalStorage;

public class ViewImageActivity extends AppCompatActivity {
    private Toolbar imageViewToolbar; //custom toolbar
    private ImageView imageView;
    private ProgressBar progressBar;
    private FloatingActionButton fabSend;

    private boolean toolbarStatus; /*-true=show*/

    private String imagePath; /*-Image Path*/
    private String imageExtension; /*-Image Extension*/
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

        addControls();
        addEvents();
    }

    private void addControls() {
         /*Toolbar*/
        toolbarStatus = true; //default is true
        imageViewToolbar = (Toolbar) findViewById(R.id.toolbarImageView);
        imageViewToolbar.setNavigationIcon(R.drawable.ic_action_nav_back);
        setSupportActionBar(imageViewToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        /*FAB Send*/
        fabSend = (FloatingActionButton) findViewById(R.id.viewImageFABSend);

        /*Progressbar*/
        progressBar = (ProgressBar) findViewById(R.id.viewImageProgressBar);

        /*Image view*/
        imageView = (ImageView) findViewById(R.id.viewImageImageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (null != this.getIntent()) {
            Bundle bundle = getIntent().getExtras();
            if (null != bundle) {
                int imageId = bundle.getInt("imageId");
                Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
                //Log.d("Image Uri", imageUri.toString());
                imagePath = ImageHelper.getRealPathFromURI(this, imageId);
                imageExtension = ImageHelper.getImageExtension(this, imageId);

                //Load thumbnail from Uri with Glide
                Glide.with(ViewImageActivity.this).load(imageUri).listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        //Hide progress bar when done
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(imageView);
            }
        }
    }

    private void addEvents() {
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        imageViewToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewImageActivity.this.finish();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*-if (ViewImageActivity.this.toolbarStatus) {
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
                }*/
                if (ViewImageActivity.this.toolbarStatus) {
                    hideOtherComponents();
                } else {
                    showOtherComponents();
                }
                ViewImageActivity.this.toolbarStatus = !ViewImageActivity.this.toolbarStatus;
            }
        });
    }

    private void hideOtherComponents() {
        this.imageViewToolbar.animate().translationY(-this.imageViewToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        this.fabSend.animate().translationY(+this.fabSend.getBottom()).setInterpolator(new AccelerateInterpolator()).start();

    }

    private void showOtherComponents() {
        this.imageViewToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        this.fabSend.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
    }
}
