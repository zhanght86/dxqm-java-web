package jnpf.base;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:54
 */
@Data
public class PaginationTime extends Pagination{
    private String startTime;
    private String endTime;
//    private String type;
}
