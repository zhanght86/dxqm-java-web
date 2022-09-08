package jnpf.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.annotations.ApiModelProperty;
import jnpf.base.UserInfo;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.ModuleService;
import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.service.FlowEngineService;
import jnpf.entity.AppDataEntity;
import jnpf.mapper.AppDataMapper;
import jnpf.model.*;
import jnpf.model.login.*;
import jnpf.permission.model.authorize.AuthorizeVO;
import jnpf.permission.service.AuthorizeService;
import jnpf.service.AppDataService;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * app常用数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-07-08
 */
@Service
public class AppDataServiceImpl extends ServiceImpl<AppDataMapper, AppDataEntity> implements AppDataService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private FlowEngineService flowEngineService;
    @Autowired
    private DictionaryDataService dictionaryDataService;

    @Override
    public List<AppDataEntity> getList(String type) {
        UserInfo userInfo = userProvider.get();
        QueryWrapper<AppDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppDataEntity::getObjectType, type).eq(AppDataEntity::getCreatorUserId, userInfo.getUserId());
        return this.list(queryWrapper);
    }

    @Override
    public List<AppDataEntity> getList() {
        QueryWrapper<AppDataEntity> queryWrapper = new QueryWrapper<>();
        return this.list(queryWrapper);
    }

    @Override
    public AppDataEntity getInfo(String objectId) {
        UserInfo userInfo = userProvider.get();
        QueryWrapper<AppDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppDataEntity::getObjectId, objectId).eq(AppDataEntity::getCreatorUserId, userInfo.getUserId());
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByObjectId(String objectId) {
        UserInfo userInfo = userProvider.get();
        QueryWrapper<AppDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppDataEntity::getObjectId, objectId).eq(AppDataEntity::getCreatorUserId, userInfo.getUserId());
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public void create(AppDataEntity entity) {
        UserInfo userInfo = userProvider.get();
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(userInfo.getUserId());
        entity.setCreatorTime(new Date());
        entity.setEnabledMark(1);
        this.save(entity);
    }

    @Override
    public void delete(AppDataEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    public List<AppFlowListAllVO> getFlowList(String type) {
        List<AppDataEntity> dataList = getList(type);
        List<AppTreeModel> modelAll = new LinkedList<>();
        List<DictionaryDataEntity> dictionList = dictionaryDataService.getList("507f4f5df86b47588138f321e0b0dac7");
        List<FlowEngineEntity> data = flowEngineService.getFlowFormList();
        for (DictionaryDataEntity entity : dictionList) {
            AppTreeModel model = new AppTreeModel();
            model.setFullName(entity.getFullName());
            model.setId(entity.getId());
            long num = data.stream().filter(t -> t.getCategory().equals(entity.getEnCode())).count();
            model.setNum(num);
            if (num > 0) {
                modelAll.add(model);
            }
        }
        for (FlowEngineEntity entity : data) {
            AppTreeModel model = JsonUtil.getJsonToBean(entity, AppTreeModel.class);
            DictionaryDataEntity dataEntity = dictionList.stream().filter(t -> t.getEnCode().equals(entity.getCategory())).findFirst().orElse(null);
            if (dataEntity != null) {
                boolean isData = dataList.stream().filter(t -> t.getObjectId().equals(entity.getId())).count() > 0;
                model.setIsData(isData);
                model.setParentId(dataEntity.getId());
                modelAll.add(model);
            }
        }
        List<SumTree<AppTreeModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<AppFlowListAllVO> result = JsonUtil.getJsonToList(trees, AppFlowListAllVO.class);
        return result;
    }

    @Override
    public List<AppDataListAllVO> getDataList(String type) {
        List<AppDataEntity> dataList = getList(type);
        AuthorizeVO authorizeModel = authorizeService.getAuthorize(true);
        List<ModuleModel> buttonList = authorizeModel.getModuleList();
        List<ModuleEntity> menuList = moduleService.getList().stream().filter(t -> "App".equals(t.getCategory()) && t.getEnabledMark() == 1).collect(Collectors.toList());
        List<UserMenuModel> list = new LinkedList<>();
        for (ModuleEntity module : menuList) {
            boolean count = buttonList.stream().filter(t -> t.getId().equals(module.getId())).count() > 0;
            UserMenuModel userMenuModel = JsonUtil.getJsonToBean(module, UserMenuModel.class);
            if (count) {
                boolean isData = dataList.stream().filter(t -> t.getObjectId().equals(module.getId())).count() > 0;
                userMenuModel.setIsData(isData);
                list.add(userMenuModel);
            }
        }
        List<SumTree<UserMenuModel>> menuAll = TreeDotUtils.convertListToTreeDot(list);
        List<AppDataListAllVO> menuListAll = JsonUtil.getJsonToList(menuAll, AppDataListAllVO.class);
        List<AppDataListAllVO> data = new LinkedList<>();
        for(AppDataListAllVO appMenu : menuListAll){
            if("-1".equals(appMenu.getParentId())){
                data.add(appMenu);
            }
        }
        return data;
    }


}
