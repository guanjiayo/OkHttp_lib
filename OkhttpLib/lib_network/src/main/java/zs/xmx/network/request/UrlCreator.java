package zs.xmx.network.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/*
 * @创建者     默小铭
 * @博客       http://blog.csdn.net/u012792686
 * @本类描述	 Get请求的字符串拼接
 * @内容说明
 *
 */
class UrlCreator {
    static String createUrlFromParams(String url, Map<String, Object> params) {

        StringBuilder builder = new StringBuilder();
        builder.append(url);
        if (url.indexOf("?") > 0 || url.indexOf("&") > 0) {
            builder.append("&");
        } else {
            builder.append("?");
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            try {
                String value = URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8");
                builder.append(entry.getKey()).append("=").append(value).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
