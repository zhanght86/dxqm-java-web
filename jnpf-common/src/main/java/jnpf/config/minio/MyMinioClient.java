package jnpf.config.minio;

import io.minio.MinioClient;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Minio客户端
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-07
 */
@EnableConfigurationProperties(MinioConfigurationProperties.class)
@Component
public class MyMinioClient {
    @Autowired
    private MinioConfigurationProperties minioConfigurationProperties;
    @Autowired
    private DefaultListableBeanFactory defaultListableBeanFactory;

    @Bean
    public void minioClient(){
        MinioClient minioClient = null;
        if (StringUtil.isNotEmpty(minioConfigurationProperties.getAccessKey())){
            minioClient = MinioClient.builder().endpoint(minioConfigurationProperties.getEndpoint()).credentials(minioConfigurationProperties.getAccessKey(),minioConfigurationProperties.getSecretKey()).build();
            //将OSS对象手动注入到容器中
            defaultListableBeanFactory.registerSingleton("minioClient",minioClient);
        }else {
            minioClient = MinioClient.builder().endpoint("yourEndpoint").credentials("yourAccessKey","yourSecretKey").build();
            defaultListableBeanFactory.registerSingleton("minioClient",minioClient);
        }
    }

}
