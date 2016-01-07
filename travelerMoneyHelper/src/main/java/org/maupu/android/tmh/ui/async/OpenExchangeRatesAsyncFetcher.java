package org.maupu.android.tmh.ui.async;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.maupu.android.tmh.TmhActivity;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.CurrencyISO4217;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.TmhLogger;

import android.util.Log;

/**
 * Fetch all currency list - no api key needed
 * @author bicnic
 */
public class OpenExchangeRatesAsyncFetcher extends AbstractOpenExchangeRates {
    private static final Class TAG = OpenExchangeRatesAsyncFetcher.class;
	private final static String OER_CACHE_CURRENCIES = "OpenExchangeRates.json";
	private List<CurrencyISO4217> currencies = null;
	
	public OpenExchangeRatesAsyncFetcher(TmhActivity context) {
		super(context, "Loading currencies");
	}
	
	public List<CurrencyISO4217> getCurrencies() {
		return currencies;
	}

	public CurrencyISO4217 getCurrency(String match) {
		if(currencies == null) {
			return null;
		} else {
			Iterator<CurrencyISO4217> it = currencies.iterator();
			while (it.hasNext()) {
				CurrencyISO4217 c = it.next();
				if(c.getName().equals(match) || c.getCode().equals(match))
					return c;
			}
		}

		return null;
	}

    @Override
	protected Integer doInBackground(Currency... params) {
    	
    	publishProgress(0);
    	
    	try {
    		currencies = getRatesList();
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	publishProgress(100);
    	
    	return currencies != null ? currencies.size() : 0;
	}


    protected List<CurrencyISO4217> getRatesList() throws Exception {
    	List<CurrencyISO4217> curList = new ArrayList<CurrencyISO4217>();

    	// Load from cache if possible
    	StringBuilder builderJson = super.loadFromCache(OER_CACHE_CURRENCIES, 2592000); // Cache valid for 30j
    	//StringBuilder builderJson = super.loadFromCache(OER_CACHE_CURRENCIES, 10);
    	
    	if(builderJson == null || "".equals(builderJson.toString())) {
    		StringBuilder sbUrl = super.getUrl(ACTION_CURRENCY_LIST, null);

    		HttpClient httpClient = new DefaultHttpClient();
    		HttpGet httpGet = new HttpGet(sbUrl.toString());

    		// get json from server
    		try {
    			HttpResponse r = httpClient.execute(httpGet);
    			StatusLine status = r.getStatusLine();
    			int statusCode = status.getStatusCode();

    			if(statusCode == 200) {
    				HttpEntity httpEntity = r.getEntity();
    				InputStream content = httpEntity.getContent();
    				BufferedReader bufReader = new BufferedReader(new InputStreamReader(content));
    				String line;
    				
    				builderJson = new StringBuilder();
    				while((line = bufReader.readLine()) != null) {
    					builderJson.append(line);
    				}

    				// Save builder to cache file
    				saveToCache(builderJson.toString(), OER_CACHE_CURRENCIES);
    			} else {
    				TmhLogger.e(TAG, "Unable to get rate list, verify your api key");
    				throw new Exception("Unable to get rate list, verify your api key - Status code not 200");
    			}
    		} catch (ClientProtocolException cpe) {
    			cpe.printStackTrace();
    		} catch (IOException ioe) {
    			//ioe.printStackTrace();
    			// Network problem ? Load from cache anyway
    			builderJson = loadFromCache(OER_CACHE_CURRENCIES, -1);
    		}
    	}

    	if(builderJson != null && ! "".equals(builderJson.toString())) {
    		// Read json from StringBuilder and parse it
    		ObjectMapper mapper = new ObjectMapper();
    		JsonNode rootNode = mapper.readValue(builderJson.toString(), JsonNode.class);

    		Iterator<String> it = (Iterator<String>)rootNode.getFieldNames();
    		while(it.hasNext()) {
    			String key = it.next();
    			JsonNode jsonNode = rootNode.get(key);
    			String value = jsonNode.getTextValue();
    			curList.add(new CurrencyISO4217(key, value));
    		}
    	}
		
		return curList;
    }

    /**
     * Get date of the last currencies' list cache available.
     * Beware : this is not the cache for rates
     * @return date of the last cache available
     */
    public static Date getCurrenciesListCacheDate() {
        return StaticData.getDateField(OER_CACHE_CURRENCIES);
    }
}
