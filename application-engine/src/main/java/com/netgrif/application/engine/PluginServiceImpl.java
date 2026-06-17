package com.netgrif.application.engine;

import com.netgrif.application.engine.adapter.spring.plugin.service.PluginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PluginServiceImpl implements PluginService {

    private final ApplicationContext applicationContext;

    @Override
    public Object call(String pluginId, String entryPoint, String method, Serializable... args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        log.info("Executing entry point [{}] with method [{}]...", entryPoint, method);
        Class<?>[] paramTypesFromRequest = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        Object bean = applicationContext.getBean(entryPoint);
        Method methodToInvoke = findMethod(bean, method, paramTypesFromRequest);
        return methodToInvoke.invoke(bean, Arrays.stream(args).toArray());
    }

    private Method findMethod(Object bean, String methodToExecute, Class<?>[] requestParamTypes)
            throws NoSuchMethodException, IllegalArgumentException {
        try {
            return bean.getClass().getMethod(methodToExecute, requestParamTypes);
        } catch (NoSuchMethodException e) {
            return findMethodWithSuperClassParams(bean, methodToExecute, requestParamTypes, e);
        }
    }

    private Method findMethodWithSuperClassParams(Object bean, String methodToExecute, Class<?>[] requestParamTypes,
                                                  NoSuchMethodException caughtException)
            throws NoSuchMethodException, IllegalArgumentException {
        Class<?> cls = bean.getClass();
        Method[] methods = cls.getMethods();
        Method methodToInvoke = null;
        outerLoop: for (Method method : methods) {
            if (!methodToExecute.equals(method.getName())) {
                continue;
            }

            Class<?>[] paramTypes = method.getParameterTypes();
            int requestParamsLen = requestParamTypes.length;
            int paramsLen = paramTypes.length;

            if (requestParamsLen == 0 && paramsLen == 0) {
                methodToInvoke = method;
                break;
            } else if (paramsLen == 0 || paramsLen != requestParamsLen) {
                continue;
            }

            for (int i = 0; i < requestParamTypes.length; ++i) {
                if (!paramTypes[i].isAssignableFrom(requestParamTypes[i])) {
                    continue outerLoop;
                }
            }

            if (methodToInvoke != null) {
                throw new IllegalArgumentException(String.format("Method %s is ambiguous for the param types %s",
                        methodToExecute, Arrays.toString(requestParamTypes)));
            }
            methodToInvoke = method;
        }

        if (methodToInvoke == null) {
            throw caughtException;
        } else {
            return methodToInvoke;
        }
    }
}
