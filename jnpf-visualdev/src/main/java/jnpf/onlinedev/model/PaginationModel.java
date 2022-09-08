package jnpf.onlinedev.model;

import jnpf.base.Pagination;
import lombok.Data;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Data
public class PaginationModel extends Pagination {
    private String json;
}
