package vn.newai.ocr.adapter;

import android.app.Activity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import vn.newai.ocr.R;

public class FileAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<String> listPDFFilePath;

    public FileAdapter(Activity activity, ArrayList<String> listPDFFilePath) {
        this.listPDFFilePath = listPDFFilePath;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return listPDFFilePath.size();
    }

    @Override
    public Object getItem(int position) {
        return listPDFFilePath.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = this.activity.getLayoutInflater();
        convertView = inflater.inflate(R.layout.item_list_file, null);
        File file = new File(this.listPDFFilePath.get(position));
        String info = Formatter.formatShortFileSize(activity, file.length()) + " | " + new SimpleDateFormat(activity.getString(R.string.format_date_time), Locale.US).format(new Date(file.lastModified()));
        ((TextView) convertView.findViewById(R.id.fileTextNameListFileItem)).setText(file.getName());
        ((TextView) convertView.findViewById(R.id.fileTextInfoListFileItem)).setText(info);
        return convertView;
    }
}
