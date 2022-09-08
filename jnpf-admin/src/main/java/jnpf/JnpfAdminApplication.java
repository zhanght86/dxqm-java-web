package jnpf;

import jnpf.listerner.JnpfListener;
import jnpf.util.TaskUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 17:12
 */
@SpringBootApplication(scanBasePackages = "jnpf",exclude={DataSourceAutoConfiguration.class})
public class JnpfAdminApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(JnpfAdminApplication.class);
        //添加监听器
        springApplication.addListeners(new JnpfListener());
        springApplication.run(args);
        System.out.println("JnpfAdmin启动完成");
        TaskUtil.task();
    }

}
