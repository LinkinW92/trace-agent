package zombie.linkin.trace.agent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * logger增强
 *
 * @author linkin
 */
public class ClassicLoggerTransformer implements ClassFileTransformer {
    private String C_PATH = "ch/qos/logback/classic/Logger";
    private String F_CLAZZ = "ch.qos.logback.classic.Logger";

    private String TRACE_ROOT;

    public ClassicLoggerTransformer(String TRACE_ROOT) {
        this.TRACE_ROOT = TRACE_ROOT;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals(C_PATH) || TRACE_ROOT == null || TRACE_ROOT == "") {
            return null;
        }
        try {
            System.out.println("Agent loading...");
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.get(F_CLAZZ);
            CtClass strClass = classPool.get("java.lang.String");
            CtClass objArray = classPool.get("java.lang.Object[]");
            CtClass levelClz = classPool.get("ch.qos.logback.classic.Level");
            CtClass markerClz = classPool.get("org.slf4j.Marker");
            CtClass thrClz = classPool.get("java.lang.Throwable");
            CtMethod buildLoggingEventAndAppend = ctClass.getDeclaredMethod("buildLoggingEventAndAppend", new CtClass[]{strClass, markerClz, levelClz, strClass, objArray, thrClz});
            buildLoggingEventAndAppend.insertBefore("{" +
                    "        String traceId = " + TRACE_ROOT + ".traceId();\n" +
                    "        if (!\"unknown\".equals(traceId)) {\n" +
                    "            $4 = $4 + \", tid:\" + traceId;\n" +
                    "        }}");
            System.out.println("Agent load finished...;");
            return ctClass.toBytecode();
        } catch (Throwable e) {
            System.out.println("Loading error...， ex: " + e.getMessage());
        }
        return classfileBuffer;
    }
}
