package com.queallytech.nfc.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class FileUtils {
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
}