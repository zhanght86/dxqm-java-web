package jnpf.base.vo;

import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:59
 */
@Data
public class TreeViewVO<T> {
    private List<T> treeList;
}
