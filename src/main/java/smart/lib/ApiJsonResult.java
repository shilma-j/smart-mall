package smart.lib;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * rest result json data
 */
public class ApiJsonResult {

    private final Map<String, Object> data = new LinkedHashMap<>();
    /**
     * 1:success 2: warn 3: error
     */
    private int status = 1;
    private String msg;

    /**
     * generate error result response
     *
     * @param msg error msg
     * @return result
     */
    public static ApiJsonResult error(String msg) {
        ApiJsonResult apiJsonResult = new ApiJsonResult();
        apiJsonResult.setStatus(3);
        apiJsonResult.setMsg(msg);
        return apiJsonResult;
    }

    /**
     * generate success result response
     *
     * @param msg success msg
     * @return result
     */
    public static ApiJsonResult success(String msg) {
        ApiJsonResult apiJsonResult = new ApiJsonResult();
        apiJsonResult.setMsg(msg);
        return apiJsonResult;
    }

    /**
     * generate warn result response
     *
     * @param msg warn msg
     * @return result
     */
    public static ApiJsonResult warn(String msg) {
        ApiJsonResult apiJsonResult = new ApiJsonResult();
        apiJsonResult.setStatus(2);
        apiJsonResult.setMsg(msg);
        return apiJsonResult;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getMsg() {
        return msg == null ? "" : msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", System.currentTimeMillis());
        map.put("status", status);
        map.put("msg", msg);
        map.put("data", data);
        return Json.encode(map);
    }
}
