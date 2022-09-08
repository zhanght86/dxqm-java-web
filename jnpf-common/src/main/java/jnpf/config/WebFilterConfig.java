package jnpf.config;

import jnpf.filter.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 9:00
 */
@Configuration
public class WebFilterConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册TestInterceptor拦截器
        InterceptorRegistration registration = registry.addInterceptor(new TokenInterceptor());
        //不拦截的路径
        registration.excludePathPatterns(excludePath());
    }

    private List<String> excludePath (){
        List<String> list = new ArrayList<>();
        list.add("/api/oauth/Login");
        list.add("/api/file/Uploader/**");
        list.add("/api/file/Download/**");
        list.add("/api/visualdev/Generater/DownloadVisCode/**");
        list.add("/api/file/DownloadModel/**");
        list.add("/api/file/Image/**");
        list.add("/api/file/ImageCode/**");
        list.add("/api/extend/DocumentPreview/**");
        list.add("/api/visualdev/DataScreen/Images/**");
        list.add("/api/extend/DocumentPreview/down/{fileName}");
        list.add("/api/system/DataMap/**");
        list.add("/api/file/**");
        list.add("/api/file/onlinePreview");
        list.add("/api/file/onlinePreview");
        //大屏图片
        list.add("/api/file/VisusalImg/**");
        list.add("/api/blade-visual/map/data");
        list.add("/api/blade-visual/visual/put-file/**");
        //避免拦截/websocket链接
        list.add("/message/websocket");
        list.add("/api/system/DictionaryData/{dictionaryTypeId}/Data/Selector");
        list.add("/swagger-ui/index.html");
        list.add("/swagger-resources/**");
        list.add("/webjars/**");
        list.add("/v3/**");
        list.add("/swagger-ui/**");
        list.add("/api/system/DataMap/{id}/Data");
        list.add("/api/system/DataInterface/{id}/Actions/Response");
        return list;
    }

}
