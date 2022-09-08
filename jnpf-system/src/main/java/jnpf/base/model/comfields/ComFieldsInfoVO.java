package jnpf.base.model.comfields;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class ComFieldsInfoVO {
    private String id;
    private String fieldName;
    private String dataType;
    @NotBlank(message = "必填")
    private String field;
    private String dataLength;
    private Integer allowNull;
    private Long creatorTime;
}
