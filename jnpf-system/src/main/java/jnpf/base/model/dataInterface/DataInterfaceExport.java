package jnpf.base.model.dataInterface;

import lombok.Data;

import java.util.Date;

/**
 * 数据接口导出模型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-04
 */
@Data
public class DataInterfaceExport {

    private String id;

    private String categoryId;

    private String fullName;

    private String dbLinkId;

    private Integer dataType;

    private String path;

    private String requestMethod;

    private String responseType;

    private String query;

    private Integer checkType;

    private String requestHeaders;

    private String requestParameters;

    private String responseParameters;

    private String enCode;

    private Long sortCode;

    private Integer enabledMark;

    private String description;

    private Date creatorTime;

    private String creatorUser;

    private Date lastModifyTime;

    private String lastModifyUser;

    private Integer deleteMark;

    private Date deleteTime;

    private String deleteUserId;
}
