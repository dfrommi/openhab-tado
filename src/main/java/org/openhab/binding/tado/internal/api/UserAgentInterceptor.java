package org.openhab.binding.tado.internal.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Interceptor to set user-agent header on API requests.
 *
 * @author Dennis Frommknecht - Iniital contribution
 */
public class UserAgentInterceptor implements Interceptor {

    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        okhttp3.Request originalRequest = chain.request();
        okhttp3.Request requestWithUserAgent = originalRequest.newBuilder().header("User-Agent", userAgent).build();
        return chain.proceed(requestWithUserAgent);
    }
}
