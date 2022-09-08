package jnpf.aop;

import jnpf.util.RedisUtil;
import jnpf.util.ServletUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 可视化开发缓存数据处理
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 17:12
 */
@Slf4j
@Aspect
@Component
public class VisiualOpaAspect {

        @Autowired
        UserProvider userProvider;
        @Autowired
        private RedisUtil redisUtil;
        @Pointcut("(execution(* jnpf.onlinedev.controller.VisualdevModelDataController.*(..))) || execution(* jnpf.onlinedev.controller.VisualdevModelAppController.*(..)))" +
                "|| execution(* jnpf.generater.controller.VisualdevGenController.*(..)))")
        public void visiualOpa() {

        }

        @After("visiualOpa()")
        public void doAroundService(){
                String method=ServletUtil.getRequest().getMethod().toLowerCase();
                if("put".equals(method)||"delete".equals(method)||"post".equals(method)){
                    Set<String> allKey=new HashSet<>(16);
                    allKey.addAll(redisUtil.getAllVisiualKeys());
                    for(String key:allKey){
                        redisUtil.remove(key);
                    }
                }
        }
    }
