package jnpf.scheduletask.service;

import jnpf.scheduletask.model.ContentNewModel;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
public interface TimeService {

    /**
     * 存储过程
     * @param model
     * @param id
     */
    void storage(ContentNewModel model, String id);

    /**
     * 接口
     * @param model
     * @param id
     */
    void connector(ContentNewModel model, String id,String token);
}
