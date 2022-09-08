package jnpf.message.service.impl;

import com.alibaba.fastjson.JSONObject;
import jnpf.base.UserInfo;
import jnpf.base.entity.SysConfigEntity;
import jnpf.base.service.SysconfigService;
import jnpf.base.util.RegexUtils;
import jnpf.exception.WxErrorException;
import jnpf.message.entity.SynThirdInfoEntity;
import jnpf.message.model.message.QyWebChatUserModel;
import jnpf.message.service.SynThirdInfoService;
import jnpf.message.service.SynThirdQyService;
import jnpf.message.util.SynQyWebChatUtil;
import jnpf.message.util.SynThirdConsts;
import jnpf.model.login.BaseSystemInfo;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserService;
import jnpf.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本系统的公司、部门、用户与企业微信的同步
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/27 11:12
 */
@Component
public class SynThirdQyServiceImpl implements SynThirdQyService {
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private SysconfigService sysconfigService;
    @Autowired
    private SynThirdInfoService synThirdInfoService;
    @Autowired
    private UserService userService;
    @Autowired
    private PositionService positionService;

    /**
     * 获取企业微信的配置信息
     * @return
     */
    @Override
    public BaseSystemInfo getQyhConfig() {
        Map<String, String> objModel = new HashMap<>();
        List<SysConfigEntity> configList = sysconfigService.getList("SysConfig");
        for (SysConfigEntity entity : configList) {
            objModel.put(entity.getFkey(), entity.getValue());
        }
        BaseSystemInfo baseSystemInfo = JsonUtil.getJsonToBean(objModel, BaseSystemInfo.class);
        return baseSystemInfo;
    }

    //------------------------------------本系统同步公司、部门到企业微信-------------------------------------

    /**
     * 根据部门的同步表信息判断同步情况
     * 有带第三方错误定位判断的功能代码 20210604
     * @param synThirdInfoEntity
     * @param qyDeptList
     * @return
     */
//    public JSONObject checkDepartmentSysToQy(SynThirdInfoEntity synThirdInfoEntity, List<QyWebChatDeptModel> qyDeptList) {
//        JSONObject retMsg = new JSONObject();
//        retMsg.put("code",true);
//        retMsg.put("flag","");
//        retMsg.put("error","");
//
//        if(synThirdInfoEntity!=null){
//            if(StringUtil.isNotEmpty(synThirdInfoEntity.getThirdObjId())) {
//                // 同步表存在企业微信ID,仍需要判断企业微信上有没此部门
//                if(qyDeptList.stream().filter(t -> t.getId().toString().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false){
//                    retMsg.put("code",false);
//                    retMsg.put("flag","1");
//                    retMsg.put("error","企业微信不存在同步表对应的部门ID!");
//                }
//            }else{
//                // 同步表的企业微信ID为空
//                retMsg.put("code",false);
//                retMsg.put("flag","2");
//                retMsg.put("error","同步表中部门对应的企业微信ID为空!");
//            }
//        }else{
//            // 上级部门未同步
//            retMsg.put("code",false);
//            retMsg.put("flag","3");
//            retMsg.put("error","部门未同步到企业微信!");
//        }
//
//        return retMsg;
//    }

    /**
     * 根据部门的同步表信息判断同步情况
     * 不带第三方错误定位判断的功能代码 20210604
     * @param synThirdInfoEntity
     * @return
     */
    public JSONObject checkDepartmentSysToQy(SynThirdInfoEntity synThirdInfoEntity) {
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("flag","");
        retMsg.put("error","");

        if(synThirdInfoEntity!=null){
            // 同步表的企业微信ID为空
            if("".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) || "null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                retMsg.put("code",false);
                retMsg.put("flag","2");
                retMsg.put("error","同步表中部门对应的企业微信ID为空!");
            }

        }else{
            // 上级部门未同步
            retMsg.put("code",false);
            retMsg.put("flag","3");
            retMsg.put("error","部门未同步到企业微信!");
        }

        return retMsg;
    }



    /**
     * 同步到企业微信的部门名称、部门英文名称是否重复
     * 有带第三方错误定位判断的功能代码 20210604
     * @param isEnglish
     * @param name
     * @param parentId
     * @param id
     * @param qyDeptList
     * @return
     */
//    public JSONObject checkDeptObjectNameSysToQy(boolean isEnglish, String name, String parentId, String id, List<QyWebChatDeptModel> qyDeptList,
//                                                 String opType, SynThirdInfoEntity synThirdInfoEntity, Integer thirdType,
//                                                 Integer dataType, String sysObjId, String thirdObjId, String deptFlag){
//        boolean isDiff = false;
//        JSONObject retMsg = new JSONObject();
//        retMsg.put("code",true);
//        retMsg.put("error","");
//
//        // 创建时：判断同一个层级的部门名称、部门英文名称不能重复
//        if(StringUtil.isEmpty(id)){
//            isDiff = qyDeptList.stream().filter(t -> t.getName().equals(name) && t.getParentid().toString().equals(parentId)).count() > 0 ? true : false;
//        }else{
//            isDiff = qyDeptList.stream().filter(t -> t.getName().equals(name) && t.getParentid().toString().equals(parentId) && !(t.getId().toString().equals(id)) ).count() > 0 ? true : false;
//        }
//        if(isDiff){
//            // 同步失败
//            Integer synState = SynThirdConsts.SYN_STATE_FAIL;
//            String description = deptFlag + (isEnglish ? "同一层次的部门英文名称重复" : "同一层次的部门名称重复");
//
//            // 更新同步表
//            saveSynThirdInfoEntity(opType,synThirdInfoEntity,thirdType,dataType,sysObjId,thirdObjId,synState,description);
//
//            retMsg.put("code", false);
//            retMsg.put("error",description);
//        }
//        return retMsg;
//    }

    /**
     * 检查部门中文名称与英文名称是否相同
     * @param cnName
     * @param EnName
     * @param opType
     * @param synThirdInfoEntity
     * @param thirdType
     * @param dataType
     * @param sysObjId
     * @param thirdObjId
     * @param deptFlag
     * @return
     */
    public JSONObject checkCnEnName(String cnName, String EnName,
                                    String opType, SynThirdInfoEntity synThirdInfoEntity, Integer thirdType,
                                    Integer dataType, String sysObjId, String thirdObjId, String deptFlag){
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("error","");
        if(cnName.equals(EnName)){
            // 同步失败
            Integer synState = SynThirdConsts.SYN_STATE_FAIL;
            String description = deptFlag + "部门中文名称与英文名称不能相同";

            // 更新同步表
            saveSynThirdInfoEntity(opType,synThirdInfoEntity,thirdType,dataType,sysObjId,thirdObjId,synState,description);

            retMsg.put("code", false);
            retMsg.put("error", description);
        }
        return retMsg;
    }



    /**
     * 将组织、用户的信息写入同步表
     * @param opType                "add":创建 “upd”:修改
     * @param synThirdInfoEntity    本地同步表信息
     * @param thirdType             第三方类型
     * @param dataType              数据类型
     * @param sysObjId              本地对象ID
     * @param thirdObjId            第三方对象ID
     * @param synState              同步状态(0:未同步;1:同步成功;2:同步失败)
     * @param description
     */
    public void saveSynThirdInfoEntity(String opType, SynThirdInfoEntity synThirdInfoEntity, Integer thirdType,
                                                    Integer dataType, String sysObjId, String thirdObjId, Integer synState,
                                                    String description) {
        UserInfo userInfo = userProvider.get();
        SynThirdInfoEntity entity = new SynThirdInfoEntity();
        String compValue = SynThirdConsts.OBJECT_OP_ADD;
        if(compValue.equals(opType)) {
            entity.setId(RandomUtil.uuId());
            entity.setThirdtype(thirdType);
            entity.setDatatype(dataType);
            entity.setSysObjId(sysObjId);
            entity.setThirdObjId(thirdObjId);
            entity.setSynstate(synState);
            // 备注当作同步失败信息来用
            entity.setDescription(description);
            entity.setCreatorUserId(userInfo.getUserId());
            entity.setCreatorTime(DateUtil.getNowDate());
            entity.setLastModifyUserId(userInfo.getUserId());
            // 修改时间当作最后同步时间来用
            entity.setLastModifyTime(DateUtil.getNowDate());
            synThirdInfoService.create(entity);
        }else{
            entity = synThirdInfoEntity;
            entity.setThirdtype(thirdType);
            entity.setDatatype(dataType);
            entity.setThirdObjId(thirdObjId);
            entity.setSynstate(synState);
            // 备注当作同步失败信息来用
            entity.setDescription(description);
            entity.setLastModifyUserId(userInfo.getUserId());
            // 修改时间当作最后同步时间来用
            entity.setLastModifyTime(DateUtil.getNowDate());
            synThirdInfoService.update(entity.getId(), entity);
        }
    }

    /**
     * 往企业微信创建部门
     * 有带错误定位判断的功能代码 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @return
     * @throws WxErrorException
     */
