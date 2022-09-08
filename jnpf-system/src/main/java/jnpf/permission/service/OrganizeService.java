package jnpf.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.permission.entity.OrganizeEntity;

import java.util.List;

/**
 * 组织机构
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public interface OrganizeService extends IService<OrganizeEntity> {

    /**
     * 列表
     *
     * @return
     */
    List<OrganizeEntity> getList();

    /**
     * 获取redis存储的部门信息
     *
     * @return
     */
     List<OrganizeEntity> getOrgRedisList();

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    OrganizeEntity getInfo(String id);

    /**
     * 通过组织名称获取组织信息
     *
     * @param fullName 主键值
     * @return
     */
    OrganizeEntity getInfoByFullName(String fullName);

    /**
     * 验证名称
     *
     * @param entity
     * @param isCheck  组织名称是否不分级判断
     * @param isFilter  是否需要过滤id
     * @return
     */
    boolean isExistByFullName(OrganizeEntity entity, boolean isCheck, boolean isFilter);

    /**
     * 验证编码
     *
     * @param entity
     * @param isCheck  组织名称是否不分级判断
     * @param isFilter  是否需要过滤id
     * @return
     */
    boolean isExistByEnCode(OrganizeEntity entity, boolean isCheck, boolean isFilter);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(OrganizeEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     */
    boolean update(String id, OrganizeEntity entity);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(OrganizeEntity entity);

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
     * 判断是否允许删除
     *
     * @param id 主键值
     * @return
     */
    boolean allowdelete(String id);

    /**
     * 获取名称
     * @return
     */
    List<OrganizeEntity> getOrganizeName(List<String> id);

}
