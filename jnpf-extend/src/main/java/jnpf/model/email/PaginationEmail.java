package jnpf.model.email;

import jnpf.base.PaginationTime;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:06
 */
@Data
public class PaginationEmail extends PaginationTime {
    private String type;
}
