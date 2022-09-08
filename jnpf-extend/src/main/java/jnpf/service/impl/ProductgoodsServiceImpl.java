package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.entity.ProductgoodsEntity;
import jnpf.mapper.ProductgoodsMapper;
import jnpf.model.productgoods.ProductgoodsPagination;
import jnpf.service.ProductgoodsService;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 产品商品
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 15:57:50
 */
@Service

public class ProductgoodsServiceImpl extends ServiceImpl<ProductgoodsMapper, ProductgoodsEntity> implements ProductgoodsService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<ProductgoodsEntity> getGoodList(String type) {
        QueryWrapper<ProductgoodsEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(type)) {
            queryWrapper.lambda().eq(ProductgoodsEntity::getType, type);
        }
        return this.list(queryWrapper);
    }

    @Override
    public List<ProductgoodsEntity> getList(ProductgoodsPagination goodsPagination) {
        QueryWrapper<ProductgoodsEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(goodsPagination.getCode())) {
            queryWrapper.lambda().like(ProductgoodsEntity::getCode, goodsPagination.getCode());
        }
        if (StringUtil.isNotEmpty(goodsPagination.getFullName())) {
            queryWrapper.lambda().like(ProductgoodsEntity::getFullName, goodsPagination.getFullName());
        }
        if (StringUtil.isNotEmpty(goodsPagination.getClassifyId())) {
            queryWrapper.lambda().like(ProductgoodsEntity::getClassifyId, goodsPagination.getClassifyId());
        }
        if (StringUtil.isNotEmpty(goodsPagination.getKeyword())) {
            queryWrapper.lambda().and(
                    t -> t.like(ProductgoodsEntity::getFullName, goodsPagination.getKeyword())
                            .or().like(ProductgoodsEntity::getCode, goodsPagination.getKeyword())
                            .or().like(ProductgoodsEntity::getProductSpecification, goodsPagination.getKeyword())
            );
        }
        //排序
        if (StringUtil.isEmpty(goodsPagination.getSidx())) {
            queryWrapper.lambda().orderByDesc(ProductgoodsEntity::getId);
        } else {
            queryWrapper = "asc".equals(goodsPagination.getSort().toLowerCase()) ? queryWrapper.orderByAsc(goodsPagination.getSidx()) : queryWrapper.orderByDesc(goodsPagination.getSidx());
        }
        Page<ProductgoodsEntity> page = new Page<>(goodsPagination.getCurrentPage(), goodsPagination.getPageSize());
        IPage<ProductgoodsEntity> userIPage = this.page(page, queryWrapper);
        return goodsPagination.setData(userIPage.getRecords(), userIPage.getTotal());
    }

    @Override
    public ProductgoodsEntity getInfo(String id) {
        QueryWrapper<ProductgoodsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ProductgoodsEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(ProductgoodsEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(userProvider.get().getUserId());
        entity.setCreatorTime(new Date());
        this.save(entity);
    }

    @Override
    public boolean update(String id, ProductgoodsEntity entity) {
        entity.setId(id);
        entity.setLastModifyUserId(userProvider.get().getUserId());
        entity.setLastModifyTime(new Date());
        return this.updateById(entity);
    }

    @Override
    public void delete(ProductgoodsEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }

}