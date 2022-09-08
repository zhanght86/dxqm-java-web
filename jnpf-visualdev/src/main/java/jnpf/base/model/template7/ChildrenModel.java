package jnpf.base.model.template7;


import jnpf.model.visiual.fields.FieLdsModel;
import lombok.Data;

import java.util.List;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Data
public class ChildrenModel {

    /**
     * 子表的属性
     */
    private List<FieLdsModel> childrenList;
    /**
     * 子表名称
     */
    private String className;
    /**
     * json原始名称
     */
    private String tableModel;
    /**
     * 子表系统控件
     */
    private List<KeyModel> systemList;
}
