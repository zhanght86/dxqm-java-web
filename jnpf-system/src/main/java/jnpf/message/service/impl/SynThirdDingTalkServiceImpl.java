package jnpf.message.service.impl;

import com.alibaba.fastjson.JSONObject;
import jnpf.base.UserInfo;
import jnpf.base.entity.SysConfigEntity;
import jnpf.base.service.SysconfigService;
import jnpf.base.util.RegexUtils;
import jnpf.message.entity.SynThirdInfoEntity;
import jnpf.message.model.message.DingTalkDeptModel;
import jnpf.message.model.message.DingTalkUserModel;
import jnpf.message.service.SynThirdDingTalkService;
import jnpf.message.service.SynThirdInfoService;
import jnpf.message.util.SynDingTalkUtil;
import jnpf.message.util.SynThirdConsts;
import jnpf.model.login.BaseSystemInfo;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.PositionService;
import jnpf.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 本系统的公司-部门-用户同步到钉钉的功能代码
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/5/7 8:42
 */
@Component
public class SynThirdDingTalkServiceImpl implements SynThirdDingTalkService {
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private PositionService positionService;
    @Autowired
    private SysconfigService sysconfigService;
    @Autowired
    private SynThirdInfoService synThirdInfoService;


    /**
     * 获取钉钉的配置信息
     * @return
     */
    @Override
    public BaseSystemInfo getDingTalkConfig() {
        Map<String, String> objModel = new HashMap<>();
        List<SysConfigEntity> configList = sysconfigService.getList("SysConfig");
        for (SysConfigEntity entity : configList) {
            objModel.put(entity.getFkey(), entity.getValue());
        }
        BaseSystemInfo baseSystemInfo = JsonUtil.getJsonToBean(objModel, BaseSystemInfo.class);
        return baseSystemInfo;
    }

    //------------------------------------本系统同步公司、部门到钉钉-------------------------------------

    /**
     * 根据部门的同步表信息判断同步情况
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param synThirdInfoEntity
     * @param dingDeptList
     * @return
     */
//    public JSONObject checkDepartmentSysToDing(SynThirdInfoEntity synThirdInfoEntity, List<DingTalkDeptModel> dingDeptList) {
//        JSONObject retMsg = new JSONObject();
//        retMsg.put("code",true);
//        retMsg.put("flag","");
//        retMsg.put("error","");
//
//        if(synThirdInfoEntity!=null){
//            if(StringUtil.isNotEmpty(synThirdInfoEntity.getThirdObjId())) {
//                // 同步表存在钉钉ID,仍需要判断钉钉上有没此部门
//                if(dingDeptList.stream().filter(t -> t.getDeptId().toString().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false){
//                    retMsg.put("code",false);
//                    retMsg.put("flag","1");
//                    retMsg.put("error","钉钉不存在同步表对应的部门ID!");
//                }
//            }else{
//                // 同步表的钉钉ID为空
//                retMsg.put("code",false);
//                retMsg.put("flag","2");
//                retMsg.put("error","同步表中部门对应的钉钉ID为空!");
//            }
//        }else{
//            // 上级部门未同步
//            retMsg.put("code",false);
//            retMsg.put("flag","3");
//            retMsg.put("error","部门未同步到钉钉!");
//        }
//
//        return retMsg;
//    }


    /**
     * 根据部门的同步表信息判断同步情况
     * 不带错第三方误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param synThirdInfoEntity
     * @return
     */
    public JSONObject checkDepartmentSysToDing(SynThirdInfoEntity synThirdInfoEntity) {
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("flag","");
        retMsg.put("error","");

        if(synThirdInfoEntity!=null){
            if("".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) || "null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                // 同步表的钉钉ID为空
                retMsg.put("code",false);
                retMsg.put("flag","2");
                retMsg.put("error","同步表中部门对应的钉钉ID为空!");
            }
        }else{
            // 上级部门未同步
            retMsg.put("code",false);
            retMsg.put("flag","3");
            retMsg.put("error","部门未同步到钉钉!");
        }

