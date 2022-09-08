package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.UserInfo;
import jnpf.base.service.BillRuleService;
import jnpf.entity.ProductEntity;
import jnpf.entity.ProductEntryEntity;
import jnpf.database.exception.DataException;
import jnpf.mapper.ProductMapper;
import jnpf.model.product.ProductPagination;
import jnpf.service.ProductEntryService;
import jnpf.service.ProductService;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 销售订单
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 10:40:59
 */
@Service

public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductEntity> implements ProductService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private ProductEntryService productEntryService;

    @Override
    public List<ProductEntity> getList(ProductPagination productPagination) {
        QueryWrapper<ProductEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(productPagination.getCode())) {
            queryWrapper.lambda().and(t -> t.like(ProductEntity::getCode, productPagination.getCode()));
        }
        if (StringUtil.isNotEmpty(productPagination.getCustomerName())) {
            queryWrapper.lambda().and(t -> t.like(ProductEntity::getCustomerName, productPagination.getCustomerName()));
        }
        if (StringUtil.isNotEmpty(productPagination.getContactTel())) {
            queryWrapper.lambda().and(t -> t.like(ProductEntity::getContactTel, productPagination.getContactTel()));
        }
        //排序
        if (StringUtil.isEmpty(productPagination.getSidx())) {
            queryWrapper.lambda().orderByDesc(ProductEntity::getId);
        } else {
            queryWrapper = "asc".equals(productPagination.getSort().toLowerCase()) ? queryWrapper.orderByAsc(productPagination.getSidx()) : queryWrapper.orderByDesc(productPagination.getSidx());
        }
        Page<ProductEntity> page = new Page<>(productPagination.getCurrentPage(), productPagination.getPageSize());
        IPage<ProductEntity> userIPage = this.page(page, queryWrapper);
        return productPagination.setData(userIPage.getRecords(), userIPage.getTotal());
    }

    @Override
    public ProductEntity getInfo(String id) {
        QueryWrapper<ProductEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ProductEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ProductEntity entity, List<ProductEntryEntity> productEntryList) throws DataException {
        UserInfo userInfo = userProvider.get();
        String code = billRuleService.getBillNumber("OrderNumber", false);
        entity.setCode(code);
        //类型
        entity.setType("市场活动");
        //制单
        entity.setSalesmanId(userInfo.getUserId());
        entity.setSalesmanName(userInfo.getUserName());
        entity.setSalesmanDate(new Date());
        //状态
        entity.setAuditState(1);
        entity.setGoodsState(1);
        entity.setCloseState(1);
        entity.setCreatorUserId(userInfo.getUserId());
        entity.setCreatorTime(new Date());
        entity.setId(RandomUtil.uuId());
        this.save(entity);
        for (ProductEntryEntity product : productEntryList) {
            product.setId(RandomUtil.uuId());
            product.setActivity("市场部全国香风健康奢护");
            product.setType("市场活动");
            product.setUtil("支");
            product.setCommandType("唯一码");
            product.setProductId(entity.getId());
            productEntryService.save(product);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(String id, ProductEntity entity, List<ProductEntryEntity> productEntryList) {
        entity.setId(id);
        entity.setLastModifyUserId(userProvider.get().getUserId());
        entity.setLastModifyTime(new Date());
        QueryWrapper<ProductEntryEntity> entryWrapper = new QueryWrapper<>();
        entryWrapper.lambda().eq(ProductEntryEntity::getProductId, entity.getId());
        productEntryService.remove(entryWrapper);
        for (ProductEntryEntity product : productEntryList) {
            product.setId(RandomUtil.uuId());
            product.setProductId(entity.getId());
            productEntryService.save(product);
        }
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ProductEntity entity) {
        if (entity != null) {
            QueryWrapper<ProductEntryEntity> entryWrapper = new QueryWrapper<>();
            entryWrapper.lambda().eq(ProductEntryEntity::getProductId, entity.getId());
            productEntryService.remove(entryWrapper);
            this.removeById(entity.getId());
        }
    }


}