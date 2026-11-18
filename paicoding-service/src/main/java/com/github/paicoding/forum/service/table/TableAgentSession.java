package com.github.paicoding.forum.service.table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.vo.table.TableLearningReq;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Request-local tools. Only validated view results reach the browser; no database writes. */
final class TableAgentSession {
    private final TableLearningReq request;
    private List<Integer> rowIds;
    private List<Integer> columnIds;
    private boolean changed;
    private Map<String,Object> export;
    private final List<String> steps = new ArrayList<>();

    TableAgentSession(TableLearningReq request) {
        this.request=request;
        rowIds=request.getVisibleRows()==null ? IntStream.rangeClosed(1,rows().size()).boxed().collect(Collectors.toList()) : new ArrayList<>(request.getVisibleRows());
        columnIds=request.getVisibleColumns()==null ? IntStream.range(0,request.getColumns().size()).boxed().collect(Collectors.toList()) : new ArrayList<>(request.getVisibleColumns());
    }
    private List<List<String>> rows() { return request.getRows()==null ? Collections.singletonList(request.getValues()) : request.getRows(); }

    static Object tools(ObjectMapper json) throws Exception {
        List<Object> tools=new ArrayList<>();
        tools.add(tool(json,"read_table","读取当前表格。query 搜索所有原始行；空字符串读取当前视图。offset 从0开始，limit最多10。返回行号和单元格；不能读取未提供的文章。",
                "{\"query\":{\"type\":\"string\"},\"offset\":{\"type\":\"integer\"},\"limit\":{\"type\":\"integer\"}}", "query","offset","limit"));
        String numericFilter = "{\"type\":\"object\",\"properties\":{" +
                "\"column\":{\"type\":\"integer\"},\"unit\":{\"type\":\"string\"}," +
                "\"min\":{\"type\":\"number\"},\"max\":{\"type\":\"number\"}}," +
                "\"required\":[\"column\",\"unit\"],\"additionalProperties\":false}";
        tools.add(tool(json,"set_table_view","修改当前页面的筛选、排序和显示列。至少、至多或范围用 numericFilter，指定 column、单元格的单位 unit（纯数字填空串）以及包含边界的 min 或 max。此时 query 仅填写另需匹配的文字，通常留空。query 空字符串清除筛选，其他值对整行做文字包含匹配；sortColumn 为0起的列索引，-1为原文顺序。visibleColumns 为0起列索引数组，空数组保留当前列。返回匹配行号和按显示顺序排列的单元格预览。不会修改原文。",
                "{\"numericFilter\":" + numericFilter + ",\"query\":{\"type\":\"string\"},\"sortColumn\":{\"type\":\"integer\"},\"descending\":{\"type\":\"boolean\"},\"visibleColumns\":{\"type\":\"array\",\"items\":{\"type\":\"integer\"}}}", "query","sortColumn","descending","visibleColumns"));
        tools.add(tool(json,"export_table","生成当前视图的 CSV 下载结果。如需先筛选或调整列，先调用 set_table_view。只有用户要求导出时调用。","{}"));
        return tools;
    }
    private static Object tool(ObjectMapper json,String name,String description,String properties,String...required) throws Exception {
        Map<String,Object> parameters=new LinkedHashMap<>();parameters.put("type","object");parameters.put("properties",json.readTree(properties));parameters.put("required",required);parameters.put("additionalProperties",false);
        Map<String,Object> function=new LinkedHashMap<>();function.put("name",name);function.put("description",description);function.put("parameters",parameters);
        Map<String,Object> result=new LinkedHashMap<>();result.put("type","function");result.put("function",function);return result;
    }
    Map<String,Object> execute(String name, JsonNode args) {
        check(args!=null && args.isObject(),"参数必须为对象");
        switch(name) {
            case "read_table": {
                String query=query(args);int offset=integer(args,"offset",0,rows().size()),limit=integer(args,"limit",1,10);
                List<Integer> ids = query.isEmpty() ? rowIds : matching(query);
                List<Integer> allColumns = IntStream.range(0, request.getColumns().size()).boxed().collect(Collectors.toList());
                steps.add("读取表格");
                return readRows(ids, allColumns, offset, limit);
            }
            case "set_table_view": {
                String query=query(args);int sort=integer(args,"sortColumn",-1,request.getColumns().size()-1);
                check(args.path("descending").isBoolean(),"descending 必须为布尔值");
                JsonNode visible=args.path("visibleColumns");check(visible.isArray(),"visibleColumns 必须为数组");
                List<Integer> nextColumns=new ArrayList<>();for(JsonNode c:visible){check(c.isIntegralNumber() && c.canConvertToInt() && c.asInt()>=0 && c.asInt()<request.getColumns().size() && !nextColumns.contains(c.asInt()),"列索引无效或重复");nextColumns.add(c.asInt());}
                List<Integer> nextRows=matching(query);
                Map<Integer, BigDecimal> numbers = new HashMap<>();
                int numericColumn = -1;
                if (args.has("numericFilter")) {
                    JsonNode filter = args.get("numericFilter");
                    numericColumn = integer(filter, "column", 0, request.getColumns().size() - 1);
                    check(filter.path("unit").isTextual(), "必须指定数值单位");
                    BigDecimal min = bound(filter, "min");
                    BigDecimal max = bound(filter, "max");
                    check(min != null || max != null, "至少需要一个数值边界");
                    check(min == null || max == null || min.compareTo(max) <= 0, "数值范围无效");
                    String unit = filter.get("unit").asText().trim();
                    for (int id : nextRows) {
                        BigDecimal value = quantity(rows().get(id - 1).get(numericColumn), unit);
                        if (value != null && (min == null || value.compareTo(min) >= 0)
                                && (max == null || value.compareTo(max) <= 0)) {
                            numbers.put(id, value);
                        }
                    }
                    nextRows.removeIf(id -> !numbers.containsKey(id));
                }
                if (sort >= 0) {
                    Comparator<Integer> order = sort == numericColumn ? Comparator.comparing(numbers::get)
                            : (a, b) -> compare(rows().get(a - 1).get(sort), rows().get(b - 1).get(sort));
                    nextRows.sort(args.get("descending").asBoolean() ? order.reversed() : order);
                }
                rowIds=nextRows;if(!nextColumns.isEmpty())columnIds=nextColumns;changed=true;steps.add("调整表格视图");return viewObservation(view());
            }
            case "export_table": export=view();steps.add("生成 CSV");return viewObservation(export);
            default: throw new IllegalArgumentException("不支持的工具");
        }
    }
    private Map<String, Object> readRows(List<Integer> ids, List<Integer> columns, int offset, int limit) {
        List<Map<String, Object>> records = new ArrayList<>();
        int totalChars = 0;
        for (int i = offset; i < Math.min(ids.size(), offset + limit); i++) {
            int id = ids.get(i);
            List<String> row = rows().get(id - 1);
            List<String> values = new ArrayList<>(columns.size());
            boolean truncated = false;
            for (int column : columns) {
                String cell = row.get(column);
                int end = Math.min(cell.length(), 500);
                if (end < cell.length() && Character.isHighSurrogate(cell.charAt(end - 1))) end--;
                values.add(cell.substring(0, end));
                totalChars += end;
                truncated |= end < cell.length();
            }
            if (totalChars > 16000) break;
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("id", id);
            record.put("values", values);
            record.put("truncated", truncated);
            records.add(record);
        }
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("total", ids.size());
        page.put("rows", records);
        page.put("nextOffset", offset + records.size());
        return page;
    }

