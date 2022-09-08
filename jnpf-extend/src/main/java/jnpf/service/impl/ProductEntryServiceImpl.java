package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.Pagination;
import jnpf.entity.ProductEntryEntity;
import jnpf.mapper.ProductEntryMapper;
import jnpf.service.ProductEntryService;
import jnpf.util.StringUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 销售订单明细
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 10:40:59
 */
@Service
public class ProductEntryServiceImpl extends ServiceImpl<ProductEntryMapper, ProductEntryEntity> implements ProductEntryService {

    @Override
    public List<ProductEntryEntity> getProductentryEntityList(String id) {
        QueryWrapper<ProductEntryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ProductEntryEntity::getProductId, id);
        return this.list(queryWrapper);
    }

    @Override
    public List<ProductEntryEntity> getProductentryEntityList(Pagination pagination) {
        QueryWrapper<ProductEntryEntity> queryWrapper = new QueryWrapper<>();
        if(StringUtil.isNotEmpty(pagination.getKeyword())){
            queryWrapper.lambda().and(
                    t->t.like(ProductEntryEntity::getProductName, pagination.getKeyword())
                        .or().like(ProductEntryEntity::getProductCode, pagination.getKeyword())
                        .or().like(ProductEntryEntity::getProductSpecification, pagination.getKeyword())
            );
        }
        Page<ProductEntryEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<ProductEntryEntity> userIPage = this.page(page, queryWrapper);
        return pagination.setData(userIPage.getRecords(), userIPage.getTotal());
    }

}