//    @Override
//    public JSONObject createDepartmentSysToQy(boolean isBatch, OrganizeEntity deptEntity) throws WxErrorException {
//        BaseSystemInfo config = getQyhConfig();
//        String corpId = config.getQyhCorpId();
//        String corpSecret = config.getQyhCorpSecret();
//        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
//        // 单条记录执行时,受开关限制
//        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynOrg();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        JSONObject object = new JSONObject();
//        List<QyWebChatDeptModel> qyDeptList = new ArrayList<>();
//        String thirdObjId = "";
//        Integer synState = 0;
//        String description = "";
//        boolean isDeptDiff = true;
//        String deptFlag = "创建：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", "创建：系统未设置单条同步");
//
//        // 支持同步
//        if(qyhIsSyn==1){
//            // 获取 access_token 值
//            tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
//            access_token = tokenObject.getString("access_token");
//
//            if (access_token != null && !"".equals(access_token)) {
//                // 获取企业微信上的所有部门列表信息
//                JSONObject deptObject = SynQyWebChatUtil.getDepartmentList(SynThirdConsts.QY_ROOT_DEPT_ID,access_token);
//                if(deptObject.getBoolean("code")) {
//                    qyDeptList = JsonUtil.getJsonToList(deptObject.getString("department"), QyWebChatDeptModel.class);
//                }else{
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = deptFlag + "获取企业微信的部门列表信息失败";
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", description);
//                    retMsg.put("retDeptId", "0");
//
//                    // 更新同步表
//                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                            Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);
//
//                    return retMsg;
//                }
//
//                object.put("id", null);
//                // name:必填项,同一个层级的部门名称不能重复
//                // name_en:必填项,同一个层级的部门名称不能重复
//                // name与name_en的值不能相同，否则会报错, 20210429
//                object.put("name", deptEntity.getFullName());
//                object.put("name_en", deptEntity.getEnCode());
//                // 从本地数据库的同步表获取对应的企业微信ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
//                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
//                    //顶级节点时，企业微信的父节点设置为1
//                    object.put("parentid", 1);
//                }else{
//                    SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());
//
//                    retMsg = checkDepartmentSysToQy(synThirdInfoEntity,qyDeptList);
//                    isDeptDiff = retMsg.getBoolean("code");
//                    if(isDeptDiff) {
//                        object.put("parentid", synThirdInfoEntity.getThirdObjId());
//                    }
//                }
//                object.put("order", deptEntity.getSortCode());
//
//                // 创建时：部门中文名称与英文名称不能相同
//                retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),SynThirdConsts.OBJECT_OP_ADD,null,
//                        Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                if (!retMsg.getBoolean("code")) {
//                    return retMsg;
//                }
//
//                // 创建时：判断同一个层级的部门中文名称不能重复
//                retMsg = checkDeptObjectNameSysToQy(false,object.getString("name"),object.getString("parentid"),thirdObjId,qyDeptList,
//                        SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                        Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                if (!retMsg.getBoolean("code")) {
//                    return retMsg;
//                }
//
//                // 创建时：判断同一个层级的部门英文名称不能重复
//                retMsg = checkDeptObjectNameSysToQy(true,object.getString("name_en"),object.getString("parentid"),thirdObjId,qyDeptList,
//                        SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                        Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                if (!retMsg.getBoolean("code")) {
//                    return retMsg;
//                }
//
//                if(isDeptDiff) {
//                    if(qyhIsSyn==1) {
//                        // 往企业微信写入公司或部门
//                        retMsg = SynQyWebChatUtil.createDepartment(object.toJSONString(), access_token);
//
//                        // 往同步写入本系统与第三方的对应信息
//                        if (retMsg.getBoolean("code")) {
//                            // 同步成功
//                            thirdObjId = retMsg.getString("retDeptId");
//                            synState = SynThirdConsts.SYN_STATE_OK;
//                        } else {
//                            // 同步失败
//                            synState = SynThirdConsts.SYN_STATE_FAIL;
//                            description = deptFlag + retMsg.getString("error");
//                        }
//                    }else{
//                        // 未设置单条同步,归并到未同步状态
//                        // 未同步
//                        synState = SynThirdConsts.SYN_STATE_NO;
//                        description = deptFlag + "系统未设置单条同步";
//
//                        retMsg.put("code", true);
//                        retMsg.put("error", description);
//                        retMsg.put("retDeptId", "0");
//                    }
//                }else{
//                    // 同步失败,上级部门无对应的企业微信ID
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = deptFlag + "部门所属的上级部门未同步到企业微信";
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", description);
//                    retMsg.put("retDeptId", "0");
//                }
//            }else{
//                synState = SynThirdConsts.SYN_STATE_FAIL;
//                description = deptFlag + "access_token值为空,不能同步信息";
//
//                retMsg.put("code", false);
//                retMsg.put("error", description);
//                retMsg.put("retDeptId", "0");
//            }
//        }
//
//        // 更新同步表
//        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);
//
//        return retMsg;
//    }


    /**
     * 往企业微信更新部门
     * 有带错误定位判断的功能代码 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @return
     * @throws WxErrorException
     */
//    @Override
//    public JSONObject updateDepartmentSysToQy(boolean isBatch, OrganizeEntity deptEntity) throws WxErrorException {
//        BaseSystemInfo config = getQyhConfig();
//        String corpId = config.getQyhCorpId();
//        String corpSecret = config.getQyhCorpSecret();
//        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
//        // 单条记录执行时,受开关限制
//        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynOrg();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        JSONObject object = new JSONObject();
//        List<QyWebChatDeptModel> qyDeptList = new ArrayList<>();
//        SynThirdInfoEntity synThirdInfoEntity = new SynThirdInfoEntity();
//        String opType = "";
//        Integer synState = 0;
//        String description = "";
//        String thirdObjId = "";
//        SynThirdInfoEntity synThirdInfoPara = new SynThirdInfoEntity();
//        boolean isDeptDiff = true;
//        String deptFlag = "更新：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", "系统未设置单条同步");
//
//        // 支持同步,设置需要同步到企业微信的对象属性值
//        if(qyhIsSyn==1) {
//            // 获取 access_token
//            tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
//            access_token = tokenObject.getString("access_token");
//
//            // 获取同步表信息
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
//
//            if (access_token != null && !"".equals(access_token)) {
//                // 获取企业微信上的所有部门列表信息
//                JSONObject deptObject = SynQyWebChatUtil.getDepartmentList(SynThirdConsts.QY_ROOT_DEPT_ID,access_token);
//                if(deptObject.getBoolean("code")) {
//                    qyDeptList = JsonUtil.getJsonToList(deptObject.getString("department"), QyWebChatDeptModel.class);
//                }else{
//                    if(synThirdInfoEntity!=null){
//                        // 修改同步表
//                        opType = SynThirdConsts.OBJECT_OP_UPD;
//                        synThirdInfoPara = synThirdInfoEntity;
//                        thirdObjId = synThirdInfoEntity.getThirdObjId();
//                    }else{
//                        // 写入同步表
//                        opType = SynThirdConsts.OBJECT_OP_ADD;
//                        synThirdInfoPara = null;
//                        thirdObjId = "";
//                    }
//
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = deptFlag + "获取企业微信的部门列表信息失败";
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", description);
//
//                    // 更新同步表
//                    saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                            Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);
//
//                    return retMsg;
//                }
//
//                object.put("id", null);
//                object.put("name", deptEntity.getFullName());
//                object.put("name_en", deptEntity.getEnCode());
//                // 从本地数据库的同步表获取对应的企业微信ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
//                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
//                    //顶级节点时，企业微信的父节点设置为1
//                    object.put("parentid", 1);
//                } else {
//                    // 判断上级部门的合法性
//                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());
//                    retMsg = checkDepartmentSysToQy(synThirdInfoEntity, qyDeptList);
//                    isDeptDiff = retMsg.getBoolean("code");
//                    if (isDeptDiff) {
//                        object.put("parentid", synThirdInfoEntity.getThirdObjId());
//                    }
//                }
//                object.put("order", deptEntity.getSortCode());
//
//                // 上级部门检查是否异常
//                if(isDeptDiff){
//                    // 获取同步表信息
//                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
//
//                    // 判断当前部门对应的第三方的合法性
//                    retMsg = checkDepartmentSysToQy(synThirdInfoEntity, qyDeptList);
//                    if (!retMsg.getBoolean("code")) {
//                        if ("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))) {
//                            // flag:3 未同步，需要创建同步到企业微信、写入同步表
//                            // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到企业微信、写入同步表
//                            if("1".equals(retMsg.getString("flag"))) {
//                                synThirdInfoService.delete(synThirdInfoEntity);
//                            }
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//
//                            // 部门中文名称与英文名称不能相同
//                            retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 判断同一个层级的部门中文名称不能重复
//                            retMsg = checkDeptObjectNameSysToQy(false,object.getString("name"),object.getString("parentid"),thirdObjId,qyDeptList,
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 判断同一个层级的部门英文名称不能重复
//                            retMsg = checkDeptObjectNameSysToQy(true,object.getString("name_en"),object.getString("parentid"),thirdObjId,qyDeptList,
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 往企业微信写入公司或部门
//                            retMsg = SynQyWebChatUtil.createDepartment(object.toJSONString(), access_token);
//
//                            // 往同步写入本系统与第三方的对应信息
//                            if(retMsg.getBoolean("code")) {
//                                // 同步成功
//                                thirdObjId = retMsg.getString("retDeptId");
//                                synState = SynThirdConsts.SYN_STATE_OK;
//                                description = "";
//                            }else{
//                                // 同步失败
//                                synState = SynThirdConsts.SYN_STATE_FAIL;
//                                description = deptFlag + retMsg.getString("error");
//                            }
//                        }
//
//                        if ("2".equals(retMsg.getString("flag"))) {
//                            // flag:2 已同步但第三方ID为空，需要创建同步到企业微信、修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = "";
//
//                            // 部门中文名称与英文名称不能相同
//                            retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 判断同一个层级的部门中文名称不能重复
//                            retMsg = checkDeptObjectNameSysToQy(false,object.getString("name"),object.getString("parentid"),thirdObjId,qyDeptList,
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 判断同一个层级的部门英文名称不能重复
//                            retMsg = checkDeptObjectNameSysToQy(true,object.getString("name_en"),object.getString("parentid"),thirdObjId,qyDeptList,
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 往企业微信写入公司或部门
//                            retMsg = SynQyWebChatUtil.createDepartment(object.toJSONString(), access_token);
//
//                            // 往同步表更新本系统与第三方的对应信息
//                            if (retMsg.getBoolean("code")) {
//                                // 同步成功
//                                thirdObjId = retMsg.getString("retDeptId");
//                                synState = SynThirdConsts.SYN_STATE_OK;
//                                description = "";
//                            } else {
//                                // 同步失败
//                                synState = SynThirdConsts.SYN_STATE_FAIL;
//                                description = deptFlag + retMsg.getString("error");
//                            }
//                        }
//
//                    } else {
//                        // 更新同步表
//                        opType = SynThirdConsts.OBJECT_OP_UPD;
//                        synThirdInfoPara = synThirdInfoEntity;
//                        thirdObjId = synThirdInfoEntity.getThirdObjId();
//
//                        // 部门中文名称与英文名称不能相同
//                        retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),
//                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                        if (!retMsg.getBoolean("code")) {
//                            return retMsg;
//                        }
//
//                        // 判断同一个层级的部门中文名称不能重复
//                        retMsg = checkDeptObjectNameSysToQy(false,object.getString("name"),object.getString("parentid"),thirdObjId,qyDeptList,
//                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                        if (!retMsg.getBoolean("code")) {
//                            return retMsg;
//                        }
//
//                        // 判断同一个层级的部门英文名称不能重复
//                        retMsg = checkDeptObjectNameSysToQy(true,object.getString("name_en"),object.getString("parentid"),thirdObjId,qyDeptList,
//                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                        if (!retMsg.getBoolean("code")) {
//                            return retMsg;
//                        }
//
//                        // 往企业微信写入公司或部门
//                        object.put("id", synThirdInfoEntity.getThirdObjId());
//                        retMsg = SynQyWebChatUtil.updateDepartment(object.toJSONString(), access_token);
//
//                        // 往同步表更新本系统与第三方的对应信息
//                        if (retMsg.getBoolean("code")) {
//                            // 同步成功
//                            synState = SynThirdConsts.SYN_STATE_OK;
//                            description = "";
//                        } else {
//                            // 同步失败
//                            synState = SynThirdConsts.SYN_STATE_FAIL;
//                            description = deptFlag + retMsg.getString("error");
//                        }
//                    }
//                }else{
//                    // 同步失败,上级部门检查有异常
//                    if(synThirdInfoEntity!=null){
//                        // 修改同步表
//                        opType = SynThirdConsts.OBJECT_OP_UPD;
//                        synThirdInfoPara = synThirdInfoEntity;
//                        thirdObjId = synThirdInfoEntity.getThirdObjId();
//                    }else{
//                        // 写入同步表
//                        opType = SynThirdConsts.OBJECT_OP_ADD;
//                        synThirdInfoPara = null;
//                        thirdObjId = "";
//                    }
//
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = deptFlag + "上级部门无对应的企业微信ID";
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", description);
//                }
//
//            }else{
//                // 同步失败
//                if(synThirdInfoEntity!=null){
//                    // 修改同步表
//                    opType = SynThirdConsts.OBJECT_OP_UPD;
//                    synThirdInfoPara = synThirdInfoEntity;
//                    thirdObjId = synThirdInfoEntity.getThirdObjId();
//                }else{
//                    // 写入同步表
//                    opType = SynThirdConsts.OBJECT_OP_ADD;
//                    synThirdInfoPara = null;
//                    thirdObjId = "";
//                }
//
//                synState = SynThirdConsts.SYN_STATE_FAIL;
//                description = deptFlag + "access_token值为空,不能同步信息";
//
//                retMsg.put("code", true);
//                retMsg.put("error", description);
//            }
//
//        }else{
//            // 未设置单条同步,归并到未同步状态
//            // 获取同步表信息
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
//            if(synThirdInfoEntity!=null){
//                // 修改同步表
//                opType = SynThirdConsts.OBJECT_OP_UPD;
//                synThirdInfoPara = synThirdInfoEntity;
//                thirdObjId = synThirdInfoEntity.getThirdObjId();
//            }else{
//                // 写入同步表
//                opType = SynThirdConsts.OBJECT_OP_ADD;
//                synThirdInfoPara = null;
//                thirdObjId = "";
//            }
//
//            synState = SynThirdConsts.SYN_STATE_NO;
//            description = deptFlag + "系统未设置单条同步";
//
//            retMsg.put("code", true);
//            retMsg.put("error", description);
//        }
//
//        // 更新同步表
//        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);
//
//        return retMsg;
//    }

    /**
     * 往企业微信删除部门
     * 有带错误定位判断的功能代码 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id        本系统的公司或部门ID
     * @return
     * @throws WxErrorException
     */
