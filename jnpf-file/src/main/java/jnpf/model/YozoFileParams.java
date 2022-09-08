package jnpf.model;


import lombok.Data;

/**
 * @author JNPF开发平台组
 */

@Data
public class YozoFileParams {
    private String url;
    /**
     * 是否强制重新转换（忽略缓存）,true为强制重新转换，false为不强制重新转换。
     */
    private Boolean noCache;

    /**
     * 针对单文档设置水印内容
     */
    private String watermark;

    /**
     * 0否1是，默认为0。针对单文档设置是否防复制
     */
    private Integer isCopy;

    /**
     * 试读功能（转换页数的起始页和转换页数的终止页，拥有对应权限的域名才能调用）
     */
    private Integer pageStart;
    private Integer pageEnd;

    /**
     * 用于无文件后缀链接，指定预览文件后缀名.
     */
    private String type;
}
