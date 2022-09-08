package jnpf.engine.model.flowengine.shuntjson.childnode;

import lombok.Data;

import java.util.List;

/**
 * 解析引擎
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
public class Properties {

    /**
     * condition属性
     **/
    private Boolean isDefault;
    private String priority;
    private List<ProperCond> conditions;

    /**
     * approver属性
     **/
    private String title;
    /**
     * 发起人
     **/
    private List<String> initiator;
    /**
     * 发起岗位
     **/
    private List<String> initiatePos;
    /**
     * 发起角色
     **/
    private List<String> initiateRole;
    /**
     * 批准人
     **/
    private List<String> approvers;
    /**
     * 批准岗位
     **/
    private List<String> approverPos;
    /**
     * 批准角色
     **/
    private List<String> approverRole;
    /**
     * 经办对象
     **/
    private String assigneeType;
    /**
     * 字段
     **/
    private List<FormOperates> formOperates;
    /**
     * 传阅岗位
     **/
    private List<String> circulatePosition;
    /**
     * 传阅人
     **/
    private List<String> circulateUser;
    /**
     * 传阅角色
     **/
    private List<String> circulateRole;
    /**
     * 流程进度
     **/
    private String progress;
    /**
     * 驳回步骤 1.上一步骤 0.返回开始
     **/
    private String rejectStep;
    /**
     * 备注
     **/
    private String description;
    /**节点事件**/
    /**
     * 是否开启节点事件
     **/
    private Boolean hasApproverfunc;
    /**
     * 节点的url
     **/
    private String approverInterfaceUrl;
    /**
     * 节点url的类型(POST、GET)
     **/
    private String approverInterfaceType;
    /**开始事件**/
    /**
     * 是否开启开始事件
     **/
    private Boolean hasInitfunc;
    /**
     * 开始的url
     **/
    private String initInterfaceUrl;
    /**
     * 开始url的类型(POST、GET)
     **/
    private String initInterfaceType;
    /**结束事件**/
    /**
     * 是否开启结束事件
     **/
    private Boolean hasEndfunc;
    /**
     * 结束的url
     **/
    private String endInterfaceUrl;
    /**
     * 结束url的类型(POST、GET)
     **/
    private String endInterfaceType;
    /**
     * 节点撤回事件
     **/
    private Boolean hasRecallFunc;
    private String recallInterfaceUrl;
    /**
     * 发起撤回事件
     **/
    private Boolean hasFlowRecallFunc;
    private String flowRecallInterfaceUrl;

    /**定时器**/
    /**
     * 天
     **/
    private Integer day = 0;
    /**
     * 时
     **/
    private Integer hour = 0;
    /**
     * 分
     **/
    private Integer minute = 0;
    /**
     * 秒
     **/
    private Integer second = 0;

    /**新加属性**/
    /**
     * 指定人审批(0 或签 1 会签)
     **/
    private Integer counterSign;
    /**
     * 自定义抄送人
     **/
    private Boolean isCustomCopy;
    /**
     * 发起人的第几级主管
     **/
    private Long managerLevel;
    /**
     * 表单字段
     **/
    private String formField;
    /**
     * 审批节点
     **/
    private String nodeId;
    /**
     * 会签比例
     **/
    private Long countersignRatio;
    /**
     * 请求路径
     **/
    private String getUserUrl;
    /**
     * 审批人为空时是否自动通过
     **/
    private Boolean noApproverHandler;
    /**
     * 前台按钮权限
     **/
    private Boolean hasAuditBtn;
    /**
     * 前台通过
     **/
    private String auditBtnText;
    /**
     * 前台按钮权限
     **/
    private Boolean hasRejectBtn;
    /**
     * 前台拒绝
     **/
    private String rejectBtnText;
    /**
     * 前台按钮权限
     **/
    private Boolean hasRevokeBtn;
    /**
     * 前台撤回
     **/
    private String revokeBtnText;
    /**
     * 前台按钮权限
     **/
    private Boolean hasTransferBtn;
    /**
     * 前台转办
     **/
    private String transferBtnText;
    /**
     * 是否有签名
     **/
    private Boolean hasSign;
    /**
     * 通知设置 1-站内信 2-邮箱 3-短信 4-钉钉 5-企业微信
     **/
    private List<String> messageType;
    /**
     * 超时设置
     **/
    private TimeOutConfig timeoutConfig;
    /**
     * 是否加签
     **/
    private Boolean hasFreeApprover;

    /**子流程属性**/
    /**
     * 审批类型 1-指定成员 2-部门主管 3-发起者主管 4-发起人
     */
    private String initiateType;
    /**
     * 子流程引擎id
     */
    private String flowId;
    /**
     * 子流程赋值
     */
    private List<FlowAssignModel> assignList;
}