//    @Override
//    public JSONObject deleteDepartmentSysToQy(boolean isBatch, String id) throws WxErrorException {
//        BaseSystemInfo config = getQyhConfig();
//        String corpId = config.getQyhCorpId();
//        String corpSecret = config.getQyhCorpSecret();
//        // 单条记录执行时,受开关限制
//        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynOrg();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        List<QyWebChatDeptModel> qyDeptList = new ArrayList<>();
//        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,id);
//        String deptFlag = "删除：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", "系统未设置单条同步");
//
//        // 支持同步
//        if(synThirdInfoEntity!=null) {
//            if(qyhIsSyn==1){
//                // 获取 access_token
//                tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
//                access_token = tokenObject.getString("access_token");
//
//                if (access_token != null && !"".equals(access_token)) {
//                    // 获取企业微信上的所有部门列表信息
//                    JSONObject deptObject = SynQyWebChatUtil.getDepartmentList(SynThirdConsts.QY_ROOT_DEPT_ID,access_token);
//                    if(deptObject.getBoolean("code")) {
//                        qyDeptList = JsonUtil.getJsonToList(deptObject.getString("department"), QyWebChatDeptModel.class);
//
//                        // 删除企业对应的部门
//                        if (qyDeptList.stream().filter(t -> t.getId().toString().equals(synThirdInfoEntity.getThirdObjId())).count() > 0 ? true : false) {
//                            retMsg = SynQyWebChatUtil.deleteDepartment(synThirdInfoEntity.getThirdObjId(), access_token);
//                            if (retMsg.getBoolean("code")) {
//                                // 同步成功,直接删除同步表记录
//                                synThirdInfoService.delete(synThirdInfoEntity);
//                            } else {
//                                // 同步失败
//                                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                        Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + retMsg.getString("error"));
//                            }
//                        }else{
//                            // 根据企业微信ID找不到相应的信息,直接删除同步表记录
//                            synThirdInfoService.delete(synThirdInfoEntity);
//                        }
//                    }else{
//                        // 同步失败,获取部门列表失败
//                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + "获取企业微信的部门列表信息失败");
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", deptFlag + "获取企业微信的部门列表信息失败");
//                    }
//
//                }else{
//                    // 同步失败
//                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                            Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + "access_token值为空,不能同步信息");
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", deptFlag + "access_token值为空,不能同步信息！");
//                }
//
//            }else{
//                // 未设置单条同步，归并到未同步状态
//                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                        Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_NO, deptFlag + "系统未设置单条同步");
//
//                retMsg.put("code", true);
//                retMsg.put("error", deptFlag + "系统未设置单条同步");
//            }
//        }
//
//        return retMsg;
//    }


    /**
     * 往企业微信创建部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     * @throws WxErrorException
     */
    @Override
    public JSONObject createDepartmentSysToQy(boolean isBatch, OrganizeEntity deptEntity,String accessToken) throws WxErrorException {
        BaseSystemInfo config = getQyhConfig();
        String corpId = config.getQyhCorpId();
        String corpSecret = config.getQyhCorpSecret();
        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
        // 单条记录执行时,受开关限制
        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        JSONObject object = new JSONObject();
        String thirdObjId = "";
        Integer synState = 0;
        String description = "";
        boolean isDeptDiff = true;
        String deptFlag = "创建：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "创建：系统未设置单条同步");

        // 支持同步
        if(qyhIsSyn==1){
            if(isBatch){
                access_token = accessToken;
            }else{
                // 获取 access_token 值
                tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            if (access_token != null && !"".equals(access_token)) {
                object.put("id", null);
                // name:必填项,同一个层级的部门名称不能重复
                // name_en:必填项,同一个层级的部门名称不能重复
                // name与name_en的值不能相同，否则会报错, 20210429
                object.put("name", deptEntity.getFullName());
                object.put("name_en", deptEntity.getEnCode());
                // 从本地数据库的同步表获取对应的企业微信ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
                    //顶级节点时，企业微信的父节点设置为1
                    object.put("parentid", 1);
                }else{
                    SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());

                    retMsg = checkDepartmentSysToQy(synThirdInfoEntity);
                    isDeptDiff = retMsg.getBoolean("code");
                    if(isDeptDiff) {
                        object.put("parentid", synThirdInfoEntity.getThirdObjId());
                    }
                }
                object.put("order", deptEntity.getSortCode());

                // 创建时：部门中文名称与英文名称不能相同
                retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),SynThirdConsts.OBJECT_OP_ADD,null,
                        Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                if (!retMsg.getBoolean("code")) {
                    return retMsg;
                }

                if(isDeptDiff) {
                    if(qyhIsSyn==1) {
                        // 往企业微信写入公司或部门
                        retMsg = SynQyWebChatUtil.createDepartment(object.toJSONString(), access_token);

                        // 往同步写入本系统与第三方的对应信息
                        if (retMsg.getBoolean("code")) {
                            // 同步成功
                            thirdObjId = retMsg.getString("retDeptId");
                            synState = SynThirdConsts.SYN_STATE_OK;
                        } else {
                            // 同步失败
                            synState = SynThirdConsts.SYN_STATE_FAIL;
                            description = deptFlag + retMsg.getString("error");
                        }
                    }else{
                        // 未设置单条同步,归并到未同步状态
                        // 未同步
                        synState = SynThirdConsts.SYN_STATE_NO;
                        description = deptFlag + "系统未设置单条同步";

                        retMsg.put("code", true);
                        retMsg.put("error", description);
                        retMsg.put("retDeptId", "0");
                    }
                }else{
                    // 同步失败,上级部门无对应的企业微信ID
                    synState = SynThirdConsts.SYN_STATE_FAIL;
                    description = deptFlag + "部门所属的上级部门未同步到企业微信";

                    retMsg.put("code", false);
                    retMsg.put("error", description);
                    retMsg.put("retDeptId", "0");
                }
            }else{
                synState = SynThirdConsts.SYN_STATE_FAIL;
                description = deptFlag + "access_token值为空,不能同步信息";

                retMsg.put("code", false);
                retMsg.put("error", description);
                retMsg.put("retDeptId", "0");
            }
        }

        // 更新同步表
        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }


    /**
     * 往企业微信更新部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     * @throws WxErrorException
     */
    @Override
    public JSONObject updateDepartmentSysToQy(boolean isBatch, OrganizeEntity deptEntity,String accessToken) throws WxErrorException {
        BaseSystemInfo config = getQyhConfig();
        String corpId = config.getQyhCorpId();
        String corpSecret = config.getQyhCorpSecret();
        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
        // 单条记录执行时,受开关限制
        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        JSONObject object = new JSONObject();
        SynThirdInfoEntity synThirdInfoEntity = new SynThirdInfoEntity();
        String opType = "";
        Integer synState = 0;
        String description = "";
        String thirdObjId = "";
        SynThirdInfoEntity synThirdInfoPara = new SynThirdInfoEntity();
        boolean isDeptDiff = true;
        String deptFlag = "更新：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "系统未设置单条同步");

        // 支持同步,设置需要同步到企业微信的对象属性值
        if(qyhIsSyn==1) {
            if(isBatch){
                access_token = accessToken;
            }else{
                // 获取 access_token
                tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());

            if (access_token != null && !"".equals(access_token)) {
                object.put("id", null);
                object.put("name", deptEntity.getFullName());
                object.put("name_en", deptEntity.getEnCode());
                // 从本地数据库的同步表获取对应的企业微信ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
                    //顶级节点时，企业微信的父节点设置为1
                    object.put("parentid", 1);
                } else {
                    // 判断上级部门的合法性
                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());
                    retMsg = checkDepartmentSysToQy(synThirdInfoEntity);
                    isDeptDiff = retMsg.getBoolean("code");
                    if (isDeptDiff) {
                        object.put("parentid", synThirdInfoEntity.getThirdObjId());
                    }
                }
                object.put("order", deptEntity.getSortCode());

                // 上级部门检查是否异常
                if(isDeptDiff){
                    // 获取同步表信息
                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());

                    // 判断当前部门对应的第三方的合法性
                    retMsg = checkDepartmentSysToQy(synThirdInfoEntity);
                    if (!retMsg.getBoolean("code")) {
                        if ("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))) {
                            // flag:3 未同步，需要创建同步到企业微信、写入同步表
                            // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到企业微信、写入同步表
                            if("1".equals(retMsg.getString("flag"))) {
                                synThirdInfoService.delete(synThirdInfoEntity);
                            }
                            opType = SynThirdConsts.OBJECT_OP_ADD;
                            synThirdInfoPara = null;
                            thirdObjId = "";

                            // 部门中文名称与英文名称不能相同
                            retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),
                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                            if (!retMsg.getBoolean("code")) {
                                return retMsg;
                            }

                            // 往企业微信写入公司或部门
                            retMsg = SynQyWebChatUtil.createDepartment(object.toJSONString(), access_token);

                            // 往同步写入本系统与第三方的对应信息
                            if(retMsg.getBoolean("code")) {
                                // 同步成功
                                thirdObjId = retMsg.getString("retDeptId");
                                synState = SynThirdConsts.SYN_STATE_OK;
                                description = "";
                            }else{
                                // 同步失败
                                synState = SynThirdConsts.SYN_STATE_FAIL;
                                description = deptFlag + retMsg.getString("error");
                            }
                        }

                        if ("2".equals(retMsg.getString("flag"))) {
                            // flag:2 已同步但第三方ID为空，需要创建同步到企业微信、修改同步表
                            opType = SynThirdConsts.OBJECT_OP_UPD;
                            synThirdInfoPara = synThirdInfoEntity;
                            thirdObjId = "";

                            // 部门中文名称与英文名称不能相同
                            retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),
                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                            if (!retMsg.getBoolean("code")) {
                                return retMsg;
                            }

                            // 往企业微信写入公司或部门
                            retMsg = SynQyWebChatUtil.createDepartment(object.toJSONString(), access_token);

                            // 往同步表更新本系统与第三方的对应信息
                            if (retMsg.getBoolean("code")) {
                                // 同步成功
                                thirdObjId = retMsg.getString("retDeptId");
                                synState = SynThirdConsts.SYN_STATE_OK;
                                description = "";
                            } else {
                                // 同步失败
                                synState = SynThirdConsts.SYN_STATE_FAIL;
                                description = deptFlag + retMsg.getString("error");
                            }
                        }

                    } else {
                        // 更新同步表
                        opType = SynThirdConsts.OBJECT_OP_UPD;
                        synThirdInfoPara = synThirdInfoEntity;
                        thirdObjId = synThirdInfoEntity.getThirdObjId();

                        // 部门中文名称与英文名称不能相同
                        retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),
                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                        if (!retMsg.getBoolean("code")) {
                            return retMsg;
                        }

                        // 往企业微信写入公司或部门
                        object.put("id", synThirdInfoEntity.getThirdObjId());
                        retMsg = SynQyWebChatUtil.updateDepartment(object.toJSONString(), access_token);

                        // 往同步表更新本系统与第三方的对应信息
                        if (retMsg.getBoolean("code")) {
                            // 同步成功
                            synState = SynThirdConsts.SYN_STATE_OK;
                            description = "";
                        } else {
                            // 同步失败
                            synState = SynThirdConsts.SYN_STATE_FAIL;
                            description = deptFlag + retMsg.getString("error");
                        }
                    }
                }else{
                    // 同步失败,上级部门检查有异常
                    if(synThirdInfoEntity!=null){
                        // 修改同步表
                        opType = SynThirdConsts.OBJECT_OP_UPD;
                        synThirdInfoPara = synThirdInfoEntity;
                        thirdObjId = synThirdInfoEntity.getThirdObjId();
                    }else{
                        // 写入同步表
                        opType = SynThirdConsts.OBJECT_OP_ADD;
                        synThirdInfoPara = null;
                        thirdObjId = "";
                    }

                    synState = SynThirdConsts.SYN_STATE_FAIL;
                    description = deptFlag + "上级部门无对应的企业微信ID";

                    retMsg.put("code", false);
                    retMsg.put("error", description);
                }

            }else{
                // 同步失败
                if(synThirdInfoEntity!=null){
                    // 修改同步表
                    opType = SynThirdConsts.OBJECT_OP_UPD;
                    synThirdInfoPara = synThirdInfoEntity;
                    thirdObjId = synThirdInfoEntity.getThirdObjId();
                }else{
                    // 写入同步表
                    opType = SynThirdConsts.OBJECT_OP_ADD;
                    synThirdInfoPara = null;
                    thirdObjId = "";
                }

                synState = SynThirdConsts.SYN_STATE_FAIL;
                description = deptFlag + "access_token值为空,不能同步信息";

                retMsg.put("code", true);
                retMsg.put("error", description);
            }

        }else{
            // 未设置单条同步,归并到未同步状态
            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
            if(synThirdInfoEntity!=null){
                // 修改同步表
                opType = SynThirdConsts.OBJECT_OP_UPD;
                synThirdInfoPara = synThirdInfoEntity;
                thirdObjId = synThirdInfoEntity.getThirdObjId();
            }else{
                // 写入同步表
                opType = SynThirdConsts.OBJECT_OP_ADD;
                synThirdInfoPara = null;
                thirdObjId = "";
            }

            synState = SynThirdConsts.SYN_STATE_NO;
            description = deptFlag + "系统未设置单条同步";

            retMsg.put("code", true);
            retMsg.put("error", description);
        }

        // 更新同步表
        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }

    /**
     * 往企业微信删除部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id        本系统的公司或部门ID
     * @param accessToken (单条调用时为空)
     * @return
     * @throws WxErrorException
     */
    @Override
    public JSONObject deleteDepartmentSysToQy(boolean isBatch, String id,String accessToken) throws WxErrorException {
        BaseSystemInfo config = getQyhConfig();
        String corpId = config.getQyhCorpId();
        String corpSecret = config.getQyhCorpSecret();
        // 单条记录执行时,受开关限制
        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,id);
        String deptFlag = "删除：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "系统未设置单条同步");

        // 支持同步
        if(synThirdInfoEntity!=null) {
            if(qyhIsSyn==1){
                if(isBatch){
                    access_token = accessToken;
                }else{
                    // 获取 access_token
                    tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
                    access_token = tokenObject.getString("access_token");
                }

                if (access_token != null && !"".equals(access_token)) {
                    if (!"".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) && !"null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                        retMsg = SynQyWebChatUtil.deleteDepartment(synThirdInfoEntity.getThirdObjId(), access_token);
                        if (retMsg.getBoolean("code")) {
                            // 同步成功,直接删除同步表记录
                            synThirdInfoService.delete(synThirdInfoEntity);
                        } else {
                            // 同步失败
                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + retMsg.getString("error"));
                        }
                    }else{
                        // 根据企业微信ID找不到相应的信息,直接删除同步表记录
                        synThirdInfoService.delete(synThirdInfoEntity);
                    }
                }else{
                    // 同步失败
                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                            Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + "access_token值为空,不能同步信息");

                    retMsg.put("code", false);
                    retMsg.put("error", deptFlag + "access_token值为空,不能同步信息！");
                }

            }else{
                // 未设置单条同步，归并到未同步状态
                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                        Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_NO, deptFlag + "系统未设置单条同步");

                retMsg.put("code", true);
                retMsg.put("error", deptFlag + "系统未设置单条同步");
            }
        }

        return retMsg;
    }


    //------------------------------------本系统同步用户到企业微信-------------------------------------

    /**
     * 获取企业微信的单个成员列表，用于更新成员信息使用
     * @param id
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public QyWebChatUserModel getQyUserById(String id, String accessToken) throws WxErrorException {
        QyWebChatUserModel userModel = new QyWebChatUserModel();
        JSONObject userObject = SynQyWebChatUtil.getUserById(id,accessToken);
        if(userObject.getBoolean("code")) {
            userModel = JsonUtil.getJsonToBean(userObject.getString("userinfo"), QyWebChatUserModel.class);
        }
        return userModel;
    }

    /**
     * 设置需要提交给企业微信接口的单个成品JSON信息
     * 有带第三方错误定位判断的功能代码 20210604
     * @param userEntity 本地用户信息
     * @param qyDeptList 企业微信的部门信息
     * @param qyWebChatUserModel
     * @return
     */
