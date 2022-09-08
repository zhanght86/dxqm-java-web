package jnpf.onlinedev.util.onlineDevUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.ProvinceEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.template6.ColumnListField;
import jnpf.base.service.*;
import jnpf.base.util.VisualUtils;
import jnpf.database.exception.DataException;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.util.JdbcUtil;
import jnpf.model.visiual.ColumnDataModel;
import jnpf.model.visiual.FormDataModel;
import jnpf.model.visiual.JnpfKeyConsts;
import jnpf.model.visiual.TableModel;
import jnpf.onlinedev.model.OnlineDevEnum.CacheKeyEnum;
import jnpf.onlinedev.model.OnlineDevEnum.OnlineDataTypeEnum;
import jnpf.onlinedev.model.OnlineDevListModel.OnlineFieldsModel;
import jnpf.onlinedev.model.OnlineDevListModel.VisualColumnSearchVO;
import jnpf.onlinedev.model.OnlineDevListModel.OnlineDevListDataVO;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserService;
import jnpf.util.*;
import jnpf.util.context.SpringContext;
import lombok.Cleanup;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/28
 */
public class OnlineDevListUtils {
	private static RedisUtil redisUtil;
	private static DictionaryDataService dictionaryDataService;
	private static UserService userService;
	private static PositionService positionService;
	private static ProvinceService provinceService;
	private static OrganizeService organizeService;
	private static VisualdevService visualdevService;
	private static VisualdevModelDataService visualdevModelDataService;
	private static  DataInterfaceService dataInterfaceService ;
	private static DblinkService dblinkService;

	public static void init() {
		dictionaryDataService = SpringContext.getBean(DictionaryDataService.class);
		userService = SpringContext.getBean(UserService.class);
		positionService = SpringContext.getBean(PositionService.class);
		redisUtil = SpringContext.getBean(RedisUtil.class);
		provinceService = SpringContext.getBean(ProvinceService.class);
		organizeService=SpringContext.getBean(OrganizeService.class);
		visualdevService=SpringContext.getBean(VisualdevService.class);
		dataInterfaceService = SpringContext.getBean(DataInterfaceService.class);
		dblinkService=SpringContext.getBean(DblinkService.class);
	}


	/**
	 * 有表数据查询
	 * @param list
	 * @param mainTable
	 * @param modelList
	 * @param columnData
	 * @param linkEntity
	 * @return
	 * @throws DataException
	 * @throws SQLException
	 */
	public static List<OnlineDevListDataVO> getHasTableList(List<OnlineDevListDataVO> list, String mainTable, List<ColumnListField> modelList, ColumnDataModel columnData, DbLinkEntity linkEntity) throws DataException, SQLException {
		@Cleanup Connection conn = VisualUtils.getDataConn(linkEntity);
		//获取主键
		String pKeyName = VisualUtils.getpKey(conn, mainTable);

		StringBuilder feilds = new StringBuilder();
		for (ColumnListField model : modelList) {
			feilds.append(model.getProp() + ",");
		}
		if (modelList.size() > 0) {
			feilds.deleteCharAt(feilds.length() - 1);
		}

		String feildsBool = feilds.toString().toLowerCase().trim();
		//判断字段是否存在主键
		Boolean keyFlag = OnlineDatabaseUtils.existKey(feildsBool, pKeyName);

		//获取查询语句
		String listResultSql = VisualUtils.getListResultSql(keyFlag, feilds.toString(), mainTable, pKeyName, columnData);
		//获取有表列表数据
		list = OnlineDatabaseUtils.getTableDataList(conn, listResultSql, pKeyName);
		//Id赋值
		list = OnlineDatabaseUtils.setDataId(pKeyName, list);
		return list;
	}

