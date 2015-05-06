package com.yzy.supercleanmaster.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.model.FileInfo;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FileChooserAdapter extends BaseAdapter
{
    LayoutInflater inflater = null;
    private List<FileInfo> mFileLists;

    public FileChooserAdapter(Context context, List<FileInfo> mFileLists)
    {
        this.inflater = LayoutInflater.from(context);
        this.mFileLists = mFileLists;
    }

    public int getCount()
    {
        return mFileLists.size();
    }

    public FileInfo getItem(int position)
    {
        return mFileLists.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.listview_file_chooser, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        FileInfo fileInfo = getItem(position);
        if (fileInfo != null)
        {
            holder.fileName.setText(fileInfo.getFileName());
            holder.filePath.setText(fileInfo.getFilePath());
            holder.fileTypeIcon.setImageDrawable(fileInfo.getFileTypeIcon());
        }
        return convertView;

    }

    static class ViewHolder
    {

        @InjectView(R.id.file_name)
        TextView fileName;

        @InjectView(R.id.file_path)
        TextView filePath;

        @InjectView(R.id.file_type_icon)
        ImageView fileTypeIcon;

        public ViewHolder(View view)
        {
            ButterKnife.inject(this, view);
        }
    }
}