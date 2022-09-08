package jnpf.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.database.exception.DataException;
import jnpf.base.Pagination;
import jnpf.base.entity.BillRuleEntity;

import java.util.List;

/**
 * 单据规则
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface BillRuleService extends IService<BillRuleEntity> {

    /**
     * 列表
     *
     * @param pagination 条件
     * @return
     */
    List<BillRuleEntity> getList(Pagination pagination);

    /**
     * 列表
     *
     * @return
     */
    List<BillRuleEntity> getList();

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    BillRuleEntity getInfo(String id);

    /**
     * 验证名称
     *
     * @param fullName 名称
     * @param id       主键值
     * @return
     */
    boolean isExistByFullName(String fullName, String id);

    /**
     * 验证编码
     *
     * @param enCode 编码
     * @param id     主键值
     * @return
     */
    boolean isExistByEnCode(String enCode, String id);

    /**
     * 获取流水号
     *
     * @param enCode 流水编码
     * @return
     */
    String getNumber(String enCode) throws DataException;

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(BillRuleEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     */
    boolean update(String id, BillRuleEntity entity);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(BillRuleEntity entity);

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
     * 获取单据流水号
     *
     * @param enCode  流水编码
     * @param isCache 是否缓存：每个用户会自动占用一个流水号，这个刷新页面也不会跳号
     * @return
     */
    String getBillNumber(String enCode, boolean isCache) throws DataException;

    /**
     * 使用单据流水号（注意：必须是缓存的单据才可以调用这个方法，否则无效）
     *
     * @param enCode 流水编码
     */
    void useBillNumber(String enCode);
}
