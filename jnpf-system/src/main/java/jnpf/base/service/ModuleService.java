package jnpf.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.model.module.ModuleExportModel;
import jnpf.database.exception.DataException;

import java.util.List;

/**
 * 系统功能
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface ModuleService extends IService<ModuleEntity> {



    /**
     * 列表
     *
     * @return
     */
    List<ModuleEntity> getList();



    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    ModuleEntity getInfo(String id);

    /**
     * 验证名称
     *
     * @param fullName 名称
     * @param id       主键值
     * @return
     */
    boolean isExistByFullName(String fullName, String id,String category);

    /**
     * 验证编码
     *
     * @param enCode 编码
     * @param id     主键值
     * @return
     */
    boolean isExistByEnCode(String enCode, String id,String category);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(ModuleEntity entity);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(ModuleEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     */
    boolean update(String id, ModuleEntity entity);

    /**
     * 导出数据
     * @param   id
     * @return  ModuleExportModel
     */
    ModuleExportModel exportData(String id);

    /**
     * 导入数据
     *
     * @param exportModel
     * @return
     * @throws DataException
     */
    boolean importData(ModuleExportModel exportModel) throws DataException;
}
