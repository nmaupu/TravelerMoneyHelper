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

import android.util.Log;

public class OpenExchangeRatesAsyncUpdater extends AbstractOpenExchangeRates {
	private final static String OER_CACHE_CURRENCIES_LATEST = "OpenExchangeRatesLatest.json";
	private String apiKey = null;

	public OpenExchangeRatesAsyncUpdater(TmhActivity context, String apiKey) {
		super(context, "Updating currencies");
		this.apiKey = apiKey;
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
				nbProcessedSucc += updateRateFromOpenExchangeRates(currency) ? 1 : 0;
				nbProcessed++;
				publishProgress(nbProcessed*100/nbTotal);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		publishProgress(100);
		return nbProcessedSucc;
	}

	public boolean updateRateFromOpenExchangeRates(Currency currency) throws Exception {
		StringBuilder url = super.getUrl(ACTION_CURRENCY_LATEST, apiKey);
		
		StringBuilder builderJson = loadFromCache(OER_CACHE_CURRENCIES_LATEST, DEFAULT_CACHE_LIMIT_TIME);
		//StringBuilder builderJson = loadFromCache(OER_CACHE_CURRENCIES_LATEST, 10);
		
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
					Log.e(OpenExchangeRatesAsyncUpdater.class.getName(), "Unable to get rate list, verify your api key");
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
}
