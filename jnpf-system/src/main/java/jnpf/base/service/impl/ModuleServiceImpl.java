package jnpf.base.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.entity.*;
import jnpf.base.mapper.ModuleMapper;
import jnpf.base.model.module.ModuleExportModel;
import jnpf.base.service.*;
import jnpf.database.exception.DataException;
import jnpf.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

/**
 * 系统功能
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class ModuleServiceImpl extends ServiceImpl<ModuleMapper, ModuleEntity> implements ModuleService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ModuleButtonService moduleButtonService;
    @Autowired
    private ModuleColumnService moduleColumnService;
    @Autowired
    private ModuleDataAuthorizeService moduleDataAuthorizeService;
    @Autowired
    private ModuleButtonService buttonService;
    @Autowired
    private ModuleColumnService columnService;
    @Autowired
    private ModuleDataAuthorizeSchemeService schemeService;
    @Autowired
    private ModuleDataAuthorizeService authorizeService;

    @Override
    public List<ModuleEntity> getList() {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(ModuleEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public ModuleEntity getInfo(String id) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(String fullName, String id, String category) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getFullName, fullName).eq(ModuleEntity::getCategory, category);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(ModuleEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String enCode, String id, String category) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getEnCode, enCode).eq(ModuleEntity::getCategory, category);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(ModuleEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(ModuleEntity entity) {
        this.removeById(entity.getId());
        QueryWrapper<ModuleButtonEntity> buttonWrapper = new QueryWrapper<>();
        buttonWrapper.lambda().eq(ModuleButtonEntity::getModuleId, entity.getId());
        moduleButtonService.remove(buttonWrapper);
        QueryWrapper<ModuleColumnEntity> columnWrapper = new QueryWrapper<>();
        columnWrapper.lambda().eq(ModuleColumnEntity::getModuleId, entity.getId());
        moduleColumnService.remove(columnWrapper);
        QueryWrapper<ModuleDataAuthorizeEntity> dataWrapper = new QueryWrapper<>();
        dataWrapper.lambda().eq(ModuleDataAuthorizeEntity::getModuleId, entity.getId());
        moduleDataAuthorizeService.remove(dataWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void create(ModuleEntity entity) {
        boolean flag = false;
        if (entity.getId() == null) {
            entity.setId(RandomUtil.uuId());
            flag = true;
        }
        this.save(entity);
        //添加默认按钮
        if ("3".equals(String.valueOf(entity.getType())) && flag) {
//            Map<String,Object> propJsonMap= JSONUtil.StringToMap(entity.getPropertyJson());
//            if(propJsonMap!=null){
//                VisualdevEntity visualdevEntity =visualdevService.getInfo(propJsonMap.get("moduleId").toString());
//                //去除模板中的F_
//                visualdevEntity= VisualUtil.delFKey(visualdevEntity);
//                if(visualdevEntity!=null){
//                    List<BtnData> btnData =new ArrayList<>();
//                    Map<String,Object> column=JSONUtil.StringToMap(visualdevEntity.getColumnData());
//                    if(column.get("columnBtnsList")!=null){
//                        btnData.addAll(JSONUtil.getJsonToList(JSONUtil.getJsonToListMap(column.get("columnBtnsList").toString()),BtnData.class));
//                    }
//                    if(column.get("btnsList")!=null){
//                        btnData.addAll(JSONUtil.getJsonToList(JSONUtil.getJsonToListMap(column.get("btnsList").toString()),BtnData.class));
//                    }
//                    if(btnData.size()>0){
//                        for(BtnData btn:btnData){
//                            ModuleButtonEntity moduleButtonEntity=new ModuleButtonEntity();
//                            moduleButtonEntity.setId(RandomUtil.uuId());
//                            moduleButtonEntity.setParentId("-1");
//                            moduleButtonEntity.setFullName(btn.getLabel());
//                            moduleButtonEntity.setEnCode("btn_"+btn.getValue());
//                            moduleButtonEntity.setSortCode(0L);
//                            moduleButtonEntity.setModuleId(entity.getId());
//                            moduleButtonEntity.setEnabledMark(1);
//                            moduleButtonEntity.setIcon(btn.getIcon());
//                            moduleButtonService.save(moduleButtonEntity);
//                        }
//                    }
//                    List<IndexGridField6Model> indexGridField6Models =new ArrayList<>();
//                    if(column.get("columnList")!=null){
//                        indexGridField6Models.addAll(JSONUtil.getJsonToList(JSONUtil.getJsonToListMap(column.get("columnList").toString()),IndexGridField6Model.class));
//                       if(indexGridField6Models.size()>0){
//                           for(IndexGridField6Model field6Model:indexGridField6Models){
//                               ModuleColumnEntity moduleColumnEntity=new ModuleColumnEntity();
//                               moduleColumnEntity.setId(RandomUtil.uuId());
//                               moduleColumnEntity.setParentId("-1");
//                               moduleColumnEntity.setFullName(field6Model.getLabel());
//                               moduleColumnEntity.setEnCode(field6Model.getProp());
//                               moduleColumnEntity.setSortCode(0L);
//                               moduleColumnEntity.setModuleId(entity.getId());
//                               moduleColumnEntity.setEnabledMark(1);
//                               moduleColumnService.save(moduleColumnEntity);
//                           }
//                       }
//                    }
//                }
//            }
        } else if ("4".equals(String.valueOf(entity.getType())) && flag) {
            for (int i = 0; i < 3; i++) {
                String fullName = "新增";
                String value = "add";
                String icon = "el-icon-plus";
                if (i == 1) {
                    fullName = "编辑";
                    value = "edit";
                    icon = "el-icon-edit";
                }
                if (i == 2) {
                    fullName = "删除";
                    value = "remove";
                    icon = "el-icon-delete";
                }
                ModuleButtonEntity moduleButtonEntity = new ModuleButtonEntity();
                moduleButtonEntity.setId(RandomUtil.uuId());
                moduleButtonEntity.setParentId("-1");
                moduleButtonEntity.setFullName(fullName);
                moduleButtonEntity.setEnCode("btn_" + value);
                moduleButtonEntity.setSortCode(0L);
                moduleButtonEntity.setModuleId(entity.getId());
                moduleButtonEntity.setEnabledMark(1);
                moduleButtonEntity.setIcon(icon);
                moduleButtonService.save(moduleButtonEntity);
            }

        }
    }

    @Override
    public boolean update(String id, ModuleEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(DateUtil.getNowDate());
        return this.updateById(entity);
    }

    @Override
    public ModuleExportModel exportData(String id) {
        //获取信息转model
        ModuleEntity moduleEntity = getInfo(id);
        List<ModuleButtonEntity> buttonServiceList = buttonService.getList(id);
        List<ModuleColumnEntity> columnServiceList = columnService.getList(id);
        List<ModuleDataAuthorizeSchemeEntity> schemeServiceList = schemeService.getList(id);
        List<ModuleDataAuthorizeEntity> authorizeServiceList = authorizeService.getList(id);
        ModuleExportModel exportModel = JsonUtil.getJsonToBean(moduleEntity, ModuleExportModel.class);
        exportModel.setButtonEntityList(buttonServiceList);
        exportModel.setColumnEntityList(columnServiceList);
        exportModel.setSchemeEntityList(schemeServiceList);
        exportModel.setAuthorizeEntityList(authorizeServiceList);
        return exportModel;
    }

    @Override
    @Transactional(rollbackFor = DataException.class)
    public boolean importData(ModuleExportModel exportModel) throws DataException {
        try {
            boolean isExists = true;
            ModuleEntity moduleEntity = JsonUtil.getJsonToBean(exportModel, ModuleEntity.class);
            //开始导入
            if (getInfo(moduleEntity.getId()) == null) {
                isExists = false;
                create(moduleEntity);
            }
            //按钮
            List<ModuleButtonEntity> buttonEntityList = JsonUtil.getJsonToList(exportModel.getButtonEntityList(), ModuleButtonEntity.class);
            for (ModuleButtonEntity buttonEntity : buttonEntityList) {
                if (buttonService.getInfo(buttonEntity.getId()) == null
                        && !buttonService.isExistByFullName(buttonEntity.getModuleId(), buttonEntity.getFullName(), buttonEntity.getId())
                        && !buttonService.isExistByEnCode(buttonEntity.getModuleId(), buttonEntity.getEnCode(), buttonEntity.getId())
                ) {
                    isExists = false;
                    buttonService.create(buttonEntity);
                }
            }
            //列表
            List<ModuleColumnEntity> columnEntityList = JsonUtil.getJsonToList(exportModel.getColumnEntityList(), ModuleColumnEntity.class);
            for (ModuleColumnEntity columnEntity : columnEntityList) {
                if (columnService.getInfo(columnEntity.getId()) == null
                        && !columnService.isExistByFullName(columnEntity.getModuleId(), columnEntity.getFullName(), columnEntity.getId())
                        && !columnService.isExistByEnCode(columnEntity.getModuleId(), columnEntity.getEnCode(), columnEntity.getId())
                ) {
                    isExists = false;
                    columnService.create(columnEntity);
                }
            }
            //数据权限方案
            List<ModuleDataAuthorizeSchemeEntity> schemeEntityList = JsonUtil.getJsonToList(exportModel.getColumnEntityList(), ModuleDataAuthorizeSchemeEntity.class);
            for (ModuleDataAuthorizeSchemeEntity schemeEntity : schemeEntityList) {
                if (schemeService.getInfo(schemeEntity.getId()) == null) {
                    isExists = false;
                    schemeService.create(schemeEntity);
                }
            }
            //数据权限
            List<ModuleDataAuthorizeEntity> authorizeEntityList = JsonUtil.getJsonToList(exportModel.getColumnEntityList(), ModuleDataAuthorizeEntity.class);
            for (ModuleDataAuthorizeEntity authorizeEntity : authorizeEntityList) {
                if (authorizeService.getInfo(authorizeEntity.getId()) == null) {
                    isExists = false;
                    authorizeService.create(authorizeEntity);
                }
            }
            return isExists;
        } catch (Exception e) {
            //手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new DataException(e.getMessage());
        }
    }
}
