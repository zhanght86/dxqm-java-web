package jnpf.message.model.message;

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
public class MessageNoticeVO {
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "发布人员")
    private String creatorUser;

    @ApiModelProperty(value = "发布时间",example = "1")
    private Long  lastModifyTime;

    @ApiModelProperty(value = "状态(0-存草稿，1-已发布)",example = "1")
    private Integer enabledMark;
}
