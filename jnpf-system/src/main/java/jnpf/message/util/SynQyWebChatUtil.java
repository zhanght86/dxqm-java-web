package jnpf.message.util;

import com.alibaba.fastjson.JSONObject;
import jnpf.exception.WxErrorException;
import jnpf.message.model.message.OraganizeListVO;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.util.wxutil.HttpUtil;

import java.util.Iterator;
import java.util.List;

/**
 * 同步到企业微信的接口
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/21 8:20
 */
public class SynQyWebChatUtil {
    /**
     * token 接口
     */
    public static final String TOKEN = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";

    //--------------------------------------------部门--------------------------------------

    /**
     * 创建部门
     */
    public static final String CREATE_DEPARTMENT = "https://qyapi.weixin.qq.com/cgi-bin/department/create?access_token=%s";

    /**
     * 更新部门
     */
    public static final String UPDATE_DEPARTMENT = "https://qyapi.weixin.qq.com/cgi-bin/department/update?access_token=%s";

    /**
     * 删除部门
     */
    public static final String DELETE_DEPARTMENT = "https://qyapi.weixin.qq.com/cgi-bin/department/delete?access_token=%s&id=%s";

    /**
     * 获取部门列表
     */
    public static final String GET_DEPARTMENT_LIST = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=%s&id=%s";

    //-------------------------------------------用户-----------------------------------------------------

    /**
     * 创建用户
     */
    public static final String CREATE_USER = "https://qyapi.weixin.qq.com/cgi-bin/user/create?access_token=%s";

    /**
     * 更新用户
     */
    public static final String UPDATE_USER = "https://qyapi.weixin.qq.com/cgi-bin/user/update?access_token=%s";

    /**
     * 删除用户
     */
    public static final String DELETE_USER = "https://qyapi.weixin.qq.com/cgi-bin/user/delete?access_token=%s&userid=%s";

    /**
     * 获取用户列表(返回精简的员工信息列表)
     */
    public static final String GET_USER_LIST = "https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token=%s&department_id=%s&fetch_child=%s";

    /**
     * 获取用户列表(返回详细的员工信息列表)
     */
    public static final String GET_USER_DETAIL_LIST = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token=%s&department_id=%s&fetch_child=%s";

    /**
     * 获取单个成员信息
     */
    public static final String GET_SINGLE_USER = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=%s&userid=%s";


    //-------------------------------------上传---------------------------------------------------

    /**
     * 上传素材
     */
    public static final String MEDIA_UPLOAD = "https://qyapi.weixin.qq.com/cgi-bin/media/upload?access_token=%s&type=%s";

    //-------------------------------------消息--------------------------------------------------

    /**
     * 发送消息
     */
    public static final String SEND_MESSAGE = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=%s";


    /**
     * 获取接口访问凭证
     */
    public static JSONObject getAccessToken(String corpId, String corpSecret)  {
        JSONObject rstObj = HttpUtil.httpRequest(String.format(TOKEN,corpId, corpSecret), "GET", null);
        rstObj.put("code",true);
        if (HttpUtil.isWxError(rstObj)) {
            rstObj.put("code",false);
            rstObj.put("access_token","");
        }
        return rstObj;
    }

