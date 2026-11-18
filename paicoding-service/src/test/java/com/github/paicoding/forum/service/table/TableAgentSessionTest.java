package com.github.paicoding.forum.service.table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.vo.table.TableLearningReq;
import com.github.paicoding.forum.service.chatai.service.impl.deepseek.DeepSeekIntegration.DeepSeekConf;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TableAgentSessionTest {
    private final ObjectMapper json=new ObjectMapper();
    private TableLearningReq sample(){TableLearningReq r=new TableLearningReq();r.setTitle("计划");r.setColumns(Arrays.asList("知识点","题数"));r.setValues(Arrays.asList("集合","2"));r.setRows(Arrays.asList(r.getValues(),Arrays.asList("并发锁","12"),Arrays.asList("并发线程","3")));r.setAction("ask");r.setQuestion("筛选并发，按题数排序并导出");return r;}
    @Test void executesFilterNumericSortVisibilityAndExport() throws Exception {
        TableAgentSession session=new TableAgentSession(sample());
        Map<String,Object> view=session.execute("set_table_view",json.readTree("{\"query\":\"并发\",\"sortColumn\":1,\"descending\":false,\"visibleColumns\":[0]}"));
        assertEquals(Arrays.asList(3,2),view.get("rowIds"));assertEquals(Collections.singletonList(0),view.get("columns"));
        assertEquals(view,session.execute("export_table",json.readTree("{}")));
        com.fasterxml.jackson.databind.JsonNode preview = json.valueToTree(view.get("preview"));
        assertEquals(json.readTree("[\"知识点\"]"), preview.get("headers"));
        assertEquals(json.readTree("[\"并发线程\"]"), preview.get("rows").get(0).get("values"));
        assertEquals(json.readTree("[\"并发锁\"]"), preview.get("rows").get(1).get("values"));
        Map<String,Object> result = session.result("完成");
        assertEquals(result.get("view"), result.get("export"));
        assertFalse(((Map<?,?>)result.get("export")).containsKey("preview"));
    }
    @Test void invalidToolsCannotChangeView() throws Exception {
        TableAgentSession session=new TableAgentSession(sample());
        assertThrows(IllegalArgumentException.class,()->session.execute("delete_table",json.readTree("{}")));
        assertThrows(IllegalArgumentException.class,()->session.execute("set_table_view",json.readTree("{\"query\":\"并发\",\"sortColumn\":1,\"descending\":false,\"visibleColumns\":[99]}")));
        assertFalse(session.result("失败").containsKey("view"));
        assertEquals(Arrays.asList(1,2,3),session.execute("export_table",json.readTree("{}")).get("rowIds"));
    }
    @Test void validatesSnapshotAndPreservesEmptyCurrentView() throws Exception {
        TableLearningReq r=sample();r.setVisibleRows(Collections.emptyList());TableAgentSession session=new TableAgentSession(r);
        assertEquals(0,session.execute("read_table",json.readTree("{\"query\":\"\",\"offset\":0,\"limit\":10}")).get("total"));
        r.setVisibleRows(Arrays.asList(1,1));assertThrows(TableException.class,()->new TableAiService(new DeepSeekConf()).validateLearningRequest(r));
        r.setVisibleRows(null);r.setRows(Collections.singletonList(Collections.singletonList("缺列")));assertThrows(TableException.class,()->new TableAiService(new DeepSeekConf()).validateLearningRequest(r));
    }
    @Test void feedsToolObservationBackAndReturnsExecutableResult() throws Exception {
        List<com.fasterxml.jackson.databind.JsonNode> requests=new ArrayList<>();
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/chat/completions",exchange->{
            requests.add(json.readTree(exchange.getRequestBody()));
            String message=requests.size()==1?"{\"role\":\"assistant\",\"content\":null,\"tool_calls\":[{\"id\":\"call1\",\"type\":\"function\",\"function\":{\"name\":\"set_table_view\",\"arguments\":\"{\\\"query\\\":\\\"并发\\\",\\\"sortColumn\\\":1,\\\"descending\\\":false,\\\"visibleColumns\\\":[]}\"}}]}":"{\"role\":\"assistant\",\"content\":\"已筛选两行\"}";
            byte[] bytes=("{\"choices\":[{\"message\":"+message+"}]}").getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();try {
            DeepSeekConf config=new DeepSeekConf();config.setApiHost("http://127.0.0.1:"+server.getAddress().getPort());config.setApiKey("test-only");config.setModel("test-model");
            Map<String,Object> result=new TableAiService(config).runAgent(sample(),"执行读者指令");
            assertEquals(2,requests.size());assertEquals("tool",requests.get(1).path("messages").path(3).path("role").asText());
            assertEquals("call1",requests.get(1).path("messages").path(3).path("tool_call_id").asText());
            com.fasterxml.jackson.databind.JsonNode observation = json.readTree(requests.get(1).path("messages").path(3).path("content").asText());
            assertEquals(json.readTree("[3,2]"), observation.get("rowIds"));
            assertEquals(json.readTree("[\"并发线程\",\"3\"]"), observation.at("/preview/rows/0/values"));
            assertEquals(json.readTree("[\"并发锁\",\"12\"]"), observation.at("/preview/rows/1/values"));
            assertEquals(Arrays.asList(3,2),((Map<?,?>)result.get("view")).get("rowIds"));
        }finally{server.stop(0);}
    }
}
