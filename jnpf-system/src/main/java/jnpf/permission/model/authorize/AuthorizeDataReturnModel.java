package jnpf.permission.model.authorize;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:26
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizeDataReturnModel {
    private String id;
    private String fullName;
    private String icon;
    private String type;
    private Long sortCode;
    private List<AuthorizeDataReturnModel> children;
}
