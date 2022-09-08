package jnpf.base.model.dataInterface;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据接口调用日志
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-10
 */
@Data
public class DataInterfaceLogVO implements Serializable {

    private String id;

//    private String invokId;

    private Date invokTime;

    private String userId;

    private String invokIp;

    private String invokDevice;

    private String invokType;

    private Integer invokWasteTime;

}
