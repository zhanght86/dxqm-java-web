package jnpf.onlinedev.model.OnlineDevListModel;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 *
 * slot
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/8/2
 */
@Data
public class SlotModel {
	private List<Map<String,Object>> options;
}
