package jnpf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.entity.AppDataEntity;
import jnpf.model.AppDataListAllVO;
import jnpf.model.AppDataListVO;
import jnpf.model.AppFlowListAllVO;

import java.util.List;

/**
 * app常用数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-07-08
 */
public interface AppDataService extends IService<AppDataEntity> {

    /**
     * 列表
     *
     * @param type 类型
     * @return
     */
    List<AppDataEntity> getList(String type);

    /**
     * 列表
     *
     * @return
     */
    List<AppDataEntity> getList();

    /**
     * 信息
     *
     * @param objectId 对象主键
     * @return
     */
    AppDataEntity getInfo(String objectId);

    /**
     * 验证名称
     *
     * @param objectId 对象主键
     * @return
     */
    boolean isExistByObjectId(String objectId);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(AppDataEntity entity);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(AppDataEntity entity);

    /**
     * 流程所有应用
     * @param type 类型
     * @return
     */
    List<AppFlowListAllVO> getFlowList(String type);

    /**
     * 流程所有应用
     * @param type 类型
     * @return
     */
    List<AppDataListAllVO> getDataList(String type);

}
