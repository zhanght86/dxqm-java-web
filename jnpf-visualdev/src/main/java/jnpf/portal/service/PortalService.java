package jnpf.portal.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.portal.entity.PortalEntity;
import jnpf.portal.model.PortalPagination;
import jnpf.portal.model.PortalSelectModel;

import java.util.List;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
public interface PortalService extends IService<PortalEntity> {

    List<PortalEntity> getList(PortalPagination pagination);

    List<PortalEntity> getList();

    PortalEntity getInfo(String id);

    void create(PortalEntity entity);

    boolean update(String id, PortalEntity entity);

    void delete(PortalEntity entity);

    List<PortalSelectModel> getList(String type);

}
