package jnpf.model.document;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:58
 */
@Data
public class DocumentListVO {
    @ApiModelProperty(value = "主键id")
    private String id;
    @ApiModelProperty(value = "文件夹名称")
    private String fullName;
    @ApiModelProperty(value = "文档分类(0-文件夹，1-文件)")
    private Integer type;
    @ApiModelProperty(value = "创建日期")
    private Long creatorTime;
    @ApiModelProperty(value = "是否分享")
    private Integer isShare;
    @ApiModelProperty(value = "大小")
    private String fileSize;
    @ApiModelProperty(value = "父级Id")
    private String parentId;
    @ApiModelProperty(value = "后缀名")
    private String fileExtension;
}
