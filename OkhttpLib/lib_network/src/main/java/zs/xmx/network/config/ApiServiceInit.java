package zs.xmx.network.config;

import android.content.Context;

/*
 * @创建者     默小铭
 * @博客       http://blog.csdn.net/u012792686
 * @本类描述	  网络库初始化
 * @内容说明
 *
 */
public class ApiServiceInit {
    public static Configurator newInstance(Context context) {
        Configurator.getInstance()
                .getConfigs()
                .put(ConfigKeys.APPLICATION_CONTEXT.name(),
                        context.getApplicationContext());
        return Configurator.getInstance();
    }

    private static Configurator getConfigurator() {
        return Configurator.getInstance();
    }

    /**
     * 其他类通过ConfigKeys定义的枚举,获取对应的参数
     */
    public static <T> T getConfiguration(Object key) {
        return getConfigurator().getConfiguration(key);
    }

    public static Context getApplicationContext() {
        return getConfiguration(ConfigKeys.APPLICATION_CONTEXT.name());
    }

}
