package zombie.linkin.trace.agent;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

/**
 * Logger transformer java agent
 *
 * @author linkin
 */
public class JavaAgent {
    private static String TRACE_ROOT = "traceroot";

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("agentArgs:" + agentArgs);
        String[] args = agentArgs.split(",");
        Map<String, String> params = new HashMap<>(8);
        if (null != agentArgs) {
            for (String arg : agentArgs.split(",")) {
                if (null != arg) {
                    String[] pairs = arg.split("=");
                    if (pairs.length == 2) {
                        params.put(pairs[0], pairs[1]);
                    }
                }
            }
        }
        final String root = params.get(TRACE_ROOT);
        instrumentation.addTransformer(new ClassicLoggerTransformer(root), true);
    }
}
