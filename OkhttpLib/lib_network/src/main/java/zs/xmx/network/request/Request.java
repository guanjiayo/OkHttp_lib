package zs.xmx.network.request;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import zs.xmx.network.ApiResponse;
import zs.xmx.network.Convert;
import zs.xmx.network.JsonCallback;
import zs.xmx.network.RestCreator;
import zs.xmx.network.cache.CacheManager;
import zs.xmx.network.config.ApiServiceInit;
import zs.xmx.network.config.ConfigKeys;

@SuppressWarnings({"unchecked", "unused"})
public abstract class Request<T, R extends Request> implements Cloneable {
    String mUrl;
    protected ConcurrentHashMap<String, String> headers = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, Object>           params  = new ConcurrentHashMap<>();

    //仅仅只访问本地缓存，即便本地缓存不存在，也不会发起网络请求
    static final int    CACHE_ONLY     = 1;
    //先访问缓存，同时发起网络的请求，成功后缓存到本地
    static final int    CACHE_FIRST    = 2;
    //仅仅只访问服务器，不存任何存储
    static final int    NET_ONLY       = 3;
    //先访问网络，成功后缓存到本地
    static final int    NET_CACHE      = 4;
    private      String cacheKey;
    private      Type   mType;
    private      int    mCacheStrategy = NET_ONLY;

    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_CACHE, NET_ONLY})
    @Retention(RetentionPolicy.SOURCE)
    @interface CacheStrategy {

    }

    Request(String url) {
        //user/list
        mUrl = url;
    }

    /**
     * 单个请求头
     */
    public R header(String key, String value) {
        headers.put(key, value);
        return (R) this;
    }

    /**
     * 多个请求头s
     */
    public R headers(HashMap<String, String> headers) {
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            headers.put(headerEntry.getKey(), headerEntry.getValue());
        }
        return (R) this;
    }


    /**
     * 单个params
     */
    public R param(String key, Object value) {
        if (value == null) {
            return (R) this;
        }
        //int byte char short long double float boolean 和他们的包装类型，但是除了 String.class 所以要额外判断
        try {
            if (value.getClass() == String.class) {
                params.put(key, value);
            } else {
                Field field = value.getClass().getField("TYPE");
                Class claz = (Class) field.get(null);
                if (claz != null && claz.isPrimitive()) {
                    params.put(key, value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) this;
    }

    /**
     * 多个params
     */
    public final R params(HashMap<String, Object> params) {
        for (Map.Entry<String, Object> paramsEntry : params.entrySet()) {
            param(paramsEntry.getKey(), paramsEntry.getValue());
        }
        return (R) this;
    }

    public R cacheStrategy(@CacheStrategy int cacheStrategy) {
        mCacheStrategy = cacheStrategy;
        return (R) this;
    }

    public R cacheKey(String key) {
        this.cacheKey = key;
        return (R) this;
    }

    public R responseType(Type type) {
        mType = type;
        return (R) this;
    }

    public R responseType(Class claz) {
        mType = claz;
        return (R) this;
    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        okhttp3.Request request = generateRequest(builder);
        return RestCreator.getOkHttpClient().newCall(request);
    }

    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    public ApiResponse<T> execute() {
        if (mType == null) {
            throw new RuntimeException("同步方法,response 返回值 类型必须设置");
        }

        if (mCacheStrategy == CACHE_ONLY) {
            return readCache();
        }

        ApiResponse<T> result;
        try {
            Response response = getCall().execute();
            result = parseResponse(response, null);
        } catch (IOException e) {
            e.printStackTrace();
            result = new ApiResponse<>();
            result.message = e.getMessage();
        }
        return result;
    }

    @SuppressLint("RestrictedApi")
    public void execute(final JsonCallback callback) {
        if (mCacheStrategy != NET_ONLY) {
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ApiResponse<T> response = readCache();
                    if (callback != null && response.body != null) {
                        callback.onCacheSuccess(response);
                    }
                }
            });
        }
        if (mCacheStrategy != CACHE_ONLY) {
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ApiResponse<T> result = new ApiResponse<>();
                    result.message = e.getMessage();
                    callback.onError(result);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    ApiResponse<T> result = parseResponse(response, callback);
                    if (!result.success) {
                        callback.onError(result);
                    } else {
                        callback.onSuccess(result);
                    }
                }
            });
        }
    }

    private ApiResponse<T> readCache() {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> result = new ApiResponse<>();
        result.status = 304;
        result.message = "缓存获取成功";
        result.body = (T) cache;
        result.success = true;
        return result;
    }

    private ApiResponse<T> parseResponse(Response response, JsonCallback<T> callback) {
        String message = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Convert convert = ApiServiceInit.getConfiguration(ConfigKeys.JSON_CONVERT);
        if (convert == null) {
            throw new RuntimeException("JSON CONVERT IS NULL");
        }
        try {
            String content = response.body().string();
            if (success) {
                if (callback != null) {
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];
                    result.body = (T) convert.convert(content, argument);
                } else if (mType != null) {
                    result.body = (T) convert.convert(content, mType);
                } else {
                    Log.e("request", "parseResponse: 无法解析 ");
                }
            } else {
                message = content;
            }
        } catch (Exception e) {
            message = e.getMessage();
            success = false;
            status = -1;
        }

        result.success = success;
        result.status = status;
        result.message = message;

        if (mCacheStrategy != NET_ONLY && result.success && result.body instanceof Serializable) {
            saveCache(result.body);
        }
        return result;
    }

    private void saveCache(T body) {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        CacheManager.save(key, body);
    }

    private String generateCacheKey() {
        cacheKey = UrlCreator.createUrlFromParams(mUrl, params);
        return cacheKey;
    }

    @NonNull
    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request<T, R>) super.clone();
    }
}
