package org.maupu.android.tmh.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import org.maupu.android.tmh.R;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public abstract class ImageUtil {
    public static void setIcon(ImageView imageView, Bitmap bitmap) {
        if (bitmap == null)
            imageView.setImageResource(R.drawable.tmh_icon_48);
        else
            imageView.setImageBitmap(bitmap);
    }

    public static void setIcon(ImageView imageView, byte[] icon) {
        if (icon != null) {
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(icon, 0, icon.length));
        }
    }

    public static Bitmap getBitmapIcon(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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

    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        byte[] ret;

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            ret = stream.toByteArray();
            stream.flush();
            stream.close();
        } catch (IOException ioe) {
            return null;
        }

        return ret;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
