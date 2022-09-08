package jnpf.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.model.dictionarytype.DictionaryExportModel;
import jnpf.database.exception.DataException;

import java.util.List;

/**
 * 字典数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface DictionaryDataService extends IService<DictionaryDataEntity> {

    /**
     * 列表
     *
     * @return
     */
    List<DictionaryDataEntity> getList();

    /**
     * 列表
     *
     * @param dictionaryTypeId 类别主键
     * @return
     */
    List<DictionaryDataEntity> getList(String dictionaryTypeId);

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    DictionaryDataEntity getInfo(String id);

    /**
     * 验证名称
     *
     * @param dictionaryTypeId 类别主键
     * @param fullName         名称
     * @param id               主键值
     * @return
     */
    boolean isExistByFullName(String dictionaryTypeId, String fullName, String id);

    /**
     * 验证编码
     *
     * @param dictionaryTypeId 类别主键
     * @param enCode           编码
     * @param id               主键值
     * @return
     */
    boolean isExistByEnCode(String dictionaryTypeId, String enCode, String id);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(DictionaryDataEntity entity);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(DictionaryDataEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     */
    boolean update(String id, DictionaryDataEntity entity);

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
    List<DictionaryDataEntity> getDictionName(List<String> id);

    /**
     * 导出数据
     * @param   id
     * @return  DictionaryExportModel
     */
    DictionaryExportModel exportData(String id);

    /**
     * 导入数据
     * @param exportModel
     * @return
     * @throws DataException
     */
    boolean importData(DictionaryExportModel exportModel) throws DataException;

}
