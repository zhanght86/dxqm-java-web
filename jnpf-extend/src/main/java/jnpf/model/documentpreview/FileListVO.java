package jnpf.model.documentpreview;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:06
 */
@Data
public class FileListVO {
    @ApiModelProperty(value = "主键id")
    private String fileId;
    @ApiModelProperty(value = "文件名称")
    private String fileName;
    @ApiModelProperty(value = "文件大小")
    private String fileSize;
    @ApiModelProperty(value = "修改时间")
    private String fileTime;
    @ApiModelProperty(value = "文件类型")
    private String fileType;
}
