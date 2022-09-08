package jnpf.onlinedev.model.OnlineDevListModel;

import jnpf.model.visiual.fields.config.ConfigModel;
import jnpf.model.visiual.fields.options.ColumnOptionModel;
import lombok.Data;

import java.util.List;


/**
 *在线开发formData
 *
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/8/2
 */
@Data
public class OnlineFieldsModel {
	private OnlineConfigModel config;
	private String vModel;
	private Boolean multiple;
	private String placeholder;
	private SlotModel slot;
	private String options;
	private PropsFatherModel props;
	private String relationField;
	private List<ColumnOptionModel> columnOptions;
	private String modelId;
	private String interfaceId;
	private String propsValue;
	private String format;
	private String type;
}
