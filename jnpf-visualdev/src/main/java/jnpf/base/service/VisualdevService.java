package jnpf.base.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.PaginationVisualdev;
import jnpf.exception.WorkFlowException;

import java.util.List;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
public interface VisualdevService extends IService<VisualdevEntity> {

    List<VisualdevEntity> getList(PaginationVisualdev paginationVisualdev);

    List<VisualdevEntity> getList();

    VisualdevEntity getInfo(String id);


    void create(VisualdevEntity entity);

    boolean update(String id, VisualdevEntity entity);

    void delete(VisualdevEntity entity) throws WorkFlowException;

}