	/**
	 * 查询条件
	 * @param list
	 * @param searchList
	 * @return
	 */
	public static List<OnlineDevListDataVO> getNoSwapList(List<OnlineDevListDataVO> list, List<VisualColumnSearchVO> searchList){
		  List<OnlineDevListDataVO>  resultList = new ArrayList<>();
		  if (searchList==null){
		  	return list;
			}
		for (OnlineDevListDataVO dataVo:list){
			int i =0;
			for (VisualColumnSearchVO vo : searchList){
					Object dataModel = dataVo.getData().get(vo.getVModel());
					if (dataModel==null){
						continue;
					}
					//多选框默认添加多选属性
					if (vo.getConfig().getJnpfKey().equals(JnpfKeyConsts.CHECKBOX)){
						vo.setMultiple(true);
					}
					if (vo.getSearchType().equals("1")){
						//多选框筛选
						if (vo.getMultiple()!=null && vo.getMultiple()==true){
							List<String> asList;
							if (String.valueOf(dataModel).contains("[")) {
								asList = JsonUtil.getJsonToList(String.valueOf(dataModel), String.class);
								System.out.println(asList);
							}else {
								String[] multipleList = String.valueOf(dataModel).split(",");
								 asList = Arrays.asList(multipleList);
							}
							String value = String.valueOf(vo.getValue());
							boolean b =false;
							if (value.contains("[")){
								List<String> valueList = JsonUtil.getJsonToList(value, String.class);
								value=valueList.get(0);
							}
							for (String s : asList){
								if (s.equals(value)){
									b=true;
								}
							}
							if (b){
								i++;
							}
						}else {
							if (String.valueOf(vo.getValue()).equals(String.valueOf(dataModel))){
								i++;
							}
						}
					}
					if (vo.getSearchType().equals("2")){
						if (String.valueOf(dataModel).contains(String.valueOf(vo.getValue()))){
							i++;
						}
					}
					if (vo.getSearchType().equals("3")){
						String key = vo.getConfig().getJnpfKey();
						switch (key){
							case JnpfKeyConsts.MODIFYTIME:
							case JnpfKeyConsts.CREATETIME:
								JSONArray timeStampArray = (JSONArray) vo.getValue();
								Long o1 =(Long) timeStampArray.get(0);
								Long o2 = (Long) timeStampArray.get(1);

									//时间戳转string格式
								String	startTime = DateUtil.daFormat(o1);
								String	endTime = DateUtil.daFormat(o2);
								//处理时间查询条件范围
								endTime=endTime.substring(0,10);
								String firstTimeDate = OnlineDatabaseUtils.getTimeFormat(startTime);
								String lastTimeDate =  OnlineDatabaseUtils.getLastTimeFormat(endTime);
								String value = OnlineDatabaseUtils.getTimeFormat(String.valueOf(dataModel));
								//只判断到日期
								SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								try {
									boolean b = DateUtil.isEffectiveDate(sdf.parse(value), sdf.parse(firstTimeDate), sdf.parse(lastTimeDate));
									if (b){
										i++;
									}
								} catch (ParseException e) {
									e.printStackTrace();
								}
								break;
							case JnpfKeyConsts.NUM_INPUT:
							case JnpfKeyConsts.CALCULATE:
								JSONArray searchArray = (JSONArray)vo.getValue();
								//数字输入查询框的两个值
								Integer firstValue = (Integer)searchArray.get(0);
								Integer secondValue = (Integer)searchArray.get(1);
								//数据
								Integer numValue = Integer.valueOf(String.valueOf(dataModel));

								//条件1,2组合的情况
								if (firstValue!=null && secondValue ==null){
									if (numValue>=firstValue){
										i++;
									}
								}
								if (firstValue!=null && secondValue!=null){
									if (numValue>=firstValue && numValue<=secondValue){
										i++;
									}
								}
								if (firstValue==null && secondValue!=null){
									if (numValue<=secondValue){
										i++;
									}
								}
								break;
							case JnpfKeyConsts.DATE:
								String starTimeDates;
								String endTimeDates;
								if (dataModel ==null){
									break;
								}
								//时间戳
								if (!String.valueOf(vo.getValue()).contains(":") && !String.valueOf(vo.getValue()).contains("-")){
									JSONArray DateTimeStampArray = (JSONArray) vo.getValue();
									Long d1 =(Long) DateTimeStampArray.get(0);
									Long d2 = (Long) DateTimeStampArray.get(1);
									long d1FirstTime = Long.parseLong(String.valueOf(d1));
									long d2LastTime = Long.parseLong(String.valueOf(d2));

									//时间戳转string格式
									starTimeDates = DateUtil.daFormat(d1FirstTime);
									endTimeDates = DateUtil.daFormat(d2LastTime);

								}else {
									//时间字符串
									String[] keyArray = String.valueOf(vo.getValue()).split(",");
									starTimeDates = keyArray[0];
									endTimeDates= keyArray[1];
								}
								if (vo.getFormat()==null){
									starTimeDates=starTimeDates.substring(0,10);
									endTimeDates=endTimeDates.substring(0,10);
								}
								starTimeDates = OnlineDatabaseUtils.getTimeFormat(starTimeDates);
								endTimeDates = OnlineDatabaseUtils.getLastTimeFormat(endTimeDates);

								String dateValue = dataModel.toString();
								if (!dateValue.contains(":") && !dateValue.contains("-")){
									//时间戳
									Long timeResult =  (Long) dataModel;
									dateValue=DateUtil.daFormat(timeResult);
								}
								dateValue=OnlineDatabaseUtils.getTimeFormat(dateValue);
								//只判断到日期
								SimpleDateFormat sdfDate =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								try {
									Boolean b = DateUtil.isEffectiveDate(sdfDate.parse(dateValue), sdfDate.parse(starTimeDates), sdfDate.parse(endTimeDates));
									if (b){
										i++;
									}
								} catch (ParseException e) {
									e.printStackTrace();
								}
								break;
							case JnpfKeyConsts.TIME:
								JSONArray timeArray =(JSONArray)vo.getValue();
								String start = String.valueOf(timeArray.get(0));
								String end =String.valueOf(timeArray.get(1));
								start=OnlineDatabaseUtils.getTimeFormat(start);
								end=OnlineDatabaseUtils.getLastTimeFormat(end);
								String timeValue = OnlineDatabaseUtils.getTimeFormat(String.valueOf(dataModel));
								SimpleDateFormat timeSim = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								try {
									boolean b = DateUtil.isEffectiveDate(timeSim.parse(timeValue), timeSim.parse(start), timeSim.parse(end));
									if (b){
										i++;
									}
								} catch (ParseException e) {
									e.printStackTrace();
								}
								break;
							default:
								break;
								}
						}
						if (i==searchList.size()){
							resultList.add(dataVo);
						}
					}
				}
					return resultList;
			}

