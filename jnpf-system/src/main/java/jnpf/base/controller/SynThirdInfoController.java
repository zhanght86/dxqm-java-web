package jnpf.base.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.database.exception.DataException;
import jnpf.exception.WxErrorException;
import jnpf.message.entity.SynThirdInfoEntity;
import jnpf.message.model.message.*;
import jnpf.message.service.SynThirdDingTalkService;
import jnpf.message.service.SynThirdInfoService;
import jnpf.message.service.SynThirdQyService;
import jnpf.message.util.SynDingTalkUtil;
import jnpf.message.util.SynQyWebChatUtil;
import jnpf.message.util.SynThirdConsts;
import jnpf.message.util.SynThirdTotal;
import jnpf.model.login.BaseSystemInfo;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.model.organize.OraganizeModel;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 第三方工具的公司-部门-用户同步表模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/25 9:30
 */
@Api(tags = "第三方信息同步",value = "SynThirdInfo")
@RestController
@RequestMapping("/api/system/SynThirdInfo")
public class SynThirdInfoController {
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private SynThirdInfoService synThirdInfoService;
    @Autowired
    private SynThirdQyService synThirdQyService;
    @Autowired
    private SynThirdDingTalkService synThirdDingTalkService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private UserService userService;

    /**
     * 新增同步表信息
     *
     * @param synThirdInfoCrForm
     * @return
     */
    @ApiOperation("新增同步表信息")
    @PostMapping
    @Transactional
    public ActionResult create(@RequestBody @Valid SynThirdInfoCrForm synThirdInfoCrForm) throws DataException {
        UserInfo userInfo = userProvider.get();
        SynThirdInfoEntity entity= JsonUtil.getJsonToBean(synThirdInfoCrForm, SynThirdInfoEntity.class);
        entity.setCreatorUserId(userInfo.getUserId());
        entity.setCreatorTime(DateUtil.getNowDate());
        entity.setId(RandomUtil.uuId());
        synThirdInfoService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 获取同步表信息
     *
     * @param id
     * @return
     */
    @ApiOperation("获取同步表信息")
    @GetMapping("/{id}")
    public SynThirdInfoEntity getInfo(@PathVariable("id") String id){
        SynThirdInfoEntity entity= synThirdInfoService.getInfo(id);
        return entity;
    }

    /**
     * 获取指定类型的同步对象
     * @param thirdType 1:企业微信 2:钉钉
     * @param dataType  1:公司 2:部门 3：用户
     * @param id        dataType对应的对象ID
     * @return
     */
    @ApiOperation("获取指定类型的同步对象")
    @GetMapping("/getInfoBySysObjId/{thirdType}/{dataType}/{id}")
    public SynThirdInfoEntity getInfoBySysObjId(@PathVariable("thirdType") String thirdType,@PathVariable("dataType") String dataType,@PathVariable("id") String id) {
        SynThirdInfoEntity entity= synThirdInfoService.getInfoBySysObjId(thirdType,dataType,id);
        return entity;
    }


    /**
     * 更新同步表信息
     *
     * @param id
     * @return
     */
    @ApiOperation("更新同步表信息")
    @PutMapping("/{id}")
    @Transactional
    public ActionResult update(@PathVariable("id") String id,@RequestBody @Valid SynThirdInfoUpForm synThirdInfoUpForm) throws DataException {
        SynThirdInfoEntity entity = synThirdInfoService.getInfo(id);
        UserInfo userInfo = userProvider.get();
        if(entity!=null){
            SynThirdInfoEntity entityUpd = JsonUtil.getJsonToBean(synThirdInfoUpForm,SynThirdInfoEntity.class);
            entityUpd.setCreatorUserId(entity.getCreatorUserId());
            entityUpd.setCreatorTime(entity.getCreatorTime());
            entityUpd.setLastModifyUserId(userInfo.getUserId());
            entityUpd.setLastModifyTime(DateUtil.getNowDate());
            synThirdInfoService.update(id,entityUpd);

            return ActionResult.success("更新成功");
        }else{
            return ActionResult.fail("更新失败，数据不存在");
        }
    }


    /**
     * 删除同步表信息
     *
     * @param id
     * @return
     */
    @ApiOperation("删除同步表信息")
    @DeleteMapping("/{id}")
    @Transactional
    public ActionResult delete(@PathVariable("id") String id){
        SynThirdInfoEntity entity = synThirdInfoService.getInfo(id);
        if(entity!=null){
            synThirdInfoService.delete(entity);
        }
        return ActionResult.success("删除成功");
    }




    /**
     * 获取第三方(如：企业微信、钉钉)的组织与用户同步统计信息
     * @param thirdType 第三方类型(1:企业微信;2:钉钉)
     * @return
     */
    @ApiOperation("获取第三方(如：企业微信、钉钉)的组织与用户同步统计信息")
    @GetMapping("/getSynThirdTotal/{thirdType}")
    public ActionResult getSynThirdTotal(@PathVariable("thirdType") String thirdType){
        List<SynThirdTotal> synTotalList = new ArrayList<>();
        synTotalList.add(synThirdInfoService.getSynTotal(thirdType, SynThirdConsts.DATA_TYPE_ORG));
        synTotalList.add(synThirdInfoService.getSynTotal(thirdType,SynThirdConsts.DATA_TYPE_USER));
        return ActionResult.success(synTotalList);
    }

    /**
     * 获取第三方(如：企业微信、钉钉)的组织或用户同步统计信息
     * @param thirdType 第三方类型(1:企业微信;2:钉钉)
     * @param dataType  数据类型(1:组织(公司与部门);2:用户)
     * @return
     */
    @ApiOperation("获取第三方(如：企业微信、钉钉)的组织或用户同步统计信息")
    @GetMapping("/getSynThirdTotal/{thirdType}/{dataType}")
    public SynThirdTotal getSynThirdTotal(@PathVariable("thirdType") String thirdType,@PathVariable("dataType") String dataType){
        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(thirdType,dataType);
        return synThirdTotal;
    }


    //==================================本系统的公司-部门-用户批量同步到企业微信==================================

    /**
     * 本地所有组织信息(包含公司和部门)同步到企业微信
     * 有带第三方错误定位判断的功能代码 20210604
     * @throws WxErrorException
     */
//    @ApiOperation("本地所有组织信息(包含公司和部门)同步到企业微信")
//    @GetMapping("/synAllOrganizeSysToQy")
//    public ActionResult synAllOrganizeSysToQy() throws WxErrorException {
//        JSONObject retMsg = new JSONObject();
//        BaseSystemInfo config = synThirdQyService.getQyhConfig();
//        String corpId = config.getQyhCorpId();
//        String corpSecret = config.getQyhCorpSecret();
//        List<QyWebChatDeptModel> qyDeptList = new ArrayList<>();
//
//        try {
//            // 获取Token值
//            JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
//            if(!tokenObject.getBoolean("code")){
//                return ActionResult.fail("获取企业微信access_token失败");
//            }
//            String access_token = tokenObject.getString("access_token");
//
//            // 获取企业微信上的所有部门列表信息
//            if (access_token != null && !"".equals(access_token)) {
//                JSONObject deptObject = SynQyWebChatUtil.getDepartmentList(SynThirdConsts.QY_ROOT_DEPT_ID, access_token);
//                if (deptObject.getBoolean("code")) {
//                    qyDeptList = JsonUtil.getJsonToList(deptObject.getString("department"), QyWebChatDeptModel.class);
//                } else {
//                    return ActionResult.fail("组织同步失败:获取企业微信的部门信息列表失败");
//                }
//            } else {
//                return ActionResult.fail("组织同步失败:获取企业微信access_token失败");
//            }
//
//            // 获取同步表、部门表的信息
//            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_ORG);
//            List<OrganizeEntity> organizeList = organizeService.getList();
//
//            // 部门进行树结构化,固化上下层级序列化
//            List<OraganizeModel> organizeModelList = JsonUtil.getJsonToList(organizeList, OraganizeModel.class);
//            List<SumTree<OraganizeModel>> trees = TreeDotUtils.convertListToTreeDot(organizeModelList);
//            List<OraganizeListVO> listVO = JsonUtil.getJsonToList(trees, OraganizeListVO.class);
//
//            // 转化成为按上下层级顺序排序的列表数据
//            List<OrganizeEntity> listByOrder = new ArrayList<>();
//            for (OraganizeListVO organizeVo : listVO) {
//                OrganizeEntity entity = organizeList.stream().filter(t -> t.getId().equals(organizeVo.getId())).findFirst().orElse(null);
//                listByOrder.add(entity);
//                SynQyWebChatUtil.getOrganizeTreeToList(organizeVo, organizeList, listByOrder);
//            }
//
//            // 根据同步表、公司表进行比较，判断不存的执行删除
//            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
//                if (organizeList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdQyService.deleteDepartmentSysToQy(true, synThirdInfoEntity.getSysObjId());
//                }
//                if (qyDeptList.stream().filter(t -> t.getId().toString().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdQyService.deleteDepartmentSysToQy(true, synThirdInfoEntity.getSysObjId());
//                }
//            }
//
//            synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_ORG);
//            // 根据公司表、同步表进行比较，决定执行创建、还是更新
//            for (OrganizeEntity organizeEntity : listByOrder) {
//                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(organizeEntity.getId())).count() > 0 ? true : false) {
//                    // 执行更新功能
//                    retMsg = synThirdQyService.updateDepartmentSysToQy(true, organizeEntity);
//                } else {
//                    // 执行创建功能
//                    retMsg = synThirdQyService.createDepartmentSysToQy(true, organizeEntity);
//                }
//            }
//        }catch (Exception e){
//            ActionResult.fail(e.toString());
//        }
//        //获取结果
//        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG);
//        return ActionResult.success(synThirdTotal);
//    }


    /**
     * 本地所有用户信息同步到企业微信
     * 有带第三方错误定位判断的功能代码 20210604
     * @throws WxErrorException
     */
//    @ApiOperation("本地所有用户信息同步到企业微信")
//    @GetMapping("/synAllUserSysToQy")
//    public ActionResult synAllUserSysToQy() throws WxErrorException {
//        JSONObject retMsg = new JSONObject();
//        BaseSystemInfo config = synThirdQyService.getQyhConfig();
//        String corpId = config.getQyhCorpId();
//        String corpSecret = config.getQyhCorpSecret();
//        List<QyWebChatUserModel> qyUserList = new ArrayList<>();
//
//        try {
//            // 获取Token值
//            JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
//            if(!tokenObject.getBoolean("code")){
//                return ActionResult.fail("获取企业微信access_token失败");
//            }
//            String access_token = tokenObject.getString("access_token");
//
//            // 获取企业微信上的所有部门列表信息
//            if (access_token != null && !"".equals(access_token)) {
//                JSONObject userObject = SynQyWebChatUtil.getUserList(SynThirdConsts.QY_ROOT_DEPT_ID, "1", access_token);
//                if (userObject.getBoolean("code")) {
//                    qyUserList = JsonUtil.getJsonToList(userObject.getString("userlist"), QyWebChatUserModel.class);
//                } else {
//                    return ActionResult.fail("用户同步失败:获取企业微信的成员信息列表失败");
//                }
//            } else {
//                return ActionResult.fail("用户同步失败:获取企业微信access_token失败");
//            }
//
//            // 获取同步表、用户表的信息
//            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_USER);
//            List<UserEntity> userList = userService.getList();
//
//            // 根据同步表、公司表进行比较，判断不存的执行删除
//            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
//                if (userList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdQyService.deleteUserSysToQy(true, synThirdInfoEntity.getSysObjId());
//                }
//                if (qyUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdQyService.deleteUserSysToQy(true, synThirdInfoEntity.getSysObjId());
//                }
//            }
//
//            // 根据公司表、同步表进行比较，决定执行创建、还是更新
//            for (UserEntity userEntity : userList) {
//                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(userEntity.getId())).count() == 0 ? true : false) {
//                    // 执行创建功能
//                    retMsg = synThirdQyService.createUserSysToQy(true, userEntity);
//                } else {
//                    // 执行更新功能
//                    retMsg = synThirdQyService.updateUserSysToQy(true, userEntity);
//                }
//            }
//        }catch (Exception e){
//            ActionResult.fail(e.toString());
//        }
//        //获取结果
//        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER);
//        return ActionResult.success(synThirdTotal);
//    }


    /**
     * 本地所有组织信息(包含公司和部门)同步到企业微信
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @throws WxErrorException
     */
    @ApiOperation("本地所有组织信息(包含公司和部门)同步到企业微信")
    @GetMapping("/synAllOrganizeSysToQy")
    public ActionResult synAllOrganizeSysToQy() throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        BaseSystemInfo config = synThirdQyService.getQyhConfig();
        String corpId = config.getQyhCorpId();
        String corpSecret = config.getQyhCorpSecret();
        String access_token = "";
        try {
            // 获取Token值
            JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
            if(!tokenObject.getBoolean("code")){
                return ActionResult.fail("获取企业微信access_token失败");
            }
            access_token = tokenObject.getString("access_token");

            // 获取同步表、部门表的信息
            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_ORG);
            List<OrganizeEntity> organizeList = organizeService.getList();

            // 部门进行树结构化,固化上下层级序列化
            List<OraganizeModel> organizeModelList = JsonUtil.getJsonToList(organizeList, OraganizeModel.class);
            List<SumTree<OraganizeModel>> trees = TreeDotUtils.convertListToTreeDot(organizeModelList);
            List<OraganizeListVO> listVO = JsonUtil.getJsonToList(trees, OraganizeListVO.class);

            // 转化成为按上下层级顺序排序的列表数据
            List<OrganizeEntity> listByOrder = new ArrayList<>();
            for (OraganizeListVO organizeVo : listVO) {
                OrganizeEntity entity = organizeList.stream().filter(t -> t.getId().equals(organizeVo.getId())).findFirst().orElse(null);
                listByOrder.add(entity);
                SynQyWebChatUtil.getOrganizeTreeToList(organizeVo, organizeList, listByOrder);
            }

            // 根据同步表、公司表进行比较，判断不存的执行删除
            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                if (organizeList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
                    //执行删除操作
                    retMsg = synThirdQyService.deleteDepartmentSysToQy(true, synThirdInfoEntity.getSysObjId(),access_token);
                }
            }

            synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_ORG);
            // 根据公司表、同步表进行比较，决定执行创建、还是更新
            for (OrganizeEntity organizeEntity : listByOrder) {
                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(organizeEntity.getId())).count() > 0 ? true : false) {
                    // 执行更新功能
                    retMsg = synThirdQyService.updateDepartmentSysToQy(true, organizeEntity,access_token);
                } else {
                    // 执行创建功能
                    retMsg = synThirdQyService.createDepartmentSysToQy(true, organizeEntity,access_token);
                }
            }
        }catch (Exception e){
            ActionResult.fail(e.toString());
        }
        //获取结果
        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG);
        return ActionResult.success(synThirdTotal);
    }


    /**
     * 本地所有用户信息同步到企业微信
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @throws WxErrorException
     */
    @ApiOperation("本地所有用户信息同步到企业微信")
    @GetMapping("/synAllUserSysToQy")
    public ActionResult synAllUserSysToQy() throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        BaseSystemInfo config = synThirdQyService.getQyhConfig();
        String corpId = config.getQyhCorpId();
        String corpSecret = config.getQyhCorpSecret();
        String access_token = "";

        try {
            // 获取Token值
            JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
            if(!tokenObject.getBoolean("code")){
                return ActionResult.fail("获取企业微信access_token失败");
            }
            access_token = tokenObject.getString("access_token");

            // 获取同步表、用户表的信息
            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_USER);
            List<UserEntity> userList = userService.getList();

            // 根据同步表、公司表进行比较，判断不存的执行删除
            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                if (userList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
                    //执行删除操作
                    retMsg = synThirdQyService.deleteUserSysToQy(true, synThirdInfoEntity.getSysObjId(),access_token);
                }
            }

            // 根据公司表、同步表进行比较，决定执行创建、还是更新
            for (UserEntity userEntity : userList) {
                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(userEntity.getId())).count() == 0 ? true : false) {
                    // 执行创建功能
                    retMsg = synThirdQyService.createUserSysToQy(true, userEntity,access_token);
                } else {
                    // 执行更新功能
                    retMsg = synThirdQyService.updateUserSysToQy(true, userEntity,access_token);
                }
            }
        }catch (Exception e){
            ActionResult.fail(e.toString());
        }

        //获取结果
        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER);
        return ActionResult.success(synThirdTotal);
    }


    //==================================本系统的公司-部门-用户批量同步到钉钉==================================

    /**
     * 本地所有组织信息(包含公司和部门)同步到钉钉
     * 有带第三方错误定位判断的功能代码 20210604
     */
