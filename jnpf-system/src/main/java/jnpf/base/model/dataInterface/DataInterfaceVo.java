package jnpf.base.model.dataInterface;

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
public class DataInterfaceVo {
    @ApiModelProperty(value = "主键id")
    private String id;
    @ApiModelProperty(value = "接口名称")
    private String fullName;
    @ApiModelProperty(value = "数据源id")
    private String dbLinkId;
    @ApiModelProperty(value = "分类id")
    private String categoryId;
    @ApiModelProperty(value = "数据类型")
    private Integer dataType;
    @ApiModelProperty(value = "请求方式")
    private String requestMethod;
    @ApiModelProperty(value = "返回类型")
    private String responseType;
    @ApiModelProperty(value = "排序")
    private Long sortCode;
    @ApiModelProperty(value = "状态(0-默认，禁用，1-启用)")
    private Integer enabledMark;
    @ApiModelProperty(value = "说明备注")
    private String description;
    @ApiModelProperty(value = "查询语句")
    private String query;
    @ApiModelProperty(value = "编码")
    private String enCode;
    @ApiModelProperty(value = "接口路径")
    private String path;
    @ApiModelProperty(value = "请求参数JSON")
    private String requestParameters;
    @ApiModelProperty(value = "请求头参数")
    private String requestHeaders;
    @ApiModelProperty(value = "验证类型")
    private Integer checkType;
}