//    public JSONObject setQyUserObject(UserEntity userEntity, List<QyWebChatDeptModel> qyDeptList, QyWebChatUserModel qyWebChatUserModel) {
//        List<UserEntity> userList = userService.getList();
//        JSONObject object = new JSONObject();
//        JSONObject retMsg = new JSONObject();
//        retMsg.put("code", true);
//        retMsg.put("error", "");
//
//        // 验证邮箱格式的合法性
//        if(StringUtil.isNotEmpty(userEntity.getEmail())){
//            if(!RegexUtils.checkEmail(userEntity.getEmail())){
//                retMsg.put("code", false);
//                retMsg.put("error", "邮箱格式不合法！");
//                retMsg.put("qyUserObject", "");
//                return retMsg;
//            }
//        }
//
//        object.put("userid", userEntity.getId());
//        object.put("name", userEntity.getRealName());
//        object.put("mobile",userEntity.getMobilePhone());
//
//        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,userEntity.getOrganizeId());
//        retMsg = checkDepartmentSysToQy(synThirdInfoEntity,qyDeptList);
//        if(retMsg.getBoolean("code")){
//            String formatString = "[%s]";
//            object.put("department",String.format(formatString,synThirdInfoEntity.getThirdObjId()));
//            object.put("main_department",synThirdInfoEntity.getThirdObjId());
//            QyWebChatDeptModel qyWebChatDeptModel = qyDeptList.stream().filter(t -> t.getId().toString().equals(synThirdInfoEntity.getThirdObjId())).findFirst().orElse(null);
//            object.put("order",String.format(formatString,qyWebChatDeptModel.getOrder()));
//            String isLeader = userList.stream().filter(t -> t.getOrganizeId().equals(userEntity.getOrganizeId()) && t.getManagerId().equals(userEntity.getId()) ).count()==0 ? "0" : "1";
//            object.put("is_leader_in_dept",String.format(formatString,isLeader));
//        }else{
//            retMsg.put("code", false);
//            retMsg.put("error", "部门找不到对应的企业微信ID！");
//            retMsg.put("qyUserObject", "");
//            return retMsg;
//        }
//        object.put("email",userEntity.getEmail());
//        PositionEntity positionEntity = positionService.getInfo(userEntity.getPositionId());
//        if(positionEntity!=null){
//            object.put("position",positionEntity.getFullName());
//        }else{
//            object.put("position","");
//        }
//        object.put("gender",userEntity.getGender().toString());
//        object.put("telephone",userEntity.getTelePhone());
//        object.put("enable",userEntity.getEnabledMark());
//        JSONObject extattr = new JSONObject();
//        extattr.put("attrs","[]");
//        object.put("extattr",extattr.toJSONString());
////        // 创建企业微信成员时才会赋值的
////        object.put("to_invite", true);
//        object.put("address",userEntity.getPostalAddress());
//        object.put("alias","");
//        object.put("avatar_mediaid","");
//        JSONObject external_profile = new JSONObject();
//        external_profile.put("external_corp_name","");
//        external_profile.put("external_attr","[]");
//        object.put("external_profile",external_profile.toJSONString());
//        object.put("external_position","");
//
//        // 修改时:未更新字段信息来源企业微信
//        if(qyWebChatUserModel!=null) {
//            object.put("alias", qyWebChatUserModel.getAlias());
//            object.put("avatar_mediaid", qyWebChatUserModel.getAvatar_mediaid());
//            object.put("external_profile", qyWebChatUserModel.getExternal_profile());
//            object.put("external_position", qyWebChatUserModel.getExternal_position());
//        }
//
//        String jsonString = object.toJSONString();
//        // 格式与用户的格式不一致就需要做处理，否则提交JSON格式验证无法通过
//        jsonString = jsonString.replaceAll("\\\\","");
//        jsonString = jsonString.replaceAll("\"\\{","{");
//        jsonString = jsonString.replaceAll("}\"","}");
//        jsonString = jsonString.replaceAll("\"\\[","[");
//        jsonString = jsonString.replaceAll("\\]\"","]");
//
//        retMsg.put("qyUserObject", jsonString);
//        return retMsg;
//    }


    /**
     * 设置需要提交给企业微信接口的单个成品JSON信息
     * 不带第三方错误定位判断的功能代码 20210604
     * @param userEntity 本地用户信息
     * @param qyWebChatUserModel
     * @return
     */
    public JSONObject setQyUserObject(UserEntity userEntity,QyWebChatUserModel qyWebChatUserModel) {
        List<UserEntity> userList = userService.getList();
        JSONObject object = new JSONObject();
        JSONObject retMsg = new JSONObject();
        retMsg.put("code", true);
        retMsg.put("error", "");

        // 验证邮箱格式的合法性
        if(StringUtil.isNotEmpty(userEntity.getEmail())){
            if(!RegexUtils.checkEmail(userEntity.getEmail())){
                retMsg.put("code", false);
                retMsg.put("error", "邮箱格式不合法！");
                retMsg.put("qyUserObject", "");
                return retMsg;
            }
        }

        object.put("userid", userEntity.getId());
        object.put("name", userEntity.getRealName());
        object.put("mobile",userEntity.getMobilePhone());

        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,userEntity.getOrganizeId());
        retMsg = checkDepartmentSysToQy(synThirdInfoEntity);
        if(retMsg.getBoolean("code")){
            String formatString = "[%s]";
            object.put("department",String.format(formatString,synThirdInfoEntity.getThirdObjId()));
            object.put("main_department",synThirdInfoEntity.getThirdObjId());
//            QyWebChatDeptModel qyWebChatDeptModel = qyDeptList.stream().filter(t -> t.getId().toString().equals(synThirdInfoEntity.getThirdObjId())).findFirst().orElse(null);
//            object.put("order",String.format(formatString,qyWebChatDeptModel.getOrder()));
            String isLeader = userList.stream().filter(t -> t.getOrganizeId().equals(userEntity.getOrganizeId()) && t.getManagerId().equals(userEntity.getId()) ).count()==0 ? "0" : "1";
            object.put("is_leader_in_dept",String.format(formatString,isLeader));
        }else{
            retMsg.put("code", false);
            retMsg.put("error", "部门找不到对应的企业微信ID！");
            retMsg.put("qyUserObject", "");
            return retMsg;
        }
        object.put("email",userEntity.getEmail());
        PositionEntity positionEntity = positionService.getInfo(userEntity.getPositionId());
        if(positionEntity!=null){
            object.put("position",positionEntity.getFullName());
        }else{
            object.put("position","");
        }
        object.put("gender",userEntity.getGender().toString());
        object.put("telephone",userEntity.getTelePhone());
        object.put("enable",userEntity.getEnabledMark());
        JSONObject extattr = new JSONObject();
        extattr.put("attrs","[]");
        object.put("extattr",extattr.toJSONString());
