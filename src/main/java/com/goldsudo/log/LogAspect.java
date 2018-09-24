package com.goldsudo.log;

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

@Aspect
@Component
public class LogAspect {
    @Autowired
    private LogLevelDao logLevelDao;

    private Logger logger = LoggerFactory.getLogger(LogAspect.class);
    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * com.goldsudo.web.*.*(..))")
    public void webLog() {
    }

    @Before("webLog()")
    public void deBefore(JoinPoint jp) throws Throwable {
        LogLevel level = logLevelDao.selectByPrimaryKey(1);
        if (level == null) {
            level = new LogLevel(1, "INFO");
        }
        logger.info("当前远程日志级别为：" + level.getLevel());
        logger.info("方法执行开始,joinPoint:" + jp);
        startTime.set(System.currentTimeMillis());
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

}
