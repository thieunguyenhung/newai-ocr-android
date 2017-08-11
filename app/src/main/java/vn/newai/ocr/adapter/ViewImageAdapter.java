package vn.newai.ocr.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;

import vn.newai.ocr.R;

public class ViewImageAdapter extends PagerAdapter {
    private ArrayList<Integer> listImageId;
    private Context context;
    private Toolbar imageViewToolbar;
    private FloatingActionButton fabSend;
    /*Toolbar status to indicate hiding or showing*/
    private boolean toolbarStatus; /*-true=show*/

    public ViewImageAdapter(Context context, ArrayList<Integer> listImageId, Toolbar imageViewToolbar, FloatingActionButton fabSend) {
        this.context = context;
        this.listImageId = listImageId;
        this.imageViewToolbar = imageViewToolbar;
        this.fabSend = fabSend;
        this.toolbarStatus = true; //default is true
    }

    @Override
    public int getCount() {
        return this.listImageId.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View imageLayout = LayoutInflater.from(context).inflate(R.layout.item_list_view_image, container, false);
        if (null != imageLayout) {
            final ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.viewImageItemProgressBar);

            Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(this.listImageId.get(position)));
            SubsamplingScaleImageView scaleImageView = (SubsamplingScaleImageView) imageLayout.findViewById(R.id.viewImageItemScaleImageView);
            scaleImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF); //Use original image rotation
            scaleImageView.setImage(ImageSource.uri(imageUri));
            scaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
                @Override
                public void onReady() {

                }

                @Override
                public void onImageLoaded() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onPreviewLoadError(Exception e) {

                }

                @Override
                public void onImageLoadError(Exception e) {

                }

                @Override
                public void onTileLoadError(Exception e) {

                }

                @Override
                public void onPreviewReleased() {

                }
            });
            scaleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*-Hide action bar and floatting action send button*/
                    if (ViewImageAdapter.this.toolbarStatus)
                        hideOtherComponents();
                    else
                        showOtherComponents();
                    ViewImageAdapter.this.toolbarStatus = !ViewImageAdapter.this.toolbarStatus;
                }
            });
        }
        container.addView(imageLayout, 0);
        return imageLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Parcelable saveState() {
        return null;
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
