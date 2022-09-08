package jnpf.database.model;


import jnpf.base.Pagination;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DbTableDataForm extends Pagination {
     private String field;
}
