package jnpf.model.email;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:06
 */
@Data
public class EmailInfoModel {
    private String fileId;
    private String fileName;
    private String fileSize;
    private String fileTime;
    private String fileState;
}
