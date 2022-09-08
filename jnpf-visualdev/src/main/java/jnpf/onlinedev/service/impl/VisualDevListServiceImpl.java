package jnpf.onlinedev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.template6.ColumnListField;
import jnpf.base.service.DblinkService;
import jnpf.database.exception.DataException;
import jnpf.database.model.DbLinkEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.service.FlowTaskService;
import jnpf.model.visiual.ColumnDataModel;
import jnpf.model.visiual.FormDataModel;
import jnpf.model.visiual.OnlineDevData;
import jnpf.model.visiual.TableModel;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;
import jnpf.onlinedev.mapper.VisualdevModelDataMapper;
import jnpf.onlinedev.model.OnlineDevListModel.OnlineFieldsModel;
import jnpf.onlinedev.model.OnlineDevListModel.VisualColumnSearchVO;
import jnpf.onlinedev.model.OnlineDevListModel.OnlineDevListDataVO;
import jnpf.onlinedev.model.PaginationModel;
import jnpf.onlinedev.service.VisualDevListService;
import jnpf.onlinedev.util.onlineDevUtil.OnlineDevListUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlinePublicUtils;
import jnpf.util.JsonUtil;
import jnpf.util.PageUtil;
import jnpf.util.StringUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 *临时实现类
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/28
 */
@Service
public class VisualDevListServiceImpl extends ServiceImpl<VisualdevModelDataMapper, VisualdevModelDataEntity> implements VisualDevListService {
	@Autowired
	private DblinkService dblinkService;
	@Autowired
	private FlowTaskService flowTaskService;
	@SneakyThrows
	@Override
	public List<Map<String,Object>> getRealList(VisualdevEntity visualdevEntity, PaginationModel paginationModel) {
		//数据源
		DbLinkEntity linkEntity = dblinkService.getInfo(visualdevEntity.getDbLinkId());

		ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(),ColumnDataModel.class);
		List<OnlineDevListDataVO> noSwapDataList =null;

		//判断有无表
		if (OnlinePublicUtils.isUseTables(visualdevEntity.getTables())){
				List<TableModel> tableModelList = JsonUtil.getJsonToList(visualdevEntity.getTables(),TableModel.class);
				TableModel tableModel =tableModelList.stream().filter(model -> model.getTypeId().equals("1")).findFirst().orElse(null);

			 List<ColumnListField> modelList = JsonUtil.getJsonToList(columnDataModel.getColumnList(), ColumnListField.class);

				noSwapDataList= OnlineDevListUtils.getHasTableList(noSwapDataList,tableModel.getTable(),modelList,columnDataModel,linkEntity);

		}else {
			noSwapDataList =this.getList(visualdevEntity.getId());
		}

		//封装查询条件
		if (StringUtil.isNotEmpty(paginationModel.getJson())){
			Map<String, Object> keyJsonMap = JsonUtil.stringToMap(paginationModel.getJson());
			List<VisualColumnSearchVO> searchVOList= JsonUtil.getJsonToList(columnDataModel.getSearchList(),VisualColumnSearchVO.class);
			searchVOList =	searchVOList.stream().map(searchVO->{
				searchVO.setValue(keyJsonMap.get(searchVO.getVModel()));
				return searchVO;
			}).filter(vo->vo.getValue()!=null).collect(Collectors.toList());
			//左侧树查询
			boolean b =false;
			if (columnDataModel.getTreeRelation()!=null){
				b = keyJsonMap.keySet().stream().anyMatch(t -> t.equalsIgnoreCase(String.valueOf(columnDataModel.getTreeRelation())));
			}
			if (b && keyJsonMap.size()>searchVOList.size()){
				String relation =String.valueOf(columnDataModel.getTreeRelation());
				VisualColumnSearchVO vo =new VisualColumnSearchVO();
				vo.setSearchType("1");
				vo.setVModel(relation);
				vo.setValue(keyJsonMap.get(relation));
				searchVOList.add(vo);
			}

			//条件查询
			noSwapDataList = OnlineDevListUtils.getNoSwapList(noSwapDataList, searchVOList);
		}

		//排序
		if (noSwapDataList.size()>0){
			if (StringUtil.isNotEmpty(paginationModel.getSidx())) {
				//排序处理
				noSwapDataList.sort((o1, o2) -> {
					Map<String, Object> i1=o1.getData();
					Map<String, Object> i2=o2.getData();
					String s1=String.valueOf(i1.get(paginationModel.getSidx()));
					String s2=String.valueOf(i2.get(paginationModel.getSidx()));
					if ("desc".equalsIgnoreCase(paginationModel.getSort())) {
						return s2.compareTo(s1);
					} else  {
						return s1.compareTo(s2);
					}
				});
			}
		}
		long total = noSwapDataList.size();

		//数据分页
		noSwapDataList = PageUtil.getListPage((int) paginationModel.getCurrentPage(), (int) paginationModel.getPageSize(), noSwapDataList);
		noSwapDataList=paginationModel.setData(noSwapDataList,total);

		//数据转换
		FormDataModel formDataModel = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
		List<OnlineFieldsModel> swapDataVoList = JsonUtil.getJsonToList(formDataModel.getFields(),OnlineFieldsModel.class);
		//递归处理控件
		List<OnlineFieldsModel> allFormDataModelList = new ArrayList<>();
		OnlinePublicUtils.recursionFields(swapDataVoList,allFormDataModelList);
		allFormDataModelList=allFormDataModelList.stream().filter(model->model.getConfig().getJnpfKey()!=null).collect(Collectors.toList());
		noSwapDataList = OnlineDevListUtils.getSwapList(noSwapDataList, allFormDataModelList,visualdevEntity.getId());

		List<Map<String, Object>> realList = noSwapDataList.stream().map(t -> {
			t.getData().put("id",t.getId());
			return t.getData();
		}).collect(Collectors.toList());

		//添加流程状态
		if(visualdevEntity.getWebType()!=null){
			if (visualdevEntity.getWebType().equals("3")){
				for (Map<String,Object> map :realList) {
					FlowTaskEntity taskEntity = flowTaskService.getInfoSubmit(map.get("id").toString());
					if (taskEntity==null){
						map.put("flowState","");
					}else {
						map.put("flowState",taskEntity.getStatus());
					}
				}
			}
		}

		//判断数据是否分组
		if (OnlineDevData.TYPE_THREE_COLUMNDATA.equals(columnDataModel.getType())) {
			return OnlineDevListUtils.groupData(realList,columnDataModel);
		}

		return realList;
	}

	@Override
	public List<OnlineDevListDataVO> getList(String modelId) {
		QueryWrapper<VisualdevModelDataEntity> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(VisualdevModelDataEntity::getVisualDevId, modelId);
		List<VisualdevModelDataEntity> list = this.list(queryWrapper);
		List<OnlineDevListDataVO> dataVoList = list.parallelStream().map(t -> {
			OnlineDevListDataVO vo = new OnlineDevListDataVO();
			vo.setId(t.getId());
			vo.setData(JsonUtil.stringToMap(t.getData()));
			return vo;
		}).collect(Collectors.toList());
		return dataVoList;
	}
}
