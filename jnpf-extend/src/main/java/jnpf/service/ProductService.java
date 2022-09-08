package jnpf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.entity.ProductEntity;
import jnpf.entity.ProductEntryEntity;
import jnpf.database.exception.DataException;
import jnpf.model.product.ProductPagination;

import java.util.List;

/**
 * 销售订单
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 10:40:59
 */
public interface ProductService extends IService<ProductEntity> {

    List<ProductEntity> getList(ProductPagination productPagination);

    ProductEntity getInfo(String id);

    void delete(ProductEntity entity);

    void create(ProductEntity entity, List<ProductEntryEntity> productEntryList ) throws DataException;

    boolean update(String id, ProductEntity entity, List<ProductEntryEntity> productEntryList );

}
