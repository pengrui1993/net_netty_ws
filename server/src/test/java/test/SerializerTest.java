//package test;
//
//import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
//import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
//import com.fasterxml.jackson.annotation.JsonTypeInfo;
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializerProvider;
//import com.fasterxml.jackson.databind.module.SimpleModule;
//import com.fasterxml.jackson.databind.ser.std.StdSerializer;
//import lombok.Data;
//import lombok.experimental.Accessors;
//import org.springframework.cache.support.NullValue;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializer;
//import org.springframework.data.redis.serializer.SerializationException;
//import org.springframework.util.StringUtils;
//
//import java.io.IOException;
//import java.io.StringWriter;
//
//public class SerializerTest {
//    @Data
//    @Accessors(chain = true)
//    static class Bean{
//        private int age;
//        private String name;
//    }
//    public static void test1() {
//        boolean gen = true;
//        Class<Bean> beanClass = Bean.class;
//        FastJsonRedisSerializer<Bean>  fs = new FastJsonRedisSerializer<>(beanClass);
//        RedisSerializer<Bean>  s = gen?new RedisSerializer<Bean>() {
//            final GenericFastJsonRedisSerializer gs = new GenericFastJsonRedisSerializer();
//            @Override
//            public byte[] serialize(Bean bean) throws SerializationException {
//                return gs.serialize(bean);
//            }
//
//            @Override
//            public Bean deserialize(byte[] bytes) throws SerializationException {
//                return (Bean)gs.deserialize(bytes);
//            }
//        }:fs;
//        Bean b;
//        System.out.println(new String(s.serialize(b=new Bean().setAge(11).setName("hello"))));//{"@type":"key.SerializerTest$Bean","age":11,"name":"hello"}
//
//        System.out.println(new String(RedisSerializer.json().serialize(b)));//{"@class":"key.SerializerTest$Bean","age":11,"name":"hello"}
//
//        System.out.println(new String(new GenericJackson2JsonRedisSerializer(new ObjectMapper()).serialize(b)));//{"age":11,"name":"hello"}
//        System.out.println(new String(new GenericJackson2JsonRedisSerializer().serialize(b)));
//
//    }
//    ObjectMapper mapper = new ObjectMapper();
//    SerializerTest(){
//        final String classPropertyTypeName = "@class";
//        class NullSer extends StdSerializer<NullValue>{
//            protected NullSer(){
//                super(NullValue.class);
//            }
//            @Override
//            public void serialize(NullValue nullValue, JsonGenerator jgen, SerializerProvider sep) throws IOException {
//                jgen.writeStartObject();
//                jgen.writeStringField(classPropertyTypeName, NullValue.class.getName());
//                jgen.writeEndObject();
//            }
//        }
//        this.mapper.registerModule((new SimpleModule()).addSerializer(new NullSer()));
//        if (StringUtils.hasText(classPropertyTypeName)) {
//            this.mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, classPropertyTypeName);
//        } else {
//            this.mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
//        }
//    }
//
//    void test() throws IOException {
//        StringWriter str = new StringWriter();
//        mapper.writeValue(str,new Bean().setAge(11).setName("hello"));
//        System.out.println(str);
//    }
//
//    public static void main(String[] args)
//            throws Exception{
//         new SerializerTest().test();
//    }
//}
