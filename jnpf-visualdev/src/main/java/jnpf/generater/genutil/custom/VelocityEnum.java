package jnpf.generater.genutil.custom;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.util.Properties;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
public enum VelocityEnum {
    /**
     * 初始化
     */
    init;

    public void initVelocity(String path){
        Properties p = new Properties();
        p.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, path);
        p.setProperty("ISO-8859-1", Constants.UTF_8);
        p.setProperty("output.encoding", Constants.UTF_8);
        Velocity.init(p);
    }

}