//        // 创建企业微信成员时才会赋值的
//        object.put("to_invite", true);
        object.put("address",userEntity.getPostalAddress());
        object.put("alias","");
        object.put("avatar_mediaid","");
        JSONObject external_profile = new JSONObject();
        external_profile.put("external_corp_name","");
        external_profile.put("external_attr","[]");
        object.put("external_profile",external_profile.toJSONString());
        object.put("external_position","");

        // 修改时:未更新字段信息来源企业微信
        if(qyWebChatUserModel!=null) {
            object.put("alias", qyWebChatUserModel.getAlias());
            object.put("avatar_mediaid", qyWebChatUserModel.getAvatar_mediaid());
            object.put("external_profile", qyWebChatUserModel.getExternal_profile());
            object.put("external_position", qyWebChatUserModel.getExternal_position());
        }

        String jsonString = object.toJSONString();
        // 格式与用户的格式不一致就需要做处理，否则提交JSON格式验证无法通过
        jsonString = jsonString.replaceAll("\\\\","");
        jsonString = jsonString.replaceAll("\"\\{","{");
        jsonString = jsonString.replaceAll("}\"","}");
        jsonString = jsonString.replaceAll("\"\\[","[");
        jsonString = jsonString.replaceAll("\\]\"","]");

        retMsg.put("qyUserObject", jsonString);
        return retMsg;
    }


    /**
     * 判断用户的手机号、邮箱是否唯一，企业微信不允许重复
     * @param mobile
     * @param email
     * @param userId
     * @param qyUserList
     * @param opType
     * @param synThirdInfoEntity
     * @param thirdType
     * @param dataType
     * @param sysObjId
     * @param thirdObjId
     * @param deptFlag
     * @return
     */
    public JSONObject checkUserMobileEmailRepeat(String mobile, String email, String userId, List<QyWebChatUserModel> qyUserList,
                                                 String opType, SynThirdInfoEntity synThirdInfoEntity, Integer thirdType,
                                                 Integer dataType, String sysObjId, String thirdObjId, String deptFlag){
        boolean isDiff = true;
        String description = "";
        JSONObject retMsg = new JSONObject();

        // 企业微信限制：手机号唯一性
        if(StringUtil.isNotEmpty(mobile)){
            if(StringUtil.isNotEmpty(userId)){
                if(qyUserList.stream().filter(t -> String.valueOf(t.getMobile()).equals(mobile) && !(t.getUserid().equals(userId))).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "企业内已有绑定手机号:" + mobile;
                }
            }else{
                if(qyUserList.stream().filter(t -> String.valueOf(t.getMobile()).equals(mobile)).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "企业内已有绑定手机号:" + mobile;
                }
            }
        }

        // 企业微信限制：邮箱地址唯一性
        if(StringUtil.isNotEmpty(email)){
            if(StringUtil.isNotEmpty(userId)){
                if(qyUserList.stream().filter(t -> String.valueOf(t.getEmail()).equals(email) && !(t.getUserid().equals(userId))).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "企业内已有绑定此邮箱:" + email;
                }
            }else{
                if(qyUserList.stream().filter(t -> String.valueOf(t.getEmail()).equals(email)).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "企业内已有绑定此邮箱:" + email;
                }
            }
        }

        retMsg.put("code",isDiff);
        retMsg.put("error",description);

        if(!isDiff){
            // 同步失败
            Integer synState = SynThirdConsts.SYN_STATE_FAIL;

            // 更新同步表
            saveSynThirdInfoEntity(opType,synThirdInfoEntity,thirdType,dataType,sysObjId,thirdObjId,synState,description);
        }

        return retMsg;
    }

    /**
     * 根据用户的同步表信息判断同步情况
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param synThirdInfoEntity
     * @param qyUserList
     * @return
     */
