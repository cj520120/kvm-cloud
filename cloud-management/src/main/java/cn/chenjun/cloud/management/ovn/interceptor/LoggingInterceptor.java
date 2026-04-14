package cn.chenjun.cloud.management.ovn.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Slf4j
public class LoggingInterceptor implements Interceptor {

    @Override
    @NotNull
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();

        if (log.isDebugEnabled()) {
            log.debug("Request: {} {}", request.method(), request.url());
        }

        long startTime = System.nanoTime();
        Response response = chain.proceed(request);
        long duration = System.nanoTime() - startTime;

        if (log.isDebugEnabled()) {
            log.debug("Response: {} in {}ms", response.code(), duration / 1_000_000);
        }

        return response;
    }
}
