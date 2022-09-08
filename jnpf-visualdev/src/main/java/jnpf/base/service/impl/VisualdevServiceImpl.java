package jnpf.base.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.mapper.VisualdevMapper;
import jnpf.base.service.VisualdevService;
import jnpf.base.model.PaginationVisualdev;
import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.service.FlowEngineService;
import jnpf.exception.WorkFlowException;
import jnpf.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Service
public class VisualdevServiceImpl extends ServiceImpl<VisualdevMapper, VisualdevEntity> implements VisualdevService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private VisualdevMapper visualdevMapper;
    @Autowired
    private FlowEngineService flowEngineService;

    @Override
    public List<VisualdevEntity> getList(PaginationVisualdev paginationVisualdev) {
        QueryWrapper<VisualdevEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(paginationVisualdev.getKeyword())) {
            queryWrapper.lambda().like(VisualdevEntity::getFullName, paginationVisualdev.getKeyword());
        }
        queryWrapper.lambda().eq(VisualdevEntity::getType, paginationVisualdev.getType());
        queryWrapper.lambda().orderByDesc(VisualdevEntity::getCreatorTime);
        return list(queryWrapper);
    }


    @Override
    public List<VisualdevEntity> getList() {
        QueryWrapper<VisualdevEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(VisualdevEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public VisualdevEntity getInfo(String id) {
        QueryWrapper<VisualdevEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualdevEntity::getId, id);
        return this.getOne(queryWrapper);
    }


    @Override
    public void create(VisualdevEntity entity) {
        if (StringUtil.isEmpty(entity.getId())){
            entity.setId(RandomUtil.uuId());
        }
        entity.setSortCode(RandomUtil.parses());
        if (StringUtil.isNotEmpty(entity.getWebType())){
            if (entity.getWebType()!=null && entity.getWebType().equals("3")){
                FlowEngineEntity flowEngineEntity = new FlowEngineEntity();
                flowEngineEntity.setId(entity.getId());
                flowEngineEntity.setFlowTemplateJson(entity.getFlowTemplateJson());
                flowEngineEntity.setEnCode("#visualDev"+entity.getEnCode());
                flowEngineEntity.setType(1);
                flowEngineEntity.setIcon("icon-ym icon-ym-node");
                flowEngineEntity.setIconBackground("#008cff");
                flowEngineEntity.setEnabledMark(1);
                flowEngineEntity.setFormType(2);
                flowEngineEntity.setTables(entity.getTables());
                flowEngineEntity.setDbLinkId(entity.getDbLinkId());
                flowEngineEntity.setFormData(entity.getFormData());
                flowEngineService.create(flowEngineEntity);
                entity.setFlowId(flowEngineEntity.getId());
            }
        }
        this.save(entity);
    }

    @Override
    public boolean update(String id, VisualdevEntity entity) {
        entity.setId(id);
        String redisKey = cacheKeyUtil.getVisiualData() + id;
        if (redisUtil.exists(redisKey)) {
            redisUtil.remove(redisKey);
        }
        //关联流程更新
        if (entity.getWebType()!=null && entity.getWebType().equals("3")){
            try {
                VisualdevEntity visualdevEntity = this.getInfo(id);
                FlowEngineEntity flowEngineEntity;
                //判断是否关联流程id
                if (visualdevEntity.getFlowId()==null){
                    flowEngineEntity=new FlowEngineEntity();
                    flowEngineEntity.setId(id);
                    flowEngineEntity.setFlowTemplateJson(entity.getFlowTemplateJson());
                    flowEngineEntity.setEnCode("#visualDev"+entity.getEnCode());
                    flowEngineEntity.setType(1);
                    flowEngineEntity.setIcon("icon-ym icon-ym-node");
                    flowEngineEntity.setIconBackground("#008cff");
                    flowEngineEntity.setEnabledMark(1);
                    flowEngineEntity.setFormType(2);
                    flowEngineEntity.setTables(entity.getTables());
                    flowEngineEntity.setDbLinkId(entity.getDbLinkId());
                    flowEngineEntity.setFormData(entity.getFormData());
                    flowEngineService.create(flowEngineEntity);
                    entity.setFlowId(flowEngineEntity.getId());
                }
                 flowEngineEntity = flowEngineService.getInfo(visualdevEntity.getFlowId());
                flowEngineEntity.setFlowTemplateJson(entity.getFlowTemplateJson());
                flowEngineEntity.setTables(entity.getTables());
                flowEngineEntity.setDbLinkId(entity.getDbLinkId());
                flowEngineEntity.setFormData(entity.getFormData());
                flowEngineService.update(visualdevEntity.getFlowId(),flowEngineEntity);
            } catch (WorkFlowException e) {
                e.printStackTrace();
            }
        }
        return this.updateById(entity);
    }

    @Override
    public void delete(VisualdevEntity entity) throws WorkFlowException {
        if (entity != null) {
            try {
                if(entity.getFlowId()!=null){
                    FlowEngineEntity flowEngineEntity = flowEngineService.getInfo(entity.getFlowId());
                    flowEngineService.delete(flowEngineEntity);
                }
            } catch (WorkFlowException e) {
                throw new WorkFlowException("删除失败");
            }
            this.removeById(entity.getId());
        }
    }
}
