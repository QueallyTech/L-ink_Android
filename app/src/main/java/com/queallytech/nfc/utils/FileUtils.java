package com.queallytech.nfc.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.queallytech.nfc.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;

    public static String getPathFromUri(Context context, Uri uri) {
        Uri mediaUri;
        if (uri == null) {
            return null;
        }
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String authority = uri.getAuthority();
            if ("com.android.externalstorage.documents".equals(authority)) {
                String[] divide = DocumentsContract.getDocumentId(uri).split(":");
                String type = divide[0];
                if ("primary".equals(type)) {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(divide[1]);
                    return path;
                }
                String path2 = "/storage/".concat(type).concat("/").concat(divide[1]);
                return path2;
            } else if ("com.android.providers.downloads.documents".equals(authority)) {
                String docId = DocumentsContract.getDocumentId(uri);
                if (docId.startsWith("raw:")) {
                    String path3 = docId.replaceFirst("raw:", "");
                    return path3;
                }
                Uri downloadUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                String path4 = queryAbsolutePath(context, downloadUri);
                return path4;
            } else if (!"com.android.providers.media.documents".equals(authority)) {
                return null;
            } else {
                String[] divide2 = DocumentsContract.getDocumentId(uri).split(":");
                String type2 = divide2[0];
                if ("image".equals(type2)) {
                    mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type2)) {
                    mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if (!"audio".equals(type2)) {
                    return null;
                } else {
                    mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String path5 = queryAbsolutePath(context, ContentUris.withAppendedId(mediaUri, Long.parseLong(divide2[1])));
                return path5;
            }
        }
        String scheme = uri.getScheme();
        if ("content".equals(scheme)) {
            String path6 = queryAbsolutePath(context, uri);
            return path6;
        } else if (!"file".equals(scheme)) {
            return null;
        } else {
            String path7 = uri.getPath();
            return path7;
        }
    }

    public static String queryAbsolutePath(Context context, Uri uri) {
        String[] projection = {"_data"};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow("_data");
                return cursor.getString(index);
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            if (cursor != null) {
                cursor.close();
                return null;
            }
            return null;
        }
    }

    public static void choosePhotoFromAlbum(Activity activity) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(activity, "android.permission.READ_MEDIA_IMAGES") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{"android.permission.READ_MEDIA_IMAGES"}, 1);
            } else {
                openAlbum(activity);
            }
        } else {
            if (ContextCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
            } else {
                openAlbum(activity);
            }
        }
    }

    public static void openAlbum(Activity activity) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        activity.startActivityForResult(intent, CHOOSE_PHOTO);
    }

    public static Uri takePhoto(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File image = null;
        try {
            String imageFileName = "photo_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            image = File.createTempFile(imageFileName, ".jpg", activity.getExternalCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri imageUri = FileProvider.getUriForFile(activity, "com.queallytech.nfc.fileprovider", image);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, TAKE_PHOTO);
        return imageUri;
    }

    public static void startCrop(Activity activity, @NonNull Uri uri) {

        String imageFileName = "photo_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(activity.getCacheDir(), imageFileName + ".jpg")));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(100);

        options.setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        options.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        options.setFreeStyleCropEnabled(true);

        uCrop.withOptions(options);
        uCrop.start(activity);
    }

    public static Bitmap rotateImage(Bitmap bm, int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;

        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);

        return bm1;
    }

    public static int readImageExif(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
}