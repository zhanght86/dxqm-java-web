package jnpf.onlinedev.model.OnlineDevListModel;

import jnpf.model.visiual.fields.config.ConfigModel;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/12
 */
@Data
public class VisualColumnSearchVO {
	/**
	 * 查询条件类型 1.等于 2.模糊 3.范围
	 */
	private String searchType;
	private String vModel;
	/**
	 * 查询值
	 */
	private Object value;
	/**
	 * 是否多选
	 */
	private Boolean multiple;
	private ConfigModel config;
	/**
	 * 时间类型格式
	 */
	private String format;
}
