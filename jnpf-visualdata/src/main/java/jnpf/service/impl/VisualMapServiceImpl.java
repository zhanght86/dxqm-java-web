package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.entity.VisualMapEntity;
import jnpf.mapper.VisualMapMapper;
import jnpf.model.VisualPagination;
import jnpf.service.VisualMapService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.util.RandomUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 大屏地图配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Service
public class VisualMapServiceImpl extends ServiceImpl<VisualMapMapper, VisualMapEntity> implements VisualMapService {

    @Override
    public List<VisualMapEntity> getList(VisualPagination pagination) {
        QueryWrapper<VisualMapEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(VisualMapEntity::getId);
        Page page = new Page(pagination.getCurrent(), pagination.getSize());
        IPage<VisualMapEntity> iPages = this.page(page, queryWrapper);
        return pagination.setData(iPages);
    }

    @Override
    public VisualMapEntity getInfo(String id) {
        QueryWrapper<VisualMapEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualMapEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(VisualMapEntity entity) {
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, VisualMapEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(VisualMapEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }
}
