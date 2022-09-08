package jnpf.base.enums;


/**
 * 操作类型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public enum OperationType {

    /**
     * 其他
     */
    Exception(0,"其他"),

    /**
     * 新增
     */
    Insert(1,"新增"),

    /**
     * 删除f
     */
    delete(2,"删除"),

    /**
     * 编辑
     */
    update(3,"编辑");

    private int code;
    private String message;

    OperationType(int code, String message){
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
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
     * @return
     */
    public static String getMessageByCode(Integer code) {
        for (OperationType status : OperationType.values()) {
            if (status.getCode().equals(code)) {
                return status.message;
            }
        }
        return null;
    }

    /**
     * 根据状态code获取枚举值
     * @return
     */
    public static OperationType getByCode(Integer code) {
        for (OperationType status : OperationType.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}
