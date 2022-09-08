package jnpf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.entity.ProductgoodsEntity;
import jnpf.model.productgoods.ProductgoodsPagination;

import java.util.List;

/**
 *
 * 产品商品
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 15:57:50
 */
public interface ProductgoodsService extends IService<ProductgoodsEntity> {

    List<ProductgoodsEntity> getGoodList(String type);

    List<ProductgoodsEntity> getList(ProductgoodsPagination productgoodsPagination);

    ProductgoodsEntity getInfo(String id);

    void delete(ProductgoodsEntity entity);

    void create(ProductgoodsEntity entity);

    boolean update( String id, ProductgoodsEntity entity);
    
}
