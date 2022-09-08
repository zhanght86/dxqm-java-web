package jnpf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.Pagination;
import jnpf.entity.ContractEntity;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 9:47
 */
public interface ContractService extends IService<ContractEntity> {

    List<ContractEntity> getlist(Pagination pagination);

    ContractEntity getInfo(String id);

    void create(ContractEntity entity);

    void update(String id, ContractEntity entity);

    void delete(ContractEntity entity);
}
