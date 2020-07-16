package com.lifeshare.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MediaHelper {


    private static final String TAG = "MediaHelper";
    private static final int RC_CAMERA_PERM = 100;
    private static MediaHelper mediaHelper;
    private int intentCode;
    private String imageCapturePath;

    public static MediaHelper getInstance() {
        if (mediaHelper == null) {
            mediaHelper = new MediaHelper();
        }
        return mediaHelper;
    }

    public static String createMediaFileName(String initial) {
        Long timeStampLong = System.currentTimeMillis();
        String timeStamp = timeStampLong.toString();
        String mImageName = initial + timeStamp + ".jpg";
        return mImageName;
    }

    public static void createMediaDirectory() {
        File mediaStorageDir = new File(
                ImageConst.getInstance().IMAGE_DIRECTORY_PATH);
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
    }


    /**
     * To save Image to SD card and get reference of it
     *
     * @return image file object reference
     */
    private File getOutputMediaFile() {
        ImageConst.getInstance().CreateFileStructure();
        File mediaStorageDir = new File(
                ImageConst.getInstance().IMAGE_DIRECTORY_PATH);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String mImageName = createMediaFileName("");

        File mediaFile;
        String path = mediaStorageDir.getPath() + File.separator + mImageName;
        mediaFile = new File(path);


        setImageCapturePath(mediaFile.getAbsolutePath());
        return mediaFile;
    }

    /**
     * To convert Image to byte Array
     *
     * @param filePath Image path
     * @return byte Array of Image
     * @throws IOException exception will be thrown when image can not be converted
     */
    public byte[] getByteArrayFromImage(String filePath) throws IOException {

        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                bos.write(buf, 0, readNum);
                //no doubt here is 0
                /*Writes len bytes from the specified byte array starting at offset
                off to this byte array output stream.*/
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        byte[] bytes = bos.toByteArray();
        return bytes;
    }

    /**
     * To retrieve real path from Content URI
     *
     * @param context    application context
     * @param contentUri
     * @return path in String
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void deleteImageFromDirectory(String selectedFilePath) {
        File dir = new File(selectedFilePath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    public String getImageCapturePath() {
        return imageCapturePath;
    }

    public void setImageCapturePath(String imageCapturePath) {
        this.imageCapturePath = imageCapturePath;
    }

    public boolean copyFile(String selectedImagePath, String storeImagePath) throws IOException {
     /*   InputStream in = new FileInputStream(selectedImagePath);
        OutputStream out = new FileOutputStream(storeImagePath);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
*/
        File file = new File(storeImagePath);
        file.createNewFile();

        Bitmap bitmap = BitmapFactory.decodeFile(new File(selectedImagePath).toString());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();


        ExifInterface ei = new ExifInterface(selectedImagePath);

        int o = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        ExifInterface exif = new ExifInterface(storeImagePath);
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(o));

        Matrix matrix = new Matrix();
        String orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        if (orientation.equals(String.valueOf(ExifInterface.ORIENTATION_NORMAL))) {

        } else if (orientation.equals(String.valueOf(ExifInterface.ORIENTATION_ROTATE_90))) {
            matrix.postRotate(90);
        } else if (orientation.equals(String.valueOf(ExifInterface.ORIENTATION_ROTATE_180))) {
            matrix.postRotate(180);
        } else if (orientation.equals(String.valueOf(ExifInterface.ORIENTATION_ROTATE_270))) {
            matrix.postRotate(270);
        }

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 55, bos);
        byte[] bitmapdata = bos.toByteArray();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        return true;
    }

    public void singleMediaScanner(Activity activity) {
        File mediaFile = new File(getImageCapturePath());
        Uri uri = Uri.fromFile(mediaFile);
        Intent scanFileIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        activity.sendBroadcast(scanFileIntent);

    }
}
