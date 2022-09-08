package jnpf.onlinedev.util.onlineDevUtil;

import jnpf.base.model.template6.ColumnListField;
import jnpf.base.util.VisualUtils;
import jnpf.database.exception.DataException;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.util.JdbcUtil;
import jnpf.model.visiual.TableModel;
import jnpf.onlinedev.model.OnlineDevListModel.OnlineDevListDataVO;
import jnpf.onlinedev.model.OnlineDevListModel.VisualColumnSearchVO;
import lombok.Cleanup;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 *
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/28
 */
public class OnlineDatabaseUtils {
	public static  List<OnlineDevListDataVO> getTableDataList(Connection conn, String sql, String pKeyName) throws SQLException {
		List<OnlineDevListDataVO> list = new ArrayList<>();
		ResultSet rs = JdbcUtil.query(conn, sql);
		List<Map<String, Object>> dataList = JdbcUtil.convertList(rs);
		for (Map<String, Object> dataMap : dataList) {
			OnlineDevListDataVO dataVo = new OnlineDevListDataVO();
			dataMap = toLowerKey(dataMap);
			dataVo.setData(dataMap);
			if (dataMap.containsKey(pKeyName.toUpperCase())) {
				dataVo.setId(String.valueOf(dataMap.get(pKeyName.toUpperCase())));
			}
			list.add(dataVo);
		}
		return list;
	}

	public static Map<String, Object> toLowerKey(Map<String, Object> map) {
		Map<String, Object> resultMap = new HashMap<>(16);
		Set<String> sets = map.keySet();
		for (String key : sets) {
			resultMap.put(key.toLowerCase(), map.get(key));
		}
		return resultMap;
	}

	public static Boolean existKey(String feilds, String pKeyName) {
		String[] strs = feilds.split(",");
		if (strs.length > 0) {
			for (String feild : strs) {
				if (feild.equals(pKeyName)) {
					return true;
				}
			}
		}
		return false;
	}

	public static List<OnlineDevListDataVO> setDataId(String keyName, List<OnlineDevListDataVO> DevList) {
		keyName = keyName.toLowerCase();
		for (OnlineDevListDataVO dataVo : DevList) {
			Map<String, Object> dataMap = dataVo.getData();
			if (dataMap.get(keyName) != null) {
				dataVo.setId(String.valueOf(dataMap.get(keyName)));
			}
		}
		return DevList;
	}

	@SneakyThrows
	public static List<OnlineDevListDataVO> getDataInTable(TableModel tableModel, List<OnlineDevListDataVO> dataVOList, DbLinkEntity linkEntity, List<ColumnListField> modelList, List<VisualColumnSearchVO> searchVOList)  {
			@Cleanup Connection connection = VisualUtils.getDataConn(linkEntity);

			String mainTable = tableModel.getTable();
			String primaryKey=VisualUtils.getpKey(connection,mainTable);
			//列表字段
			List<String> columnDataFields = modelList.stream().map(field-> {
					return field.getProp();
			}).collect(Collectors.toList());
			String columnDataField= String.join(",",columnDataFields);
		String sql;
		if (searchVOList!=null){
			//条件字段
			StringBuilder searchField = new StringBuilder();
			for (VisualColumnSearchVO searchVO : searchVOList){
				String jnpfKey = searchVO.getConfig().getJnpfKey();
				Boolean isMultiple = searchVO.getMultiple();
				if (searchVO.getSearchType().equals("1")){
					searchField.append(searchVO.getVModel()+"=? and");
				}
				if (searchVO.getSearchType().equals("2")){
					if (isMultiple){

					}
					searchField.append(searchVO.getVModel()+"like '%?'% and");
				}
				if (searchVO.getSearchType().equals("3")){

				}
			}

			 sql = "Select "+columnDataField+" from "+mainTable+" where " + searchField;
		}else {
			sql = "Select "+columnDataField+" from "+mainTable+" where 1=1 ";
		}

		PreparedStatement preparedStatement =connection.prepareStatement(sql);
		int i =1;
		for (VisualColumnSearchVO searchVO : searchVOList){
			preparedStatement.setObject(i,searchVO.getValue());
		}
		return null;
	}
	/**
	 * 转换时间格式
	 * @param time
	 * @return
	 */
	public static String getTimeFormat(String time){
		String result;
		switch (time.length()){
			case 16:
				result=time+":00";
				break;
			case 19:
				result=time;
				break;
			case 21:
				result=time.substring(0,time.length()-2);
				break;
			case 10:
				result=time+" 00:00:00";
				break;
			case 8:
				result="2000-01-01 "+time;
				break;
			default:
				result="";
				break;
		}
		return result;
	}

	public static String getLastTimeFormat(String time){
		String result;
		switch (time.length()){
			case 16:
				result=time+":00";
				break;
			case 19:
				result=time;
				break;
			case 10:
				result=time+" 23:59:59";
				break;
			case 8:
				result="2000-01-01 "+time;
				break;
			default:
				result="";
				break;
		}
		return result;
	}

}
