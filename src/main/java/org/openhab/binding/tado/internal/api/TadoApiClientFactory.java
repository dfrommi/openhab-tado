package org.openhab.binding.tado.internal.api;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.openhab.binding.tado.internal.api.auth.OAuth;
import org.openhab.binding.tado.internal.api.auth.OAuthFlow;
import org.openhab.binding.tado.internal.api.client.PUBLICApi;
import org.openhab.binding.tado.internal.api.converter.OverlayTerminationConditionTemplateConverter;
import org.openhab.binding.tado.internal.api.converter.TerminationConditionConverter;
import org.openhab.binding.tado.internal.api.converter.ZoneCapabilitiesConverter;
import org.openhab.binding.tado.internal.api.converter.ZoneSettingConverter;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

public class TadoApiClientFactory {
    private static final String OAUTH_SCOPE = "home.user";
    private static final String OAUTH_CLIENT_ID = "public-api-preview";
    private static final String OAUTH_CLIENT_SECRET = "4HJGRffVR8xb3XdEUQpjgZ1VplJi6Xgw";
    private static final String OAUTH_TOKEN_URL = "https://auth.tado.com/oauth/token";
    private static final String API_URL = "https://my.tado.com/api/v2/";
    private static final String USER_AGENT = "openhab/tado/1.0";

    public TadoApiClient create(String username, String password) {
        PUBLICApi publicApi = createPublicApi(username, password);
        return new TadoApiClient(publicApi);
    }

    private PUBLICApi createPublicApi(String username, String password) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .registerTypeAdapter(GenericZoneSetting.class, new ZoneSettingConverter())
                .registerTypeAdapter(OverlayTerminationCondition.class, new TerminationConditionConverter())
                .registerTypeAdapter(OverlayTerminationConditionTemplate.class,
                        new OverlayTerminationConditionTemplateConverter())
                .registerTypeAdapter(GenericZoneCapabilities.class, new ZoneCapabilitiesConverter()).create();

        Builder adapterBuilder = new Retrofit.Builder().baseUrl(API_URL)
                // .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson));

        ApiClient apiClient = new ApiClient();
        apiClient.setAdapterBuilder(adapterBuilder);
        apiClient.getOkBuilder().addInterceptor(new UserAgentInterceptor(USER_AGENT));

        OAuth oauth = new OAuth(OAuthClientRequest.tokenLocation(OAUTH_TOKEN_URL).setScope(OAUTH_SCOPE)
                .setClientId(OAUTH_CLIENT_ID).setClientSecret(OAUTH_CLIENT_SECRET));
        oauth.setFlow(OAuthFlow.password);

        oauth.getTokenRequestBuilder().setUsername(username).setPassword(password);

        apiClient.addAuthorization("oauth", oauth);
        // configureLogging(apiClient);

        return apiClient.createService(PUBLICApi.class);
    }

    private void configureLogging(ApiClient apiClient) {
        // Add logging interceptor to HTTP Client if Debug is enabled. Make it configurable?
        Logger logger = LoggerFactory.getLogger(PUBLICApi.class);
        if (logger.isDebugEnabled()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String msg) {
                    logger.debug(msg);
                }
            });
            loggingInterceptor.setLevel(Level.BODY);
            apiClient.getOkBuilder().addNetworkInterceptor(loggingInterceptor);
        }
    }
}
