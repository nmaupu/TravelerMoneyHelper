package org.maupu.android.tmh.ui;

import java.io.FileInputStream;

import org.maupu.android.tmh.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public abstract class ImageViewHelper {
    private static final String TAG = ImageViewHelper.class.getName();

	public static void setIcon(Context ctx, ImageView imageView, String filename) {
        Log.d(TAG, "Setting icon " + filename);
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
