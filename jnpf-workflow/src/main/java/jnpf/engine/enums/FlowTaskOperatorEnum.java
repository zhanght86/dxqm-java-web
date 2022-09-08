package jnpf.engine.enums;

/**
 * 经办对象
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public enum FlowTaskOperatorEnum {

    //发起者主管
    LaunchCharge("1","发起者主管"),
    //部门经理
    DepartmentCharge("2","部门经理"),
    //发起者本人
    InitiatorMe("3","发起者本人"),
    //变量
    Variate("4","变量"),
    //环节
    Tache("5","环节"),
    //指定人
    Nominator("6","指定人"),
    //加签
    FreeApprover("7","加签"),
    //会签
    FixedJointlyApprover("8","会签"),
    //服务
    Serve("9","服务"),
    //子节点指定成员
    ChildFixedJointlyApprover("1","子节点指定成员"),
    //子节点部门主管
    ChildDepartmentCharge("2","子节点部门主管"),
    //子节点发起者主管
    ChildLaunchCharge("3","子节点发起者主管"),
    //子节点发起者本人
    ChildInitiatorMe("4","子节点发起者本人");


    private String code;
    private String message;

    FlowTaskOperatorEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 根据状态code获取枚举名称
     *
     * @return
     */
    public static String getMessageByCode(String code) {
        for (FlowTaskOperatorEnum status : FlowTaskOperatorEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.message;
            }
        }
        return null;
    }

    /**
     * 根据状态code获取枚举值
     *
     * @return
     */
    public static FlowTaskOperatorEnum getByCode(String code) {
        for (FlowTaskOperatorEnum status : FlowTaskOperatorEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
