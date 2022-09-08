package jnpf.model.productclassify;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * 产品分类
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 14:34:04
 */
@Data
public class ProductclassifyModel extends SumTree {

    /** 名称 */
    private String fullName;

}