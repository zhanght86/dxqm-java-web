package jnpf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.Pagination;
import jnpf.entity.CustomerEntity;

import java.util.List;

/**
 *
 * 客户信息
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 14:09:05
 */
public interface CustomerService extends IService<CustomerEntity> {

    List<CustomerEntity> getList(Pagination pagination);

    CustomerEntity getInfo(String id);

    void delete(CustomerEntity entity);

    void create(CustomerEntity entity);

    boolean update( String id, CustomerEntity entity);
    
}
