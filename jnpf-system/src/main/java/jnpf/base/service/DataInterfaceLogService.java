package jnpf.base.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.Pagination;
import jnpf.base.entity.DataInterfaceLogEntity;

import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
public interface DataInterfaceLogService extends IService<DataInterfaceLogEntity> {

    /**
     * 添加日志
     *
     * @param dateInterfaceId 接口Id
     * @param invokWasteTime  执行时间
     */
    void create(String dateInterfaceId, Integer invokWasteTime);

    /**
     * 获取调用日志列表
     *
     * @param invokId 接口id
     * @return
     */
    List<DataInterfaceLogEntity> getList(String invokId, Pagination pagination);

}
