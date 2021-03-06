package org.maupu.android.tmh.ui.async;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

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
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.TmhLogger;

import android.util.Log;

public class OpenExchangeRatesAsyncUpdater extends AbstractOpenExchangeRates {
    private final static Class TAG = OpenExchangeRatesAsyncUpdater.class;
	private final static String OER_CACHE_CURRENCIES_LATEST = "OpenExchangeRatesLatest.json";
    /**
     *  Stored as a long in StaticData for further reference. Corresponds to the freshness of rates
     *  given by OpenExchangeRates api (field timestamp)
     *  @see https://openexchangerates.org/documentation
     */
    private final static String OER_CURRENCIES_TIMESTAMP = "OERCurrenciesTimestamp";
	private String apiKey = null;
    private boolean cacheEnabled = true;

    public OpenExchangeRatesAsyncUpdater(TmhActivity context, String apiKey) {
        super(context, "Updating currencies");
        this.apiKey = apiKey;
    }

    public OpenExchangeRatesAsyncUpdater(TmhActivity context, String apiKey, boolean cacheEnabled) {
        super(context, "Updating currencies");
        this.apiKey = apiKey;
        this.cacheEnabled = cacheEnabled;
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
				nbProcessedSucc += updateRateFromOpenExchangeRates(currency, cacheEnabled) ? 1 : 0;
				nbProcessed++;
				publishProgress(nbProcessed*100/nbTotal);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		publishProgress(100);
		return nbProcessedSucc;
	}

	public boolean updateRateFromOpenExchangeRates(Currency currency, boolean cacheEnabled) throws Exception {
		StringBuilder url = super.getUrl(ACTION_CURRENCY_LATEST, apiKey);

        StringBuilder builderJson = null;
        if(cacheEnabled)
		    builderJson = loadFromCache(OER_CACHE_CURRENCIES_LATEST, DEFAULT_CACHE_LIMIT_TIME);
		
		// Load from internet
		if(builderJson == null || "".equals(builderJson.toString())) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url.toString());

			// get json from server - all rates on USD basis
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
					
					// Cache json values
					saveToCache(builderJson.toString(), OER_CACHE_CURRENCIES_LATEST);
				} else {
					TmhLogger.e(TAG, "Unable to get rate list, verify your api key");
					throw new Exception("Unable to get rate list, verify your api key - Status code not 200");
				}
			} catch (ClientProtocolException cpe) {
				cpe.printStackTrace();
			} catch (IOException ioe) {
				//ioe.printStackTrace();
				// Network problem ? Load from cache anyway
				builderJson = loadFromCache(OER_CACHE_CURRENCIES_LATEST, -1);
			}
		}

		// Parse json
		if(builderJson != null && ! "".equals(builderJson.toString())) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readValue(builderJson.toString(), JsonNode.class);

			try {
				Currency mainCurrency = StaticData.getMainCurrency();

                // Store latest timestamp in StaticData
                JsonNode timestampNode = rootNode.get("timestamp");
                if(timestampNode != null) {
                    Long timestampValue = timestampNode.getLongValue();
                    StaticData.setDateField(OER_CURRENCIES_TIMESTAMP, timestampValue);
                }

				JsonNode ratesNode = rootNode.get("rates");
				JsonNode nodeMainVal = ratesNode.get(mainCurrency.getIsoCode());
				Double mainVal = nodeMainVal.getDoubleValue();

				JsonNode n = ratesNode.get(currency.getIsoCode());
				Double v = n.getDoubleValue();

				// Compute change rate with main currency
				Double newVal = v / mainVal;

				currency.setRateCurrencyLinked(newVal);
				currency.setLastUpdate(new Date());
				if(currency.getId() != null)
					currency.update();

			} catch (NullPointerException npe) {
				// Misc problem
				return false;
			}
		} else {
			// Nothing to parse
			return false;
		}
		
		// Everything ok !
		return true;
	}

    /**
     * Get date of the last currencies' rates cache available.
     * @return date of the last cache available
     */
    public static Date getCurrencyRatesCacheDate() {
        return StaticData.getDateField(OER_CACHE_CURRENCIES_LATEST);
    }

    /**
     * Get date of the last update given by OpenExchangeRates API
     * Give the latest update in cache if using cache data
     * @return date of the last rates' update
     */
    public static Date getCurrencyRatesLastUpdate() {
        return StaticData.getDateField(OER_CURRENCIES_TIMESTAMP);
    }
}
