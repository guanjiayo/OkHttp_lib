package zs.xmx.network;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

/*
 * @创建者     默小铭
 * @博客       http://blog.csdn.net/u012792686
 * @本类描述	   Json转 Java Bean的转换器
 * @内容说明    注意要按自己服务器后台定义的json数据为准
 *
 */
public class JsonConvert implements Convert {
    //默认的Json转 Java Bean的转换器
    @Override
    public Object convert(String response, Type type) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            Object data1 = data.get("data");
            return JSON.parseObject(data1.toString(), type);
        }
        return null;
    }
}
