package jnpf.util.file;

import lombok.Data;

/**
 * 文件储存类型常量类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-04-06
 */
@Data
public class StorageType {
    /**
     * 本地存储
     */
    public static final String STORAGE = "storage";

    /**
     * Minio存储
     */
    public static final String MINIO = "minio";

}
