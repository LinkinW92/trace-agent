package zombie.linkin.trace.springcloud.config;

import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import static feign.Util.UTF_8;
import static feign.Util.decodeOrDefault;

/**
 * Log for a feign request
 *
 * @author linkin
 */
@Slf4j
public class FeignLogger extends Logger {

    @Override
    protected void log(String configKey, String format, Object... args) {
        log.info(String.format(methodTag(configKey) + format, args));
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        StringBuilder builder = new StringBuilder()
                .append(" request, ");
        Map<String, Collection<String>> queries = request.requestTemplate().queries();
        if (null != queries) {
            builder.append("queries: ");
            for (Map.Entry<String, Collection<String>> entry : queries.entrySet()) {
                if (null == entry.getValue() || CollectionUtils.isEmpty(entry.getValue())) {
                    continue;
                }
                builder.append(entry.getKey()).append("=").append(Joiner.COMMA.join(entry.getValue())).append(";");
            }
        }
        final byte[] bodyBytes = request.body();
        final Charset charset = request.charset();
        if (null != bodyBytes && null != charset) {
            builder.append(", body: ").append(new String(bodyBytes, charset));
        }
        log(configKey, "%s", builder.toString());
    }

    @Override
    protected Response logAndRebufferResponse(String configKey,
                                              Level logLevel,
                                              Response response,
                                              long elapsedTime) {
        try {
            if (null != response.body()) {
                byte[] bodyData = Util.toByteArray(response.body().asInputStream());
                StringBuilder builder = new StringBuilder(" response, ")
                        .append(decodeOrDefault(bodyData, UTF_8, "Binary data"));
                log(configKey, "%s", builder.toString());
                return response.toBuilder().body(bodyData).build();
            }
        } catch (Exception e) {
            log.error("Unexpected error while tracing response, ex:{}", e);
        }
        return response;
    }
}