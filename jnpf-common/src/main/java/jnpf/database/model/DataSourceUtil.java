package jnpf.database.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
@Component
public class DataSourceUtil {

    public static final String PREFIX = "spring.datasource";

    /**
     * 数据库类型
     */
    @Value("${" + PREFIX + ".dbtype}")
    private String dataType;

    /**
     * 地址
     */
    @Value("${" + PREFIX + ".host}")
    private String host;

    /**
     * 端口
     */
    @Value("${" + PREFIX + ".port}")
    private String port;

    /**
     * 库名
     */
    @Value("${" + PREFIX + ".dbname}")
    private String dbName;

    /**
     * 账号
     */
    @Value("${" + PREFIX + ".username}")
    private String userName;
    /**
     * 密码
     */
    @Value("${" + PREFIX + ".password}")
    private String password;

    /**
     * 表空间
     */
    @Value("${" + PREFIX + ".tablespace}")
    private String tableSpace;

    /**===============必须与用户一致==================**/

    /**
     * 模式
     */
    /*@Value("${" + PREFIX + ".schema}")
    private String dbSchema;*/

    /**================20210727暂时废弃======================**/

    /**
     * 驱动包
     */
    /*@Value("${spring.datasource.druid.driver-class-name}")
    private String driverClassName;*/

    /**
     * 数据连接字符串
     */
    /*@Value("${spring.datasource.druid.url}")
    private String url;*/
    /**
     * 空库名
     */
    /*@Value("${spring.datasource.druid.dbnull}")
    private String dbNull;*/

    /**
     * 初始库名
     */
    /*@Value("${" + PREFIX + ".dbinit}")
    private String dbInit*/;

}
