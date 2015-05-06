package com.yzy.supercleanmaster.model;

import android.graphics.drawable.Drawable;

public class FileInfo
{
    private Drawable fileTypeIcon;
    private String fileName;
    private String filePath;


    public FileInfo(Drawable fileTypeIcon, String fileName, String filePath)
    {
        this.fileTypeIcon = fileTypeIcon;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public Drawable getFileTypeIcon()
    {
        return fileTypeIcon;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public void setFileTypeIcon(Drawable fileTypeIcon)
    {
        this.fileTypeIcon = fileTypeIcon;
    }
}