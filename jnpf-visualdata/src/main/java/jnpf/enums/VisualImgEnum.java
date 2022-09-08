package jnpf.enums;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
public enum VisualImgEnum {
    /**
     * 背景图片
     */
    BG("0", "bg"),
    /**
     * 图片框
     */
    BORDER("1", "border"),
    /**
     * 图片
     */
    SOURCE("2", "source"),
    /**
     * banner
     */
    BANNER("3", "banner"),
    /**
     * 大屏截图
     */
    SCREENSHOT("4", "screenShot");

    private String code;
    private String message;

    VisualImgEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 判断名称是否存在
     *
     * @return boolean
     */
    public static VisualImgEnum getByMessage(String type) {
        for (VisualImgEnum status : VisualImgEnum.values()) {
            if (status.getMessage().equals(type)) {
                return status;
            }
        }
        return null;
    }

}
