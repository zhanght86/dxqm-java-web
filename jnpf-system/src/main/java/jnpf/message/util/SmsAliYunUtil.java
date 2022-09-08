package jnpf.message.util;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

/**
 * 阿里云发送短信
 *
 * @版本： V3.2.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/21 11:45
 */
public class SmsAliYunUtil {
    public static JSONObject sendSms(String accessKeyId, String accessSecret,
                                     String templateId, String templateParam,
                                     String phoneNumbers, String signName) {
        JSONObject retMsg = new JSONObject();
        try {

            // 产品名称:云通信短信API产品,开发者无需替换
            String product = "Dysmsapi";
            // 产品域名,开发者无需替换
            String domain = "dysmsapi.aliyuncs.com";

            // 可自助调整超时时间
            System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
            System.setProperty("sun.net.client.defaultReadTimeout", "10000");

            // 初始化acsClient,暂不支持region化
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessSecret);
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
            IAcsClient acsClient = new DefaultAcsClient(profile);

            // 组装请求对象-具体描述见控制台-文档部分内容
            SendSmsRequest request = new SendSmsRequest();
            // 必填:待发送手机号
            // 必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式；发送国际/港澳台消息时，接收号码格式为国际区号+号码，如“85200000000”
            request.setPhoneNumbers(phoneNumbers);
            // 必填:短信签名-可在短信控制台中找到
            request.setSignName(signName);
            // 必填:短信模板-可在短信控制台中找到
            request.setTemplateCode(templateId);
            // 可选:模板中的变量替换JSON串,如模板内容为"亲爱的用户,您的验证码为${code}"时,此处的值为
            // 友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
            // 参考：request.setTemplateParam("{\"变量1\":\"值1\",\"变量2\":\"值2\",\"变量3\":\"值3\"}")
            // request.setTemplateParam("{\"code\":\"" + templateParam + "\"}");
            request.setTemplateParam(templateParam);


            // 选填-上行短信扩展码(无特殊需求用户请忽略此字段)
            // request.setSmsUpExtendCode("90997");

            // 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
            // request.setOutId("yourOutId");

            // hint 此处可能会抛出异常，注意catch
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            if(sendSmsResponse.getCode()!= null && sendSmsResponse.getCode().equals("OK")){
                retMsg.put("code",true);
                retMsg.put("error","");
            }else {
                retMsg.put("code",false);
                retMsg.put("error",sendSmsResponse.getCode());
            }
        }catch (ClientException e){
            retMsg.put("code",false);
            retMsg.put("error",e.getMessage());
        }
        return retMsg;
    }

}
