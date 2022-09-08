package jnpf.portal.model;

import lombok.Data;

import java.util.Date;

/**
 * 门户导入导出
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/15
 */
@Data
public class PortalExportDataVo {

	private String id;

	private String description;

	private Long sortCode;

	private Integer enabledMark;

	private Date creatorTime;

	private String creatorUser;

	private Date lastModifyTime;

	private String lastModifyUser;

	private Integer deleteMark;

	private Date deleteTime;

	private String deleteUserId;

	private String fullName;

	private String enCode;

	private String category;

	private String formData;

	private String modelType;
}
