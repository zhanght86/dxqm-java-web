package jnpf.model;

import lombok.Data;

/**
 * 解析引擎
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:19
 */
@Data
public class FormModel {

    //卡片
    private String shadow;
    private String header;

    //栅格
    private Integer span;

    //折叠
    private String title;
    private String name;
    private String model;
    private Boolean accordion;

    //标签页
    private String tabPosition;
    private String type;

    //折叠、标签公用
    private String active;
    /**判断折叠、标签是否最外层 0.不是 1.是**/
    private String outermost;
}
