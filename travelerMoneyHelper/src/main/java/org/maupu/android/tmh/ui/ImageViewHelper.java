package org.maupu.android.tmh.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.util.TmhLogger;

import java.io.FileInputStream;

public abstract class ImageViewHelper {
    private static final Class TAG = ImageViewHelper.class;

    public static void setIcon(Context ctx, ImageView imageView, String filename) {
        TmhLogger.d(TAG, "Setting icon " + filename);
        try {
            FileInputStream fIn = ctx.openFileInput(filename);
            imageView.setImageBitmap(BitmapFactory.decodeStream(fIn));
        } catch (Exception e) {
            // Problem occured, setting default icon
            imageView.setImageResource(R.drawable.tmh_icon_48);
        }
    }

    public static Bitmap getBitmapIcon(Context ctx, String filename) {
        try {
            FileInputStream fIn = ctx.openFileInput(filename);
            return BitmapFactory.decodeStream(fIn);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
