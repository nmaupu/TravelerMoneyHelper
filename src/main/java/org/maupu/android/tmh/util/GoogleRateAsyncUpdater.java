package org.maupu.android.tmh.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.maupu.android.tmh.R;
import org.maupu.android.tmh.TmhActivity;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.StaticData;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class GoogleRateAsyncUpdater extends AsyncTask<Currency, Integer, Integer> {
	private final static StringBuilder googleUpdateRateURL = new StringBuilder();
	private TmhActivity context;
	private ProgressDialog waitSpinner;
	
	public GoogleRateAsyncUpdater(TmhActivity context) {
		this.context = context;
		waitSpinner = new ProgressDialog(this.context);
		waitSpinner.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		waitSpinner.setTitle(R.string.currency);
		waitSpinner.setMax(100);
		
		// Setting url
		googleUpdateRateURL.append("http://www.google.com/ig/calculator?hl=en&q=1")
			.append(StaticData.getMainCurrency().getIsoCode())
			.append("%3D%3F");
	}

	protected Integer doInBackground(Currency... currencies) {
		if(currencies == null || currencies.length == 0)
			return 0;
		
		int nbTotal = currencies.length;
		int nbProcessedSucc = 0;
		int nbProcessed = 0;
		publishProgress(0);
		
		for(Currency currency : currencies) {
			try {
				nbProcessedSucc += updateRateFromGoogle(currency) ? 1 : 0;
				nbProcessed++;
				publishProgress(nbProcessed*100/nbTotal);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		publishProgress(100);
		return nbProcessedSucc;
    }

    protected void onProgressUpdate(Integer... newProgresses) {
    	if(!waitSpinner.isShowing())
    		waitSpinner.show();
    	
    	waitSpinner.incrementProgressBy(newProgresses[0]);
    	
    	if(newProgresses[0] == 100) {
    		waitSpinner.dismiss();
    		context.refreshDisplay();
    	}
    }

    protected void onPostExecute(Long result) {
    }
    
    
    private boolean updateRateFromGoogle(Currency cur) throws Exception {
		String isoCode = cur.getIsoCode();
		if(isoCode == null || "".equals(isoCode))
			throw new Exception("isoCode is not defined");
		
		StringBuilder builder = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(googleUpdateRateURL+isoCode);
		
		try {
			HttpResponse r = httpClient.execute(httpGet);
			StatusLine status = r.getStatusLine();
			int statusCode = status.getStatusCode();
			
			if(statusCode == 200) {
				HttpEntity httpEntity = r.getEntity();
				InputStream content = httpEntity.getContent();
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(content));
				String line;
				while((line = bufReader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(Currency.class.toString(), "Unable to get rate for "+isoCode);
				throw new Exception("An error occured getting rate for "+isoCode+" - Status code not 200");
			}
			
		} catch (ClientProtocolException cpe) {
			cpe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		Log.i(Currency.class.toString(), "Result = "+builder.toString());
		try {
			JSONObject jsonObj = new JSONObject(builder.toString());
			
			String error = jsonObj.getString("error");
			
			if(!"".equals(error)) {
				throw new Exception("An error occured getting rate for "+isoCode+" - "+error);
			}
			
			// Why the heck google returns a float with coma and the name of currency ??
			String result = jsonObj.getString("rhs");
			
			// Getting only the first token and get rid of currency name
			StringTokenizer st = new StringTokenizer(result, " ");
			if (st.hasMoreTokens()) {
				String tok = st.nextToken();
				
				// Replacing , by . for parsing double to work
				tok = tok.replace(',', '.');
				// Deleting no numbers chars if any (no-break space for instance)
				tok = tok.replaceAll( "[^\\d\\.]", "" );
				
				Double rate = Double.parseDouble(tok);
				DecimalFormat twoDForm = new DecimalFormat("#.##");
				String sRate = twoDForm.format(rate);
				sRate = sRate.replace(',', '.');
				cur.setRateCurrencyLinked(Double.parseDouble(sRate));
				cur.update();
				
				Log.d(Currency.class.toString(), "Rate for "+isoCode+" = "+sRate);
			} else {
				throw new Exception("Response is unreadable for "+isoCode);
			}
			
			Log.d(Currency.class.toString(), "JSON : "+jsonObj.getString("rhs"));
		} catch (JSONException je) {
			throw je;
		}
		
		return true;
	}
}
