package jnpf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.Pagination;
import jnpf.entity.ProductEntryEntity;

import java.util.List;

/**
 *
 * 销售订单明细
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 10:40:59
 */
public interface ProductEntryService extends IService<ProductEntryEntity> {

    List<ProductEntryEntity> getProductentryEntityList(String id);

    List<ProductEntryEntity> getProductentryEntityList(Pagination pagination);
}
