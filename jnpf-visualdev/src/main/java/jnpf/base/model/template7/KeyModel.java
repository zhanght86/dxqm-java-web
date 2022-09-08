package jnpf.base.model.template7;


import lombok.Data;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Data
public class KeyModel {
    /**
     * 系统自带属性
     */
    private String jnpfKey;
    /**
     * 字段名称
     */
    private String model;
    /**
     * 规则数据
     */
    private String rule;
}