    /**
     * 发送消息 20210416 Add By GongXishan
     * 不抛出异常，返回Json
     */
    public static JSONObject sendMessage(String message, String accessToken){
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(SEND_MESSAGE, accessToken), "POST", message);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);
        return retMsg;
    }


    /**
     * 向企业微信发送信息
     * @param corpId
     * @param corpSecret
     * @param agentId
     * @param toUserId
     * @param contents
     * @return
     */
    public static JSONObject sendWxMessage(String corpId, String corpSecret, String agentId, String toUserId, String contents)  {
        JSONObject retMsg = null;
        JSONObject message = null;
        JSONObject tokenObject = null;
        JSONObject content = null;

        message = new JSONObject();
        message.put("touser", toUserId);
        message.put("agentid", agentId);
        content = new JSONObject();
        content.put("content", contents);
        message.put("text", content);
        message.put("msgtype", "text");
        tokenObject = getAccessToken(corpId, corpSecret);
        if(tokenObject.getString("access_token")!=null && !"".equals(tokenObject.getString("access_token"))){
            retMsg = sendMessage(message.toJSONString(), tokenObject.getString("access_token"));
        }else
        {
            retMsg.put("code",false);
            retMsg.put("error","access_token值为空,不能发送信息！");
        }
        return retMsg;
    }

    //------------------------------------企业微信接口：部门管理的增删改查-------------------------------------

    /**
     * 创建部门
     * @param department  json数据格式
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public static JSONObject createDepartment(String department, String accessToken) throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        String qyDeptId = "0";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(CREATE_DEPARTMENT, accessToken), "POST", department);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }else{
            codeFlag = true;
            qyDeptId = rstObj.getInteger("id").toString();
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);
        retMsg.put("retDeptId",qyDeptId);
        return retMsg;
    }

    /**
     * 更新部门
     * @param department  json数据格式
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public static JSONObject updateDepartment(String department, String accessToken) throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(UPDATE_DEPARTMENT, accessToken), "POST", department);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);

        return retMsg;
    }

    /**
     * 删除部门
     * @param id    部门ID
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public static JSONObject deleteDepartment(String id, String accessToken) throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(DELETE_DEPARTMENT, accessToken, id), "GET", null);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);

        return retMsg;
    }

    /**
     * 部门列表
     * @param id  根部门ID
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public static JSONObject getDepartmentList(String id, String accessToken) throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        String departmentStr = "";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(GET_DEPARTMENT_LIST, accessToken, id), "GET", null);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }else{
            departmentStr = rstObj.getString("department");
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);
        retMsg.put("department",departmentStr);
        return retMsg;
    }

    //------------------------------------------企业微信接口：用户管理的增删改查--------------------------------------------

    /**
     * 创建用户
     * @param user  json数据格式
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public static JSONObject createUser(String user, String accessToken) throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(CREATE_USER, accessToken), "POST", user);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);
        return retMsg;
    }

    /**
     * 更新用户
     * @param user  json数据格式
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public static JSONObject updateUser(String user, String accessToken) throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(UPDATE_USER, accessToken), "POST", user);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);
        return retMsg;
    }

    /**
     * 删除用户
     * @param id    用户ID
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public static JSONObject deleteUser(String id, String accessToken) throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(DELETE_USER, accessToken, id), "GET", null);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);
        return retMsg;
    }

    /**
     * 获取单个成员信息
     * @param id    企业微信成员ID
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public static JSONObject getUserById(String id, String accessToken) throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        String userInfo = "";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(GET_SINGLE_USER, accessToken, id), "GET", null);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }else{
            userInfo = rstObj.toJSONString();
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);
        retMsg.put("userinfo",userInfo);
        return retMsg;
    }


    /**
     * 获取用户列表(返回精简的成员信息)
     * @param id
     * @param isGetChild  1-递归获取，0-只获取本部门
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public static JSONObject getUserList(String id, String isGetChild, String accessToken) throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        String userList = "";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(GET_USER_LIST, accessToken, id,isGetChild), "GET", null);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }else{
            userList = rstObj.getString("userlist");
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);
        retMsg.put("userlist",userList);
        return retMsg;
    }

    /**
     * 获取用户列表(返回详细的成员信息)
     * @param id
     * @param isGetChild  1-递归获取，0-只获取本部门
     * @param accessToken
     * @return
     * @throws WxErrorException
     */
    public static JSONObject getUserDetailList(String id, String isGetChild, String accessToken) throws WxErrorException {
        JSONObject retMsg = new JSONObject();
        boolean codeFlag = true;
        String errorMsg = "";
        String userList = "";
        JSONObject rstObj = HttpUtil.httpRequest(String.format(GET_USER_DETAIL_LIST, accessToken, id,isGetChild), "GET", null);
        if (HttpUtil.isWxError(rstObj)) {
            codeFlag = false;
            errorMsg = rstObj.toString();
        }else{
            userList = rstObj.getString("userlist");
        }
        retMsg.put("code",codeFlag);
        retMsg.put("error",errorMsg);
        retMsg.put("userlist",userList);
        return retMsg;
    }


    /**
     * 按目录树结构数据转化为列表
     * @param selectorVO
     * @param organizeList
     * @param listByOrder
     */
    public static void getOrganizeTreeToList(OraganizeListVO selectorVO, List<OrganizeEntity> organizeList, List<OrganizeEntity> listByOrder){
        if(selectorVO.isHasChildren()) {
            List<OraganizeListVO> voChildren = selectorVO.getChildren();
            Iterator<OraganizeListVO> iterator = voChildren.iterator();
            while (iterator.hasNext()) {
                OraganizeListVO organizeSelectorVO = iterator.next();
                OrganizeEntity entity = organizeList.stream().filter(t -> t.getId().equals(organizeSelectorVO.getId())).findFirst().orElse(null);
                listByOrder.add(entity);
                if (organizeSelectorVO.isHasChildren()) {
                    getOrganizeTreeToList(organizeSelectorVO, organizeList, listByOrder);
                }
            }
        }
    }

}
