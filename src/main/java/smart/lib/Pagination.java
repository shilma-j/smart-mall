package smart.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import smart.config.AppConfig;
import smart.entity.BaseEntity;
import smart.util.DbUtils;
import smart.util.RequestUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 分页类
 */
public final class Pagination {
    private static final Pattern patternSelect = Pattern.compile("^(\\s*select.+)\\sfrom\\s+",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    private static final Pattern patternOrderBy = Pattern.compile("(\\sorder\\s+by\\s+.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    private final long pageSize;
    private final Object[] params;
    private final String sql;
    private final List<Map<String, Object>> pages = new LinkedList<>();
    private long currentPage;
    private List<Map<String, Object>> rows;
    /**
     * http get method query
     */
    private String httpQuery = "?";
    private long startIndex;
    private long endIndex;
    private long totalPages;
    private long totalRecords = 0;

    /**
     * 初始化分页类
     *
     * @param builder builder
     */
    private Pagination(Builder builder) {
        this.sql = builder.sql;
        this.params = builder.params;
        this.pageSize = builder.pageSize;
        if (builder.query != null) {
            builder.query.forEach((k, v) -> httpQuery += String.format("%s=%s&",
                    URLEncoder.encode(k, StandardCharsets.UTF_8), URLEncoder.encode(v, StandardCharsets.UTF_8)));
        }
        initTotalRecords();
        totalPages = totalRecords / pageSize;
        if (totalRecords % pageSize > 0) {
            totalPages++;
        }
        currentPage = builder.page > 0 ? builder.page : 1;
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        long pageOffset = currentPage - 6;
        if (totalPages - pageOffset < 10) {
            pageOffset = totalPages - 10;
        }
        if (pageOffset < 0) {
            pageOffset = 0L;
        }
        // 显示页数范围10页
        for (long l = pageOffset; l < pageOffset + 10 && l < totalPages; ) {
            l++;
            Map<String, Object> map = new HashMap<>();
            map.put("num", l);
            pages.add(map);
        }
        initRows();
    }

    /**
     * 获取记录条数
     *
     * @param sql select语句
     * @return 记录数
     */
    private static String getCountSql(String sql) {
        Matcher matcher = patternSelect.matcher(sql);
        if (matcher.find()) {
            String g = matcher.group(1);
            sql = sql.substring(g.length());
            var matcher1 = patternOrderBy.matcher(sql);
            if (matcher1.find()) {
                String orderBy = matcher1.group(1);
                sql = sql.substring(0, sql.length() - orderBy.length());
            }
            return "select count(*)" + sql;

        } else {
            return null;
        }
    }

    /**
     * new builder
     *
     * @param sql    查询sql
     * @param params 参数
     * @return builder
     */
    public static Builder newBuilder(String sql, Object... params) {
        return new Builder().sql(sql, params);
    }

    /**
     * 获取当前页的记录
     *
     * @return 当前页记录
     */
    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public <T extends BaseEntity> List<T> getRows(Class<T> t) {
        ObjectMapper mapper = AppConfig.getContext().getBean(ObjectMapper.class);
        List<T> result = new ArrayList<>();
        rows.forEach(row -> result.add(mapper.convertValue(row, t)));
        return result;
    }

    /**
     * 初始化当前页记录
     */
    private void initRows() {
        startIndex = (currentPage - 1) * pageSize;
        if (startIndex < 0) {
            startIndex = 0;
        }
        String sql1 = String.format("%s limit %d,%d", sql, startIndex, pageSize);
        rows = new ArrayList<>();
        AppConfig.getJdbcTemplate().queryForList(sql1, params).forEach(row -> {
            Map<String, Object> row1 = new LinkedHashMap<>();
            row.keySet().forEach(key -> row1.put(DbUtils.underscoresToCamelCaseNaming(key), row.get(key)));
            rows.add(row1);
        });

        endIndex = startIndex + rows.size();
    }

    /**
     * 生成网页分页页脚
     *
     * @return html
     */
    public String generateWebPagination() {
        StringBuilder html = new StringBuilder("<ul class=\"pagination\">\n");
        if (currentPage == 1) {
            html.append("<li class=\"page-item disabled\">" +
                    "<a class=\"page-link\" aria-disabled=\"true\">首页</a></li>\n");
        } else {
            html.append(String.format("<li class=\"page-item\">" +
                    "<a class=\"page-link\" aria-disabled=\"true\" href=\"%spage=1\">首页</a></li>\n", httpQuery));
        }
        long curPageNum;
        for (Map<String, Object> page : pages) {
            curPageNum = (long) page.get("num");
            if (currentPage == curPageNum) {
                html.append(String.format(" <li class=\"page-item active\"><a class=\"page-link\">%d</a></li>\n",
                        curPageNum));
            } else {
                html.append(String.format(" <li class=\"page-item\"><a class=\"page-link\" href=\"%spage=%d\">%d</a></li>\n",
                        httpQuery, curPageNum, curPageNum));
            }
        }
        if (currentPage == totalPages) {
            html.append("<li class=\"page-item disabled\">" +
                    "<a class=\"page-link\" aria-disabled=\"true\">尾页</a></li>\n");
        } else {
            html.append(String.format("<li class=\"page-item\">" +
                            "<a class=\"page-link\" aria-disabled=\"true\" href=\"%spage=%d\">尾页</a></li>\n",
                    httpQuery, totalPages));
        }
        html.append(String.format("<li class=\"page-item\"><a class=\"page-link\">当前第%d页,共%d页</a></li>\n", currentPage, totalPages));
        html.append("</ul>\n");
        return html.toString();
    }


    /**
     * 生成bootstrap的分页页脚
     *
     * @return html
     */
    public String generateBootstrapPagination() {
        StringBuilder html = new StringBuilder("<ul class=\"pagination justify-content-end\">\n");
        html.append(String.format("<li class=\"page-item%s\">" +
                        "<a class=\"page-link\" aria-disabled=\"true\" href=\"%spage=1\">&lt;&lt;&lt;</a></li>\n",
                currentPage == 1 ? " disabled" : "", httpQuery));
        for (Map<String, Object> page : pages) {
            long curPageNum = (long) page.get("num");
            html.append(String.format(" <li class=\"page-item%s\"><a class=\"page-link\" href=\"%spage=%d\">%d</a></li>\n",
                    currentPage == curPageNum ? " active" : "", httpQuery, curPageNum, curPageNum));
        }
        html.append(String.format("<li class=\"page-item%s\">" +
                        "<a class=\"page-link\" aria-disabled=\"true\" href=\"%spage=%d\">&gt;&gt;&gt;</a></li>\n",
                currentPage == totalPages ? " disabled" : "", httpQuery, totalPages));
        html.append("</ul>\n");
        return html.toString();
    }

    /**
     * 获取当前页
     *
     * @return 当前页
     */
    public long getCurrentPage() {
        return currentPage;
    }

    /**
     * 获取结束索引
     *
     * @return 结束索引
     */
    public long getEndIndex() {
        return endIndex;
    }

    /**
     * http query
     *
     * @return http query
     */
    public String getHttpQuery() {
        return httpQuery;
    }


    /**
     * 页脚页列表
     *
     * @return 页脚页列表
     */
    public List<Map<String, Object>> getPages() {
        return pages;
    }

    /**
     * 获取开始索引
     *
     * @return 开始索引
     */
    public long getStartIndex() {
        return startIndex;
    }

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    public long getTotalPages() {
        return totalPages;
    }

    /**
     * 获取总记录数
     *
     * @return 总记录数
     */
    public long getTotalRecords() {
        return totalRecords;
    }

    /**
     * 初始化总记录数
     */
    private void initTotalRecords() {
        String countSql = getCountSql(sql);
        if (countSql == null) {
            Exception ex = new Exception("sql statement error: " + sql);
            ex.printStackTrace();
        } else {
            Long count = AppConfig.getJdbcTemplate().queryForObject(countSql, Long.class, params);
            totalRecords = count == null ? 0 : count;
        }
    }

    public static final class Builder {
        private long pageSize = 20;
        private long page = 1;
        private Map<String, String> query = null;
        private String sql = null;
        private Object[] params = null;


        public Pagination build() {
            return new Pagination(this);
        }

        /**
         * 设置当前页
         *
         * @param page 当前页
         * @return this
         */
        public Builder page(long page) {
            this.page = page;
            return this;
        }

        /**
         * 从http request 中读取当前页,参数名 page
         *
         * @param request http request
         * @return this
         */
        public Builder page(HttpServletRequest request) {
            page(request, "page");
            return this;
        }

        /**
         * 从http request 中读取当前页
         *
         * @param request http request
         * @param name    request name
         * @return this
         */
        public Builder page(HttpServletRequest request, String name) {
            page(RequestUtils.getLong(request, name));
            return this;
        }

        /**
         * 设置每页记录数
         *
         * @param pageSize 每页记录数
         * @return this
         */
        public Builder pageSize(long pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        /**
         * http 查询参数，在生成html页脚时使用
         *
         * @param query http query
         * @return this
         */
        public Builder query(Map<String, String> query) {
            this.query = query;
            return this;
        }

        /**
         * 查询sql
         *
         * @param sql    sql
         * @param params 参数
         * @return this
         */
        public Builder sql(String sql, Object... params) {
            this.sql = sql;
            this.params = params;
            return this;
        }
    }
}