//    @ApiOperation("本地所有组织信息(包含公司和部门)同步到钉钉")
//    @GetMapping("/synAllOrganizeSysToDing")
//    public ActionResult synAllOrganizeSysToDing(){
//        JSONObject retMsg = new JSONObject();
//        BaseSystemInfo config = synThirdDingTalkService.getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//
//        try {
//            // 获取Token值
//            JSONObject tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//            if(!tokenObject.getBoolean("code")){
//                return ActionResult.fail("获取企业微信access_token失败");
//            }
//            String access_token = tokenObject.getString("access_token");
//
//            // 获取钉钉上的所有部门列表信息
//            if (access_token != null && !"".equals(access_token)) {
//                JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
//                if (deptObject.getBoolean("code")) {
//                    dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                } else {
//                    return ActionResult.fail("组织同步失败:获取钉钉的部门信息列表失败");
//                }
//            } else {
//                return ActionResult.fail("组织同步失败:获取钉钉的access_token失败");
//            }
//
//            // 获取同步表、部门表的信息
//            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_ORG);
//            List<OrganizeEntity> organizeList = organizeService.getList();
//
//            // 部门进行树结构化,固化上下层级序列化
//            List<OraganizeModel> organizeModelList = JsonUtil.getJsonToList(organizeList, OraganizeModel.class);
//            List<SumTree<OraganizeModel>> trees = TreeDotUtils.convertListToTreeDot(organizeModelList);
//            List<OraganizeListVO> listVO = JsonUtil.getJsonToList(trees, OraganizeListVO.class);
//
//            // 转化成为按上下层级顺序排序的列表数据
//            List<OrganizeEntity> listByOrder = new ArrayList<>();
//            for (OraganizeListVO organizeVo : listVO) {
//                OrganizeEntity entity = organizeList.stream().filter(t -> t.getId().equals(organizeVo.getId())).findFirst().orElse(null);
//                listByOrder.add(entity);
//                SynDingTalkUtil.getOrganizeTreeToList(organizeVo, organizeList, listByOrder);
//            }
//
//            // 根据同步表、公司表进行比较，判断不存的执行删除
//            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
//                if (organizeList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdDingTalkService.deleteDepartmentSysToDing(true, synThirdInfoEntity.getSysObjId(), dingDeptList);
//                }
//                if (dingDeptList.stream().filter(t -> t.getDeptId().toString().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdDingTalkService.deleteDepartmentSysToDing(true, synThirdInfoEntity.getSysObjId(), dingDeptList);
//                }
//                // 手工清除(到钉钉取需要递归取很慢)
//                if(retMsg.getBoolean("code") && !"".equals(synThirdInfoEntity.getThirdObjId()) && !"null".equals(synThirdInfoEntity.getThirdObjId())) {
//                    List<DingTalkDeptModel> deleteDeptList = dingDeptList.stream().filter(t->t.getDeptId().equals(synThirdInfoEntity.getThirdObjId())).collect(Collectors.toList());
//                    if(deleteDeptList!=null){
//                        dingDeptList.removeAll(deleteDeptList);
//                    }
//                }
//            }
//
//            synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_ORG);
//            // 根据公司表、同步表进行比较，决定执行创建、还是更新
//            for (OrganizeEntity organizeEntity : listByOrder) {
//                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(organizeEntity.getId())).count() > 0 ? true : false) {
//                    // 执行更新功能
//                    retMsg = synThirdDingTalkService.updateDepartmentSysToDing(true, organizeEntity, dingDeptList);
//                    // 手工添加(到钉钉取需要递归取很慢)
//                    if(retMsg.getBoolean("code")) {
//                        String dingDeptId=retMsg.getString("retDeptId");
//                        if(dingDeptList.stream().filter(t->t.getDeptId().equals(dingDeptId)).count()==0?true:false) {
//                            DingTalkDeptModel dingDept = new DingTalkDeptModel();
//                            dingDept.setDeptId(Long.parseLong(dingDeptId));
//                            dingDeptList.add(dingDept);
//                        }
//                    }
//                } else {
//                    // 执行创建功能
//                    retMsg = synThirdDingTalkService.createDepartmentSysToDing(true, organizeEntity, dingDeptList);
//                    // 手工添加(到钉钉取需要递归取很慢)
//                    if(retMsg.getBoolean("code")) {
//                        DingTalkDeptModel dingDept = new DingTalkDeptModel();
//                        dingDept.setDeptId(Long.parseLong(retMsg.getString("retDeptId")));
//                        dingDeptList.add(dingDept);
//                    }
//
//                }
//            }
//        }catch (Exception e){
//            ActionResult.fail(e.toString());
//        }
//
//        //获取结果
//        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG);
//        return ActionResult.success(synThirdTotal);
//    }


    /**
     * 本地所有用户信息同步到钉钉
     * 有带第三方错误定位判断的功能代码 20210604
     */
