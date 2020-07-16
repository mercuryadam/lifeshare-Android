package com.lifeshare.imagepicker;

import android.os.Environment;

import java.io.File;

public class ImageConst {
    private static ImageConst INSTANCE;
    public final String IMAGE_DIRECTORY_PATH;

    private final String PARENT_FOLDER = "/LifeShare";
    private final String IMAGE_FOLDER = "/Images";

    public ImageConst() {
        IMAGE_DIRECTORY_PATH = GetExternalStorage()
                + PARENT_FOLDER
                + IMAGE_FOLDER;

    }

    public static ImageConst getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImageConst();
        }
        return INSTANCE;
    }

    public String GetExternalStorage() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public File CreateFileStructure() {
        File mParentFolder = new File(Environment.getExternalStorageDirectory() + PARENT_FOLDER);
        CheckIfDirExist(mParentFolder);
        File mSubFolderFolder = new File(Environment.getExternalStorageDirectory() + PARENT_FOLDER + IMAGE_FOLDER);
        CheckIfDirExist(mSubFolderFolder);
        return mParentFolder;
    }

    public void CheckIfDirExist(File mFileObject) {
        if (!mFileObject.exists()) {
            mFileObject.mkdir();
        } else {
            if (!mFileObject.isDirectory()) {
                mFileObject.mkdir();
            }
        }
    }

}
