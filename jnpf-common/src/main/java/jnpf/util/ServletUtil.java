package jnpf.util;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:56
 */
public class ServletUtil {

    /**
     * 获取ServletPath
     */
    public static String getServletPath() {
        return ServletUtil.getRequest().getServletPath();
    }


    /**
     * 获取Request Payload
     */
    public static String getPayload() {
        ServletInputStream is;
        try {
            is = ServletUtil.getRequest().getInputStream();
            int nRead = 1;
            int nTotalRead = 0;
            byte[] bytes = new byte[10240 * 20];
            while (nRead > 0) {
                nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
                if (nRead > 0) {
                    nTotalRead = nTotalRead + nRead;
                }
            }
            String str = new String(bytes, 0, nTotalRead, Constants.UTF_8);
            return str;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }



    /**
     * 获取User-Agent
     */
    public static String getUserAgent() {
        return ServletUtil.getHeader("User-Agent");
    }

    /**
     * 判断是否是手机端登陆
     */
    public static boolean getIsMobileDevice() {
        return isMobileDevice(ServletUtil.getUserAgent());
    }

    /**
     * 获取HTTP头信息
     */
    public static String getHeader(String name) {
        if(getRequest()!=null){
            return getRequest().getHeader(name);
        }
        return null;
    }

    /**
     * 获取表单参数
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取String参数
     */
    public static String getParameter(String name, String defaultValue) {
        return ConvertUtil.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name) {
        return ConvertUtil.toInt(getRequest().getParameter(name));
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return ConvertUtil.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest() {
        if(getRequestAttributes()!=null){
            return getRequestAttributes().getRequest();
        }
        return null;
    }

    /**
     * 获取response
     */
    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取session
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     * @return null
     */
    public static String renderString(HttpServletResponse response, String string) {
        try {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Cache-Control","no-cache");
            response.setContentType("application/json");
            response.setCharacterEncoding(Constants.UTF_8);
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 是否是Ajax异步请求
     *
     * @param request
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String accept = request.getHeader("accept");
        if (accept != null && accept.indexOf("application/json") != -1) {
            return true;
        }
        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.indexOf("XMLHttpRequest") != -1) {
            return true;
        }
        String uri = request.getRequestURI();
        if (inStringIgnoreCase(uri, ".json", ".xml")) {
            return true;
        }
        String ajax = request.getParameter("__ajax");
        if (inStringIgnoreCase(ajax, "json", "xml")) {
            return true;
        }
        return false;
    }

    /**
     * 是否包含字符串
     *
     * @param str  验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase((s.trim()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 返回JSONObject对象
     */
    public static JSONObject getJsonObject() {
        String builder = ServletUtil.getPayload();
        return JSONObject.parseObject(builder);
    }

    /**
     * 判断是否是移动设备
     * @param requestHeader
     * @return
     */
    public static boolean isMobileDevice(String requestHeader) {
        String[] deviceArray = new String[]{"android", "windows phone", "iphone", "ios", "ipad" ,"mqqbrowser"};
        if (requestHeader == null) {
            return false;
        }
        requestHeader = requestHeader.toLowerCase();
        for (int i = 0; i < deviceArray.length; i++) {
            if (requestHeader.indexOf(deviceArray[i]) > 0) {
                return true;
            }
        }
        return false;
    }
}
