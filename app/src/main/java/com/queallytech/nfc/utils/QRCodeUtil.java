package com.queallytech.nfc.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class QRCodeUtil {
    public static Bitmap createQRCodeBitmap(String content, int width, int height, String character_set, String error_correction_level, String margin, int color_black, int color_white, Bitmap logoBitmap, float logoPercent, Bitmap bitmap_black) {
        Bitmap bitmap_black2 = bitmap_black;
        if (!TextUtils.isEmpty(content) && width >= 0 && height >= 0) {
            try {
                Hashtable<EncodeHintType, String> hints = new Hashtable<>();
                if (!TextUtils.isEmpty(character_set)) {
                    hints.put(EncodeHintType.CHARACTER_SET, character_set);
                }
                if (!TextUtils.isEmpty(error_correction_level)) {
                    hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
                }
                if (!TextUtils.isEmpty(margin)) {
                    hints.put(EncodeHintType.MARGIN, margin);
                }
                BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
                if (bitmap_black2 != null) {
                    bitmap_black2 = Bitmap.createScaledBitmap(bitmap_black2, width, height, false);
                }
                int[] pixels = new int[width * height];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (bitMatrix.get(x, y)) {
                            if (bitmap_black2 != null) {
                                pixels[(y * width) + x] = bitmap_black2.getPixel(x, y);
                            } else {
                                pixels[(y * width) + x] = color_black;
                            }
                        } else {
                            pixels[(y * width) + x] = color_white;
                        }
                    }
                }
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                if (logoBitmap != null) {
                    return addLogo(bitmap, logoBitmap, logoPercent);
                }
                return bitmap;
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Nullable
    private static Bitmap addLogo(@Nullable Bitmap srcBitmap, @Nullable Bitmap logoBitmap, float logoPercent) {
        if (srcBitmap == null) {
            return null;
        }
        if (logoBitmap == null) {
            return srcBitmap;
        }
        logoPercent = (logoPercent < 0.0f || logoPercent > 1.0f) ? 0.2f : 0.2f;
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        int logoWidth = logoBitmap.getWidth();
        int logoHeight = logoBitmap.getHeight();
        float scaleWidth = (srcWidth * logoPercent) / logoWidth;
        float scaleHeight = (srcHeight * logoPercent) / logoHeight;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(srcBitmap, 0.0f, 0.0f, (Paint) null);
        canvas.scale(scaleWidth, scaleHeight, srcWidth / 2, srcHeight / 2);
        canvas.drawBitmap(logoBitmap, (srcWidth / 2) - (logoWidth / 2), (srcHeight / 2) - (logoHeight / 2), (Paint) null);
        return bitmap;
    }
}
