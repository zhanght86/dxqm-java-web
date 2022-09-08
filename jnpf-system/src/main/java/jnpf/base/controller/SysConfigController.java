package jnpf.base.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.entity.EmailConfigEntity;
import jnpf.base.entity.SysConfigEntity;
import jnpf.base.model.systemconfig.EmailTestForm;
import jnpf.base.model.systemconfig.SysConfigModel;
import jnpf.base.service.SysconfigService;
import jnpf.message.entity.QyWebChatModel;
import jnpf.message.model.message.DingTalkModel;
import jnpf.message.util.DingTalkUtil;
import jnpf.message.util.QyWebChatUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.wxutil.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "系统配置", value =  "SysConfig")
@RestController
@RequestMapping("/api/system/SysConfig")
public class SysConfigController {

    @Autowired
    private SysconfigService sysconfigService;

    /**
     * 列表
     *
     * @return
     */
    @ApiOperation("列表")
    @GetMapping
    public ActionResult list() {
        List<SysConfigEntity> list = sysconfigService.getList("SysConfig");
        HashMap<String, String> map = new HashMap<>(16);
        for (SysConfigEntity sys : list) {
            map.put(sys.getFkey(), sys.getValue());
        }
        SysConfigModel sysConfigModel= JsonUtil.getJsonToBean(map,SysConfigModel.class);
        return ActionResult.success(sysConfigModel);
    }

    /**
     * 保存设置
     *
     * @return
     */
    @ApiOperation("更新系统配置")
    @PutMapping
    public ActionResult save(@RequestBody SysConfigModel sysConfigModel) {
        List<SysConfigEntity> entitys = new ArrayList<>();
        Map<String, Object> map = JsonUtil.entityToMap(sysConfigModel);
        map.put("isLog","1");
        map.put("sysTheme","1");
        map.put("pageSize","30");
        map.put("lastLoginTime",1);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            SysConfigEntity entity = new SysConfigEntity();
            entity.setId(RandomUtil.uuId());
            entity.setFkey(entry.getKey());
            entity.setValue(String.valueOf(entry.getValue()));
            entitys.add(entity);
        }
        sysconfigService.save(entitys);
        return ActionResult.success("操作成功");
    }

    /**
     * 邮箱账户密码验证
     *
     * @return
     */
    @ApiOperation("邮箱连接测试")
    @PostMapping("/Email/Test")
    public ActionResult checkLogin(@RequestBody EmailTestForm emailTestForm) {
        EmailConfigEntity entity = JsonUtil.getJsonToBean(emailTestForm, EmailConfigEntity.class);
        String result = sysconfigService.checkLogin(entity);
        if ("true".equals(result)) {
            return ActionResult.success("验证成功");
        } else {
            return ActionResult.fail(result);
        }
    }





    //=====================================测试企业微信、钉钉的连接=====================================

    /**
     * 测试企业微信配置的连接功能
     * @param qyWebChatModel
     * @param type              0-发送消息,1-同步组织
     * @return
     */
    @ApiOperation("测试企业微信配置的连接")
    @PostMapping("{type}/testQyWebChatConnect")
    public ActionResult testQyWebChatConnect(@PathVariable("type") String type, @RequestBody @Valid QyWebChatModel qyWebChatModel){
        JSONObject retMsg = new JSONObject();
        // 测试发送消息、组织同步的连接
        String corpId = qyWebChatModel.getQyhCorpId();
        String agentSecret = qyWebChatModel.getQyhAgentSecret();
        String corpSecret = qyWebChatModel.getQyhCorpSecret();
        // 测试发送消息的连接
        if ("0".equals(type)){
            retMsg = QyWebChatUtil.getAccessToken(corpId,agentSecret);
            if (HttpUtil.isWxError(retMsg)) {
                return ActionResult.fail("测试发送消息的连接失败："+retMsg.getString("errmsg"));
            }
            return ActionResult.success("测试发送消息连接成功");
        }else if ("1".equals(type)){
            retMsg = QyWebChatUtil.getAccessToken(corpId,corpSecret);
            if (HttpUtil.isWxError(retMsg)) {
                return ActionResult.fail("测试组织同步的连接失败："+retMsg.getString("errmsg"));
            }
            return ActionResult.success("测试组织同步连接成功");
        }
        return ActionResult.fail("测试连接类型错误");
    }

    /**
     * 测试钉钉配置的连接功能
     *
     * @param dingTalkModel
     * @return
     */
    @ApiOperation("测试钉钉配置的连接")
    @PostMapping("/testDingTalkConnect")
    public ActionResult testDingTalkConnect(@RequestBody @Valid DingTalkModel dingTalkModel) {
        JSONObject retMsg = new JSONObject();
        // 测试钉钉配置的连接
        String appKey = dingTalkModel.getDingSynAppKey();
        String appSecret = dingTalkModel.getDingSynAppSecret();
        String agentId = dingTalkModel.getDingAgentId();
        // 测试钉钉的连接
        retMsg = DingTalkUtil.getAccessToken(appKey,appSecret);
        if (!retMsg.getBoolean("code")) {
            return ActionResult.fail("测试钉钉连接失败："+retMsg.getString("error"));
        }

        return ActionResult.success("测试钉钉连接成功");
    }

}
