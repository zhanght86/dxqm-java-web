package jnpf.model;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author JNPF开发平台组
 */
@Data
@Component
public class YozoParams implements InitializingBean {


    @Value("${config.YozoDomainKey}")
    private String domainKey;

    @Value("${config.YozoDomain}")
    private String domain;

    @Value("${config.YozoCloudDomain}")
    private String cloudDomain;

    @Value("${config.YozoAppId}")
    private String appId;

    @Value("${config.YozoAppKey}")
    private String appKey;

    @Value("${config.YozoEditDomain}")
    private String editDomain;

    @Value("${config.Domain}")
    private String jnpfDomain;

    public static String DOMAIN_KEY;
    public static String DOMAIN;
    public static String CLOUD_DOMAIN;
    public static String APP_ID;
    public static String APP_KEY;
    public static String EDIT_DOMAIN;
    public static String JNPF_DOMAINS;

    @Override
    public void afterPropertiesSet() {
        DOMAIN=domain;
        DOMAIN_KEY=domainKey;
        CLOUD_DOMAIN=cloudDomain;
        APP_ID=appId;
        APP_KEY=appKey;
        EDIT_DOMAIN=editDomain;
        JNPF_DOMAINS=jnpfDomain;
    }
}
