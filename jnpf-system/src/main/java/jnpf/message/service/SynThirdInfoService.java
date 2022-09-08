package jnpf.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.message.entity.SynThirdInfoEntity;
import jnpf.message.util.SynThirdTotal;

import java.util.List;

/**
 * 第三方工具的公司-部门-用户同步表模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/23 17:29
 */
public interface SynThirdInfoService extends IService<SynThirdInfoEntity> {

    /**
     * 获取指定第三方工具、指定数据类型的数据列表
     * @param thirdType
     * @param dataType
     * @return
     */
    List<SynThirdInfoEntity> getList(String thirdType,String dataType);

    /**
     * 获取同步的详细信息
     * @param id
     * @return
     */
    SynThirdInfoEntity getInfo(String id);

    void create(SynThirdInfoEntity entity);

    boolean update(String id,SynThirdInfoEntity entity);

    void delete(SynThirdInfoEntity entity);

    /**
     * 获取指定第三方工具、指定数据类型、对象ID的同步信息
     * @param thirdType
     * @param dataType
     * @param id
     * @return
     */
    SynThirdInfoEntity getInfoBySysObjId(String thirdType,String dataType,String id);

    /**
     * 获取指定第三方工具、指定数据类型的同步统计信息
     * @param thirdType
     * @param dataType
     * @return
     */
    SynThirdTotal getSynTotal(String thirdType, String dataType);

}
