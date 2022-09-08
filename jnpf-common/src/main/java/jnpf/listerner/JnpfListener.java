package jnpf.listerner;

import jnpf.config.ConfigValueUtil;
import jnpf.util.RedisUtil;
import jnpf.util.context.SpringContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
public class JnpfListener implements ApplicationListener<ContextRefreshedEvent> {

    private ConfigValueUtil configValueUtil;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        configValueUtil=SpringContext.getBean(ConfigValueUtil.class);
        if("false".equals(configValueUtil.getTestVersion())){
            RedisUtil redisUtil = SpringContext.getBean(RedisUtil.class);
            redisUtil.removeAll();
        }
    }
}