package cn.adai.drpc.client;

import cn.adai.drpc.model.DRPCRequest;
import cn.adai.drpc.model.DRPCResponse;
import com.alibaba.fastjson2.JSON;
import cn.adai.drpc.annotation.DRPCRemoteClient;
import cn.adai.drpc.annotation.DRPCSerializeBinder;
import cn.adai.drpc.annotation.DRPCThrowableBinder;
import cn.adai.drpc.exception.DRPCBusinessException;
import cn.adai.drpc.exception.DRPCException;
import org.springframework.util.StringUtils;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author: coderAdai
 * @date 2022/08/18 11:42
 * @description: TODO
 */
public class RemoteServiceProxy<T> implements InvocationHandler {

    private static final RemoteCaller remoteCaller = new RemoteCaller();

    private final T target;

    public RemoteServiceProxy(T target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        DRPCRemoteClient remoteClientAnnotation = method.getDeclaringClass().getAnnotation(DRPCRemoteClient.class);
        if (remoteClientAnnotation == null) {
            throw new DRPCException("Annotation `DRPCRemoteClient` configuration error.");
        }

        String serverName = remoteClientAnnotation.serverName();
        String serviceIdentifier = remoteClientAnnotation.serviceIdentifier();
        if (!StringUtils.hasLength(serverName) || !StringUtils.hasLength(serviceIdentifier)) {
            throw new DRPCException("serverName or serviceIdentifier in `DRPCRemoteClient` can not be empty.");
        }

        DRPCSerializeBinder[] serializerAnnotations = method.getDeclaredAnnotationsByType(DRPCSerializeBinder.class);
        DRPCRequest request = this.makeRequest(serverName, serviceIdentifier, method, args, serializerAnnotations);
        DRPCResponse result = remoteCaller.call(request);

        DRPCThrowableBinder throwableBinderAnnotation = method.getDeclaredAnnotation(DRPCThrowableBinder.class);
        this.checkResultSuccess(result, throwableBinderAnnotation);

        try {
            Class<?> clazz = this.getSerializerClass(serializerAnnotations, result.getResultType());
            if (result.isList()) {
                return JSON.parseArray(result.getResultValue(), clazz);
            }
            return JSON.parseObject(result.getResultValue(), clazz);
        } catch (ClassNotFoundException e) {
            throw new DRPCException("Class not found, please check your package classes.", e);
        } catch (Exception e) {
            throw new DRPCException(e);
        }
    }

    private DRPCRequest makeRequest(String serverName, String serviceIdentifier, Method method, Object[] args,
                                    DRPCSerializeBinder[] annotations) {
        DRPCRequest request = new DRPCRequest();
        request.setServerName(serverName);
        request.setIdentifier(serviceIdentifier);
        request.setMethodName(method.getName());
        request.setArgs(this.makeArgumentList(method, args, annotations));
        return request;
    }

    private List<DRPCRequest.Argument> makeArgumentList(Method method, Object[] args, DRPCSerializeBinder[] annotations) {
        List<DRPCRequest.Argument> argumentList = new ArrayList<>();
        if (args == null) {
            return argumentList;
        }

        for (int i = 0; i < args.length; i++) {
            Object argValue = args[i];

            DRPCRequest.Argument argument = new DRPCRequest.Argument();

            final String findTypeName;
            if (AbstractList.class.equals(argValue.getClass().getSuperclass())) {
                // 集合类型
                ParameterizedType parameterizedType = (ParameterizedType) method.getParameters()[i].getParameterizedType();
                findTypeName = parameterizedType.getActualTypeArguments()[0].getTypeName();
                argument.setListClassName(method.getParameters()[i].getType().getName());
                argument.setIsList(true);
            } else {
                // 对象类型
                findTypeName = argValue.getClass().getTypeName();
                argument.setIsList(false);
            }

            String className = getArgClassName(argValue, findTypeName, annotations);
            argument.setTypeClassName(className);

            Object val = this.convertArgValue(argValue, className);
            argument.setObject(val);

            argumentList.add(argument);
        }

        return argumentList;
    }

    private String getArgClassName(Object argValue, String findTypeName, DRPCSerializeBinder[] annotations) {
        // 找到匹配的注解则使用注解的映射, 没找到用默认的class类型
        Optional<DRPCSerializeBinder> serializer = Arrays.stream(annotations)
                .filter(a -> a.currentClass().getTypeName().equals(findTypeName))
                .findFirst();
        return serializer.isPresent() ? serializer.get().remoteClassName() : argValue.getClass().getName();
    }

    private Object convertArgValue(Object argValue, String className) {
        if (String.class.getTypeName().equals(className)) {
            return argValue;
        }
        return JSON.toJSONString(argValue);
    }

    private void checkResultSuccess(DRPCResponse result, DRPCThrowableBinder annotation) throws Throwable {
        if (result == null) {
            throw new DRPCException("Failed to call drpc remote server.");
        }

        if (result.isSuccess()) {
            return;
        }

        if (!result.isBusinessException()) {
            throw new DRPCException(result.getMessage());
        }

        if (annotation != null) {
            Class<? extends Throwable> exceptionClass = annotation.exceptionClass();
            throw exceptionClass.getConstructor(String.class).newInstance(result.getMessage());
        }

        throw new DRPCBusinessException(result.getMessage());
    }

    private Class<?> getSerializerClass(DRPCSerializeBinder[] serializerAnnotations, String resultType) throws ClassNotFoundException {
        if (serializerAnnotations != null && serializerAnnotations.length > 0) {
            Optional<DRPCSerializeBinder> serializer = Arrays.stream(serializerAnnotations)
                    .filter(a -> a.remoteClassName().equals(resultType))
                    .findFirst();
            if (serializer.isPresent()) {
                return serializer.get().currentClass();
            }
        }
        return Class.forName(resultType);
    }
}