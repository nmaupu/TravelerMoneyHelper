package org.maupu.android.tmh.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public final class RawFileHelper<T extends Object> {
	private Context ctx;
	private int rawFileId;
	private ICallback<T> callback;
	
	public RawFileHelper(Context ctx, int rawFileId) {
		this.ctx = ctx;
		this.rawFileId = rawFileId;
	}
	
	public void setCallback(ICallback<T> callback) {
		this.callback = callback;
	}

	@SuppressWarnings("unchecked")
	public List<T> getRawFile() {
		List<T> ret = new ArrayList<T>();

		// Loading list
		InputStream inputStream = ctx.getResources().openRawResource(rawFileId);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

		try {
			String line;
			while ((line = br.readLine()) != null) {
				Log.d("", "Current line = "+line);
				if(callback != null)
					ret.add(callback.callback(line));
				else
					ret.add((T)line);
			}
			br.close();
		} catch (IOException ioe) {
			Log.e("RawFileHelper", ioe.getMessage());
		}
		
		return ret;
	}
}