//    public JSONObject checkUserSysToQy(SynThirdInfoEntity synThirdInfoEntity, List<QyWebChatUserModel> qyUserList) {
//        JSONObject retMsg = new JSONObject();
//        retMsg.put("code",true);
//        retMsg.put("flag","");
//        retMsg.put("error","");
//
//        if(synThirdInfoEntity!=null){
//            if(StringUtil.isNotEmpty(synThirdInfoEntity.getThirdObjId())) {
//                // 同步表存在企业微信ID,仍需要判断企业微信上有没此用户
//                if(qyUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false){
//                    retMsg.put("code",false);
//                    retMsg.put("flag","1");
//                    retMsg.put("error","企业微信不存在同步表对应的用户ID!");
//                }
//            }else{
//                // 同步表的企业微信ID为空
//                retMsg.put("code",false);
//                retMsg.put("flag","2");
//                retMsg.put("error","同步表中用户对应的第三方ID为空!");
//            }
//        }else{
//            // 上级用户未同步
//            retMsg.put("code",false);
//            retMsg.put("flag","3");
//            retMsg.put("error","用户未同步!");
//        }
//
//        return retMsg;
//    }

    /**
     * 根据用户的同步表信息判断同步情况
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param synThirdInfoEntity
     * @return
     */
    public JSONObject checkUserSysToQy(SynThirdInfoEntity synThirdInfoEntity) {
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("flag","");
        retMsg.put("error","");

        if(synThirdInfoEntity!=null){
            if("".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) || "null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                // 同步表的企业微信ID为空
                retMsg.put("code",false);
                retMsg.put("flag","2");
                retMsg.put("error","同步表中用户对应的第三方ID为空!");
            }
        }else{
            // 上级用户未同步
            retMsg.put("code",false);
            retMsg.put("flag","3");
            retMsg.put("error","用户未同步!");
        }

        return retMsg;
    }


    /**
     * 往企业微信创建成员信息
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @return
     * @throws WxErrorException
     */
//    @Override
//    public JSONObject createUserSysToQy(boolean isBatch, UserEntity userEntity) throws WxErrorException {
//        BaseSystemInfo config = getQyhConfig();
//        String corpId = config.getQyhCorpId();
//        String corpSecret = config.getQyhCorpSecret();
//        // 单条记录执行时,受开关限制
//        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynUser();
//        JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
//        String access_token = tokenObject.getString("access_token");
//        JSONObject retMsg = new JSONObject();
//        String userObjectModel = "";
//        List<QyWebChatDeptModel> qyDeptList = new ArrayList<>();
//        List<QyWebChatUserModel> qyUserList = new ArrayList<>();
//        String thirdObjId = "";
//        Integer synState = 0;
//        String description = "";
//        String userFlag = "创建：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", userFlag + "系统未设置单条同步");
//
//        // 企业微信限制：不能手机号、邮箱同时为空
//        if(StringUtil.isEmpty(userEntity.getMobilePhone()) && StringUtil.isEmpty(userEntity.getEmail()) && "1".equals(qyhIsSyn)){
//            retMsg.put("code", false);
//            retMsg.put("error", userFlag + "企业微信不允许手机号、邮箱不能同时为空！");
//        }
//
//        if (qyhIsSyn==1){
//            if(retMsg.getBoolean("code")){
//                if (access_token != null && !"".equals(access_token)) {
//                    // 获取企业微信上的所有部门列表信息
//                    JSONObject deptObject = SynQyWebChatUtil.getDepartmentList(SynThirdConsts.QY_ROOT_DEPT_ID,access_token);
//                    if(deptObject.getBoolean("code")) {
//                        qyDeptList = JsonUtil.getJsonToList(deptObject.getString("department"), QyWebChatDeptModel.class);
//                    }else{
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取企业微信的部门列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//
//                    // 获取企业微信上的所有成员列表信息
//                    JSONObject userObject = SynQyWebChatUtil.getUserDetailList(SynThirdConsts.QY_ROOT_DEPT_ID,"1",access_token);
//                    if(userObject.getBoolean("code")) {
//                        qyUserList = JsonUtil.getJsonToList(userObject.getString("userlist"), QyWebChatUserModel.class);
//                    }else{
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取企业微信的成员列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//
//                    // 判断用户的手机号、邮箱是否唯一,不能重复
//                    retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,qyUserList,
//                            SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                            Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                    if (!retMsg.getBoolean("code")) {
//                        return retMsg;
//                    }
//
//                    // 要同步到企业微信的对象赋值
//                    retMsg = setQyUserObject(userEntity, qyDeptList, null);
//                    if (retMsg.getBoolean("code")) {
//                        userObjectModel = retMsg.getString("qyUserObject");
//                        // 往企业微信写入成员
//                        retMsg = SynQyWebChatUtil.createUser(userObjectModel, access_token);
//
//                        // 往同步写入本系统与第三方的对应信息
//                        if (retMsg.getBoolean("code")) {
//                            // 同步成功
//                            thirdObjId = userEntity.getId();
//                            synState = SynThirdConsts.SYN_STATE_OK;
//                        } else {
//                            // 同步失败
//                            synState = SynThirdConsts.SYN_STATE_FAIL;
//                            description = userFlag + retMsg.getString("error");
//                        }
//                    }else{
//                        // 同步失败,原因：部门找不到对应的第三方ID、邮箱格式不合法
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + retMsg.getString("error");
//                    }
//
//                }else{
//                    // 同步失败
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = userFlag + "access_token值为空,不能同步信息";
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", description);
//                }
//
//            }else {
//                // 同步失败,原因：企业微信不允许手机号、邮箱不能同时为空
//                synState = SynThirdConsts.SYN_STATE_FAIL;
//                description = userFlag + retMsg.getString("error");
//            }
//        }else{
//            // 无须同步，未同步状态
//            synState = SynThirdConsts.SYN_STATE_NO;
//            description = userFlag + "系统未设置单条同步";
//
//            retMsg.put("code", true);
//            retMsg.put("error", description);
//        }
//
//        // 更新同步表
//        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//        return retMsg;
//    }


    /**
     * 往企业微信更新成员信息
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @return
     * @throws WxErrorException
     */
