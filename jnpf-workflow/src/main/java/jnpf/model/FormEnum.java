package jnpf.model;

/**
 * 引擎模板
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月29日 上午9:18
 */
public enum FormEnum {
    //栅格
    row("row"),
    //折叠
    collapse("collapse"),
    //标签
    tab("tab"),
    //子表
    table("table"),
    //主表
    mast("mast"),
    //卡片
    card("card");

    private String message;

    FormEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
