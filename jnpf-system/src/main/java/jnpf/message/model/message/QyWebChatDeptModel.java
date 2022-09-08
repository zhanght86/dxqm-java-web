package jnpf.message.model.message;

import lombok.Data;

/**
 * 企业微信获取部门的对象模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/25 11:10
 */
@Data
public class QyWebChatDeptModel {
    /**
     * 部门ID
     */
    private Integer id;
    /**
     * 部门中文名称
     */
    private String name;
    /**
     * 部门英文名称
     */
    private String name_en;
    /**
     * 部门的上级部门
     */
    private Integer parentid;
    /**
     * 部门排序
     */
    private Integer order;
}
