# Trace Agent

A javaagent for logs advanced.This agent will automatically append a "tid"(Trace ID for a request)
after each log output.What you need to do is just config your SpringCloud microservice,
and then add this agent to you application, just like:
#### -javaagent:/usr/local/env/trace-agent-0.0.1.RELEASE.jar=traceroot=com.xxx.TraceInterceptor

The "traceroot" parameter tells the agent how to get a "tid" from current running context.






