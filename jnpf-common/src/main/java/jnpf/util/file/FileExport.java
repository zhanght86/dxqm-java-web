package jnpf.util.file;

import jnpf.base.vo.DownloadVO;

/**
 * 导入导出工厂类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-04
 */
public interface FileExport {

    /**
     *  导出
     * @param obj           要转成Json的类
     * @param filePath      写入位置
     * @return
     */
    DownloadVO exportFile(Object obj, String filePath);

}
