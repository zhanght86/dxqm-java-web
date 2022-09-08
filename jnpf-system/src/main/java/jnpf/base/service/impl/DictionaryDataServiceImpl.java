package jnpf.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.DictionaryTypeEntity;
import jnpf.base.mapper.DictionaryDataMapper;
import jnpf.base.model.dictionarydata.DictionaryDataExportModel;
import jnpf.base.model.dictionarytype.DictionaryExportModel;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.DictionaryTypeService;
import jnpf.database.exception.DataException;
import jnpf.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字典数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class DictionaryDataServiceImpl extends ServiceImpl<DictionaryDataMapper, DictionaryDataEntity> implements DictionaryDataService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private DictionaryTypeService dictionaryTypeService;

    @Override
    public List<DictionaryDataEntity> getList() {
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(DictionaryDataEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<DictionaryDataEntity> getList(String dictionaryTypeId) {
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(
                t->t.eq(DictionaryDataEntity::getDictionaryTypeId,dictionaryTypeId)
                    .or().eq(DictionaryDataEntity::getEnCode, dictionaryTypeId)
        );
        queryWrapper.lambda().orderByAsc(DictionaryDataEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public DictionaryDataEntity getInfo(String id) {
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryDataEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(String dictionaryTypeId, String fullName, String id) {
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryDataEntity::getFullName, fullName).eq(DictionaryDataEntity::getDictionaryTypeId, dictionaryTypeId);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(DictionaryDataEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String dictionaryTypeId, String enCode, String id) {
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryDataEntity::getEnCode, enCode).eq(DictionaryDataEntity::getDictionaryTypeId, dictionaryTypeId);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(DictionaryDataEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public void delete(DictionaryDataEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    public void create(DictionaryDataEntity entity) {
        //判断id是否为空,为空则为新建
        if (StringUtil.isEmpty(entity.getId())){
            entity.setId(RandomUtil.uuId());
            entity.setSimpleSpelling(PinYinUtil.getFirstSpell(entity.getFullName()).toUpperCase());
            entity.setCreatorUserId(userProvider.get().getUserId());
        }
        this.save(entity);
    }

    @Override
    public boolean update(String id, DictionaryDataEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(DateUtil.getNowDate());
        entity.setLastModifyUserId(userProvider.get().getUserId());
       return this.updateById(entity);
    }

    @Override
    public boolean first(String id) {
        boolean isOk = false;
        //获取要上移的那条数据的信息
        DictionaryDataEntity upEntity = this.getById(id);
        Long upSortCode = upEntity.getSortCode() == null ? 0 : upEntity.getSortCode();
        //查询上几条记录
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DictionaryDataEntity::getDictionaryTypeId, upEntity.getDictionaryTypeId())
                .eq(DictionaryDataEntity::getParentId, upEntity.getParentId())
                .lt(DictionaryDataEntity::getSortCode, upSortCode)
                .orderByDesc(DictionaryDataEntity::getSortCode);
        List<DictionaryDataEntity> downEntity = this.list(queryWrapper);
        if (downEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = upEntity.getSortCode();
            upEntity.setSortCode(downEntity.get(0).getSortCode());
            downEntity.get(0).setSortCode(temp);
            updateById(downEntity.get(0));
            updateById(upEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    public boolean next(String id) {
        boolean isOk = false;
        //获取要下移的那条数据的信息
        DictionaryDataEntity downEntity = this.getById(id);
        Long upSortCode = downEntity.getSortCode() == null ? 0 : downEntity.getSortCode();
        //查询下几条记录
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DictionaryDataEntity::getDictionaryTypeId, downEntity.getDictionaryTypeId())
                .eq(DictionaryDataEntity::getParentId, downEntity.getParentId())
                .gt(DictionaryDataEntity::getSortCode, upSortCode)
                .orderByAsc(DictionaryDataEntity::getSortCode);
        List<DictionaryDataEntity> upEntity = this.list(queryWrapper);
        if (upEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = downEntity.getSortCode();
            downEntity.setSortCode(upEntity.get(0).getSortCode());
            upEntity.get(0).setSortCode(temp);
            updateById(upEntity.get(0));
            updateById(downEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    public List<DictionaryDataEntity> getDictionName(List<String> id) {
        List<DictionaryDataEntity> dictionList = new ArrayList<>();
        if (id.size() > 0) {
            QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(DictionaryDataEntity::getEnCode, id);
            dictionList = this.list(queryWrapper);
        }
        return dictionList;
    }

    @Override
    public DictionaryExportModel exportData(String id) {
        //获取数据分类字段详情
        DictionaryTypeEntity typeEntity = dictionaryTypeService.getInfo(id);
        DictionaryExportModel exportModel = new DictionaryExportModel();
        //递归子分类
        Set<DictionaryTypeEntity> set = new HashSet<>();
        List<DictionaryTypeEntity> typeEntityList = dictionaryTypeService.getList();
        getDictionaryTypeEntitySet(typeEntity, set, typeEntityList);
        List<DictionaryTypeEntity> collect = new ArrayList<>(set);
        //判断是否有子分类
        if (collect.size() > 0){
            exportModel.setList(collect);
        }
        //判断是否需要new
        if (exportModel.getList() == null) {
            List<DictionaryTypeEntity> list = new ArrayList<>();
            list.add(typeEntity);
            exportModel.setList(list);
        }else {
            exportModel.getList().add(typeEntity);
        }
        //获取该类型下的数据
        List<DictionaryDataExportModel> modelList = new ArrayList<>();
        for (DictionaryTypeEntity dictionaryTypeEntity : exportModel.getList()) {
            List<DictionaryDataEntity> entityList = getList(dictionaryTypeEntity.getId());
            for (DictionaryDataEntity dictionaryDataEntity : entityList) {
                DictionaryDataExportModel dataExportModel = JsonUtil.getJsonToBean(dictionaryDataEntity, DictionaryDataExportModel.class);
                modelList.add(dataExportModel);
            }
        }
        exportModel.setModelList(modelList);
        return exportModel;
    }

    /**
     * 递归字典分类
     * @param dictionaryTypeEntity 数据字典类型实体
     */
    private void getDictionaryTypeEntitySet(DictionaryTypeEntity dictionaryTypeEntity, Set<DictionaryTypeEntity> set, List<DictionaryTypeEntity> typeEntityList){
        //是否含有子分类
        List<DictionaryTypeEntity> collect = typeEntityList.stream().filter(t -> dictionaryTypeEntity.getId().equals(t.getParentId())).collect(Collectors.toList());
        if (collect.size() > 0){
            for (DictionaryTypeEntity typeEntity : collect) {
                set.add(typeEntity);
                getDictionaryTypeEntitySet(typeEntity, set, typeEntityList);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = DataException.class)
    public boolean importData(DictionaryExportModel exportModel) throws DataException {
        try {
            boolean isExists = true;
            List<DictionaryTypeEntity> list = JsonUtil.getJsonToList(exportModel.getList(), DictionaryTypeEntity.class);
            List<DictionaryDataEntity> entityList = JsonUtil.getJsonToList(exportModel.getModelList(), DictionaryDataEntity.class);
            //遍历插入分类
            for (DictionaryTypeEntity entity : list) {
                if (dictionaryTypeService.getInfo(entity.getId()) == null
                        && !dictionaryTypeService.isExistByEnCode(entity.getEnCode(), entity.getId())
                        && !dictionaryTypeService.isExistByFullName(entity.getFullName(), entity.getId())
                ) {
                    isExists = false;
                    dictionaryTypeService.create(entity);
                }
            }
            for (DictionaryDataEntity entity1 : entityList) {
                DictionaryDataEntity dataEntity = getInfo(entity1.getId());
                if (dataEntity == null && dictionaryTypeService.getInfo(dataEntity.getDictionaryTypeId()) != null
                        && !isExistByFullName(entity1.getDictionaryTypeId(), entity1.getFullName(), entity1.getId())
                        && !isExistByEnCode(entity1.getDictionaryTypeId(), entity1.getEnCode(), entity1.getId())
                ) {
                    isExists = false;
                    create(entity1);
                }
            }
            return isExists;
        }catch (Exception e) {
            //手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new DataException(e.getMessage());
        }
    }

}