	/**
	 * 数据转换
	 * @param list
	 * @param swapDataVoList
	 * @return
	 */
	@SneakyThrows
	public static List<OnlineDevListDataVO>	getSwapList(List<OnlineDevListDataVO> list, List<OnlineFieldsModel> swapDataVoList, String visualDevId)  {
		init();

			//从缓存中取出数据
			if (!redisUtil.exists(visualDevId)){
				sysNeedSwapData(swapDataVoList,visualDevId);
			}

		//组织
		Map<Object, Object> orgMap = redisUtil.getMap(visualDevId + CacheKeyEnum.ORG.getName());
		//岗位
		Map<Object, Object> posMap = redisUtil.getMap(visualDevId + CacheKeyEnum.POS.getName());
		//人员
		Map<Object, Object> userMap = redisUtil.getMap(visualDevId + CacheKeyEnum.USER.getName());
		//省市区
		Map<Object, Object> proMap = redisUtil.getMap(visualDevId + CacheKeyEnum.PRO.getName());


		for (OnlineFieldsModel swapDataVo: swapDataVoList){
			String jnpfKey = swapDataVo.getConfig().getJnpfKey();
			String vModel= swapDataVo.getVModel();
			String dataType =String.valueOf(swapDataVo.getConfig().getDataType());
			for (OnlineDevListDataVO listVo : list){
				Map<String, Object> dataMap = listVo.getData();
				if (StringUtil.isEmpty(String.valueOf(dataMap.get(vModel)))|| dataMap.get(vModel)==null){
					continue;
				}
				if (String.valueOf(dataMap.get(vModel)).equals("[]")||String.valueOf(dataMap.get(vModel)).equals("null")){
					dataMap.put(vModel,null);
				}else {
					switch (jnpfKey){
						//公司组件
						case JnpfKeyConsts.COMSELECT:
							//部门组件
						case JnpfKeyConsts.DEPSELECT:
							//所属部门
						case JnpfKeyConsts.CURRDEPT:
							//所属公司
						case JnpfKeyConsts.CURRORGANIZE:
							String orgData = getDataInMethod(orgMap,dataMap.get(vModel));
							dataMap.put(vModel,orgData);
							break;

						//岗位组件
						case JnpfKeyConsts.POSSELECT:
							//所属岗位
						case JnpfKeyConsts.CURRPOSITION:
							String posData = getDataInMethod(posMap,dataMap.get(vModel));
							dataMap.put(vModel,posData);
							break;

						//用户组件
						case JnpfKeyConsts.USERSELECT:
							//创建用户
						case JnpfKeyConsts.CREATEUSER:
							//修改用户
						case JnpfKeyConsts.MODIFYUSER:
							String userData = getDataInMethod(userMap,dataMap.get(vModel));
							dataMap.put(vModel,userData);
							break;

						//省市区联动
						case JnpfKeyConsts.ADDRESS:
							List<String> proDataS = JsonUtil.getJsonToList(String.valueOf(dataMap.get(vModel)), String.class);
							proDataS=	proDataS.stream().map(pro->{
								return String.valueOf(proMap.get(pro));
							}).collect(Collectors.toList());
							dataMap.put(vModel,String.join(",",proDataS));
							break;
						//开关
						case JnpfKeyConsts.SWITCH:
						String switchValue=	String.valueOf(dataMap.get(vModel)).equals("1") ? "开":"关";
						dataMap.put(vModel,switchValue);
							break;
							//级联
						case JnpfKeyConsts.CASCADER:
							String redisKey ;
							if (dataType.equals(OnlineDataTypeEnum.STATIC.getType())){
								redisKey = vModel+"_" +OnlineDataTypeEnum.STATIC.getType();
								}else if (dataType.equals(OnlineDataTypeEnum.DYNAMIC.getType())){
								redisKey = vModel+"_" +OnlineDataTypeEnum.DYNAMIC.getType();
							}else {
								redisKey = vModel+"_" +OnlineDataTypeEnum.DICTIONARY.getType();
							}
							Map<Object, Object> cascaderMap = redisUtil.getMap(redisKey);
							String value = String.valueOf(dataMap.get(vModel));
							Boolean isMultiple = swapDataVo.getProps().getProps().isMultiple();
							if (isMultiple){
								String[][] data = JsonUtil.getJsonToBean(value,String[][].class);
								StringBuilder cascaderData = new StringBuilder();
								for (String[] casData :data){
									String casDataS=String.join(",",casData);
									casDataS= String.valueOf(cascaderMap.get(casDataS)) ;
									cascaderData.append(casDataS+",");
								}
								cascaderData=cascaderData.deleteCharAt(cascaderData.length()-1);
								dataMap.put(vModel,cascaderData);
							}else {
								String s1 = getDataInMethod(cascaderMap, dataMap.get(vModel));
								dataMap.put(vModel,s1);
							}
							break;
						case JnpfKeyConsts.CHECKBOX:
							String checkBox ;
							if (dataType.equals(OnlineDataTypeEnum.STATIC.getType())){
								checkBox = vModel+"_" +OnlineDataTypeEnum.STATIC.getType();
							}else if (dataType.equals(OnlineDataTypeEnum.DYNAMIC.getType())){
								checkBox = vModel+"_" +OnlineDataTypeEnum.DYNAMIC.getType();
							}else {
								checkBox = vModel+"_" +OnlineDataTypeEnum.DICTIONARY.getType();
							}
							Map<Object, Object> checkboxMap = redisUtil.getMap(checkBox);
							String s2 = getDataInMethod(checkboxMap,dataMap.get(vModel));
							dataMap.put(vModel,s2);
							break;
						case JnpfKeyConsts.RELATIONFORM:
							//取关联表单数据
							VisualdevEntity entity = visualdevService.getInfo(swapDataVo.getModelId());
							DbLinkEntity linkEntity = dblinkService.getInfo((entity.getDbLinkId()));
							@Cleanup Connection connection = VisualUtils.getDataConn(linkEntity);
							List<TableModel> tableModels= JsonUtil.getJsonToList(entity.getTables(),TableModel.class);
							Map<String, Object> formDataMap =new HashMap<>(16);
							if (tableModels.size()>0){
								//取关联主表
								TableModel tableModel = tableModels.stream().filter(table -> table.getTypeId().equals("1")).findFirst().orElse(null);
								String relationField = swapDataVo.getRelationField();
								String relationMainTable = tableModel.getTable();
								//取关联主键
								String pkeyName =VisualUtils.getpKey(connection,relationMainTable);
								String sql = "select " + relationField + " from " +relationMainTable + " where " + pkeyName +"='"+dataMap.get(vModel)+"'";
								ResultSet query = JdbcUtil.query(connection, sql);
								formDataMap = JdbcUtil.convertMapString(query);
							}else {
									VisualdevModelDataInfoVO vo = visualdevModelDataService.infoDataChange(String.valueOf(dataMap.get(vModel)), entity);
									if (vo!=null) {
										formDataMap = JsonUtil.stringToMap(vo.getData());
									}
							}
							OnlineDevListDataVO devListDataVo = new OnlineDevListDataVO();
							formDataMap= OnlinePublicUtils.mapKeyToLower(formDataMap);
							devListDataVo.setData(formDataMap);
							List<OnlineDevListDataVO> formList =new ArrayList<>();
							formList.add(devListDataVo);

							FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
							List<OnlineFieldsModel> formDataVolist = JsonUtil.getJsonToList(formDataModel.getFields(),OnlineFieldsModel.class);
							List<OnlineDevListDataVO> formSwapList = getSwapList(formList, formDataVolist, swapDataVo.getModelId());
							OnlineDevListDataVO dataVo = formSwapList.stream().findFirst().orElse(null);
							dataMap.put(vModel,String.valueOf(OnlinePublicUtils.getFirstOrNull(dataVo.getData())));
							break;
						case JnpfKeyConsts.POPUPSELECT:
							Object data = dataInterfaceService.infoToId(swapDataVo.getInterfaceId()).getData();
							List<Map<String, Object>> mapList =(List<Map<String, Object>>) data;
							mapList.stream().filter(map->map.get(swapDataVo.getPropsValue()).equals(dataMap.get(vModel)))
													.forEach(modelMap-> dataMap.put(vModel,modelMap.get(swapDataVo.getColumnOptions().get(0).getValue())));
							break;
						case JnpfKeyConsts.MODIFYTIME:
						case JnpfKeyConsts.CREATETIME:
						case JnpfKeyConsts.DATE:
							//判断是否为时间戳格式
							String format;
							String dateData = String.valueOf(dataMap.get(vModel));
							String dateSwapInfo = swapDataVo.getFormat()!=null ? swapDataVo.getFormat() :swapDataVo.getType()!=null&& swapDataVo.getType().equals(JnpfKeyConsts.DATE)? "yyyy-MM-dd" : "yyyy-MM-dd HH:mm:ss";
							if (!dateData.contains("-") && !dateData.contains(":") && dateData.length()>10){
								DateTimeFormatter ftf = DateTimeFormatter.ofPattern(dateSwapInfo);
								format = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli((Long)dataMap.get(vModel)), ZoneId.of("+8")));
						}else{
								format=dateData;
							}
								if (format.contains(".")){
									format=	format.substring(0,format.lastIndexOf("."));
								}
							SimpleDateFormat sdf = new SimpleDateFormat(dateSwapInfo);
							Date date = sdf.parse(format);
							String outTime = sdf.format(sdf.parse(DateUtil.dateFormat(date)));
							dataMap.put(vModel,outTime);
							break;
						default:break;
					}
					//转换数据接口的数据
					if (dataType!=null){
						if(!jnpfKey.equals(JnpfKeyConsts.CASCADER) && !jnpfKey.equals(JnpfKeyConsts.CHECKBOX)){
							//静态数据
							if (dataType.equals(OnlineDataTypeEnum.STATIC.getType())){
								String redisKey = vModel+"_" +OnlineDataTypeEnum.STATIC.getType();
								Map<Object, Object> staticMap = redisUtil.getMap(redisKey);
								String s = getDataInMethod(staticMap, dataMap.get(vModel));
								dataMap.put(vModel,s);

								//远端数据
							}else if (dataType.equals(OnlineDataTypeEnum.DYNAMIC.getType())){
								String redisKey = vModel+"_" +OnlineDataTypeEnum.DYNAMIC.getType();
								Map<Object, Object> dynamicMap = redisUtil.getMap(redisKey);
								String s = getDataInMethod(dynamicMap, dataMap.get(vModel));
								dataMap.put(vModel,s);

								//数据字典
							}else if (dataType.equals(OnlineDataTypeEnum.DICTIONARY.getType())){
								String redisKey = vModel+"_" +OnlineDataTypeEnum.DICTIONARY.getType();
								Map<Object, Object> dictionaryMap = redisUtil.getMap(redisKey);
								String s = getDataInMethod(dictionaryMap, dataMap.get(vModel));
								dataMap.put(vModel,s);
							}
						}
					}
				}

			}
		}
		return list;
	}

	/**
	 * 保存需要转换的数据到redis(系统控件)
	 * @param swapDataVoList
	 */
	@SneakyThrows
	public static void sysNeedSwapData(List<OnlineFieldsModel> swapDataVoList, String visualDevId){
		init();
		for (OnlineFieldsModel swapDataVo :swapDataVoList){
			String jnpfKey = swapDataVo.getConfig().getJnpfKey();
			String dataType = swapDataVo.getConfig().getDataType();
			switch (jnpfKey){
				//公司组件
				case JnpfKeyConsts.COMSELECT:
				//部门组件
				case JnpfKeyConsts.DEPSELECT:
					//所属部门
				case JnpfKeyConsts.CURRDEPT:
					//所属公司
				case JnpfKeyConsts.CURRORGANIZE:
					List<OrganizeEntity> organizeEntityList = organizeService.getList();
					Map<String,String> orgMap =new HashMap<>(16);
					organizeEntityList.stream().forEach(org->{
						orgMap.put(org.getId(),org.getFullName());
					});
					redisUtil.insert(visualDevId + CacheKeyEnum.ORG.getName(),orgMap,60*3);
					break;

					//岗位组件
				case JnpfKeyConsts.POSSELECT:
					//所属岗位
				case JnpfKeyConsts.CURRPOSITION:
					List<PositionEntity> positionList = positionService.getList();
					Map<String,String> positionMap =new HashMap<>(16);
					positionList.stream().forEach(positionEntity -> positionMap.put(positionEntity.getId(),positionEntity.getFullName()));
					redisUtil.insert(visualDevId + CacheKeyEnum.POS.getName(),positionMap,60*3);
					break;

					//用户组件
				case JnpfKeyConsts.USERSELECT:
					//创建用户
				case JnpfKeyConsts.CREATEUSER:
					//修改用户
				case JnpfKeyConsts.MODIFYUSER:
					List<UserEntity> userEntityList = userService.getList();
					Map<String ,String> userMap =new HashMap<>(16);
					userEntityList.stream().forEach(userEntity -> userMap.put(userEntity.getId(),userEntity.getRealName()));
					redisUtil.insert(visualDevId+ CacheKeyEnum.USER.getName(),userMap,60*3);
					break;

				//省市区联动
				case JnpfKeyConsts.ADDRESS:
					List<ProvinceEntity> provinceEntityList = provinceService.getAllList();
					Map<String,String> provinceMap = new HashMap<>(16);
					provinceEntityList.stream().forEach(p->provinceMap.put(p.getId(),p.getFullName()));
					redisUtil.insert(visualDevId+CacheKeyEnum.PRO.getName(),provinceMap,60*3);
					break;
				default:break;
			}

			if (dataType!=null){

				//数据接口的数据存放
				String label;
				String value;
				List<Map<String, Object>> options =null ;
				if (swapDataVo.getConfig().getJnpfKey().equals(JnpfKeyConsts.CASCADER) || swapDataVo.getConfig().getJnpfKey().equals(JnpfKeyConsts.TREESELECT)){
					label=swapDataVo.getProps().getProps().getLabel();
					value = swapDataVo.getProps().getProps().getValue();
				}else {
					label = swapDataVo.getConfig().getProps().getLabel();
					value =swapDataVo.getConfig().getProps().getValue();
				}
				if (dataType.equals(OnlineDataTypeEnum.STATIC.getType())) {
					if (swapDataVo.getOptions() != null) {
						options = JsonUtil.getJsonToListMap(swapDataVo.getOptions());
						String Children = swapDataVo.getProps().getProps().getChildren();
						JSONArray data = JsonUtil.getListToJsonArray(options);
						getOptions(label, value, Children, data, options);
					} else {
						options = swapDataVo.getSlot().getOptions();
					}
				}
				if (dataType.equals(OnlineDataTypeEnum.DYNAMIC.getType())){
					Object data = dataInterfaceService.infoToId(swapDataVo.getConfig().getPropsUrl()).getData();
					if(data instanceof  List){
						options = (List<Map<String, Object>>) data;
					}
				}
				if (dataType.equals(OnlineDataTypeEnum.DICTIONARY.getType())){
					List<DictionaryDataEntity> list = dictionaryDataService.getList(swapDataVo.getConfig().getDictionaryType());
					options=list.stream().map(dic->{
						Map<String,Object> dictionaryMap = new HashMap<>(16);
						dictionaryMap.put("id",dic.getId());
						dictionaryMap.put("fullName",dic.getFullName());
						return dictionaryMap;
					}).collect(Collectors.toList());
				}


				Map<String,String> dataInterfaceMap=new HashMap<>(16);
				options.stream().forEach(o->{
					dataInterfaceMap.put(String.valueOf(o.get(value)),String.valueOf(o.get(label)));
				});

					//静态数据
					if (dataType.equals(OnlineDataTypeEnum.STATIC.getType())){
						String redisKey = swapDataVo.getVModel()+"_" +OnlineDataTypeEnum.STATIC.getType();
						redisUtil.insert(redisKey,dataInterfaceMap,60*3);
						//远端数据
					}else if (dataType.equals(OnlineDataTypeEnum.DYNAMIC.getType())){
						String redisKey = swapDataVo.getVModel()+"_" +OnlineDataTypeEnum.DYNAMIC.getType();
						redisUtil.insert(redisKey,dataInterfaceMap,60*3);
						//数据字典
					}else if (dataType.equals(OnlineDataTypeEnum.DICTIONARY.getType())){
						String redisKey = swapDataVo.getVModel()+"_" +OnlineDataTypeEnum.DICTIONARY.getType();
						redisUtil.insert(redisKey,dataInterfaceMap,60*3);
					}
			}
		}
		redisUtil.insert(visualDevId,DateUtil.getNow(),60*3);
	}

	/**
	 * 递归查询
	 * @param label
	 * @param value
	 * @param Children
	 * @param data
	 * @param options
	 */
	public static void getOptions( String label, String value,String Children,JSONArray data,List<Map<String,Object>> options){
		for (int i =0;i<data.size();i++){
			JSONObject ob = data.getJSONObject(i);
			Map<String,Object> tree = new HashMap<>(16);
			tree.put(value,String.valueOf(ob.get(value)));
			tree.put(label,String.valueOf(ob.get(label)));
			options.add(tree);
			if (ob.get(Children)!=null){
				JSONArray childrenArray = ob.getJSONArray(Children);
				getOptions(label,value,Children,childrenArray,options);
			}
		}
	}

	public static List<Map<String,Object>> groupData(List<Map<String, Object>> realList, ColumnDataModel columnDataModel){
		List<Map<String, Object>> columnList = JsonUtil.getJsonToListMap(columnDataModel.getColumnList());
		String firstField;
		String groupField=columnDataModel.getGroupField();
		Map<String, Object> map = columnList.stream().filter(t -> !String.valueOf(t.get("prop")).equals(columnDataModel.getGroupField())).findFirst().orElse(null);
		firstField=String.valueOf(map.get("prop"));

		Map<String,List<Map<String,Object>>> twoMap = new HashMap<>(16);

		for (Map<String,Object> realMap : realList){
			String value = String.valueOf(realMap.get(groupField));
			boolean isKey = twoMap.get(value)!=null;
			if(isKey){
				List<Map<String, Object>> maps = twoMap.get(value);
				maps.add(realMap);
				twoMap.put(value,maps);
			}else {
				List<Map<String,Object>> childrenList = new ArrayList<>();
				childrenList.add(realMap);
				twoMap.put(value,childrenList);
			}
		}

		List<Map<String,Object>> resultList =new ArrayList<>();
		for (String key :twoMap.keySet()){
			Map<String,Object> thirdMap =new HashMap<>(16);
			thirdMap.put(firstField,key!="null" ? key : "");
			thirdMap.put("top",true);
			thirdMap.put("id",RandomUtil.uuId());
			thirdMap.put("children",twoMap.get(key));
			resultList.add(thirdMap);
		}
		return resultList;
	}

	public static String getDataInMethod(	Map<Object, Object> redisMap ,Object modelData){
		String s2 ;
		if (String.valueOf(modelData).contains("[")) {
			List<String> modelDataList = JsonUtil.getJsonToList(String.valueOf(modelData), String.class);
			modelDataList = modelDataList.stream().map(s -> String.valueOf(redisMap.get(s))).collect(Collectors.toList());
			 s2 = String.join(",", modelDataList);
		}
		else {
			String[] modelDatas = String.valueOf(modelData).split(",") ;
			StringBuilder dynamicData = new StringBuilder();
			for (int i=0;i<modelDatas.length;i++){
				modelDatas[i]=String.valueOf(redisMap.get(modelDatas[i]));
				dynamicData.append(modelDatas[i]+",");
			}
			s2=	dynamicData.deleteCharAt(dynamicData.length()-1).toString();
		}
		return s2;
	}
}

