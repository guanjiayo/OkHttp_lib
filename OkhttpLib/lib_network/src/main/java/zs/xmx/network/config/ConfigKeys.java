package zs.xmx.network.config;

/*
 * @创建者     默小铭
 * @博客       http://blog.csdn.net/u012792686
 * @本类描述   配置信息的key枚举类
 * @内容说明   枚举虽然会损耗一点性能,但它的线程安全
 *   todo  后续可能扩展,如下载文件的路径,上传文件的路径,network的拦截器,动态切换baseUrl,okhttp的配置暴露出去
 */
public enum ConfigKeys {
    APPLICATION_CONTEXT,//全局上下文
    NATIVE_API_HOST,//服务器域名
    INTERCEPTOR,//拦截器
    JSON_CONVERT,//Json解析器
    CONFIG_READY//配置完成状态FLAG
}
