package jnpf.base.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.entity.EmailConfigEntity;
import jnpf.base.entity.SysConfigEntity;
import jnpf.model.login.BaseSystemInfo;

import java.util.List;

/**
 * 系统配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface SysconfigService extends IService<SysConfigEntity> {

    /**
     * 列表
     *
     * @return
     */
    List<SysConfigEntity> getList(String type);

    /**
     * 信息
     *
     * @return
     */
    BaseSystemInfo getWeChatInfo();


    BaseSystemInfo getSysInfo();
    /**
     * 保存系统配置
     *
     * @param entitys 实体对象
     */
    void save(List<SysConfigEntity> entitys);
    /**
     * 保存公众号配置
     *
     * @param entitys 实体对象
     */
    boolean saveMp(List<SysConfigEntity> entitys);
    /**
     * 保存企业号配置
     *
     * @param entitys 实体对象
     */
    void saveQyh(List<SysConfigEntity> entitys);

    /**
     * 邮箱验证
     *
     * @param configEntity
     * @return
     */
    String checkLogin(EmailConfigEntity configEntity);
}
