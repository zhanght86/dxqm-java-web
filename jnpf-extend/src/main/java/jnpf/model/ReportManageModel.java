package jnpf.model;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:21
 */
@Data
public class ReportManageModel {
    @JSONField(name = "F_Id")
    private String id;
    @JSONField(name = "F_FullName")
    private String fullName;
    @JSONField(name = "F_Category")
    private String category;
    @JSONField(name = "F_UrlAddress")
    private String urlAddress;
}
