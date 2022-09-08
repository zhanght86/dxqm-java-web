package jnpf.base;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class GridModel {
    /**
     *  排序列
     */
    private String sidx;
    /**
     *  排序类型
     */
    private String sord;
    /**
     *  查询条件
     */
    private String queryJson;
    /**
     * 查询关键字
     */
    private String keyword;
}
