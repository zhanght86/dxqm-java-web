package jnpf.utils;

import jnpf.model.YozoFileParams;
import jnpf.model.YozoParams;
import org.springframework.util.StringUtils;

/**
 * @author JNPF开发平台组
 */
public class SplicingUrlUtil {
    /**
     * 永中预览url拼接
     * @param params
     * @return
     */
    public static String getPreviewUrl( YozoFileParams params) {
        StringBuilder paramsUrl = new StringBuilder();
        if (!StringUtils.isEmpty(params.getNoCache())) {
            paramsUrl.append("&noCache=" + params.getNoCache());
        }
        if (!StringUtils.isEmpty(params.getWatermark())) {
            paramsUrl.append("&watermark=" + params.getWatermark());
        }
        if (!StringUtils.isEmpty(params.getIsCopy())) {
            paramsUrl.append("&isCopy=" + params.getIsCopy());
        }
        if (!StringUtils.isEmpty(params.getPageStart())) {
            paramsUrl.append("&pageStart=" + params.getPageStart());
        }
        if (!StringUtils.isEmpty(params.getPageEnd())) {
            paramsUrl.append("&pageEnd=" + params.getPageEnd());
        }
        if (!StringUtils.isEmpty(params.getType())) {
            paramsUrl.append("&type=" + params.getType());
        }
        String s = paramsUrl.toString();
        String previewUrl= YozoParams.DOMAIN+"?k=" + YozoParams.DOMAIN_KEY + "&url=" + params.getUrl() + s;
        return previewUrl;
    }

}
