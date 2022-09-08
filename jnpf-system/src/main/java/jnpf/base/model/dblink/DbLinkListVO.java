package jnpf.base.model.dblink;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DbLinkListVO {
    @ApiModelProperty(value = "连接名称")
    private String fullName;
    @ApiModelProperty(value = "连接驱动")
    private String dbType;
    @ApiModelProperty(value = "主机名称")
    private String host;
    @ApiModelProperty(value = "端口")
    private String port;
    @ApiModelProperty(value = "创建时间",example = "1")
    private Long creatorTime;
    @ApiModelProperty(value = "创建人")
    @JSONField(name = "creatorUserId")
    private String creatorUser;
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "修改时间")
    private Long lastModifyTime;
    @ApiModelProperty(value = "修改用户")
    @JSONField(name = "lastModifyUserId")
    private String lastModifyUser;
    @ApiModelProperty(value = "有效标志")
    private Integer enabledMark;
    @ApiModelProperty(value = "排序码")
    private Long sortCode;
    @ApiModelProperty(value = "数量")
    private Long num;
    @ApiModelProperty(value = "子节点")
    private List<DbLinkListVO> children;

}
