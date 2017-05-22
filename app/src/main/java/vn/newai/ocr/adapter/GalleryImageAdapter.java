package vn.newai.ocr.adapter;

import android.app.Activity;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import vn.newai.ocr.R;

public class GalleryImageAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<Integer> listImageId;
    private int imageWidthPixel, imageHeightPixel;

    public GalleryImageAdapter(Activity activity, ArrayList<Integer> listImageId, int imageWidthPixel, int imageHeightPixel) {
        this.activity = activity;
        this.listImageId = listImageId;
        this.imageWidthPixel = imageWidthPixel;
        this.imageHeightPixel = imageHeightPixel;
    }

    @Override
    public int getCount() {
        return listImageId.size();
    }

    @Override
    public Object getItem(int position) {
        return listImageId.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = this.activity.getLayoutInflater();
        convertView = inflater.inflate(R.layout.item_list_gallery, null);

        //Image view
        ImageView imageView = (ImageView) convertView.findViewById(R.id.galleryImageViewGridThumbItem);
        imageView.requestLayout();
        imageView.getLayoutParams().width = this.imageWidthPixel;
        imageView.getLayoutParams().height = this.imageHeightPixel;
        //imageView.setImageURI(this.listImageId.get(position));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //Progress bar
        final ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.galleryProgressBarGridThumbItem);

        //thumbnail uri
        Uri thumbnailURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(this.listImageId.get(position)));
        //Load thumbnail from Uri with Glide
        Glide.with(this.activity).load(thumbnailURI).listener(new RequestListener<Uri, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(imageView);

        return convertView;
    }
}
