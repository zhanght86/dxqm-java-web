package jnpf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.PaginationTime;
import jnpf.entity.LogEntity;
import jnpf.model.UserLogForm;

import java.util.List;

/**
 * 系统日志
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface LogService extends IService<LogEntity> {

    /**
     * 列表
     *
     * @param userLogForm
     * @return
     */
    List<LogEntity> getList(UserLogForm userLogForm);

    /**
     * 列表
     *
     * @param category  日志分类
     * @param paginationTime 分页条件
     * @return
     */
    List<LogEntity> getList(int category, PaginationTime paginationTime);

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    LogEntity getInfo(String id);


    /**
     * 删除
     * @param ids
     * @return
     */
    boolean delete(String[] ids);

    /**
     * 写入日志
     *
     * @param userId    用户Id
     * @param userName  用户名称
     * @param abstracts 摘要
     */
    void writeLogAsync(String userId, String userName, String abstracts);

    /**
     * 请求日志
     *
     * @param logEntity 实体对象
     */
    void writeLogAsync(LogEntity logEntity);
}
