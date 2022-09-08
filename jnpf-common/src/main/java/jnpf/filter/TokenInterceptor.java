package jnpf.filter;

import jnpf.base.ActionResult;
import jnpf.base.ActionResultCode;
import jnpf.base.UserInfo;
import jnpf.config.ConfigValueUtil;
import jnpf.util.*;
import jnpf.util.context.SpringContext;
import jnpf.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:58
 */
public class TokenInterceptor implements HandlerInterceptor {


    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private RedisUtil redisUtil;

    private void init() {
        userProvider = SpringContext.getBean(UserProvider.class);
        configValueUtil = SpringContext.getBean(ConfigValueUtil.class);
        redisUtil = SpringContext.getBean(RedisUtil.class);
    }

    /**
     * 在请求处理之前进行调用（Controller方法调用之前）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        init();
        String path = request.getServletPath();
        System.out.println("请求路径:" + path);
        String method = request.getMethod();
        if ("OPTIONS".equals(method)) {
            return true;
        }
        String token = UserProvider.getToken();
        UserInfo userInfo = userProvider.get();
        String realToken = JwtUtil.getRealToken(token);
        //测试版本可以关闭验证
        if ("false".equals(configValueUtil.getTestVersion())) {


            //token验证
            if (StringUtil.isEmpty(realToken) || !redisUtil.exists(realToken)) {
                ActionResult result = ActionResult.fail(ActionResultCode.SessionOverdue.getCode(), ActionResultCode.SessionOverdue.getMessage());
                ServletUtil.renderString(response, JsonUtil.getObjectToString(result));
                return false;
            }
            //是否过期
//            Date exp = JwtUtil.getExp(token);
//            if (exp.getTime() < System.currentTimeMillis()) {
//                ActionResult result = ActionResult.fail(ActionResultCode.SessionOverdue.getCode(), ActionResultCode.SessionOverdue.getMessage());
//                ServletUtil.renderString(response, JsonUtil.getObjectToString(result));
//                return false;
//            }
//            是否在线
            if (!userProvider.isOnLine()) {
                ActionResult result = ActionResult.fail(ActionResultCode.SessionOffLine.getCode(), ActionResultCode.SessionOffLine.getMessage());
                redisUtil.remove(realToken);
                ServletUtil.renderString(response, JsonUtil.getObjectToString(result));
                return false;
            }
            //增加在线过期时间
            tokenTimeout(userInfo);
        }
        return true;
    }

    /**
     * 请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
//        String path = request.getServletPath();
//        System.out.println("postHandle："+path);
    }

    /**
     * 在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//        String path = request.getServletPath();
//        System.out.println("afterCompletion："+path);
    }

    /**
     * 重新给redis中的token设置有效时间
     * @param userInfo
     */
    private void tokenTimeout(UserInfo userInfo){
        String tenantId = StringUtil.isNotEmpty(userInfo.getTenantId())?userInfo.getTenantId():"";
        String userId = userInfo.getUserId();
        String onlineInfo=tenantId+CacheKeyUtil.LOGINONLINE+userId;
        redisUtil.expire(onlineInfo,userInfo.getTokenTimeout()*60);
        redisUtil.expire(userInfo.getId(),userInfo.getTokenTimeout()*60);
    }

}
