package jnpf.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.entity.VisualCategoryEntity;
import jnpf.mapper.VisualCategoryMapper;
import jnpf.model.VisualPagination;
import jnpf.service.VisualCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.util.RandomUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 大屏分类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Service
public class VisualCategoryServiceImpl extends ServiceImpl<VisualCategoryMapper, VisualCategoryEntity> implements VisualCategoryService {

    @Override
    public List<VisualCategoryEntity> getList(VisualPagination pagination) {
        QueryWrapper<VisualCategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(VisualCategoryEntity::getCategorykey);
        Page page = new Page(pagination.getCurrent(), pagination.getSize());
        IPage<VisualCategoryEntity> iPages = this.page(page, queryWrapper);
        return pagination.setData(iPages);
    }

    @Override
    public List<VisualCategoryEntity> getList() {
        QueryWrapper<VisualCategoryEntity> queryWrapper = new QueryWrapper<>();
        return this.list(queryWrapper);
    }

    @Override
    public boolean isExistByValue(String value, String id) {
        QueryWrapper<VisualCategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualCategoryEntity::getCategoryvalue, value);
        if (!StringUtils.isEmpty(id)) {
            queryWrapper.lambda().ne(VisualCategoryEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public VisualCategoryEntity getInfo(String id) {
        QueryWrapper<VisualCategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualCategoryEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(VisualCategoryEntity entity) {
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, VisualCategoryEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(VisualCategoryEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }
}
