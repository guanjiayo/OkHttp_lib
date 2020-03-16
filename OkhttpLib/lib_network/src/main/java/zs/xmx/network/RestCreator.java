package zs.xmx.network;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import zs.xmx.network.config.ApiServiceInit;
import zs.xmx.network.config.ConfigKeys;
import zs.xmx.network.https.HttpsUtils;

/*
 * @创建者     默小铭
 * @博客       http://blog.csdn.net/u012792686
 * @本类描述	  生成OkHttpClient
 * @内容说明     todo  超时时间和拦截器由外部传进来
 *
 */
public class RestCreator {

    private static final int                    TIME_OUT     = 30;
    private static final ArrayList<Interceptor> INTERCEPTORS = ApiServiceInit.getConfiguration(ConfigKeys.INTERCEPTOR);
    private static final OkHttpClient.Builder   BUILDER      = new OkHttpClient.Builder();

    private static OkHttpClient.Builder addInterceptor() {
        if (INTERCEPTORS != null && !INTERCEPTORS.isEmpty()) {
            for (Interceptor interceptor : INTERCEPTORS) {
                //仅在response调用一次
                BUILDER.addInterceptor(interceptor);
                //request 和 response 都调用一次
                //BUILDER.addNetworkInterceptor(interceptor);
            }
        }
        return BUILDER;
    }

    public static OkHttpClient getOkHttpClient() {
        //https处理
        BUILDER.hostnameVerifier(new HostnameVerifier() {
            @SuppressLint("BadHostnameVerifier")
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        //拦截器
        if (INTERCEPTORS != null && !INTERCEPTORS.isEmpty()) {
            for (Interceptor interceptor : INTERCEPTORS) {
                //仅在response调用一次
                BUILDER.addInterceptor(interceptor);
                //request 和 response 都调用一次
                //BUILDER.addNetworkInterceptor(interceptor);
            }
        }
        BUILDER.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
        BUILDER.readTimeout(TIME_OUT, TimeUnit.SECONDS);
        BUILDER.writeTimeout(TIME_OUT, TimeUnit.SECONDS);
        /*
          trust all the https point
         */
        BUILDER.sslSocketFactory(HttpsUtils.initSSLSocketFactory(),
                HttpsUtils.initTrustManager());
        return BUILDER.build();
    }


}
