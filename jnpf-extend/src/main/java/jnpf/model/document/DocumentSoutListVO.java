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
public class DocumentSoutListVO {
    @ApiModelProperty(value = "主键id")
    private String id;
    @ApiModelProperty(value = "文件夹名称")
    private String fullName;
    @ApiModelProperty(value = "文档分类(0-文件夹，1-文件)")
    private Integer type;
    @ApiModelProperty(value = "共享日期")
    private String shareTime;
    @ApiModelProperty(value = "大小")
    private String fileSize;

}
