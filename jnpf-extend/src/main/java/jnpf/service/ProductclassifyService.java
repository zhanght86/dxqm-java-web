package jnpf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.entity.ProductclassifyEntity;

import java.util.List;

/**
 *
 * 产品分类
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 14:34:04
 */
public interface ProductclassifyService extends IService<ProductclassifyEntity> {

    List<ProductclassifyEntity> getList();

    ProductclassifyEntity getInfo(String id);

    void delete(ProductclassifyEntity entity);

    void create(ProductclassifyEntity entity);

    boolean update( String id, ProductclassifyEntity entity);
    
}