//    @Override
//    public JSONObject updateUserSysToQy(boolean isBatch, UserEntity userEntity) throws WxErrorException {
//        BaseSystemInfo config = getQyhConfig();
//        String corpId = config.getQyhCorpId();
//        String corpSecret = config.getQyhCorpSecret();
//        // 单条记录执行时,受开关限制
//        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynUser();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        String userObjectModel = "";
//        List<QyWebChatDeptModel> qyDeptList = new ArrayList<>();
//        List<QyWebChatUserModel> qyUserList = new ArrayList<>();
//        SynThirdInfoEntity synThirdInfoEntity = new SynThirdInfoEntity();
//        SynThirdInfoEntity entity = new SynThirdInfoEntity();
//        String opType = "";
//        SynThirdInfoEntity synThirdInfoPara = new SynThirdInfoEntity();
//        String thirdObjId = "";
//        Integer synState = 0;
//        String description = "";
//        String userFlag = "更新：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", userFlag + "系统未设置单条同步");
//
//        // 企业微信限制：不能手机号、邮箱同时为空
//        if(StringUtil.isEmpty(userEntity.getMobilePhone()) && StringUtil.isEmpty(userEntity.getEmail()) && "1".equals(qyhIsSyn)){
//            retMsg.put("code", false);
//            retMsg.put("error", userFlag + "企业微信不允许手机号、邮箱不能同时为空！");
//        }
//
//        // 支持同步
//        if (qyhIsSyn==1){
//            if(retMsg.getBoolean("code")){
//                // 获取 access_token
//                tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
//                access_token = tokenObject.getString("access_token");
//
//                // 获取同步表信息
//                synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
//                if (access_token != null && !"".equals(access_token)) {
//                    // 获取企业微信上的所有部门列表信息
//                    JSONObject deptObject = SynQyWebChatUtil.getDepartmentList(SynThirdConsts.QY_ROOT_DEPT_ID,access_token);
//                    if(deptObject.getBoolean("code")) {
//                        qyDeptList = JsonUtil.getJsonToList(deptObject.getString("department"), QyWebChatDeptModel.class);
//                    }else{
//                        if(synThirdInfoEntity!=null){
//                            // 修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = synThirdInfoEntity.getThirdObjId();
//                        }else{
//                            // 写入同步表
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//                        }
//
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取企业微信的部门列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,synState,description);
//
//                        return retMsg;
//                    }
//
//                    // 获取企业微信上的所有成员列表信息
//                    JSONObject userObject = SynQyWebChatUtil.getUserDetailList(SynThirdConsts.QY_ROOT_DEPT_ID,"1",access_token);
//                    if(userObject.getBoolean("code")) {
//                        qyUserList = JsonUtil.getJsonToList(userObject.getString("userlist"), QyWebChatUserModel.class);
//                    }else{
//                        if(synThirdInfoEntity!=null){
//                            // 修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = synThirdInfoEntity.getThirdObjId();
//                        }else{
//                            // 写入同步表
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//                        }
//
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取企业微信的成员列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,synState,description);
//
//                        return retMsg;
//                    }
//
//                    // 要同步到企业微信的对象赋值
//                    retMsg = setQyUserObject(userEntity,qyDeptList,null);
//                    if (retMsg.getBoolean("code")) {
//                        // 判断当前用户对应的第三方的合法性
//                        userObjectModel = retMsg.getString("qyUserObject");
//                        retMsg = checkUserSysToQy(synThirdInfoEntity, qyUserList);
//                        if (!retMsg.getBoolean("code")) {
//                            if("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))){
//                                // flag:3 未同步，需要创建同步到企业微信、写入同步表
//                                // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到企业微信、写入同步表
//                                if("1".equals(retMsg.getString("flag"))) {
//                                    synThirdInfoService.delete(synThirdInfoEntity);
//                                }
//                                opType = SynThirdConsts.OBJECT_OP_ADD;
//                                synThirdInfoPara = null;
//                                thirdObjId = "";
//
//                                // 判断用户的手机号、邮箱是否唯一,不能重复
//                                retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,qyUserList,
//                                        opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                                if (!retMsg.getBoolean("code")) {
//                                    return retMsg;
//                                }
//
//                                // 往企业微信写入成员
//                                retMsg = SynQyWebChatUtil.createUser(userObjectModel, access_token);
//                                if(retMsg.getBoolean("code")) {
//                                    // 同步成功
//                                    thirdObjId = userEntity.getId();
//                                    synState = SynThirdConsts.SYN_STATE_OK;
//                                    description = "";
//                                }else{
//                                    // 同步失败
//                                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                                    description = userFlag + retMsg.getString("error");
//                                }
//                            }
//
//                            if("2".equals(retMsg.getString("flag"))){
//                                // 已同步但第三方ID为空，需要创建同步到企业微信、修改同步表
//                                opType = SynThirdConsts.OBJECT_OP_UPD;
//                                synThirdInfoPara = synThirdInfoEntity;
//                                thirdObjId = "";
//
//                                // 判断用户的手机号、邮箱是否唯一,不能重复
//                                retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,qyUserList,
//                                        opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                                if (!retMsg.getBoolean("code")) {
//                                    return retMsg;
//                                }
//
//                                // 往企业微信写入成员
//                                retMsg = SynQyWebChatUtil.createUser(userObjectModel, access_token);
//                                if(retMsg.getBoolean("code")) {
//                                    // 同步成功
//                                    thirdObjId = userEntity.getId();
//                                    synState = SynThirdConsts.SYN_STATE_OK;
//                                    description = "";
//                                }else{
//                                    // 同步失败
//                                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                                    description = userFlag + retMsg.getString("error");
//                                }
//                            }
//                        }else{
//                            // 更新同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = synThirdInfoEntity.getThirdObjId();
//
//                            // 判断用户的手机号、邮箱是否唯一,不能重复
//                            retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,qyUserList,
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 获取当前成员信息
//                            QyWebChatUserModel qyWebChatUserModel = getQyUserById(synThirdInfoEntity.getThirdObjId(),access_token);
//                            if("0".equals(qyWebChatUserModel.getErrcode())){
//                                // 要同步到企业微信的对象重新赋值
//                                retMsg = setQyUserObject(userEntity,qyDeptList,qyWebChatUserModel);
//                                userObjectModel = retMsg.getString("qyUserObject");
//
//                                // 往企业微信更新成员信息
//                                retMsg = SynQyWebChatUtil.updateUser(userObjectModel, access_token);
//                                if(retMsg.getBoolean("code")) {
//                                    // 同步成功
//                                    synState = SynThirdConsts.SYN_STATE_OK;
//                                    description = "";
//                                }else{
//                                    // 同步失败
//                                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                                    description = userFlag + retMsg.getString("error");
//                                }
//                            }else{
//                                // 同步失败,获取企业微信当前用户信息失败
//                                synState = SynThirdConsts.SYN_STATE_FAIL;
//                                description = userFlag + "获取企业微信当前用户信息失败";
//                            }
//                        }
//                    }else{
//                        // 同步失败,原因：用户所属部门找不到相应的企业微信ID、邮箱格式不合法
//                        if(synThirdInfoEntity!=null){
//                            // 修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = synThirdInfoEntity.getThirdObjId();
//                        }else{
//                            // 写入同步表
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//                        }
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + retMsg.getString("error");
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//                    }
//                }else{
//                    // 同步失败
//                    if(synThirdInfoEntity!=null){
//                        // 修改同步表
//                        opType = SynThirdConsts.OBJECT_OP_UPD;
//                        synThirdInfoPara = synThirdInfoEntity;
//                        thirdObjId = synThirdInfoEntity.getThirdObjId();
//                    }else{
//                        // 写入同步表
//                        opType = SynThirdConsts.OBJECT_OP_ADD;
//                        synThirdInfoPara = null;
//                        thirdObjId = "";
//                    }
//
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = userFlag + "access_token值为空,不能同步信息";
//
//                    retMsg.put("code", true);
//                    retMsg.put("error", description);
//                }
//            }else {
//                // 同步失败,原因：企业微信不允许手机号、邮箱不能同时为空;
//                // 获取同步表信息
//                synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
//                if(synThirdInfoEntity!=null){
//                    // 修改同步表
//                    opType = SynThirdConsts.OBJECT_OP_UPD;
//                    synThirdInfoPara = synThirdInfoEntity;
//                    thirdObjId = synThirdInfoEntity.getThirdObjId();
//                }else{
//                    // 写入同步表
//                    opType = SynThirdConsts.OBJECT_OP_ADD;
//                    synThirdInfoPara = null;
//                    thirdObjId = "";
//                }
//                synState = SynThirdConsts.SYN_STATE_FAIL;
//                description = userFlag + retMsg.getString("error");
//
//                retMsg.put("code", false);
//                retMsg.put("error", description);
//            }
//        }else{
//            // 未设置单条同步,归并到未同步状态
//            // 获取同步表信息
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
//            if(synThirdInfoEntity!=null){
//                // 修改同步表
//                opType = SynThirdConsts.OBJECT_OP_UPD;
//                synThirdInfoPara = synThirdInfoEntity;
//                thirdObjId = synThirdInfoEntity.getThirdObjId();
//            }else{
//                // 写入同步表
//                opType = SynThirdConsts.OBJECT_OP_ADD;
//                synThirdInfoPara = null;
//                thirdObjId = "";
//            }
//
//            synState = SynThirdConsts.SYN_STATE_NO;
//            description = userFlag + "系统未设置单条同步";
//
//            retMsg.put("code", true);
//            retMsg.put("error", description);
//        }
//
//        // 更新同步表
//        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,synState,description);
//
//        return retMsg;
//    }


    /**
     * 往企业微信删除成员信息
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id   本系统的公司或部门ID
     * @return
     * @throws WxErrorException
     */
