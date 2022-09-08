package jnpf.engine.model;

import lombok.Data;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:17
 */
@Data
public class FlowHandleModel {
    /**意见**/
    private String handleOpinion;
    /**指定人**/
    private String freeApproverUserId;
    /**表单数据**/
    private Object formData;
    /**编码**/
    private String enCode;
    /**自定义抄送人**/
    private String copyIds;
    /**签名**/
    private String signImg;
    /**指派节点**/
    private String nodeCode;

}
