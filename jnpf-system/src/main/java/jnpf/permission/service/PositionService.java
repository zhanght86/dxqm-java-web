package jnpf.permission.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.model.position.PaginationPosition;

import java.util.List;

/**
 * 岗位信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public interface PositionService extends IService<PositionEntity> {

    /**
     * 列表
     *
     * @return
     */
    List<PositionEntity> getList();

    /**
     * 获取redis存储的岗位信息
     *
     * @return
     */
    List<PositionEntity> getPosRedisList();

    /**
     * 列表
     *
     * @param  paginationPosition 条件
     * @return
     */
    List<PositionEntity> getList(PaginationPosition paginationPosition);

    /**
     * 列表
     * @param userId 用户主键
     * @return
     */
    List<PositionEntity> getListByUserId(String userId);

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    PositionEntity getInfo(String id);

    /**
     * 验证名称
     *
     * @param entity
     * @param isFilter 是否过滤
     * @return
     */
    boolean isExistByFullName(PositionEntity entity, boolean isFilter);

    /**
     * 验证编码
     *
     * @param entity
     * @param isFilter 是否过滤
     * @return
     */
    boolean isExistByEnCode(PositionEntity entity, boolean isFilter);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(PositionEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     */
    boolean update(String id, PositionEntity entity);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(PositionEntity entity);

    /**
     * 上移
     *
     * @param id 主键值
     */
    boolean first(String id);

    /**
     * 下移
     *
     * @param id 主键值
     */
    boolean next(String id);

    /**
     * 获取名称
     * @return
     */
    List<PositionEntity> getPositionName(List<String> id);
}
