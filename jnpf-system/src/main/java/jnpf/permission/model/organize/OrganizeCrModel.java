package jnpf.permission.model.organize;

import io.swagger.annotations.ApiModelProperty;
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
public class OrganizeCrModel {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "公司简称")
    private String shortName;
    private String webSite;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "所属行业")
    private String industry;
    private String foundedTime;
    private String address;
    private String managerName;
    private String managerTelePhone;
    private String managerMobilePhone;
    private String manageEmail;
    private String bankName;
    private String bankAccount;
    private String businessscope;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "公司性质")
    private String enterpriseNature;
    private String fax;
    private String telePhone;

}
