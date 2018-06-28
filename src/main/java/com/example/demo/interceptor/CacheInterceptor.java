package com.example.demo.interceptor;

import com.example.demo.annotation.Cacheable;
import com.example.demo.cache.Cache;
import com.example.demo.utils.CacheKey;
import com.example.demo.utils.StringUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fupeng-ds on 2018/6/27.
 */
public class CacheInterceptor implements MethodInterceptor {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        boolean present = method.isAnnotationPresent(Cacheable.class);
        if (!present) {
            return invocation.proceed();
        } else {
            Cacheable cacheable = method.getAnnotation(Cacheable.class);
            Class clzz;
            Type type = method.getGenericReturnType();
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                clzz = (Class) pt.getRawType();
            } else if (type instanceof Class) {
                clzz = (Class)type;
            } else {
                return invocation.proceed();
            }
            CachePair pair = parseArgs(invocation, method, clzz, cacheable);
            return process(pair);

        }
    }

    private Object process(final CachePair pair) throws Throwable {
        MethodInvocation invocation = pair.getInvocation();
        CacheKey type = pair.getType();
        Object result;
        if (type == CacheKey.CACHE) {
            if (StringUtil.isEmpty(pair.getField())) {
                result = Cache.get(pair.getKey(), pair.getReturnType());
            } else {
                result = Cache.get(pair.getKey(), pair.getField(), pair.getReturnType());
            }
            if (result != null) {
                return result;
            }
        }
        result = invocation.proceed();
        CacheRunnable runnable = new CacheRunnable(pair.getKey(), pair.getField(), pair.getExpaire(), pair.getType(), result);
        executorService.submit(runnable);
        return result;
    }

    private CachePair parseArgs(MethodInvocation invocation, Method method, Class clazz, Cacheable cacheable) {
        CachePair pair = new CachePair();
        pair.setExpaire(cacheable.expire());
        pair.setType(cacheable.type());
        pair.setReturnType(clazz);
        pair.setInvocation(invocation);
        //解析spel表达式
        EvaluationContext context = new StandardEvaluationContext();
        Object[] arguments = invocation.getArguments();
        ParameterNameDiscoverer pnd = new LocalVariableTableParameterNameDiscoverer();
        String[] parameterNames = pnd.getParameterNames(method);
        if (arguments.length == parameterNames.length) {
            int len = arguments.length;
            for (int i = 0; i < len; i++) {
                //key:参数名称，value:参数值
                context.setVariable(parameterNames[i], arguments[i]);
            }
            String key = cacheable.key();
            ExpressionParser parser = new SpelExpressionParser();
            //生成key
            pair.setKey(parser.parseExpression(key).getValue(context, String.class));

            String field = cacheable.field();
            if (StringUtil.notEmpty(field)) {
                //生成field
                pair.setField(parser.parseExpression(field).getValue(context, String.class));
            }
        }
        return pair;
    }

    private class CacheRunnable implements Runnable {

        private String key;

        private String field;

        private int expire;

        CacheKey type;

        Object result;

        public CacheRunnable(String key, String field, int expire, CacheKey type, Object result) {
            this.key = key;
            this.field = field;
            this.expire = expire;
            this.type = type;
            this.result = result;
        }

        @Override
        public void run() {
            switch (type) {
                case CACHE:
                    if (StringUtil.isEmpty(field)) {
                        Cache.set(key, result, expire);
                    } else {
                        Cache.set(key, field, result, expire);
                    }
                    break;
                case REMOVE:
                    Cache.del(key);
                    break;
            }
        }
    }

    private class CachePair {
        String key;
        String field;
        int expaire;
        CacheKey type;
        Class returnType;
        MethodInvocation invocation;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public int getExpaire() {
            return expaire;
        }

        public void setExpaire(int expaire) {
            this.expaire = expaire;
        }

        public CacheKey getType() {
            return type;
        }

        public void setType(CacheKey type) {
            this.type = type;
        }

        public Class getReturnType() {
            return returnType;
        }

        public void setReturnType(Class returnType) {
            this.returnType = returnType;
        }

        public MethodInvocation getInvocation() {
            return invocation;
        }

        public void setInvocation(MethodInvocation invocation) {
            this.invocation = invocation;
        }
    }
}
