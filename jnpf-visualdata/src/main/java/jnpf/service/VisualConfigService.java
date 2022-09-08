package jnpf.service;

import jnpf.entity.VisualConfigEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 大屏基本配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
public interface VisualConfigService extends IService<VisualConfigEntity> {

    /**
     * 信息
     *
     * @return
     */
    List<VisualConfigEntity> getList();

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    VisualConfigEntity getInfo(String id);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(VisualConfigEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     * @return
     */
    boolean update(String id, VisualConfigEntity entity);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(VisualConfigEntity entity);
}
