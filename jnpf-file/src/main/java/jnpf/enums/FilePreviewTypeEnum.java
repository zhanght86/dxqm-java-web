package jnpf.enums;
/**
 * 文件预览方式
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/5/6
 */
public enum FilePreviewTypeEnum {
    /**
     * yozo:永中预览;  doc:kk文档预览;
     */
    YOZO_ONLINE_PREVIEW("yozoOnlinePreview"),
    LOCAL_PREVIEW("localPreview");
    FilePreviewTypeEnum(String type) {
        this.type = type;
    }
    private String type;

    public String getType() {
        return type;
    }
}
