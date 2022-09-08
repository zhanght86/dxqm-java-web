package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.entity.VisualConfigEntity;
import jnpf.entity.VisualEntity;
import jnpf.database.exception.DataException;
import jnpf.mapper.VisualMapper;
import jnpf.model.visual.VisualPaginationModel;
import jnpf.service.VisualConfigService;
import jnpf.service.VisualService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 大屏基本信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Service
public class VisualServiceImpl extends ServiceImpl<VisualMapper, VisualEntity> implements VisualService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private VisualConfigService configService;

    @Override
    public List<VisualEntity> getList(VisualPaginationModel pagination) {
        QueryWrapper<VisualEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualEntity::getCategory, pagination.getCategory());
        queryWrapper.lambda().orderByAsc(VisualEntity::getCreateTime);
        Page page = new Page(pagination.getCurrent(), pagination.getSize());
        IPage<VisualEntity> iPages = this.page(page, queryWrapper);
        return pagination.setData(iPages);
    }

    @Override
    public List<VisualEntity> getList() {
        QueryWrapper<VisualEntity> queryWrapper = new QueryWrapper<>();
        return this.list(queryWrapper);
    }

    @Override
    public VisualEntity getInfo(String id) {
        QueryWrapper<VisualEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(VisualEntity entity, VisualConfigEntity configEntity) {
        entity.setId(RandomUtil.uuId());
        entity.setCreateTime(new Date());
        entity.setUpdateUser(userProvider.get().getUserId());
        entity.setStatus("1");
        entity.setIsDeleted("0");
        this.save(entity);
        configEntity.setVisualId(entity.getId());
        configService.create(configEntity);
    }

    @Override
    public boolean update(String id, VisualEntity entity, VisualConfigEntity configEntity) {
        entity.setId(id);
        entity.setUpdateTime(new Date());
        entity.setUpdateUser(userProvider.get().getUserId());
        boolean flag = this.updateById(entity);
        if (configEntity != null) {
            configService.update(configEntity.getId(), configEntity);
        }
        return flag;
    }

    @Override
    public void delete(VisualEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
            VisualConfigEntity config = configService.getInfo(entity.getId());
            configService.delete(config);
        }
    }

    @Override
    public void createInport(VisualEntity entity, VisualConfigEntity configEntity) throws DataException {
        try {
            this.save(entity);
            configService.create(configEntity);
        }catch (Exception e){
            throw new DataException("数据已存在");
        }

    }
}