//    @ApiOperation("本地所有用户信息同步到钉钉")
//    @GetMapping("/synAllUserSysToDing")
//    public ActionResult synAllUserSysToDing() throws ParseException {
//        JSONObject retMsg = new JSONObject();
//        BaseSystemInfo config = synThirdDingTalkService.getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        List<DingTalkUserModel> dingUserList = new ArrayList<>();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        try {
//            // 获取Token值
//            JSONObject tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//            if(!tokenObject.getBoolean("code")){
//                return ActionResult.success("获取企业微信access_token失败");
//            }
//            String access_token = tokenObject.getString("access_token");
//
//            // 获取钉钉上的所有部门列表信息
//            if (access_token != null && !"".equals(access_token)) {
//                JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
//                if (deptObject.getBoolean("code")) {
//                    dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                } else {
//                    return ActionResult.fail("组织同步失败:获取钉钉的部门信息列表失败");
//                }
//            } else {
//                return ActionResult.fail("组织同步失败:获取钉钉的access_token失败");
//            }
//
//            // 获取钉钉上的所有部门列表信息
//            if (access_token != null && !"".equals(access_token)) {
//                JSONObject userObject = SynDingTalkUtil.getUserList(dingDeptList, access_token);
//                if (userObject.getBoolean("code")) {
//                    dingUserList = JsonUtil.getJsonToList(userObject.getObject("userlist", List.class), DingTalkUserModel.class);
//                } else {
//                    return ActionResult.fail("用户同步失败:获取钉钉的成员信息列表失败");
//                }
//            } else {
//                return ActionResult.fail("用户同步失败:获取钉钉的access_token失败");
//            }
//
//            // 获取同步表、用户表的信息
//            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_USER);
//            List<UserEntity> userList = userService.getList();
//
//            // 根据同步表、公司表进行比较，判断不存的执行删除
//            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
//                if (userList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdDingTalkService.deleteUserSysToDing(true, synThirdInfoEntity.getSysObjId(), dingDeptList, dingUserList);
//                }
//                if (dingUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdDingTalkService.deleteUserSysToDing(true, synThirdInfoEntity.getSysObjId(), dingDeptList, dingUserList);
//                }
//                // 手工清除(到钉钉取需要递归取很慢)
//                if(retMsg.getBoolean("code") && !"".equals(synThirdInfoEntity.getThirdObjId()) && !"null".equals(synThirdInfoEntity.getThirdObjId())) {
//                    List<DingTalkUserModel> deleteUserList = dingUserList.stream().filter(t->t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).collect(Collectors.toList());
//                    if(deleteUserList!=null){
//                        dingUserList.removeAll(deleteUserList);
//                    }
//                }
//            }
//
//            // 根据公司表、同步表进行比较，决定执行创建、还是更新
//            for (UserEntity userEntity : userList) {
//                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(userEntity.getId())).count() == 0 ? true : false) {
//                    // 执行创建功能
//                    retMsg = synThirdDingTalkService.createUserSysToDing(true, userEntity, dingDeptList, dingUserList);
//                    // 手工添加(到钉钉取需要递归取很慢)
//                    if(retMsg.getBoolean("code")) {
//                        DingTalkUserModel dingUser = new DingTalkUserModel();
//                        dingUser.setUserid(userEntity.getId());
//                        dingUser.setMobile(userEntity.getMobilePhone());
//                        dingUser.setEmail(userEntity.getEmail());
//                        dingUserList.add(dingUser);
//                    }
//                } else {
//                    // 执行更新功能
//                    retMsg = synThirdDingTalkService.updateUserSysToDing(true, userEntity, dingDeptList, dingUserList);
//                    // 手工添加(到钉钉取需要递归取很慢)
//                    if(retMsg.getBoolean("code")) {
//                        String dingUserId=userEntity.getId();
//                        if(dingUserList.stream().filter(t->t.getUserid().equals(dingUserId)).count()==0?true:false) {
//                            DingTalkUserModel dingUser = new DingTalkUserModel();
//                            dingUser.setUserid(dingUserId);
//                            dingUser.setMobile(userEntity.getMobilePhone());
//                            dingUser.setEmail(userEntity.getEmail());
//                            dingUserList.add(dingUser);
//                        }
//                    }
//                }
//            }
//        }catch (Exception e){
//            ActionResult.fail(e.toString());
//        }
//
//        //获取结果
//        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER);
//        return ActionResult.success(synThirdTotal);
//    }



    /**
     * 本地所有组织信息(包含公司和部门)同步到钉钉
     * 不带第三方错误定位判断的功能代码 20210604
     */
    @ApiOperation("本地所有组织信息(包含公司和部门)同步到钉钉")
    @GetMapping("/synAllOrganizeSysToDing")
    public ActionResult synAllOrganizeSysToDing(){
        JSONObject retMsg = new JSONObject();
        BaseSystemInfo config = synThirdDingTalkService.getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();

        try {
            // 获取Token值
            JSONObject tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
            if(!tokenObject.getBoolean("code")){
                return ActionResult.fail("获取钉钉的access_token失败");
            }
            String access_token = tokenObject.getString("access_token");

            // 获取同步表、部门表的信息
            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_ORG);
            List<OrganizeEntity> organizeList = organizeService.getList();

            // 部门进行树结构化,固化上下层级序列化
            List<OraganizeModel> organizeModelList = JsonUtil.getJsonToList(organizeList, OraganizeModel.class);
            List<SumTree<OraganizeModel>> trees = TreeDotUtils.convertListToTreeDot(organizeModelList);
            List<OraganizeListVO> listVO = JsonUtil.getJsonToList(trees, OraganizeListVO.class);

            // 转化成为按上下层级顺序排序的列表数据
            List<OrganizeEntity> listByOrder = new ArrayList<>();
            for (OraganizeListVO organizeVo : listVO) {
                OrganizeEntity entity = organizeList.stream().filter(t -> t.getId().equals(organizeVo.getId())).findFirst().orElse(null);
                listByOrder.add(entity);
                SynDingTalkUtil.getOrganizeTreeToList(organizeVo, organizeList, listByOrder);
            }

            // 根据同步表、公司表进行比较，判断不存的执行删除
            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                if (organizeList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
                    //执行删除操作
                    retMsg = synThirdDingTalkService.deleteDepartmentSysToDing(true, synThirdInfoEntity.getSysObjId(),access_token);
                }
            }

            synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_ORG);
            // 根据公司表、同步表进行比较，决定执行创建、还是更新
            for (OrganizeEntity organizeEntity : listByOrder) {
                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(organizeEntity.getId())).count() > 0 ? true : false) {
                    // 执行更新功能
                    retMsg = synThirdDingTalkService.updateDepartmentSysToDing(true, organizeEntity,access_token);
                } else {
                    // 执行创建功能
                    retMsg = synThirdDingTalkService.createDepartmentSysToDing(true, organizeEntity,access_token);
                }
            }
        }catch (Exception e){
            ActionResult.fail(e.toString());
        }

        //获取结果
        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG);
        return ActionResult.success(synThirdTotal);
    }


    /**
     * 本地所有用户信息同步到钉钉
     * 不带第三方错误定位判断的功能代码 20210604
     */
    @ApiOperation("本地所有用户信息同步到钉钉")
    @GetMapping("/synAllUserSysToDing")
    public ActionResult synAllUserSysToDing() throws ParseException {
        JSONObject retMsg = new JSONObject();
        BaseSystemInfo config = synThirdDingTalkService.getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();

        try {
            // 获取Token值
            JSONObject tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
            if(!tokenObject.getBoolean("code")){
                return ActionResult.success("获取钉钉的access_token失败");
            }
            String access_token = tokenObject.getString("access_token");

            // 获取同步表、用户表的信息
            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_USER);
            List<UserEntity> userList = userService.getList();

            // 根据同步表、公司表进行比较，判断不存的执行删除
            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                if (userList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
                    // 执行删除操作
                    retMsg = synThirdDingTalkService.deleteUserSysToDing(true, synThirdInfoEntity.getSysObjId(),access_token);
                }
            }

            // 根据公司表、同步表进行比较，决定执行创建、还是更新
            for (UserEntity userEntity : userList) {
                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(userEntity.getId())).count() == 0 ? true : false) {
                    // 执行创建功能
                    retMsg = synThirdDingTalkService.createUserSysToDing(true, userEntity,access_token);
                } else {
                    // 执行更新功能
                    retMsg = synThirdDingTalkService.updateUserSysToDing(true, userEntity,access_token);
                }
            }
        }catch (Exception e){
            ActionResult.fail(e.toString());
        }

        //获取结果
        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER);
        return ActionResult.success(synThirdTotal);
    }

}
