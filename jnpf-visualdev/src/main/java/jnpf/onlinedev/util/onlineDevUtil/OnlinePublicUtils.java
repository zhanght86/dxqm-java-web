package jnpf.onlinedev.util.onlineDevUtil;

import jnpf.model.visiual.JnpfKeyConsts;
import jnpf.model.visiual.OnlineDevData;
import jnpf.onlinedev.model.OnlineDevListModel.OnlineFieldsModel;
import jnpf.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线开发公用
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/28
 */
public class OnlinePublicUtils {
	/**
	 * 判断有表无表
	 * @return
	 */
	public static Boolean isUseTables(String tableJson){
			if (!StringUtil.isEmpty(tableJson) && !OnlineDevData.TABLE_CONST.equals(tableJson)) {
				return true;
			}
				return false;
		}

	/**
	 * map key转小写
	 * @param requestMap
	 * @return
	 */
	public static Map<String, Object> mapKeyToLower(Map<String, Object> requestMap) {
		// 非空校验
		if (requestMap.isEmpty()) {
			return null;
		}
		// 初始化放转换后数据的Map
		Map<String, Object> responseMap = new HashMap<>(16);
		// 使用迭代器进行循环遍历
		Set<String> requestSet = requestMap.keySet();
		Iterator<String> iterator = requestSet.iterator();
		iterator.forEachRemaining(obj -> {
			// 判断Key对应的Value是否为Map
			if ((requestMap.get(obj) instanceof Map)) {
				// 递归调用，将value中的Map的key转小写
				responseMap.put(obj.toLowerCase(), mapKeyToLower((Map) requestMap.get(obj)));
			} else {
				// 直接将key小写放入responseMap
				responseMap.put(obj.toLowerCase(), requestMap.get(obj));
			}
		});

		return responseMap;
	}


	/**
	 * 获取map中第一个数据值
	 *
	 * @param map 数据源
	 * @return
	 */
	public static Object getFirstOrNull(Map<String, Object> map) {
		Object obj = null;
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			obj = entry.getValue();
			if (obj != null) {
				break;
			}
		}
		return  obj;
	}

	/**
	 * 去除无用的控件
	 * @param fieldsModelList
	 * @return
	 */
	public static void removeUseless(List<OnlineFieldsModel> fieldsModelList) {
		for (int i = 0 ; i<fieldsModelList.size();i++){
			if (fieldsModelList.get(i).getConfig().getJnpfKey()==null){
				continue;
			}
			if (fieldsModelList.get(i).getConfig().getJnpfKey().equals(JnpfKeyConsts.CHILD_TABLE)){
				fieldsModelList.remove(i);
			}
		}
	}


	/**
	 * 递归控件
	 * @param fieldsModelList
	 * @return
	 */
	public static void recursionFields(List<OnlineFieldsModel> fieldsModelList,List<OnlineFieldsModel> allFieldsModelList){
		//去除子表控件
		removeUseless(fieldsModelList);
		for (OnlineFieldsModel model:fieldsModelList){
			if (model.getConfig().getChildren()!=null){
				recursionFields(model.getConfig().getChildren(),allFieldsModelList);
			}
			allFieldsModelList.add(model);
		}
	}
}

