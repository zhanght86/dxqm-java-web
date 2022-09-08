package jnpf.model.visiual;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class DownloadCodeForm {
    /**
     * 所属模块
     */
    private String module;
    /**
     * 主功能名称
     */
    private String className;
    /**
     * 子表名称集合
     */
    private String subClassName;
    /**
     * 主功能备注
     */
    private String description;

    /**
     * 数据源id
     */
    private String dataSourceId;

}