//    @Override
//    public JSONObject deleteUserSysToQy(boolean isBatch, String id) throws WxErrorException {
//        BaseSystemInfo config = getQyhConfig();
//        String corpId = config.getQyhCorpId();
//        String corpSecret = config.getQyhCorpSecret();
//        // 单条记录执行时,受开关限制
//        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynUser();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        List<QyWebChatUserModel> qyUserList = new ArrayList<>();
//        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER,id);
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", "系统未设置单条同步");
//
//        // 支持同步
//        if(synThirdInfoEntity!=null) {
//            if(qyhIsSyn==1) {
//                // 获取 access_token
//                tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
//                access_token = tokenObject.getString("access_token");
//                if (access_token != null && !"".equals(access_token)) {
//                    // 获取企业微信上的所有成员信息列表
//                    JSONObject userObject = SynQyWebChatUtil.getUserList(SynThirdConsts.QY_ROOT_DEPT_ID,"1",access_token);
//                    if(userObject.getBoolean("code")) {
//                        qyUserList = JsonUtil.getJsonToList(userObject.getString("userlist"), QyWebChatUserModel.class);
//
//                        // 删除企业对应的用户
//                        if(qyUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() > 0 ? true : false){
//                            retMsg = SynQyWebChatUtil.deleteUser(synThirdInfoEntity.getThirdObjId(), access_token);
//                            if (retMsg.getBoolean("code")) {
//                                // 同步成功,直接删除同步表记录
//                                synThirdInfoService.delete(synThirdInfoEntity);
//                            }else{
//                                // 同步失败
//                                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, retMsg.getString("error"));
//                            }
//                        }else{
//                            // 根据企业微信ID找不到相应的信息,直接删除同步表记录
//                            synThirdInfoService.delete(synThirdInfoEntity);
//                        }
//                    }else{
//                        // 同步失败，获取企业微信的成员列表信息失败
//                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "获取企业微信的成员列表信息失败");
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", "获取企业微信的成员列表信息失败");
//                    }
//                }else{
//                    // 同步失败
//                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                            Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "access_token值为空,不能同步信息");
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", "access_token值为空,不能同步信息！");
//                }
//            }else{
//                // 未设置单条同步，归并到未同步状态
//                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
//                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_NO, "系统未设置同步");
//
//                retMsg.put("code", true);
//                retMsg.put("error", "系统未设置单条同步");
//            }
//        }
//
//        return retMsg;
//    }


    /**
     * 往企业微信创建成员信息
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param accessToken (单条调用时为空)
     * @return
     * @throws WxErrorException
     */
    @Override
    public JSONObject createUserSysToQy(boolean isBatch, UserEntity userEntity,String accessToken) throws WxErrorException {
        BaseSystemInfo config = getQyhConfig();
        String corpId = config.getQyhCorpId();
        String corpSecret = config.getQyhCorpSecret();
        // 单条记录执行时,受开关限制
        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynUser();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        String userObjectModel = "";
        String thirdObjId = "";
        Integer synState = 0;
        String description = "";
        String userFlag = "创建：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", userFlag + "系统未设置单条同步");

        // 企业微信限制：不能手机号、邮箱同时为空
        if(StringUtil.isEmpty(userEntity.getMobilePhone()) && StringUtil.isEmpty(userEntity.getEmail()) && "1".equals(qyhIsSyn)){
            retMsg.put("code", false);
            retMsg.put("error", userFlag + "企业微信不允许手机号、邮箱不能同时为空！");
        }

        if (qyhIsSyn==1){
            if(retMsg.getBoolean("code")){
                if(isBatch){
                    access_token = accessToken;
                }else{
                    // 获取 access_token
                    tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
                    access_token = tokenObject.getString("access_token");
                }

                if (access_token != null && !"".equals(access_token)) {
                    // 要同步到企业微信的对象赋值
                    retMsg = setQyUserObject(userEntity, null);
                    if (retMsg.getBoolean("code")) {
                        userObjectModel = retMsg.getString("qyUserObject");
                        // 往企业微信写入成员
                        retMsg = SynQyWebChatUtil.createUser(userObjectModel, access_token);

                        // 往同步写入本系统与第三方的对应信息
                        if (retMsg.getBoolean("code")) {
                            // 同步成功
                            thirdObjId = userEntity.getId();
                            synState = SynThirdConsts.SYN_STATE_OK;
                        } else {
                            // 同步失败
                            synState = SynThirdConsts.SYN_STATE_FAIL;
                            description = userFlag + retMsg.getString("error");
                        }
                    }else{
                        // 同步失败,原因：部门找不到对应的第三方ID、邮箱格式不合法
                        synState = SynThirdConsts.SYN_STATE_FAIL;
                        description = userFlag + retMsg.getString("error");
                    }

                }else{
                    // 同步失败
                    synState = SynThirdConsts.SYN_STATE_FAIL;
                    description = userFlag + "access_token值为空,不能同步信息";

                    retMsg.put("code", false);
                    retMsg.put("error", description);
                }

            }else {
                // 同步失败,原因：企业微信不允许手机号、邮箱不能同时为空
                synState = SynThirdConsts.SYN_STATE_FAIL;
                description = userFlag + retMsg.getString("error");
            }
        }else{
            // 无须同步，未同步状态
            synState = SynThirdConsts.SYN_STATE_NO;
            description = userFlag + "系统未设置单条同步";

            retMsg.put("code", true);
            retMsg.put("error", description);
        }

        // 更新同步表
        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);

        return retMsg;
    }


    /**
     * 往企业微信更新成员信息
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param accessToken (单条调用时为空)
     * @return
     * @throws WxErrorException
     */
    @Override
    public JSONObject updateUserSysToQy(boolean isBatch, UserEntity userEntity,String accessToken) throws WxErrorException {
        BaseSystemInfo config = getQyhConfig();
        String corpId = config.getQyhCorpId();
        String corpSecret = config.getQyhCorpSecret();
        // 单条记录执行时,受开关限制
        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynUser();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        String userObjectModel = "";
        SynThirdInfoEntity synThirdInfoEntity = new SynThirdInfoEntity();
        SynThirdInfoEntity entity = new SynThirdInfoEntity();
        String opType = "";
        SynThirdInfoEntity synThirdInfoPara = new SynThirdInfoEntity();
        String thirdObjId = "";
        Integer synState = 0;
        String description = "";
        String userFlag = "更新：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", userFlag + "系统未设置单条同步");

        // 企业微信限制：不能手机号、邮箱同时为空
        if(StringUtil.isEmpty(userEntity.getMobilePhone()) && StringUtil.isEmpty(userEntity.getEmail()) && "1".equals(qyhIsSyn)){
            retMsg.put("code", false);
            retMsg.put("error", userFlag + "企业微信不允许手机号、邮箱不能同时为空！");
        }

        // 支持同步
        if (qyhIsSyn==1){
            if(retMsg.getBoolean("code")){
                // 获取 access_token
                if(isBatch){
                    access_token = accessToken;
                }else{
                    tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
                    access_token = tokenObject.getString("access_token");
                }

                // 获取同步表信息
                synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
                if (access_token != null && !"".equals(access_token)) {
                    // 要同步到企业微信的对象赋值
                    retMsg = setQyUserObject(userEntity,null);
                    if (retMsg.getBoolean("code")) {
                        // 判断当前用户对应的第三方的合法性
                        userObjectModel = retMsg.getString("qyUserObject");
                        retMsg = checkUserSysToQy(synThirdInfoEntity);
                        if (!retMsg.getBoolean("code")) {
                            if("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))){
                                // flag:3 未同步，需要创建同步到企业微信、写入同步表
                                // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到企业微信、写入同步表
                                if("1".equals(retMsg.getString("flag"))) {
                                    synThirdInfoService.delete(synThirdInfoEntity);
                                }
                                opType = SynThirdConsts.OBJECT_OP_ADD;
                                synThirdInfoPara = null;
                                thirdObjId = "";

                                // 往企业微信写入成员
                                retMsg = SynQyWebChatUtil.createUser(userObjectModel, access_token);
                                if(retMsg.getBoolean("code")) {
                                    // 同步成功
                                    thirdObjId = userEntity.getId();
                                    synState = SynThirdConsts.SYN_STATE_OK;
                                    description = "";
                                }else{
                                    // 同步失败
                                    synState = SynThirdConsts.SYN_STATE_FAIL;
                                    description = userFlag + retMsg.getString("error");
                                }
                            }

                            if("2".equals(retMsg.getString("flag"))){
                                // 已同步但第三方ID为空，需要创建同步到企业微信、修改同步表
                                opType = SynThirdConsts.OBJECT_OP_UPD;
                                synThirdInfoPara = synThirdInfoEntity;
                                thirdObjId = "";

                                // 往企业微信写入成员
                                retMsg = SynQyWebChatUtil.createUser(userObjectModel, access_token);
                                if(retMsg.getBoolean("code")) {
                                    // 同步成功
                                    thirdObjId = userEntity.getId();
                                    synState = SynThirdConsts.SYN_STATE_OK;
                                    description = "";
                                }else{
                                    // 同步失败
                                    synState = SynThirdConsts.SYN_STATE_FAIL;
                                    description = userFlag + retMsg.getString("error");
                                }
                            }
                        }else{
                            // 更新同步表
                            opType = SynThirdConsts.OBJECT_OP_UPD;
                            synThirdInfoPara = synThirdInfoEntity;
                            thirdObjId = synThirdInfoEntity.getThirdObjId();

                            // 获取当前成员信息
                            QyWebChatUserModel qyWebChatUserModel = getQyUserById(synThirdInfoEntity.getThirdObjId(),access_token);
                            if("0".equals(qyWebChatUserModel.getErrcode())){
                                // 要同步到企业微信的对象重新赋值
                                retMsg = setQyUserObject(userEntity,qyWebChatUserModel);
                                userObjectModel = retMsg.getString("qyUserObject");

                                // 往企业微信更新成员信息
                                retMsg = SynQyWebChatUtil.updateUser(userObjectModel, access_token);
                                if(retMsg.getBoolean("code")) {
                                    // 同步成功
                                    synState = SynThirdConsts.SYN_STATE_OK;
                                    description = "";
                                }else{
                                    // 同步失败
                                    synState = SynThirdConsts.SYN_STATE_FAIL;
                                    description = userFlag + retMsg.getString("error");
                                }
                            }else{
                                // 同步失败,获取企业微信当前用户信息失败
                                synState = SynThirdConsts.SYN_STATE_FAIL;
                                description = userFlag + "获取企业微信当前用户信息失败";
                            }
                        }
                    }else{
                        // 同步失败,原因：用户所属部门找不到相应的企业微信ID、邮箱格式不合法
                        if(synThirdInfoEntity!=null){
                            // 修改同步表
                            opType = SynThirdConsts.OBJECT_OP_UPD;
                            synThirdInfoPara = synThirdInfoEntity;
                            thirdObjId = synThirdInfoEntity.getThirdObjId();
                        }else{
                            // 写入同步表
                            opType = SynThirdConsts.OBJECT_OP_ADD;
                            synThirdInfoPara = null;
                            thirdObjId = "";
                        }
                        synState = SynThirdConsts.SYN_STATE_FAIL;
                        description = userFlag + retMsg.getString("error");

                        retMsg.put("code", false);
                        retMsg.put("error", description);
                    }
                }else{
                    // 同步失败
                    if(synThirdInfoEntity!=null){
                        // 修改同步表
                        opType = SynThirdConsts.OBJECT_OP_UPD;
                        synThirdInfoPara = synThirdInfoEntity;
                        thirdObjId = synThirdInfoEntity.getThirdObjId();
                    }else{
                        // 写入同步表
                        opType = SynThirdConsts.OBJECT_OP_ADD;
                        synThirdInfoPara = null;
                        thirdObjId = "";
                    }

                    synState = SynThirdConsts.SYN_STATE_FAIL;
                    description = userFlag + "access_token值为空,不能同步信息";

                    retMsg.put("code", true);
                    retMsg.put("error", description);
                }
            }else {
                // 同步失败,原因：企业微信不允许手机号、邮箱不能同时为空;
                // 获取同步表信息
                synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
                if(synThirdInfoEntity!=null){
                    // 修改同步表
                    opType = SynThirdConsts.OBJECT_OP_UPD;
                    synThirdInfoPara = synThirdInfoEntity;
                    thirdObjId = synThirdInfoEntity.getThirdObjId();
                }else{
                    // 写入同步表
                    opType = SynThirdConsts.OBJECT_OP_ADD;
                    synThirdInfoPara = null;
                    thirdObjId = "";
                }
                synState = SynThirdConsts.SYN_STATE_FAIL;
                description = userFlag + retMsg.getString("error");

                retMsg.put("code", false);
                retMsg.put("error", description);
            }
        }else{
            // 未设置单条同步,归并到未同步状态
            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
            if(synThirdInfoEntity!=null){
                // 修改同步表
                opType = SynThirdConsts.OBJECT_OP_UPD;
                synThirdInfoPara = synThirdInfoEntity;
                thirdObjId = synThirdInfoEntity.getThirdObjId();
            }else{
                // 写入同步表
                opType = SynThirdConsts.OBJECT_OP_ADD;
                synThirdInfoPara = null;
                thirdObjId = "";
            }

            synState = SynThirdConsts.SYN_STATE_NO;
            description = userFlag + "系统未设置单条同步";

            retMsg.put("code", true);
            retMsg.put("error", description);
        }

        // 更新同步表
        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }


    /**
     * 往企业微信删除成员信息
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id   本系统的公司或部门ID
     * @param accessToken (单条调用时为空)
     * @return
     * @throws WxErrorException
     */
    @Override
    public JSONObject deleteUserSysToQy(boolean isBatch, String id,String accessToken) throws WxErrorException {
        BaseSystemInfo config = getQyhConfig();
        String corpId = config.getQyhCorpId();
        String corpSecret = config.getQyhCorpSecret();
        // 单条记录执行时,受开关限制
        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynUser();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER,id);

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "系统未设置单条同步");

        // 支持同步
        if(synThirdInfoEntity!=null) {
            if(qyhIsSyn==1) {
                // 获取 access_token
                if(isBatch){
                    access_token = accessToken;
                }else{
                    tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
                    access_token = tokenObject.getString("access_token");
                }


                if (access_token != null && !"".equals(access_token)) {
                    if (!"".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) && !"null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                        retMsg = SynQyWebChatUtil.deleteUser(synThirdInfoEntity.getThirdObjId(), access_token);
                        if (retMsg.getBoolean("code")) {
                            // 同步成功,直接删除同步表记录
                            synThirdInfoService.delete(synThirdInfoEntity);
                        }else{
                            // 同步失败
                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, retMsg.getString("error"));
                        }
                    }else{
                        // 根据企业微信ID找不到相应的信息,直接删除同步表记录
                        synThirdInfoService.delete(synThirdInfoEntity);
                    }

                }else{
                    // 同步失败
                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                            Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "access_token值为空,不能同步信息");

                    retMsg.put("code", false);
                    retMsg.put("error", "access_token值为空,不能同步信息！");
                }
            }else{
                // 未设置单条同步，归并到未同步状态
                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_NO, "系统未设置同步");

                retMsg.put("code", true);
                retMsg.put("error", "系统未设置单条同步");
            }
        }

        return retMsg;
    }

}
