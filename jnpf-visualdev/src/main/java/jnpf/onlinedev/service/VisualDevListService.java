package jnpf.onlinedev.service;

import jnpf.base.entity.VisualdevEntity;
import jnpf.onlinedev.model.OnlineDevListModel.OnlineDevListDataVO;
import jnpf.onlinedev.model.PaginationModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *列表临时接口
 *
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/28
 */
@Service
public interface VisualDevListService {
	/**
	 * 获取列表
	 * @param visualdevEntity
	 * @param paginationModel
	 * @return
	 */
	List<Map<String,Object>>  getRealList(VisualdevEntity visualdevEntity, PaginationModel paginationModel);

	/**
	 * 无表查询
	 * @param modelId
	 * @return
	 */
	 List<OnlineDevListDataVO> getList(String modelId);
}
