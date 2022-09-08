package jnpf.onlinedev.model;

import lombok.Data;

/**
 * 批量删除id集合
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/6/17
 */
@Data
public class BatchRemoveIdsVo {
		private String[] ids;
}
