package jnpf.generater.model;


import lombok.Data;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/23
 */
@Data
public class SearchTypeModel {
	private String vModel;
	private String dataType;
	private Integer searchType;
	private String label;
	private String jnpfKey;
	private String format;
}
