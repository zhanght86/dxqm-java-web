package jnpf.service;

import jnpf.entity.VisualDbEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.model.VisualPagination;

import java.util.List;
import java.util.Map;

/**
 * 大屏数据源配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
public interface VisualDbService extends IService<VisualDbEntity> {

    /**
     * 列表
     *
     * @param pagination 条件
     * @return
     */
    List<VisualDbEntity> getList(VisualPagination pagination);

    /**
     * 列表
     *
     * @return
     */
    List<VisualDbEntity> getList();

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    VisualDbEntity getInfo(String id);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(VisualDbEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     * @return
     */
    boolean update(String id, VisualDbEntity entity);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(VisualDbEntity entity);

    /**
     * 测试连接
     *
     * @param entity 实体对象
     * @return
     */
    boolean dbTest(VisualDbEntity entity);

    /**
     * 执行sql
     *
     * @param entity 实体对象
     * @param sql    sql
     * @return
     */
    List<Map<String,Object>> query(VisualDbEntity entity, String sql);
}
