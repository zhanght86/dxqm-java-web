package jnpf.base.model.module;

import jnpf.base.entity.ModuleButtonEntity;
import jnpf.base.entity.ModuleColumnEntity;
import jnpf.base.entity.ModuleDataAuthorizeEntity;
import jnpf.base.entity.ModuleDataAuthorizeSchemeEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 系统菜单导出模型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-17
 */
@Data
public class ModuleExportModel {
    private String id;
    private String parentId="0";
    private Integer type;
    private String fullName;
    private String enCode;
    private String urlAddress;
    private Integer isButtonAuthorize;
    private Integer isColumnAuthorize;
    private Integer isDataAuthorize;
    private String propertyJson;
    private String description;
    private Long sortCode;
    private Integer enabledMark=0;
    private Date creatorTime;
    private String creatorUserId;
    private Date lastModifyTime;
    private String lastModifyUserId;
    private Integer deleteMark;
    private Date deleteTime;
    private String deleteUserId;
    private String icon;
    private String linkTarget;
    private String category;

    private List<ModuleButtonEntity> buttonEntityList;
    private List<ModuleColumnEntity> columnEntityList;
    private List<ModuleDataAuthorizeSchemeEntity> schemeEntityList;
    private List<ModuleDataAuthorizeEntity> authorizeEntityList;
}
