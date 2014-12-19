package com.ym.superimageengine.utils.smart;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Ã»ÓÃ×÷·Ï
 * @author s0ng
 *
 */
public class BitmapImage implements SmartImage {
    private Bitmap bitmap;

    public BitmapImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap(Context context) {
        return bitmap;
    }
}