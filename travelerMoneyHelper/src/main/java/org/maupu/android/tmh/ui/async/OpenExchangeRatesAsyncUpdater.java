package org.maupu.android.tmh.ui.async;

import androidx.fragment.app.FragmentActivity;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.TmhLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OpenExchangeRatesAsyncUpdater extends AbstractOpenExchangeRates {
    private final static Class TAG = OpenExchangeRatesAsyncUpdater.class;
    private final static String OER_CACHE_CURRENCIES_LATEST = "OpenExchangeRatesLatest.json";
    /**
     * Stored as a long in StaticData for further reference. Corresponds to the freshness of rates
     * given by OpenExchangeRates api (field timestamp)
     *
     * @see <a href="https://openexchangerates.org/documentation">https://openexchangerates.org/documentation</a>
     */
    private final static String OER_CURRENCIES_TIMESTAMP = "OERCurrenciesTimestamp";
    private String apiKey;
    private boolean cacheEnabled = true;

    public OpenExchangeRatesAsyncUpdater(FragmentActivity context, String apiKey) {
        super(context, "Updating currencies");
        this.apiKey = apiKey;
    }

    public OpenExchangeRatesAsyncUpdater(FragmentActivity context, String apiKey, boolean cacheEnabled) {
        super(context, "Updating currencies");
        this.apiKey = apiKey;
        this.cacheEnabled = cacheEnabled;
    }

    protected Exception doInBackground(Currency... currencies) {
        if (currencies == null || currencies.length == 0)
            return new Exception("No currency given");

        int nbTotal = currencies.length;
        int nbProcessedSucc = 0;
        int nbProcessed = 0;
        publishProgress(0);

        for (Currency currency : currencies) {
            try {
                nbProcessedSucc += updateRateFromOpenExchangeRates(currency, cacheEnabled) ? 1 : 0;
                nbProcessed++;
                publishProgress(nbProcessed * 100 / nbTotal);
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
        }

        publishProgress(100);
        return null;
    }

    public boolean updateRateFromOpenExchangeRates(Currency currency, boolean cacheEnabled) throws Exception {
        StringBuilder url = AbstractOpenExchangeRates.getUrl(ACTION_CURRENCY_LATEST, apiKey);
        boolean errorOccurred = false;

        StringBuilder builderJson = null;
        if (cacheEnabled)
            builderJson = loadFromCache(OER_CACHE_CURRENCIES_LATEST, DEFAULT_CACHE_LIMIT_TIME);

        // Load from internet
        if (builderJson == null || "".equals(builderJson.toString())) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .build();
            Request req = new Request.Builder()
                    .url(url.toString())
                    .build();

            try (Response response = client.newCall(req).execute()) {
                int statusCode = response.code();
                if (statusCode == 200) {
                    InputStream content = response.body().byteStream();
                    BufferedReader bufReader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    builderJson = new StringBuilder();
                    while ((line = bufReader.readLine()) != null) {
                        builderJson.append(line);
                    }

                    // Cache json values
                    saveToCache(builderJson.toString(), OER_CACHE_CURRENCIES_LATEST);
                } else {
                    TmhLogger.e(TAG, "Unable to get rate list, verify your api key");
                    throw new Exception("Unable to get rate list, verify your api key - Status code not 200");
                }
            } catch (IOException ioe) {
                // Network problem ? Load from cache anyway
                builderJson = loadFromCache(OER_CACHE_CURRENCIES_LATEST, -1);
                errorOccurred = true;
            } catch (Exception e) {
                e.printStackTrace();
                errorOccurred = true;
            }
        }

        // Parse json
        if (builderJson != null && !"".equals(builderJson.toString())) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(builderJson.toString(), JsonNode.class);

            try {
                Currency mainCurrency = StaticData.getMainCurrency();

                // Store latest timestamp in StaticData
                JsonNode timestampNode = rootNode.get("timestamp");
                if (timestampNode != null) {
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
                if (currency.getId() != null)
                    currency.update();

            } catch (NullPointerException npe) {
                // Misc problem
                return false;
            }
        } else {
            // Nothing to parse
            return false;
        }

        // Everything ok or error occurred during process
        return errorOccurred;
    }

    /**
     * Get date of the last currencies' rates cache available.
     *
     * @return date of the last cache available
     */
    public static Date getCurrencyRatesCacheDate() {
        return StaticData.getDateField(OER_CACHE_CURRENCIES_LATEST);
    }

    /**
     * Get date of the last update given by OpenExchangeRates API
     * Give the latest update in cache if using cache data
     *
     * @return date of the last rates' update
     */
    public static Date getCurrencyRatesLastUpdate() {
        return StaticData.getDateField(OER_CURRENCIES_TIMESTAMP);
    }
}
