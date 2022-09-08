package jnpf.permission.model.position;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class PositionListVO {
    private String id;
    private String fullName;
    private String enCode;
    private String type;
    private Long creatorTime;
    private String description;
    @JSONField(name = "organizeId")
    private String department;
    private Integer enabledMark;
    @ApiModelProperty(value = "排序")
    private Long sortCode;
}