    private Map<String, Object> viewObservation(Map<String, Object> view) {
        Map<String, Object> observation = new LinkedHashMap<>(view);
        Map<String, Object> preview = readRows(rowIds, columnIds, 0, 10);
        preview.put("headers", columnIds.stream().map(request.getColumns()::get).collect(Collectors.toList()));
        observation.put("preview", preview);
        return observation;
    }

    private List<Integer> matching(String query) {
        String q=query.toLowerCase(Locale.ROOT);return IntStream.rangeClosed(1,rows().size()).filter(id->rows().get(id-1).stream().anyMatch(v->v.toLowerCase(Locale.ROOT).contains(q))).boxed().collect(Collectors.toList());
    }
    private static BigDecimal bound(JsonNode filter, String key) {
        if (!filter.has(key)) return null;
        check(filter.get(key).isNumber(), key + " 必须是数值");
        return filter.get(key).decimalValue();
    }

    private static BigDecimal quantity(String cell, String unit) {
        String text = cell.trim();
        if (!text.endsWith(unit)) return null;
        try {
            return new BigDecimal(text.substring(0, text.length() - unit.length()).trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static int compare(String a,String b) {
        try{return new java.math.BigDecimal(a.trim()).compareTo(new java.math.BigDecimal(b.trim()));}catch(NumberFormatException e){return a.compareToIgnoreCase(b);}
    }
    private String query(JsonNode args){check(args.path("query").isTextual() && args.path("query").asText().length()<=500,"query 必须为500字以内文本");return args.path("query").asText().trim();}
    private int integer(JsonNode args,String key,int min,int max){JsonNode n=args.path(key);check(n.isIntegralNumber() && n.canConvertToInt() && n.asInt()>=min && n.asInt()<=max,key+" 超出范围");return n.asInt();}
    private static void check(boolean ok,String error){if(!ok)throw new IllegalArgumentException(error);}
    private Map<String,Object> view(){Map<String,Object> view=new LinkedHashMap<>();view.put("rowIds",new ArrayList<>(rowIds));view.put("columns",new ArrayList<>(columnIds));return view;}
    Map<String,Object> result(String answer){Map<String,Object> result=new LinkedHashMap<>();result.put("answer",answer);result.put("steps",steps);if(changed)result.put("view",view());if(export!=null)result.put("export",export);return result;}
}
