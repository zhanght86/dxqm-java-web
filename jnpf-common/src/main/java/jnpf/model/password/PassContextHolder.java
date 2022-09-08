package jnpf.model.password;

/**
 * 给token提供username
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 17:24
 */
public class PassContextHolder {

    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置用户名
     */
    public static void setUserName(String dbName) {
        THREAD_LOCAL.set(dbName);
    }
    /**
     * 获取当前用户
     */
    public static String getUserName() {
        String str = THREAD_LOCAL.get();
        return str;
    }
    /**
     * 移除当前线程变量
     */
    public static void removeUserName() {
        THREAD_LOCAL.remove();
    }
}
