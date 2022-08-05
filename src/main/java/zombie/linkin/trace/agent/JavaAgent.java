package zombie.linkin.trace.agent;

import java.lang.instrument.Instrumentation;

/**
 * Logger transformer java agent
 *
 * @author linkin
 */
public class JavaAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        instrumentation.addTransformer(new ClassicLoggerTransformer(), true);
    }
}
