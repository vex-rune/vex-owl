package com.vex.query.criteria;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON 序列化/反序列化测试
 */
@DisplayName("JSON 序列化测试")
class JsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("CriteriaExpression 序列化")
    class ExpressionSerialization {

        @Test
        @DisplayName("序列化等于表达式")
        void testSerializeEqExpression() throws Exception {
            CriteriaExpression expr = CriteriaExpression.eq("status", "ACTIVE");
            String json = objectMapper.writeValueAsString(expr);

            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"expression\""));
            assertTrue(json.contains("\"field\":\"status\""));
            assertTrue(json.contains("\"op\":\"eq\""));
            assertTrue(json.contains("\"value\":\"ACTIVE\""));
        }

        @Test
        @DisplayName("序列化 BETWEEN 表达式")
        void testSerializeBetweenExpression() throws Exception {
            CriteriaExpression expr = CriteriaExpression.between("age", 18, 65);
            String json = objectMapper.writeValueAsString(expr);

            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"expression\""));
            assertTrue(json.contains("\"field\":\"age\""));
            assertTrue(json.contains("\"op\":\"between\""));
        }

        @Test
        @DisplayName("反序列化等于表达式")
        void testDeserializeEqExpression() throws Exception {
            String json = """
                {
                    "type":"expression",
                    "field":"status",
                    "op":"eq",
                    "value":"ACTIVE"
                }
                """;
            CriteriaExpression expr = objectMapper.readValue(json, CriteriaExpression.class);

            assertEquals("status", expr.getField());
            assertEquals(Operator.eq, expr.getOp());
            assertEquals("ACTIVE", expr.getValue());
        }

        @Test
        @DisplayName("反序列化 IN 表达式")
        void testDeserializeInExpression() throws Exception {
            String json = """
                {
                    "type":"expression",
                    "field":"status",
                    "op":"in",
                    "value":["ACTIVE","PENDING"]
                }
                """;
            CriteriaExpression expr = objectMapper.readValue(json, CriteriaExpression.class);

            assertEquals("status", expr.getField());
            assertEquals(Operator.in, expr.getOp());
            assertNotNull(expr.getValue());
        }
    }

    @Nested
    @DisplayName("CriteriaPredicate 序列化")
    class PredicateSerialization {

        @Test
        @DisplayName("序列化 AND 条件组合")
        void testSerializeAndPredicate() throws Exception {
            CriteriaPredicate predicate = CriteriaPredicate.and(
                CriteriaExpression.eq("status", "ACTIVE"),
                CriteriaExpression.gte("age", 18)
            );
            String json = objectMapper.writeValueAsString(predicate);

            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"predicate\""));
            assertTrue(json.contains("\"logic\":\"and\""));
            assertTrue(json.contains("\"expressions\""));
        }

        @Test
        @DisplayName("序列化嵌套条件组合")
        void testSerializeNestedPredicate() throws Exception {
            CriteriaPredicate predicate = CriteriaPredicate.and(
                CriteriaExpression.eq("status", "ACTIVE"),
                CriteriaPredicate.or(
                    CriteriaExpression.exp("username", "%张%"),
                    CriteriaExpression.exp("username", "%李%")
                )
            );
            String json = objectMapper.writeValueAsString(predicate);

            assertNotNull(json);
            // 应该包含两个 type: predicate（外层和内层）
            int count = json.split("\"type\":\"predicate\"").length - 1;
            assertEquals(2, count);
        }

        @Test
        @DisplayName("反序列化 AND 条件组合")
        void testDeserializeAndPredicate() throws Exception {
            String json = """
                {
                    "type":"predicate",
                    "logic":"and",
                    "expressions":[
                        {"type":"expression","field":"status","op":"eq","value":"ACTIVE"},
                        {"type":"expression","field":"age","op":"gte","value":18}
                    ]
                }
                """;

            CriteriaPredicate predicate = objectMapper.readValue(json, CriteriaPredicate.class);

            assertEquals(Logic.and, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
            
            // 验证第一个表达式
            QueryCriterion first = predicate.getExpressions()[0];
            assertInstanceOf(CriteriaExpression.class, first);
            CriteriaExpression expr1 = (CriteriaExpression) first;
            assertEquals("status", expr1.getField());
            assertEquals(Operator.eq, expr1.getOp());
        }

        @Test
        @DisplayName("反序列化嵌套条件组合")
        void testDeserializeNestedPredicate() throws Exception {
            String json = """
                {
                    "type":"predicate",
                    "logic":"and",
                    "expressions":[
                        {"type":"expression","field":"status","op":"eq","value":"ACTIVE"},
                        {"type":"predicate","logic":"or","expressions":[
                            {"type":"expression","field":"username","op":"exp","value":"%张%"},
                            {"type":"expression","field":"username","op":"exp","value":"%李%"}
                        ]}
                    ]
                }
                """;

            CriteriaPredicate predicate = objectMapper.readValue(json, CriteriaPredicate.class);

            assertEquals(Logic.and, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
            
            // 验证嵌套的 OR 条件
            QueryCriterion second = predicate.getExpressions()[1];
            assertInstanceOf(CriteriaPredicate.class, second);
            CriteriaPredicate nested = (CriteriaPredicate) second;
            assertEquals(Logic.or, nested.getLogic());
            assertEquals(2, nested.getExpressions().length);
        }
    }

    @Nested
    @DisplayName("QueryCriteria 完整序列化")
    class QueryCriteriaSerialization {

        @Test
        @DisplayName("序列化完整查询对象")
        void testSerializeQueryCriteria() throws Exception {
            QueryCriteria query = QueryCriteria.of()
                .select("id", "username", "email")
                .exclude("password")
                .filter(CriteriaPredicate.and(
                    CriteriaExpression.eq("status", "ACTIVE"),
                    CriteriaExpression.gte("age", 18)
                ))
                .orderBy(SortOrder.desc("createTime"))
                .paging(PageRequest.of(0, 20));

            String json = objectMapper.writeValueAsString(query);

            assertNotNull(json);
            assertTrue(json.contains("\"select\""));
            assertTrue(json.contains("\"exclude\""));
            assertTrue(json.contains("\"filter\""));
            assertTrue(json.contains("\"orderBy\""));
            assertTrue(json.contains("\"paging\""));
        }

        @Test
        @DisplayName("反序列化完整查询对象")
        void testDeserializeQueryCriteria() throws Exception {
            String json = """
                {
                    "select":["id","username","email"],
                    "exclude":["password"],
                    "filter":{
                        "type":"predicate",
                        "logic":"and",
                        "expressions":[
                            {"type":"expression","field":"status","op":"eq","value":"ACTIVE"},
                            {"type":"expression","field":"age","op":"gte","value":18}
                        ]
                    },
                    "orderBy":[{"property":"createTime","direction":"DESC"}],
                    "paging":{"page":0,"size":20}
                }
                """;

            QueryCriteria query = objectMapper.readValue(json, QueryCriteria.class);

            assertNotNull(query);
            assertEquals(3, query.getSelect().length);
            assertEquals(1, query.getExclude().length);
            assertNotNull(query.getFilter());
            assertEquals(Logic.and, query.getFilter().getLogic());
            assertEquals(1, query.getOrderBy().length);
            assertEquals(0, query.getPaging().getPage());
            assertEquals(20, query.getPaging().getSize());
        }

        @Test
        @DisplayName("序列化后再反序列化（往返测试）")
        void testRoundTrip() throws Exception {
            // 创建原始对象
            QueryCriteria original = QueryCriteria.of()
                .select("id", "username", "email")
                .filter(CriteriaPredicate.and(
                    CriteriaExpression.eq("status", "ACTIVE"),
                    CriteriaPredicate.or(
                        CriteriaExpression.exp("username", "%张%"),
                        CriteriaExpression.exp("username", "%李%")
                    )
                ))
                .orderBy(SortOrder.desc("createTime"))
                .paging(PageRequest.of(0, 20));

            // 序列化
            String json = objectMapper.writeValueAsString(original);

            // 反序列化
            QueryCriteria deserialized = objectMapper.readValue(json, QueryCriteria.class);

            // 验证
            assertNotNull(deserialized);
            assertArrayEquals(original.getSelect(), deserialized.getSelect());
            assertNotNull(deserialized.getFilter());
            assertEquals(original.getFilter().getLogic(), deserialized.getFilter().getLogic());
            assertEquals(original.getOrderBy().length, deserialized.getOrderBy().length);
            assertEquals(original.getPaging().getPage(), deserialized.getPaging().getPage());
            assertEquals(original.getPaging().getSize(), deserialized.getPaging().getSize());
        }
    }

    @Nested
    @DisplayName("SortOrder 和 PageRequest 序列化")
    class ComponentSerialization {

        @Test
        @DisplayName("序列化 SortOrder")
        void testSerializeSortOrder() throws Exception {
            SortOrder sort = SortOrder.desc("createTime");
            String json = objectMapper.writeValueAsString(sort);

            assertTrue(json.contains("\"property\":\"createTime\""));
            assertTrue(json.contains("\"direction\":\"DESC\""));
        }

        @Test
        @DisplayName("反序列化 SortOrder")
        void testDeserializeSortOrder() throws Exception {
            String json = """
                {
                    "property":"createTime",
                    "direction":"DESC"
                }
                """;
            SortOrder sort = objectMapper.readValue(json, SortOrder.class);

            assertEquals("createTime", sort.getProperty());
            assertEquals("DESC", sort.getDirection());
            assertTrue(sort.checkDesc());
        }

        @Test
        @DisplayName("序列化 PageRequest")
        void testSerializePageRequest() throws Exception {
            PageRequest paging = PageRequest.of(1, 50);
            String json = objectMapper.writeValueAsString(paging);

            assertTrue(json.contains("\"page\":1"));
            assertTrue(json.contains("\"size\":50"));
        }

        @Test
        @DisplayName("反序列化 PageRequest")
        void testDeserializePageRequest() throws Exception {
            String json = """
                {
                    "page":1,
                    "size":50
                }
                """;
            PageRequest paging = objectMapper.readValue(json, PageRequest.class);

            assertEquals(1, paging.getPage());
            assertEquals(50, paging.getSize());
        }
    }
}
