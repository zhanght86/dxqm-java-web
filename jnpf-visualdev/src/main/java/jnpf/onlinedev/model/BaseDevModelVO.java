package jnpf.onlinedev.model;

import lombok.Data;

import java.util.List;

/**
 * 功能设计导入导出模型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/15
 */
@Data
public class BaseDevModelVO {

	private String id;

	private String description;

	private String sortCode;

	private String enabledMark;

	private String creatorTime;

	private String creatorUser;

	private String lastModifyTime;

	private String lastModifyUser;

	private String deleteMark;

	private String deleteTime;

	private String deleteUserId;

	private String fullName;

	private String enCode;

	private String state;

	private String type;

	private String tables;

	private String category;

	private String formData;

	private String columnData;

	private String dbLinkId;

	private String webType;

	private String flowTemplateJson;

	private String modelType;
}
