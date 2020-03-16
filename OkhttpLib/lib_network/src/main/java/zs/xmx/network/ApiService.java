package zs.xmx.network;

import zs.xmx.network.config.ApiServiceInit;
import zs.xmx.network.config.ConfigKeys;
import zs.xmx.network.request.GetRequest;
import zs.xmx.network.request.PostRequest;

public class ApiService {
    private static final String BASE_URL = ApiServiceInit.getConfiguration(ConfigKeys.NATIVE_API_HOST);

    public static <T> GetRequest<T> get(String url) {
        return new GetRequest<>(BASE_URL + url);
    }

    public static <T> PostRequest<T> post(String url) {
        return new PostRequest<>(BASE_URL + url);
    }
}
