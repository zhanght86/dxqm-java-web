package jnpf.service;

import jnpf.entity.VisualConfigEntity;
import jnpf.entity.VisualEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.database.exception.DataException;
import jnpf.model.visual.VisualPaginationModel;

import java.util.*;

/**
 * 大屏基本信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
public interface VisualService extends IService<VisualEntity> {

    /**
     * 列表
     *
     * @param pagination 条件
     * @return
     */
    List<VisualEntity> getList(VisualPaginationModel pagination);

    /**
     * 列表
     *
     * @return
     */
    List<VisualEntity> getList();

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    VisualEntity getInfo(String id);

    /**
     * 创建
     *
     * @param entity       实体对象
     * @param configEntity 配置属性
     */
    void create(VisualEntity entity, VisualConfigEntity configEntity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     * @param configEntity 配置属性
     * @return
     */
    boolean update(String id, VisualEntity entity, VisualConfigEntity configEntity);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(VisualEntity entity);

    /**
     * 创建
     *
     * @param entity       实体对象
     * @param configEntity 配置属性
     */
    void createInport(VisualEntity entity, VisualConfigEntity configEntity) throws DataException;

}
