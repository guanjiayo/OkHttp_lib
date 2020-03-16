package zs.xmx.okhttp;

import android.app.Application;

import okhttp3.logging.HttpLoggingInterceptor;
import zs.xmx.network.JsonConvert;
import zs.xmx.network.config.ApiServiceInit;

/*
 * @创建者     默小铭
 * @博客       http://blog.csdn.net/u012792686
 * @本类描述
 * @内容说明
 *
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiServiceInit.newInstance(this).withNativeApiHost("http://www.baidu.com")
                .withInterceptor(initLogInterceptor())
                .withJsonConvert(new JsonConvert())//TODO 这个得再抽出去一下
                .configure();

        ApiServiceInit.newInstance(this).withNativeApiHost("").configure();
    }

    /**
     * 初始化日志拦截器
     */
    private HttpLoggingInterceptor initLogInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }


}
