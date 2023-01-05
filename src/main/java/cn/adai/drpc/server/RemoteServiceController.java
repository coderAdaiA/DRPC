package cn.adai.drpc.server;

import cn.adai.drpc.consts.DRPCConstants;
import com.alibaba.fastjson2.JSON;
import cn.adai.drpc.model.DRPCRequest;
import cn.adai.drpc.model.DRPCResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * @author: coderAdai
 * @date 2022/08/18 11:46
 * @description: TODO
 */
@Slf4j
@RestController
public class RemoteServiceController {

    private static final String MEDIA_TYPE_JSON = "application/json;charset=utf-8";

    @PostMapping(value = "/cn/adai/drpc", produces = MEDIA_TYPE_JSON, consumes = MEDIA_TYPE_JSON)
    public DRPCResponse drpcMain(@RequestHeader(HttpHeaders.USER_AGENT) String userAgent,
                                 @RequestBody DRPCRequest request) {
        if (!StringUtils.hasLength(userAgent) || !DRPCConstants.REMOTE_CALLER_USERAGENT.equals(userAgent)) {
            return null;
        }

        String identifier = request.getIdentifier();
        String methodName = request.getMethodName();
        Object beanObject = RemoteServiceBeanStore.getServiceBean(identifier);
        if (beanObject == null) {
            return DRPCResponse.makeFailResult("Target bean could not found, identifier: " + identifier);
        }

        return this.invoke(beanObject, methodName, request.getArgs());
    }

    private DRPCResponse invoke(Object beanObject, String methodName, List<DRPCRequest.Argument> args) {
        try {
            Class<?>[] argClassArray = this.convertArgClass(args);
            Method method = beanObject.getClass().getMethod(methodName, argClassArray);

            Class<?> returnType = method.getReturnType();
            String returnTypeName = returnType.getName();

            boolean isList = false;
            if (returnType.equals(List.class) || returnType.equals(ArrayList.class) || returnType.equals(Array.class)) {
                returnTypeName = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0].getTypeName();
                isList = true;
            }

            Object[] argValueArray = this.getArgValueArray(args);
            try {
                Object result = method.invoke(beanObject, argValueArray);
                if (isList) {
                    return DRPCResponse.makeSuccessListResult(returnTypeName, JSON.toJSONString(result));
                }
                return DRPCResponse.makeSuccessResult(returnTypeName, JSON.toJSONString(result));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                log.error("DRPC method call exception", e);
                return DRPCResponse.makeFailResult("Server call method exception.");
            } catch (Exception businessEx) {
                return DRPCResponse.makeFailResult(businessEx.getMessage(), true);
            }

        } catch (Exception e) {
            log.warn("DRPC Remote call exception.", e);
            return DRPCResponse.makeFailResult("Server parsing exception, message: " + e.getMessage());
        }
    }

    private Object[] getArgValueArray(List<DRPCRequest.Argument> args) throws ClassNotFoundException {
        Object[] argValueArray = new Object[args.size()];

        for (int i = 0; i < args.size(); i++) {
            DRPCRequest.Argument argument = args.get(i);
            Object object = argument.getObject();
            if (String.class.getTypeName().equals(argument.getTypeClassName())) {
                argValueArray[i] = object;
            } else if (argument.getIsList()) {
                argValueArray[i] = JSON.parseArray((String) object, Class.forName(argument.getTypeClassName()));
            } else {
                argValueArray[i] = JSON.parseObject((String) object, Class.forName(argument.getTypeClassName()));
            }
        }

        return argValueArray;
    }

    private Class<?>[] convertArgClass(List<DRPCRequest.Argument> args) throws ClassNotFoundException {
        if (args == null) {
            return new Class<?>[0];
        }

        Class<?>[] argClasses = new Class<?>[args.size()];
        for (int i = 0; i < args.size(); i++) {
            DRPCRequest.Argument argument = args.get(i);
            if (argument.getIsList()) {
                argClasses[i] = Class.forName(argument.getListClassName());
            } else {
                argClasses[i] = Class.forName(argument.getTypeClassName());
            }
        }
        return argClasses;
    }
}