        return retMsg;
    }


    /**
     * 检查部门名称不能含有特殊字符
     * @param deptName
     * @param opType
     * @param synThirdInfoEntity
     * @param thirdType
     * @param dataType
     * @param sysObjId
     * @param thirdObjId
     * @param deptFlag
     * @return
     */
    public JSONObject checkDeptName(String deptName, String opType, SynThirdInfoEntity synThirdInfoEntity, Integer thirdType,
                                    Integer dataType, String sysObjId, String thirdObjId, String deptFlag){
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("error","");
        if(deptName.indexOf("-")>-1 || deptName.indexOf(",")>-1 || deptName.indexOf("，")>-1){
            // 同步失败
            Integer synState = SynThirdConsts.SYN_STATE_FAIL;
            String description = deptFlag + "部门名称不能含有,、，、-三种特殊字符";

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
     * 往钉钉创建组织-部门
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param dingDeptListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject createDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity, List<DingTalkDeptModel> dingDeptListPara) {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        DingTalkDeptModel deptModel = new DingTalkDeptModel();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
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
//        if(dingIsSyn==1){
//            // 获取 access_token 值
//            tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//            access_token = tokenObject.getString("access_token");
//
//            if (access_token != null && !"".equals(access_token)) {
//                // 获取钉钉上的所有部门列表信息
//                if(isBatch){
//                    dingDeptList = dingDeptListPara;
//                }else {
//                    JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
//                    if (deptObject.getBoolean("code")) {
//                        dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                    } else {
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = deptFlag + "获取钉钉的部门列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), deptEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//                }
//
//                deptModel.setDeptId(null);
//                deptModel.setName(deptEntity.getFullName());
//                // 从本地数据库的同步表获取对应的钉钉ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
//                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
//                    //顶级节点时，钉钉的父节点设置为1
//                    deptModel.setParentId(SynThirdConsts.DING_ROOT_DEPT_ID);
//                }else{
//                    SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());
//
//                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity,dingDeptList);
//                    isDeptDiff = retMsg.getBoolean("code");
//                    if(isDeptDiff) {
//                        deptModel.setParentId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));
//                    }
//                }
//                deptModel.setOrder(deptEntity.getSortCode());
//                deptModel.setCreateDeptGroup(false);
//
//                // 创建时：部门名称不能带有特殊字符
//                retMsg = checkDeptName(deptEntity.getFullName(),SynThirdConsts.OBJECT_OP_ADD,null,
//                        Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                if (!retMsg.getBoolean("code")) {
//                    return retMsg;
//                }
//
//                if(isDeptDiff) {
//                    if(dingIsSyn==1) {
//                        // 往钉钉写入公司或部门
//                        retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);
//
//                        // 往同步写入本系统与第三方的对应信息
//                        if (retMsg.getBoolean("code")) {
//                            // 同步成功
//                            thirdObjId = retMsg.getString("retDeptId");
//                            retMsg.put("retDeptId", thirdObjId);
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
//                    // 同步失败,上级部门无对应的钉钉ID
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = deptFlag + "部门所属的上级部门未同步到钉钉";
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", description);
//                    retMsg.put("retDeptId", "0");
//                }
//
//            }else{
//                synState = SynThirdConsts.SYN_STATE_FAIL;
//                description = deptFlag + "access_token值为空,不能同步信息";
//
//                retMsg.put("code", false);
//                retMsg.put("error", description);
//                retMsg.put("retDeptId", "0");
//            }
//
//        }
//
//        // 更新同步表
//        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);
//
//        return retMsg;
//    }


    /**
     * 往钉钉更新组织-部门
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param dingDeptListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject updateDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity, List<DingTalkDeptModel> dingDeptListPara) {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        DingTalkDeptModel deptModel = new DingTalkDeptModel();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
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
//        // 支持同步,设置需要同步到钉钉的对象属性值
//        if(dingIsSyn==1) {
//            // 获取 access_token
//            tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//            access_token = tokenObject.getString("access_token");
//
//            // 获取同步表信息
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
//
//            if (access_token != null && !"".equals(access_token)) {
//                // 获取钉钉上的所有部门列表信息
//                if(isBatch){
//                    dingDeptList = dingDeptListPara;
//                }else {
//                    JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
//                    if (deptObject.getBoolean("code")) {
//                        dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                    } else {
//                        if (synThirdInfoEntity != null) {
//                            // 修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = synThirdInfoEntity.getThirdObjId();
//                        } else {
//                            // 写入同步表
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//                        }
//
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = deptFlag + "获取钉钉的部门列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(opType, synThirdInfoPara, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), deptEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//                }
//
//                deptModel.setDeptId(null);
//                deptModel.setName(deptEntity.getFullName());
//                // 从本地数据库的同步表获取对应的钉钉ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
//                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
//                    //顶级节点时，钉钉的父节点设置为1
//                    deptModel.setParentId(SynThirdConsts.DING_ROOT_DEPT_ID);
//                } else {
//                    // 判断上级部门的合法性
//                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());
//                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity, dingDeptList);
//                    isDeptDiff = retMsg.getBoolean("code");
//                    if (isDeptDiff) {
//                        deptModel.setParentId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));
//                    }
//                }
//                deptModel.setOrder(deptEntity.getSortCode());
//
//                // 上级部门检查是否异常
//                if(isDeptDiff){
//                    // 获取同步表信息
//                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
//
//                    // 判断当前部门对应的第三方的合法性
//                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity, dingDeptList);
//                    if (!retMsg.getBoolean("code")) {
//                        if ("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))) {
//                            // flag:3 未同步，需要创建同步到钉钉、写入同步表
//                            // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到钉钉、写入同步表
//                            if("1".equals(retMsg.getString("flag"))) {
//                                synThirdInfoService.delete(synThirdInfoEntity);
//                            }
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//
//                            // 创建时：部门名称不能带有特殊字符
//                            retMsg = checkDeptName(deptEntity.getFullName(),
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 往钉钉写入公司或部门
//                            retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);
//
//                            // 往同步写入本系统与第三方的对应信息
//                            if(retMsg.getBoolean("code")) {
//                                // 同步成功
//                                thirdObjId = retMsg.getString("retDeptId");
//                                retMsg.put("retDeptId", thirdObjId);
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
//                            // flag:2 已同步但第三方ID为空，需要创建同步到钉钉、修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = "";
//
//                            // 创建时：部门名称不能带有特殊字符
//                            retMsg = checkDeptName(deptEntity.getFullName(),
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 往钉钉写入公司或部门
//                            retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);
//
//                            // 往同步表更新本系统与第三方的对应信息
//                            if (retMsg.getBoolean("code")) {
//                                // 同步成功
//                                thirdObjId = retMsg.getString("retDeptId");
//                                retMsg.put("retDeptId", thirdObjId);
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
//                        // 部门名称不能带有特殊字符
//                        retMsg = checkDeptName(deptEntity.getFullName(),
//                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                        if (!retMsg.getBoolean("code")) {
//                            return retMsg;
//                        }
//
//                        // 往钉钉写入公司或部门
//                        deptModel.setDeptId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));
//
//                        // 设置部门主管：只有在更新时才可以执行
//                        // 初始化时：组织同步=>用户同步=>组织同步(用来更新部门主管的)
//                        if(StringUtil.isNotEmpty(deptEntity.getManager())){
//                            SynThirdInfoEntity userThirdInfo = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,deptEntity.getManager());
//                            if(userThirdInfo!=null){
//                                if(StringUtil.isNotEmpty(userThirdInfo.getThirdObjId())) {
//                                    deptModel.setDeptManagerUseridList(userThirdInfo.getThirdObjId());
//                                }
//                            }
//                        }
//
//                        retMsg = SynDingTalkUtil.updateDepartment(deptModel, access_token);
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
//                    description = deptFlag + "上级部门无对应的钉钉ID";
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
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
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
//        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);
//
//        return retMsg;
//    }


    /**
     * 往钉钉删除组织-部门
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id        本系统的公司或部门ID
     * @param dingDeptListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject deleteDepartmentSysToDing(boolean isBatch, String id, List<DingTalkDeptModel> dingDeptListPara) {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,id);
//        String deptFlag = "删除：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", "系统未设置单条同步");
//
//        // 支持同步
//        if(synThirdInfoEntity!=null) {
//            if(dingIsSyn==1){
//                // 获取 access_token
//                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//                access_token = tokenObject.getString("access_token");
//
//                if (access_token != null && !"".equals(access_token)) {
//                    // 获取钉钉上的所有部门列表信息
//                    if(isBatch){
//                        dingDeptList = dingDeptListPara;
//                    }else{
//                        JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID,access_token);
//                        if(deptObject.getBoolean("code")) {
//                            dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                        }else{
//                            // 同步失败,获取部门列表失败
//                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + "获取企业微信的部门列表信息失败");
//
//                            retMsg.put("code", false);
//                            retMsg.put("error", deptFlag + "获取钉钉的部门列表信息失败");
//                            return retMsg;
//                        }
//                    }
//
//                    // 删除钉钉对应的部门
//                    if (dingDeptList.stream().filter(t -> t.getDeptId().toString().equals(synThirdInfoEntity.getThirdObjId())).count() > 0 ? true : false) {
//                        retMsg = SynDingTalkUtil.deleteDepartment(Long.parseLong(synThirdInfoEntity.getThirdObjId()), access_token);
//                        if (retMsg.getBoolean("code")) {
//                            // 同步成功,直接删除同步表记录
//                            synThirdInfoService.delete(synThirdInfoEntity);
//                        } else {
//                            // 同步失败
//                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + retMsg.getString("error"));
//                        }
//                    }else{
//                        // 根据钉钉ID找不到相应的信息,直接删除同步表记录
//                        synThirdInfoService.delete(synThirdInfoEntity);
//                    }
//                }else{
//                    // 同步失败
//                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                            Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + "access_token值为空,不能同步信息");
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", deptFlag + "access_token值为空,不能同步信息！");
//                }
//
//            }else{
//                // 未设置单条同步，归并到未同步状态
//                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
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
     * 往钉钉创建组织-部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject createDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity,String accessToken) {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        DingTalkDeptModel deptModel = new DingTalkDeptModel();
        String thirdObjId = "";
        Integer synState = 0;
        String description = "";
        boolean isDeptDiff = true;
        String deptFlag = "创建：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "创建：系统未设置单条同步");

        // 支持同步
        if(dingIsSyn==1){
            // 获取 access_token 值
            if(isBatch) {
                access_token = accessToken;
            }else{
                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            if (access_token != null && !"".equals(access_token)) {
                deptModel.setDeptId(null);
                deptModel.setName(deptEntity.getFullName());
                // 从本地数据库的同步表获取对应的钉钉ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
                    //顶级节点时，钉钉的父节点设置为1
                    deptModel.setParentId(SynThirdConsts.DING_ROOT_DEPT_ID);
                }else{
                    SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());

                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity);
                    isDeptDiff = retMsg.getBoolean("code");
                    if(isDeptDiff) {
                        deptModel.setParentId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));
                    }
                }
                deptModel.setOrder(deptEntity.getSortCode());
                deptModel.setCreateDeptGroup(false);

                // 创建时：部门名称不能带有特殊字符
                retMsg = checkDeptName(deptEntity.getFullName(),SynThirdConsts.OBJECT_OP_ADD,null,
                        Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                if (!retMsg.getBoolean("code")) {
                    return retMsg;
                }

                if(isDeptDiff) {
                    if(dingIsSyn==1) {
                        // 往钉钉写入公司或部门
                        retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);

                        // 往同步写入本系统与第三方的对应信息
                        if (retMsg.getBoolean("code")) {
                            // 同步成功
                            thirdObjId = retMsg.getString("retDeptId");
                            retMsg.put("retDeptId", thirdObjId);
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
                    // 同步失败,上级部门无对应的钉钉ID
                    synState = SynThirdConsts.SYN_STATE_FAIL;
                    description = deptFlag + "部门所属的上级部门未同步到钉钉";

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
        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }


    /**
     * 往钉钉更新组织-部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject updateDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity,String accessToken) {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        DingTalkDeptModel deptModel = new DingTalkDeptModel();
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

        // 支持同步,设置需要同步到钉钉的对象属性值
        if(dingIsSyn==1) {
            // 获取 access_token
            if(isBatch) {
                access_token = accessToken;
            }else{
                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());

            if (access_token != null && !"".equals(access_token)) {
                deptModel.setDeptId(null);
                deptModel.setName(deptEntity.getFullName());
                // 从本地数据库的同步表获取对应的钉钉ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
                    //顶级节点时，钉钉的父节点设置为1
                    deptModel.setParentId(SynThirdConsts.DING_ROOT_DEPT_ID);
                } else {
                    // 判断上级部门的合法性
                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());

                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity);
                    isDeptDiff = retMsg.getBoolean("code");
                    if (isDeptDiff) {
                        deptModel.setParentId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));
                    }
                }
                deptModel.setOrder(deptEntity.getSortCode());

                // 上级部门检查是否异常
                if(isDeptDiff){
                    // 获取同步表信息
                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());

                    // 判断当前部门对应的第三方的合法性
                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity);
                    if (!retMsg.getBoolean("code")) {
                        if ("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))) {
                            // flag:3 未同步，需要创建同步到钉钉、写入同步表
                            // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到钉钉、写入同步表
                            if("1".equals(retMsg.getString("flag"))) {
                                synThirdInfoService.delete(synThirdInfoEntity);
                            }
                            opType = SynThirdConsts.OBJECT_OP_ADD;
                            synThirdInfoPara = null;
                            thirdObjId = "";

                            // 创建时：部门名称不能带有特殊字符
                            retMsg = checkDeptName(deptEntity.getFullName(),
                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                            if (!retMsg.getBoolean("code")) {
                                return retMsg;
                            }

                            // 往钉钉写入公司或部门
                            retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);

                            // 往同步写入本系统与第三方的对应信息
                            if(retMsg.getBoolean("code")) {
                                // 同步成功
                                thirdObjId = retMsg.getString("retDeptId");
                                retMsg.put("retDeptId", thirdObjId);
                                synState = SynThirdConsts.SYN_STATE_OK;
                                description = "";
                            }else{
                                // 同步失败
                                synState = SynThirdConsts.SYN_STATE_FAIL;
                                description = deptFlag + retMsg.getString("error");
                            }
                        }

                        if ("2".equals(retMsg.getString("flag"))) {
                            // flag:2 已同步但第三方ID为空，需要创建同步到钉钉、修改同步表
                            opType = SynThirdConsts.OBJECT_OP_UPD;
                            synThirdInfoPara = synThirdInfoEntity;
                            thirdObjId = "";

                            // 创建时：部门名称不能带有特殊字符
                            retMsg = checkDeptName(deptEntity.getFullName(),
                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                            if (!retMsg.getBoolean("code")) {
                                return retMsg;
                            }

                            // 往钉钉写入公司或部门
                            retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);

                            // 往同步表更新本系统与第三方的对应信息
                            if (retMsg.getBoolean("code")) {
                                // 同步成功
                                thirdObjId = retMsg.getString("retDeptId");
                                retMsg.put("retDeptId", thirdObjId);
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

                        // 部门名称不能带有特殊字符
                        retMsg = checkDeptName(deptEntity.getFullName(),
                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                        if (!retMsg.getBoolean("code")) {
                            return retMsg;
                        }

                        // 往钉钉写入公司或部门
                        deptModel.setDeptId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));

                        // 设置部门主管：只有在更新时才可以执行
                        // 初始化时：组织同步=>用户同步=>组织同步(用来更新部门主管的)
                        if(StringUtil.isNotEmpty(deptEntity.getManager())){
                            SynThirdInfoEntity userThirdInfo = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,deptEntity.getManager());
                            if(userThirdInfo!=null){
                                if(StringUtil.isNotEmpty(userThirdInfo.getThirdObjId())) {
                                    deptModel.setDeptManagerUseridList(userThirdInfo.getThirdObjId());
                                }
                            }
                        }

                        retMsg = SynDingTalkUtil.updateDepartment(deptModel, access_token);

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
                    description = deptFlag + "上级部门无对应的钉钉ID";

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
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
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
        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }


    /**
     * 往钉钉删除组织-部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id        本系统的公司或部门ID
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject deleteDepartmentSysToDing(boolean isBatch, String id,String accessToken) {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,id);
        String deptFlag = "删除：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "系统未设置单条同步");

        // 支持同步
        if(synThirdInfoEntity!=null) {
            if(dingIsSyn==1){
                // 获取 access_token
                if(isBatch) {
                    access_token = accessToken;
                }else{
                    tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                    access_token = tokenObject.getString("access_token");
                }

                if (access_token != null && !"".equals(access_token)) {
                    // 删除钉钉对应的部门
                    if (!"".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) && !"null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                        retMsg = SynDingTalkUtil.deleteDepartment(Long.parseLong(synThirdInfoEntity.getThirdObjId()), access_token);
                        if (retMsg.getBoolean("code")) {
                            // 同步成功,直接删除同步表记录
                            synThirdInfoService.delete(synThirdInfoEntity);
                        } else {
                            // 同步失败
                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + retMsg.getString("error"));
                        }
                    }else{
                        // 根据钉钉ID找不到相应的信息,直接删除同步表记录
                        synThirdInfoService.delete(synThirdInfoEntity);
                    }
                }else{
                    // 同步失败
                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                            Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + "access_token值为空,不能同步信息");

                    retMsg.put("code", false);
                    retMsg.put("error", deptFlag + "access_token值为空,不能同步信息！");
                }

            }else{
                // 未设置单条同步，归并到未同步状态
                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                        Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_NO, deptFlag + "系统未设置单条同步");

                retMsg.put("code", true);
                retMsg.put("error", deptFlag + "系统未设置单条同步");
            }
        }

        return retMsg;
    }


    //------------------------------------本系统同步用户到钉钉-------------------------------------

    /**
     * 设置需要提交给钉钉接口的单个成员信息
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param userEntity 本地用户信息
     * @param dingDeptList 钉钉的部门信息
     * @return
     */
//    public JSONObject setDingUserObject(UserEntity userEntity, List<DingTalkDeptModel> dingDeptList) throws ParseException {
//        DingTalkUserModel userModel = new DingTalkUserModel();
//        JSONObject retMsg = new JSONObject();
//        retMsg.put("code", true);
//        retMsg.put("error", "");
//
//        // 验证邮箱格式的格式合法性、唯一性
//        if(StringUtil.isNotEmpty(userEntity.getEmail())){
//            if(!RegexUtils.checkEmail(userEntity.getEmail())){
//                retMsg.put("code", false);
//                retMsg.put("error", "邮箱格式不合法！");
//                retMsg.put("dingUserObject", null);
//                return retMsg;
//            }
//        }
//
//        // 判断手机号的合法性
//        if(StringUtil.isNotEmpty(userEntity.getMobilePhone())){
//            if(!RegexUtils.checkMobile(userEntity.getMobilePhone())){
//                retMsg.put("code", false);
//                retMsg.put("error", "手机号不合法！");
//                retMsg.put("dingUserObject", null);
//                return retMsg;
//            }
//        }
//
//        userModel.setUserid(userEntity.getId());
//        userModel.setName(userEntity.getRealName());
//        userModel.setMobile(userEntity.getMobilePhone());
//        userModel.setTelephone(userEntity.getLandline());
//        userModel.setJobNumber(userEntity.getAccount());
//
//        PositionEntity positionEntity = positionService.getInfo(userEntity.getPositionId());
//        String jobName = "";
//        if(positionEntity!=null){
//            jobName = positionEntity.getFullName();
//            userModel.setTitle(jobName);
//        }
//
//        userModel.setWorkPlace(userEntity.getPostalAddress());
//
//        if(userEntity.getEntryDate()!= null){
//            String entryDate = DateUtil.daFormat(userEntity.getEntryDate());
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            df.setTimeZone(TimeZone.getTimeZone("GMT"));
//            if(df.parse(entryDate).getTime()>0) {
//                userModel.setHiredDate(df.parse(entryDate).getTime());
//            }
//        }
//
//        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,userEntity.getOrganizeId());
//        retMsg = checkDepartmentSysToDing(synThirdInfoEntity,dingDeptList);
//        if(retMsg.getBoolean("code")){
//            userModel.setDeptIdList(synThirdInfoEntity.getThirdObjId());
//        }else{
//            retMsg.put("code", false);
//            retMsg.put("error", "部门找不到对应的钉钉ID！");
//            retMsg.put("dingUserObject", null);
//            return retMsg;
//        }
//        userModel.setEmail(userEntity.getEmail());
//
//        retMsg.put("dingUserObject", userModel);
//        return retMsg;
//    }


    /**
     * 设置需要提交给钉钉接口的单个成员信息
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param userEntity 本地用户信息
     * @return
     */
    public JSONObject setDingUserObject(UserEntity userEntity) throws ParseException {
        DingTalkUserModel userModel = new DingTalkUserModel();
        JSONObject retMsg = new JSONObject();
        retMsg.put("code", true);
        retMsg.put("error", "");

        // 验证邮箱格式的格式合法性、唯一性
        if(StringUtil.isNotEmpty(userEntity.getEmail())){
            if(!RegexUtils.checkEmail(userEntity.getEmail())){
                retMsg.put("code", false);
                retMsg.put("error", "邮箱格式不合法！");
                retMsg.put("dingUserObject", null);
                return retMsg;
            }
        }

        // 判断手机号的合法性
        if(StringUtil.isNotEmpty(userEntity.getMobilePhone())){
            if(!RegexUtils.checkMobile(userEntity.getMobilePhone())){
                retMsg.put("code", false);
                retMsg.put("error", "手机号不合法！");
                retMsg.put("dingUserObject", null);
                return retMsg;
            }
        }

        userModel.setUserid(userEntity.getId());
        userModel.setName(userEntity.getRealName());
        userModel.setMobile(userEntity.getMobilePhone());
        userModel.setTelephone(userEntity.getLandline());
        userModel.setJobNumber(userEntity.getAccount());

        PositionEntity positionEntity = positionService.getInfo(userEntity.getPositionId());
        String jobName = "";
        if(positionEntity!=null){
            jobName = positionEntity.getFullName();
            userModel.setTitle(jobName);
        }

        userModel.setWorkPlace(userEntity.getPostalAddress());

        if(userEntity.getEntryDate()!= null){
            String entryDate = DateUtil.daFormat(userEntity.getEntryDate());
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            if(df.parse(entryDate).getTime()>0) {
                userModel.setHiredDate(df.parse(entryDate).getTime());
            }
        }

        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,userEntity.getOrganizeId());
        // retMsg = checkDepartmentSysToDing(synThirdInfoEntity,dingDeptList);
        retMsg = checkDepartmentSysToDing(synThirdInfoEntity);
        if(retMsg.getBoolean("code")){
            userModel.setDeptIdList(synThirdInfoEntity.getThirdObjId());
        }else{
            retMsg.put("code", false);
            retMsg.put("error", "部门找不到对应的钉钉ID！");
            retMsg.put("dingUserObject", null);
            return retMsg;
        }
        userModel.setEmail(userEntity.getEmail());

        retMsg.put("dingUserObject", userModel);
        return retMsg;
    }


    /**
     * 判断用户的手机号、邮箱是否唯一，钉钉不允许重复
     * @param mobile
     * @param email
     * @param userId
     * @param dingUserList
     * @param opType
     * @param synThirdInfoEntity
     * @param thirdType
     * @param dataType
     * @param sysObjId
     * @param thirdObjId
     * @param deptFlag
     * @return
     */
    public JSONObject checkUserMobileEmailRepeat(String mobile, String email, String userId, List<DingTalkUserModel> dingUserList,
                                                 String opType, SynThirdInfoEntity synThirdInfoEntity, Integer thirdType,
                                                 Integer dataType, String sysObjId, String thirdObjId, String deptFlag){
        boolean isDiff = true;
        String description = "";
        JSONObject retMsg = new JSONObject();

        // 钉钉限制：手机号唯一性
        if(StringUtil.isNotEmpty(mobile)){
            if(StringUtil.isNotEmpty(userId)){
                if(dingUserList.stream().filter(t -> String.valueOf(t.getMobile()).equals(mobile) && !(t.getUserid().equals(userId))).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "钉钉内已有绑定手机号:" + mobile;
                }
            }else{
                if(dingUserList.stream().filter(t -> String.valueOf(t.getMobile()).equals(mobile)).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "钉钉内已有绑定手机号:" + mobile;
                }
            }
        }

        // 钉钉限制：邮箱地址唯一性
        if(StringUtil.isNotEmpty(email)){
            if(StringUtil.isNotEmpty(userId)){
                if(dingUserList.stream().filter(t -> String.valueOf(t.getEmail()).equals(email) && !(t.getUserid().equals(userId))).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "钉钉内已有绑定此邮箱:" + email;
                }
            }else{
                if(dingUserList.stream().filter(t -> String.valueOf(t.getEmail()).equals(email)).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "钉钉内已有绑定此邮箱:" + email;
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
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param synThirdInfoEntity
     * @param dingUserList
     * @return
     */
//    public JSONObject checkUserSysToDing(SynThirdInfoEntity synThirdInfoEntity, List<DingTalkUserModel> dingUserList) {
//        JSONObject retMsg = new JSONObject();
//        retMsg.put("code",true);
//        retMsg.put("flag","");
//        retMsg.put("error","");
//
//        if(synThirdInfoEntity!=null){
//            if(StringUtil.isNotEmpty(synThirdInfoEntity.getThirdObjId())) {
//                // 同步表存在钉钉ID,仍需要判断钉钉上有没此用户
//                if(dingUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false){
//                    retMsg.put("code",false);
//                    retMsg.put("flag","1");
//                    retMsg.put("error","钉钉不存在同步表对应的用户ID!");
//                }
//            }else{
//                // 同步表的企业微信ID为空
//                retMsg.put("code",false);
//                retMsg.put("flag","2");
//                retMsg.put("error","同步表中用户对应的钉钉ID为空!");
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
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param synThirdInfoEntity
     * @return
     */
    public JSONObject checkUserSysToDing(SynThirdInfoEntity synThirdInfoEntity) {
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("flag","");
        retMsg.put("error","");

        if(synThirdInfoEntity!=null){
            if("".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) || "null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                // 同步表的企业微信ID为空
                retMsg.put("code",false);
                retMsg.put("flag","2");
                retMsg.put("error","同步表中用户对应的钉钉ID为空!");
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
     * 往钉钉创建用户
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param dingDeptListPara 单条执行时为null
     * @param dingUserListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject createUserSysToDing(boolean isBatch, UserEntity userEntity, List<DingTalkDeptModel> dingDeptListPara,
//                                          List<DingTalkUserModel> dingUserListPara) throws ParseException {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
//        JSONObject tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//        String access_token = tokenObject.getString("access_token");
//        JSONObject retMsg = new JSONObject();
//        DingTalkUserModel userObjectModel = new DingTalkUserModel();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        List<DingTalkUserModel> dingUserList = new ArrayList<>();
//        String thirdObjId = "";
//        Integer synState = 0;
//        String description = "";
//        String userFlag = "创建：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", userFlag + "系统未设置单条同步");
//
//        if (dingIsSyn==1){
//            if (access_token != null && !"".equals(access_token)) {
//                // 获取企业微信上的所有部门列表信息
//                if(isBatch){
//                    dingDeptList = dingDeptListPara;
//                }else{
//                    JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID,access_token);
//                    if(deptObject.getBoolean("code")) {
//                        dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department",List.class), DingTalkDeptModel.class);
//                    }else{
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取钉钉的部门列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//                }
//
//                // 获取钉钉上的所有用户列表信息
//                if(isBatch){
//                    dingUserList = dingUserListPara;
//                }else {
//                    JSONObject userObject = SynDingTalkUtil.getUserList(dingDeptList, access_token);
//                    if (userObject.getBoolean("code")) {
//                        dingUserList = JsonUtil.getJsonToList(userObject.getObject("userlist", List.class), DingTalkUserModel.class);
//                    } else {
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取钉钉的用户列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//                }
//
//                // 判断用户的手机号、邮箱是否唯一,不能重复
//                retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,dingUserList,
//                        SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                if (!retMsg.getBoolean("code")) {
//                    return retMsg;
//                }
//
//                // 要同步到钉钉的对象赋值
//                retMsg = setDingUserObject(userEntity, dingDeptList);
//                if (retMsg.getBoolean("code")) {
//                    userObjectModel = retMsg.getObject("dingUserObject",DingTalkUserModel.class);
//
//                    // 往企业微信写入成员
//                    retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);
//
//                    // 往同步写入本系统与第三方的对应信息
//                    if (retMsg.getBoolean("code")) {
//                        // 同步成功
//                        thirdObjId = userEntity.getId();
//                        synState = SynThirdConsts.SYN_STATE_OK;
//                    } else {
//                        // 同步失败
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + retMsg.getString("error");
//                    }
//                }else{
//                    // 同步失败,原因：部门找不到对应的第三方ID、邮箱格式不合法
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = userFlag + retMsg.getString("error");
//                }
//
//            }else{
//                // 同步失败
//                synState = SynThirdConsts.SYN_STATE_FAIL;
//                description = userFlag + "access_token值为空,不能同步信息";
//
//                retMsg.put("code", false);
//                retMsg.put("error", description);
//            }
//
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
//        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//        return retMsg;
//    }


    /**
     * 往钉钉更新用户
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param dingDeptListPara 单条执行时为null
     * @param dingUserListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject updateUserSysToDing(boolean isBatch, UserEntity userEntity, List<DingTalkDeptModel> dingDeptListPara,
//                                          List<DingTalkUserModel> dingUserListPara) throws ParseException {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        DingTalkUserModel userObjectModel = new DingTalkUserModel();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        List<DingTalkUserModel> dingUserList = new ArrayList<>();
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
//        // 支持同步
//        if (dingIsSyn==1){
//            // 获取 access_token
//            tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//            access_token = tokenObject.getString("access_token");
//
//            // 获取同步表信息
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
//            if (access_token != null && !"".equals(access_token)) {
//                // 获取企业微信上的所有部门列表信息
//                if(isBatch){
//                    dingDeptList = dingDeptListPara;
//                }else{
//                    JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID,access_token);
//                    if(deptObject.getBoolean("code")) {
//                        dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department",List.class), DingTalkDeptModel.class);
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
//                        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,synState,description);
//
//                        return retMsg;
//                    }
//                }
//
//
//                // 获取钉钉上的所有用户列表信息
//                if(isBatch){
//                    dingUserList = dingUserListPara;
//                }else {
//                    JSONObject userObject = SynDingTalkUtil.getUserList(dingDeptList, access_token);
//                    if (userObject.getBoolean("code")) {
//                        dingUserList = JsonUtil.getJsonToList(userObject.getObject("userlist", List.class), DingTalkUserModel.class);
//                    } else {
//                        if (synThirdInfoEntity != null) {
//                            // 修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = synThirdInfoEntity.getThirdObjId();
//                        } else {
//                            // 写入同步表
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//                        }
//
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取钉钉的用户列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(opType, synThirdInfoPara, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//                }
//
//                // 要同步到企业微信的对象赋值
//                retMsg = setDingUserObject(userEntity,dingDeptList);
//                if (retMsg.getBoolean("code")) {
//                    // 判断当前用户对应的第三方的合法性
//                    userObjectModel = retMsg.getObject("dingUserObject",DingTalkUserModel.class);
//                    retMsg = checkUserSysToDing(synThirdInfoEntity, dingUserList);
//                    if (!retMsg.getBoolean("code")) {
//                        if("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))){
//                            // flag:3 未同步，需要创建同步到企业微信、写入同步表
//                            // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到企业微信、写入同步表
//                            if("1".equals(retMsg.getString("flag"))) {
//                                synThirdInfoService.delete(synThirdInfoEntity);
//                            }
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//
//                            // 判断用户的手机号、邮箱是否唯一,不能重复
//                            retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,dingUserList,
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 往企业微信写入成员
//                            retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);
//                            if(retMsg.getBoolean("code")) {
//                                // 同步成功
//                                thirdObjId = userEntity.getId();
//                                synState = SynThirdConsts.SYN_STATE_OK;
//                                description = "";
//                            }else{
//                                // 同步失败
//                                synState = SynThirdConsts.SYN_STATE_FAIL;
//                                description = userFlag + retMsg.getString("error");
//                            }
//                        }
//
//                        if("2".equals(retMsg.getString("flag"))){
//                            // 已同步但第三方ID为空，需要创建同步到企业微信、修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = "";
//
//                            // 判断用户的手机号、邮箱是否唯一,不能重复
//                            retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,dingUserList,
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 往企业微信写入成员
//                            retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);
//                            if(retMsg.getBoolean("code")) {
//                                // 同步成功
//                                thirdObjId = userEntity.getId();
//                                synState = SynThirdConsts.SYN_STATE_OK;
//                                description = "";
//                            }else{
//                                // 同步失败
//                                synState = SynThirdConsts.SYN_STATE_FAIL;
//                                description = userFlag + retMsg.getString("error");
//                            }
//                        }
//
//                    }else{
//                        // 更新同步表
//                        opType = SynThirdConsts.OBJECT_OP_UPD;
//                        synThirdInfoPara = synThirdInfoEntity;
//                        thirdObjId = synThirdInfoEntity.getThirdObjId();
//
//                        // 判断用户的手机号、邮箱是否唯一,不能重复
//                        retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,dingUserList,
//                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                        if (!retMsg.getBoolean("code")) {
//                            return retMsg;
//                        }
//
//                        // 往企业微信更新成员信息
//                        retMsg = SynDingTalkUtil.updateUser(userObjectModel, access_token);
//                        if(retMsg.getBoolean("code")) {
//                            // 同步成功
//                            synState = SynThirdConsts.SYN_STATE_OK;
//                            description = "";
//                        }else{
//                            // 同步失败
//                            synState = SynThirdConsts.SYN_STATE_FAIL;
//                            description = userFlag + retMsg.getString("error");
//                        }
//
//                    }
//
//                }else{
//                    // 同步失败,原因：用户所属部门找不到相应的企业微信ID、邮箱格式不合法
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
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = userFlag + retMsg.getString("error");
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", description);
//                }
//
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
//                description = userFlag + "access_token值为空,不能同步信息";
//
//                retMsg.put("code", true);
//                retMsg.put("error", description);
//            }
//
//        }else{
//            // 未设置单条同步,归并到未同步状态
//            // 获取同步表信息
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
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
//        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,synState,description);
//
//        return retMsg;
//    }


    /**
     * 往钉钉删除用户
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id   本系统的公司或部门ID
     * @param dingDeptListPara 单条执行时为null
     * @param dingUserListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject deleteUserSysToDing(boolean isBatch, String id, List<DingTalkDeptModel> dingDeptListPara,
//                                          List<DingTalkUserModel> dingUserListPara) {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        List<DingTalkUserModel> dingUserList = new ArrayList<>();
//        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,id);
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", "系统未设置单条同步");
//
//        // 支持同步
//        if(synThirdInfoEntity!=null) {
//            if(dingIsSyn==1) {
//                // 获取 access_token
//                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//                access_token = tokenObject.getString("access_token");
//
//                if (access_token != null && !"".equals(access_token)) {
//                    // 获取企业微信上的所有部门列表信息
//                    if(isBatch){
//                        dingDeptList = dingDeptListPara;
//                    }else {
//                        JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
//                        if (deptObject.getBoolean("code")) {
//                            dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                        } else {
//                            // 更新同步表
//                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "获取钉钉的部门列表信息失败");
//
//                            retMsg.put("code", true);
//                            retMsg.put("error", "获取钉钉的部门列表信息失败");
//                            return retMsg;
//                        }
//                    }
//
//                    // 获取企业微信上的所有成员信息列表
//                    if(isBatch){
//                        dingUserList = dingUserListPara;
//                    }else{
//                        JSONObject userObject = SynDingTalkUtil.getUserList(dingDeptList,access_token);
//                        if(userObject.getBoolean("code")) {
//                            dingUserList = JsonUtil.getJsonToList(userObject.getObject("userlist",List.class), DingTalkUserModel.class);
//                        }else{
//                            // 同步失败，获取企业微信的成员列表信息失败
//                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "获取企业微信的成员列表信息失败");
//
//                            retMsg.put("code", false);
//                            retMsg.put("error", "获取企业微信的成员列表信息失败");
//                            return retMsg;
//                        }
//                    }
//
//                    // 删除企业对应的用户
//                    if(dingUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() > 0 ? true : false){
//                        retMsg = SynDingTalkUtil.deleteUser(synThirdInfoEntity.getThirdObjId(), access_token);
//                        if (retMsg.getBoolean("code")) {
//                            // 同步成功,直接删除同步表记录
//                            synThirdInfoService.delete(synThirdInfoEntity);
//                        }else{
//                            // 同步失败
//                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, retMsg.getString("error"));
//                        }
//                    }else{
//                        // 根据企业微信ID找不到相应的信息,直接删除同步表记录
//                        synThirdInfoService.delete(synThirdInfoEntity);
//                    }
//                }else{
//                    // 同步失败
//                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                            Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "access_token值为空,不能同步信息");
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", "access_token值为空,不能同步信息！");
//                }
//
//            }else{
//                // 未设置单条同步，归并到未同步状态
//                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
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
     * 往钉钉创建用户
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject createUserSysToDing(boolean isBatch, UserEntity userEntity,String accessToken) throws ParseException {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        DingTalkUserModel userObjectModel = new DingTalkUserModel();
        String thirdObjId = "";
        Integer synState = 0;
        String description = "";
        String userFlag = "创建：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", userFlag + "系统未设置单条同步");

        if (dingIsSyn==1){
            if(isBatch){
                access_token = accessToken;
            }else{
                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            if (access_token != null && !"".equals(access_token)) {
                // 要同步到钉钉的对象赋值
                retMsg = setDingUserObject(userEntity);
                if (retMsg.getBoolean("code")) {
                    userObjectModel = retMsg.getObject("dingUserObject",DingTalkUserModel.class);

                    // 往企业微信写入成员
                    retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);

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

        }else{
            // 无须同步，未同步状态
            synState = SynThirdConsts.SYN_STATE_NO;
            description = userFlag + "系统未设置单条同步";

            retMsg.put("code", true);
            retMsg.put("error", description);
        }

        // 更新同步表
        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);

        return retMsg;
    }


    /**
     * 往钉钉更新用户
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject updateUserSysToDing(boolean isBatch, UserEntity userEntity,String accessToken) throws ParseException {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        DingTalkUserModel userObjectModel = new DingTalkUserModel();
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

        // 支持同步
        if (dingIsSyn==1){
            // 获取 access_token
            if(isBatch){
                access_token = accessToken;
            }else{
                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
            if (access_token != null && !"".equals(access_token)) {
                // 要同步到企业微信的对象赋值
                retMsg = setDingUserObject(userEntity);
                if (retMsg.getBoolean("code")) {
                    // 判断当前用户对应的第三方的合法性
                    userObjectModel = retMsg.getObject("dingUserObject",DingTalkUserModel.class);
                    retMsg = checkUserSysToDing(synThirdInfoEntity);
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
                            retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);
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
                            retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);
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

                        // 往企业微信更新成员信息
                        retMsg = SynDingTalkUtil.updateUser(userObjectModel, access_token);
                        if(retMsg.getBoolean("code")) {
                            // 同步成功
                            synState = SynThirdConsts.SYN_STATE_OK;
                            description = "";
                        }else{
                            // 同步失败
                            synState = SynThirdConsts.SYN_STATE_FAIL;
                            description = userFlag + retMsg.getString("error");
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

        }else{
            // 未设置单条同步,归并到未同步状态
            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
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
        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }


    /**
     * 往钉钉删除用户
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id   本系统的公司或部门ID
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject deleteUserSysToDing(boolean isBatch, String id,String accessToken) {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,id);

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "系统未设置单条同步");

        // 支持同步
        if(synThirdInfoEntity!=null) {
            if(dingIsSyn==1) {
                // 获取 access_token
                if(isBatch){
                    access_token = accessToken;
                }else{
                    tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                    access_token = tokenObject.getString("access_token");
                }

                if (access_token != null && !"".equals(access_token)) {
                    // 删除企业对应的用户
                    if (!"".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) && !"null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                        retMsg = SynDingTalkUtil.deleteUser(synThirdInfoEntity.getThirdObjId(), access_token);
                        if (retMsg.getBoolean("code")) {
                            // 同步成功,直接删除同步表记录
                            synThirdInfoService.delete(synThirdInfoEntity);
                        }else{
                            // 同步失败
                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, retMsg.getString("error"));
                        }
                    }else{
                        // 根据企业微信ID找不到相应的信息,直接删除同步表记录
                        synThirdInfoService.delete(synThirdInfoEntity);
                    }
                }else{
                    // 同步失败
                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                            Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "access_token值为空,不能同步信息");

                    retMsg.put("code", false);
                    retMsg.put("error", "access_token值为空,不能同步信息！");
                }

            }else{
                // 未设置单条同步，归并到未同步状态
                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_NO, "系统未设置同步");

                retMsg.put("code", true);
                retMsg.put("error", "系统未设置单条同步");
            }
        }

        return retMsg;
    }

}
