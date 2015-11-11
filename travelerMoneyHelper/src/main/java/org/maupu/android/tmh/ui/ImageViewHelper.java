package org.maupu.android.tmh.ui;

import java.io.FileInputStream;

import org.maupu.android.tmh.R;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public abstract class ImageViewHelper {
	public static void setIcon(Context ctx, ImageView imageView, String filename) {
		try {
			FileInputStream fIn = ctx.openFileInput(filename);
			imageView.setImageBitmap(BitmapFactory.decodeStream(fIn));
		} catch (Exception e) {
			// Problem occured, setting default icon
			imageView.setImageResource(R.drawable.icon_default);
		}
	}
}
