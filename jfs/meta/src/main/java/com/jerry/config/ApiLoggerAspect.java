package com.jerry.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerry.utils.RequestUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.CodeSigner;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Slf4j
@Component
public class ApiLoggerAspect {
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;



    public ApiLoggerAspect(HttpServletRequest request,ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @Pointcut("execution(* com.jerry.controller..*Controller.*(..))")
    public void apiLog(){
    }

    //定义一个内部类，返回日志的处理
    @Data
    @Accessors(chain = true) //链式
    public static class LogModel{
        private String ip; //具体文件的序号
        private String method; //请求的方法
        private String path; //具体的路径
        private Map<String,Object> params; //请求的参数
        private Object response; //状态回应
        private ExceptionInfo exception;
        private Long timestamp; //时间戳
        private Long cost;//记录接口花费多少时间去响应

    }
    @Around("apiLog()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        LogModel logModel = new LogModel();
        long start = System.currentTimeMillis();
        Object result = point.proceed();
        //解析请求参数
        parseRequest(point,logModel);
        //设置返回值
        logModel.setResponse(result);
        long end = System.currentTimeMillis();
        logModel.setTimestamp(end);
        logModel.setCost(end-start);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logModel));
        return result;
    }
    @AfterThrowing(value = "apiLog()",throwing = "e")
    public void afterThrowing(JoinPoint point,Throwable e) throws JsonProcessingException {
        long start = System.currentTimeMillis();
        StackTraceElement[] stackTrace = e.getStackTrace();
        String stackTracing = Arrays.toString(stackTrace).replace("]", "").replace("[", "");
        StackTraceElement stackTraceElement = e.getStackTrace()[0];
        ExceptionInfo exceptionInfo = new ExceptionInfo();
                           exceptionInfo.setMessage(e.getMessage())
                                        .setFilename(stackTraceElement.getFileName())
                                        .setClassName(stackTraceElement.getClassName())
                                        .setMethodName(stackTraceElement.getMethodName())
                                        .setDetails(stackTracing);
                           LogModel logModel = new LogModel();
                           logModel.setException(exceptionInfo);
        //解析请求参数
        parseRequest((ProceedingJoinPoint) point,logModel);
        //设置返回值
        long end = System.currentTimeMillis();
        logModel.setTimestamp(end);
        logModel.setCost(end-start);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logModel));
    }

    private void parseRequest(ProceedingJoinPoint point,LogModel logModel){
        String ip = RequestUtil.getIpAddr(request);
        String method = request.getMethod();
        String path = request.getRequestURI();

        Object[] args = point.getArgs();
        CodeSignature signature = (CodeSignature) point.getSignature();
        String[] paraNames = signature.getParameterNames();
        Map<String,Object> params = new HashMap<>(paraNames.length);
        for (int i = 0; i < paraNames.length; i++) {
            params.put(paraNames[i],args[i].toString());
        }
        logModel.setIp(ip)
                .setMethod(method)
                .setPath(path.toString())
                .setParams(params);

    }
    //定义一个异常内部类
    @Data
    @Accessors(chain = true)
    public static class ExceptionInfo{
        private String message; //报错的哪一个具体消息
        private String filename; //具体的文件名
        private String className; //具体哪一个类出现异常
        private String methodName;//具体哪一个方法名
        private Integer lineNumber; //具体哪一行代码出现问题
        private Object details; //有没有需要补充的信息
    }

}
