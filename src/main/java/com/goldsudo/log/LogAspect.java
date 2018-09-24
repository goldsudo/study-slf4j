package com.goldsudo.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.goldsudo.dao.LogLevelDao;
import com.goldsudo.domain.LogLevel;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Aspect
@Component
public class LogAspect {
    private volatile String currentLevel = "INFO";
    private volatile long lastCheckTime = 0L;
    private volatile long TWEENTY_SECONDS = 20000L;
    private static Set<String> LOG_LEVEL_SET;

    static {
        LOG_LEVEL_SET = new HashSet<>();
        LOG_LEVEL_SET.add("OFF");
        LOG_LEVEL_SET.add("TRACE");
        LOG_LEVEL_SET.add("DEBUG");
        LOG_LEVEL_SET.add("INFO");
        LOG_LEVEL_SET.add("WARN");
        LOG_LEVEL_SET.add("ERROR");
        LOG_LEVEL_SET.add("ALL");
    }

    @Autowired
    private LogLevelDao logLevelDao;

    private Logger logger = LoggerFactory.getLogger(LogAspect.class);
    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * com.goldsudo.web.*.*(..))")
    public void webLog() {
    }

    @Before("webLog()")
    public void deBefore(JoinPoint jp) throws Throwable {
        if (timeExpired()) {
            LogLevel logLevel = logLevelDao.selectByPrimaryKey(1);
            if (logLevel == null) {
                logLevel = new LogLevel(1, "INFO");
            }
            logger.info("远程日志级别为：" + logLevel.getLevel() + " 当前日志级别为：" + currentLevel);
            if (validLevel(logLevel.getLevel()) && !currentLevel.equals(logLevel.getLevel())) {
                updateLogLevel(logLevel);
                logger.info("调整日志级别为：" + logLevel.getLevel());
                currentLevel = logLevel.getLevel();
            }
        }
        logger.info("方法执行开始,joinPoint:" + jp);
        startTime.set(System.currentTimeMillis());
    }

    @Before("webLog()")
    public void before(JoinPoint jp) {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录下请求内容
        logger.info("URL : " + request.getRequestURL().toString());
        logger.info("HTTP_METHOD : " + request.getMethod());
        logger.info("IP : " + request.getRemoteAddr());
        logger.info("CLASS_METHOD : " + jp.getSignature().getDeclaringTypeName() + "." + jp.getSignature().getName());
        logger.info("ARGS : " + Arrays.toString(jp.getArgs()));
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        logger.info("方法的返回值 : " + ret);
    }

    //后置异常通知
    @AfterThrowing("webLog()")
    public void throwss(JoinPoint jp) {
        logger.error("方法异常时执行.....");
    }

    //后置最终通知,final增强，不管是抛出异常或者正常退出都会执行
    @After("webLog()")
    public void after(JoinPoint jp) {
        logger.info("方法执行结束,joinPoint:" + jp + "，执行耗时：" + (System.currentTimeMillis() - startTime.get()) + " ms.");
    }

    private void updateLogLevel(LogLevel logLevel) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        //获取应用中的所有logger实例
        List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
        //遍历更改每个logger实例的级别,可以通过http请求传递参数进行动态配置
        for (ch.qos.logback.classic.Logger logger : loggerList) {
            logger.setLevel(Level.toLevel(logLevel.getLevel()));
        }
    }

    private boolean timeExpired() {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastCheckTime;
        if (timeDiff > TWEENTY_SECONDS) {
            lastCheckTime = currentTime;
            return true;
        }
        return false;
    }

    private boolean validLevel(String level) {
        return LOG_LEVEL_SET.contains(level);
    }

}
