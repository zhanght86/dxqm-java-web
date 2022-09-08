package jnpf.base.model.dbbackup;

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
public class DbBackupListVO {

    @ApiModelProperty(value = "备份主键")
    private String id;

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "文件大小")
    private String fileSize;

    @ApiModelProperty(value = "创建时间",example = "1")
    private Long creatorTime;

    @ApiModelProperty(value = "文件访问地址")
    private String fileUrl;

}
