package jnpf.base.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.database.exception.DataException;
import jnpf.base.ActionResult;
import jnpf.base.entity.DataInterfaceEntity;
import jnpf.base.model.dataInterface.PaginationDataInterface;

import java.util.List;
import java.util.Map;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
public interface DataInterfaceService extends IService<DataInterfaceEntity> {
    /**
     * 获取接口列表(分页)
     */
    List<DataInterfaceEntity> getList(PaginationDataInterface pagination);

    /**
     * 获取接口列表下拉框
     * @return
     */
    List<DataInterfaceEntity> getList();

    /**
     * 获取接口数据
     */
    DataInterfaceEntity getInfo(String id);

    /**
     * 添加数据接口
     */
    void create(DataInterfaceEntity entity);

    /**
     * 修改接口
     */
    boolean update(DataInterfaceEntity entity,String id) throws DataException;

    /**
     * 删除接口
     */
    void delete(DataInterfaceEntity entity);

    /**
     * 判断接口名称是否重复
     * @param fullName
     * @param id
     * @return
     */
    boolean isExistByFullName(String fullName, String id);

    /**
     * 判断编码是否重复
     *
     * @param fullName
     * @param id
     * @return
     */
    boolean isExistByEnCode(String fullName, String id);

    /**
     * 访问接口
     */
    List<Map<String,Object>> get(String id, String sql) throws DataException;

    /**
     * 访问接口路径
     * @param id
     * @return
     */
    ActionResult infoToId(String id);

}
