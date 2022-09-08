package jnpf.message.service;

import com.alibaba.fastjson.JSONObject;
import jnpf.message.model.message.DingTalkDeptModel;
import jnpf.message.model.message.DingTalkUserModel;
import jnpf.model.login.BaseSystemInfo;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.UserEntity;

import java.text.ParseException;
import java.util.List;

/**
 * 钉钉组织-部门-用户的同步业务
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/5/7 8:42
 */
public interface SynThirdDingTalkService {

    /**
     * 获取钉钉的配置信息
     * @return
     */
    BaseSystemInfo getDingTalkConfig();


    //------------------------------------本系统同步公司、部门到钉钉-------------------------------------

    /**
     * 本地同步单个公司或部门到钉钉(供调用)
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param dingDeptListPara 单条执行时为null
     * @return
     */
//    JSONObject createDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity, List<DingTalkDeptModel> dingDeptListPara);


    /**
     * 本地更新单个公司或部门到钉钉(供调用)
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param dingDeptListPara 单条执行时为null
     * @return
     */
//    JSONObject updateDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity, List<DingTalkDeptModel> dingDeptListPara);


    /**
     * 本地删除单个公司或部门，同步到钉钉(供调用)
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id        本系统的公司或部门ID
     * @param dingDeptListPara 单条执行时为null
     * @return
     */
//    JSONObject deleteDepartmentSysToDing(boolean isBatch, String id, List<DingTalkDeptModel> dingDeptListPara);



    /**
     * 本地同步单个公司或部门到钉钉(供调用)
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    JSONObject createDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity,String accessToken);


    /**
     * 本地更新单个公司或部门到钉钉(供调用)
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    JSONObject updateDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity,String accessToken);


    /**
     * 本地删除单个公司或部门，同步到钉钉(供调用)
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id        本系统的公司或部门ID
     * @param accessToken (单条调用时为空)
     * @return
     */
    JSONObject deleteDepartmentSysToDing(boolean isBatch, String id,String accessToken);



    //------------------------------------本系统同步用户到钉钉-------------------------------------

    /**
     * 本地用户创建同步到钉钉的用户(单个)
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param dingDeptListPara 单条执行时为null
     * @param dingUserListPara 单条执行时为null
     * @return
     */
//    JSONObject createUserSysToDing(boolean isBatch, UserEntity userEntity, List<DingTalkDeptModel> dingDeptListPara,
//                                   List<DingTalkUserModel> dingUserListPara) throws ParseException;


    /**
     * 本地更新用户信息或部门到钉钉的成员用户(单个)
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param dingDeptListPara 单条执行时为null
     * @param dingUserListPara 单条执行时为null
     * @return
     */
//    JSONObject updateUserSysToDing(boolean isBatch, UserEntity userEntity, List<DingTalkDeptModel> dingDeptListPara,
//                                   List<DingTalkUserModel> dingUserListPara) throws ParseException;


    /**
     * 本地删除单个用户，同步到钉钉用户
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id   本系统的公司或部门ID
     * @param dingDeptListPara 单条执行时为null
     * @param dingUserListPara 单条执行时为null
     * @return
     */
//    JSONObject deleteUserSysToDing(boolean isBatch, String id, List<DingTalkDeptModel> dingDeptListPara,
//                                   List<DingTalkUserModel> dingUserListPara);



    /**
     * 本地用户创建同步到钉钉的用户(单个)
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    JSONObject createUserSysToDing(boolean isBatch, UserEntity userEntity,String accessToken) throws ParseException;


    /**
     * 本地更新用户信息或部门到钉钉的成员用户(单个)
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    JSONObject updateUserSysToDing(boolean isBatch, UserEntity userEntity,String accessToken) throws ParseException;


    /**
     * 本地删除单个用户，同步到钉钉用户
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id   本系统的公司或部门ID
     * @param accessToken (单条调用时为空)
     * @return
     */
    JSONObject deleteUserSysToDing(boolean isBatch, String id,String accessToken);

}
