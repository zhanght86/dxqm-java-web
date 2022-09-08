package jnpf.base.model.template7;


import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.util.treeutil.SumTree;
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
public class HtmlModel extends SumTree {

    /**
     * 类型 栅格row,卡片card,子表table,主表mast
     */
    private String jnpfkey;
    /**
     * json原始名称
     */
    private String vmodel;
    /**
     * 主表属性
     */
    private FieLdsModel fieLdsModel;
    /**
     * 子表list属性
     */
    private List<FieLdsModel> tablFieLdsModel;
    /**
     * 控件宽度
     */
    private String span;
    /**
     * 结束
     */
    private String end="0";

}
