package jnpf.aop;

import jnpf.base.LogSortEnum;
import jnpf.base.UserInfo;
import jnpf.config.ConfigValueUtil;
import jnpf.entity.LogEntity;
import jnpf.service.LogService;
import jnpf.util.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 17:12
 */
@Slf4j
@Aspect
@Component
@Order(2)
public class RequestLogAspect {

    @Autowired
    UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private LogService logService;

    @Pointcut("(execution(* jnpf.*.controller.*.*(..)) || execution(* jnpf.message.websocket.WebSocket.*(..)))&&!execution(* jnpf.controller.UtilsController.*(..)) ")
    public void requestLog() {

    }

    @Around("requestLog()")
    public Object doAroundService(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object obj = pjp.proceed();
        long costTime = System.currentTimeMillis() - startTime;
        printLog(costTime);
        return obj;
    }

    private void printLog(long costTime)
    {
        UserInfo userInfo=userProvider.get();
        if(StringUtil.isEmpty(userInfo.getUserId())){
            return;
        }
        LogEntity entity = new LogEntity();
        entity.setId(RandomUtil.uuId());
        entity.setCategory(LogSortEnum.Operate.getCode());
        entity.setUserId(userInfo.getUserId());
        entity.setUserName(userInfo.getUserName() + "/" + userInfo.getUserAccount());
        //请求耗时
        entity.setRequestDuration((int)costTime);
        entity.setRequestUrl(ServletUtil.getRequest().getServletPath());
        entity.setRequestMethod(ServletUtil.getRequest().getMethod());
        entity.setCategory(5);
        entity.setUserId(userInfo.getUserId());
        entity.setIpAddress(IpUtil.getIpAddr());
        entity.setCreatorTime(new Date());
        entity.setPlatForm(ServletUtil.getUserAgent());
        logService.save(entity);
    }
}
