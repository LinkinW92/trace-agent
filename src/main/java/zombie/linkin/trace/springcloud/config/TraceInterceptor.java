package zombie.linkin.trace.springcloud.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Intercept a request, add a header parameter named "tid" if it's not present.
 * For a request from web frontend, there's no tid present. This interceptor will
 * inherit a tid from upstream microservice, then the log will be joined.
 *
 * @author linkin
 */
@Slf4j
@Component
public class TraceInterceptor implements HandlerInterceptor {

    /**
     * 对于每个请求增加traceId
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse, Object handler) {
        this.privateAddHeader(httpServletRequest, Constants.TRACE_ID, UUID.randomUUID().toString().replaceAll("-", ""));
        return true;
    }

    private void privateAddHeader(HttpServletRequest request, String key, String value) {
        Class<? extends HttpServletRequest> requestClass = request.getClass();
        try {
            Field requestField = requestClass.getDeclaredField("request");
            requestField.setAccessible(true);
            Object requestObj = requestField.get(request);
            Field coyoteRequestField = requestObj.getClass().getDeclaredField("coyoteRequest");
            coyoteRequestField.setAccessible(true);
            Object coyoteRequestObj = coyoteRequestField.get(requestObj);
            Field headersField = coyoteRequestObj.getClass().getDeclaredField("headers");
            headersField.setAccessible(true);
            MimeHeaders headersObj = (MimeHeaders) headersField.get(coyoteRequestObj);
            if (null == headersObj.getHeader(key)) {
                headersObj.removeHeader(key);
                headersObj.addValue(key).setString(value);
            }
        } catch (Exception e) {
            log.error("Reflect set header {}, url:{}, error:{}", key, request.getRequestURL(), e.getMessage());
        }
    }

    /**
     * This method will be called from the javaagent
     *
     * @return
     */
    @SuppressWarnings("unused")
    public static String traceId() {
        String tid;
        try {
            // We first extract a tid from a request
            if (null != RequestContextHolder.getRequestAttributes()) {
                HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                tid = httpServletRequest.getHeader(Constants.TRACE_ID);
                if (!StringUtils.isEmpty(tid)) {
                    return tid;
                }
            }

            // Get a tid from local thread for a next try.
            tid = TraceContext.get();
            if (!StringUtils.isEmpty(tid)) {
                return tid;
            }
        } catch (Exception e) {

        }
        return "unknown";
    }


    public static class TraceContext {

        private final static ThreadLocal itl = new ThreadLocal();

        public static void trace(String tid) {
            if (!StringUtils.isEmpty(tid)) {
                itl.set(tid);
            }
        }

        public static String get() {
            Object obj = itl.get();
            return null == obj ? null : obj.toString();
        }

        public static void remove() {
            itl.remove();
        }
    }
}