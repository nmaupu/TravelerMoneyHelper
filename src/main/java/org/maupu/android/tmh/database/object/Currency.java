package org.maupu.android.tmh.database.object;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Date;
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
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.DatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class Currency extends BaseObject {
	private static final long serialVersionUID = 1L;
	private final static String googleUpdateRateURL = "http://www.google.com/ig/calculator?hl=en&q=1EUR%3D%3F";

	private String longName;
	private String shortName;
	private Double tauxEuro;
	private Date lastUpdate;
	private String isoCode;
	
	public String getLongName() {
		return longName;
	}
	public String getShortName() {
		return shortName;
	}
	public Double getTauxEuro() {
		return tauxEuro;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public String getIsoCode() {
		return isoCode;
	}
	public void setLongName(String longName) {
		this.longName = longName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public void setTauxEuro(Double tauxEuro) {
		this.tauxEuro = tauxEuro;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}
	
	public ContentValues createContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(CurrencyData.KEY_LONG_NAME, this.getLongName());
		cv.put(CurrencyData.KEY_SHORT_NAME, this.getShortName());
		cv.put(CurrencyData.KEY_TAUX_EURO, this.getTauxEuro());
		if(this.getLastUpdate() != null) {
			cv.put(CurrencyData.KEY_LAST_UPDATE, DatabaseHelper.formatDateForSQL(this.getLastUpdate()));
		}
		cv.put(CurrencyData.KEY_ISO_CODE, this.getIsoCode());
		
		return cv;
	}
	
	@Override
	public String getTableName() {
		return CurrencyData.TABLE_NAME;
	}
	
	@Override
	public BaseObject toDTO(Cursor cursor) throws IllegalArgumentException {
		this.reset();
		int idxId = cursor.getColumnIndexOrThrow(CurrencyData.KEY_ID);
		int idxLongName = cursor.getColumnIndexOrThrow(CurrencyData.KEY_LONG_NAME);
		int idxShortName = cursor.getColumnIndexOrThrow(CurrencyData.KEY_SHORT_NAME);
		int idxTauxEuro = cursor.getColumnIndexOrThrow(CurrencyData.KEY_TAUX_EURO);
		int idxLastUpdate = cursor.getColumnIndexOrThrow(CurrencyData.KEY_LAST_UPDATE);
		int idxIsoCode = cursor.getColumnIndexOrThrow(CurrencyData.KEY_ISO_CODE);
		
		if(! cursor.isClosed() && ! cursor.isBeforeFirst() && ! cursor.isAfterLast()) {
			this._id = cursor.getInt(idxId);
			this.setLongName(cursor.getString(idxLongName));
			this.setShortName(cursor.getString(idxShortName));
			this.setTauxEuro(cursor.getDouble(idxTauxEuro));
			String sDate = cursor.getString(idxLastUpdate);
			if(sDate != null) {
				this.setLastUpdate(DatabaseHelper.toDate(sDate));
			}
			this.setIsoCode(cursor.getString(idxIsoCode));
		}
		
		return super.getFromCache();
	}
	
	@Override
	public boolean validate() {
		return true;
	}
	
	@Override
	public void reset() {
		super._id = null;
		this.lastUpdate = null;
		this.longName = null;
		this.shortName = null;
		this.tauxEuro = null;
		this.isoCode = null;
	}
	
	@Override
	public String toString() {
		return this.getLongName();
	}
	@Override
	public String getDefaultOrderColumn() {
		return CurrencyData.KEY_LONG_NAME;
	}
	
	public void updateRateFromGoogle() throws Exception {
		String isoCode = getIsoCode();
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
			
			if(!"".equals(error))
				throw new Exception("An error occured getting rate for "+isoCode+" - "+error);
			
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
				this.setTauxEuro(Double.parseDouble(sRate));
				
				Log.d(Currency.class.toString(), "Rate for "+isoCode+" = "+sRate);
			} else {
				throw new Exception("Response is unreadable for "+isoCode);
			}
			
			Log.d(Currency.class.toString(), "JSON : "+jsonObj.getString("rhs"));
		} catch (JSONException je) {
			throw je;
		}
	}
}
