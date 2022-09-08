package jnpf.engine.model.flowengine;


import lombok.Data;

import java.util.Map;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:17
 */
@Data
public class FlowModel {
    /**判断新增**/
    private String id;
    /**引擎id**/
    private String flowId;
    /**流程主键**/
    private String processId;
    /**流程标题**/
    private String flowTitle;
    /**紧急程度**/
    private Integer flowUrgent;
    /**流水号**/
    private String billNo;
    /**提交表单对象**/
    private Object formEntity;
    /**审核表单数据**/
    private Map<String,Object> formData;
    /**加签人**/
    private String freeApproverUserId;
    /**0.提交 1.保存 **/
    private String status;
    /**意见**/
    private String handleOpinion;
    /**签名**/
    private String signImg;
    /**加签人**/
    private String copyIds;
    /**子流程**/
    private String parentId;
    /**创建人**/
    private String userId;
}
