package jnpf.config.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Minio属性配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-07
 */
@ConfigurationProperties(prefix = "jnpf.minio")
public class MinioConfigurationProperties {
    /**
     * 服务端地址
     */
    private String endpoint;
    /**
     * 账号
     */
    private String accessKey;
    /**
     * 密码
     */
    private String secretKey;
    private String fileHost;
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getFileHost() {
        return fileHost;
    }

    public void setFileHost(String fileHost) {
        this.fileHost = fileHost;
    }
}
