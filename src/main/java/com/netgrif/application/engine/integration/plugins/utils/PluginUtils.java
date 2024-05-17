package com.netgrif.application.engine.integration.plugins.utils;

import com.google.protobuf.ByteString;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.TaskPair;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PluginUtils {
    private final IWorkflowService workflowService;
    private final ITaskService taskService;
    private final IUserService userService;

    /**
     * Finds task id in provided case by transition id
     *
     * @param aCase Case instance where to find task id
     * @param transId transition id of the task id
     *
     * @return optional task id
     * */
    public static String findTaskIdInCase(Case aCase, String transId) {
        Optional<TaskPair> taskPairOpt = aCase.getTasks().stream()
                .filter((taskPair -> taskPair.getTransition().equals(transId)))
                .findFirst();
        if (taskPairOpt.isEmpty()) {
            throw new IllegalStateException("Case with id [" + aCase.getStringId() + "] should have at least 1 existing task.");
        }
        return taskPairOpt.get().getTask();
    }

    /**
     * Checks if the plugin is active.
     *
     * @param pluginCase case instance to be checked
     *
     * @return true if the plugin is active
     * */
    public static boolean isPluginActive(Case pluginCase) {
        return (Boolean) pluginCase.getFieldValue(PluginConstants.PLUGIN_ACTIVE_FIELD_ID);
    }

    /**
     * Finds plugin name from the dataSet of the provided plugin case
     * */
    public static String getPluginName(Case pluginCase) {
        return (String) pluginCase.getFieldValue(PluginConstants.PLUGIN_NAME_FIELD_ID);
    }

    /**
     * Finds plugin url from the dataSet of the provided plugin case
     * */
    public static String getPluginUrl(Case pluginCase) {
        return (String) pluginCase.getFieldValue(PluginConstants.PLUGIN_URL_FIELD_ID);
    }

    /**
     * Finds plugin port from the dataSet of the provided plugin case
     * */
    public static int getPluginPort(Case pluginCase) {
        Double result = (Double) pluginCase.getFieldValue(PluginConstants.PLUGIN_PORT_FIELD_ID);
        return result.intValue();
    }

    /**
     * Finds plugin identifier from the dataSet of the provided plugin case
     * */
    public static String getPluginIdentifier(Case pluginCase) {
        return (String) pluginCase.getFieldValue(PluginConstants.PLUGIN_IDENTIFIER_FIELD_ID);
    }

    /**
     * Gets plugin entry point cases
     *
     * @param pluginCase case instance, which is associated with the entry points
     *
     * @return list of entry point cases
     * */
    @SuppressWarnings("unchecked")
    public List<Case> getPluginEntryPoints(Case pluginCase) {
        List<String> caseIds = (List<String>) pluginCase.getFieldValue(PluginConstants.PLUGIN_ENTRY_POINTS_FIELD_ID);
        return caseIds == null || caseIds.isEmpty() ? new ArrayList<>() : workflowService.findAllById(caseIds);
    }

    /**
     * Finds entry point case ids from the dataSet of the provided plugin case
     * */
    @SuppressWarnings("unchecked")
    public static List<String> getPluginEntryPointIds(Case pluginCase) {
        return (List<String>) pluginCase.getFieldValue(PluginConstants.PLUGIN_ENTRY_POINTS_FIELD_ID);
    }

    /**
     * Gets plugin entry point cases
     *
     * @param entryPointCase case instance, which is associated with the methods
     *
     * @return list of method cases
     * */
    @SuppressWarnings("unchecked")
    public List<Case> getEntryPointMethods(Case entryPointCase) {
        List<String> caseIds = (List<String>) entryPointCase.getFieldValue(PluginConstants.ENTRY_POINT_METHODS_FIELD_ID);
        return caseIds == null || caseIds.isEmpty() ? new ArrayList<>() : workflowService.findAllById(caseIds);
    }

    /**
     * Finds method case ids from the dataSet of the provided entry point case
     * */
    @SuppressWarnings("unchecked")
    public static List<String> getEntryPointMethodIds(Case entryPointCase) {
        return (List<String>) entryPointCase.getFieldValue(PluginConstants.ENTRY_POINT_METHODS_FIELD_ID);
    }

    /**
     * Finds entry point name from the dataSet of the provided entry point case
     * */
    public static String getEntryPointName(Case entryPointCase) {
        return (String) entryPointCase.getFieldValue(PluginConstants.ENTRY_POINT_NAME_FIELD_ID);
    }

    /**
     * Finds method name from the dataSet of the provided method case
     * */
    public static String getMethodName(Case methodCase) {
        return (String) methodCase.getFieldValue(PluginConstants.METHOD_NAME_FIELD_ID);
    }

    /**
     * Finds method arguments from the dataSet of the provided method case
     * */
    @SuppressWarnings("unchecked")
    public static List<String> getMethodArguments(Case methodCase) {
        return (List<String>) methodCase.getFieldValue(PluginConstants.METHOD_ARGUMENTS_FIELD_ID);
    }

    /**
     * Finds method signature hash from the dataSet of the provided method case
     * */
    public static String getMethodSignatureHash(Case methodCase) {
        return (String) methodCase.getFieldValue(PluginConstants.METHOD_HASHED_SIGNATURE_FIELD_ID);
    }

    /**
     * Assigns task by provided task id. If the task is already assigned, it cancels the task and tries again.
     *
     * @param taskId task id of the task to be assigned
     *
     * @return task assign outcome
     * */
    public AssignTaskEventOutcome safelyAssignTask(String taskId) throws TransitionNotExecutableException {
        try {
            return taskService.assignTask(taskId);
        } catch (TransitionNotExecutableException maybeRethrow) {
            Task aTask = taskService.findOne(taskId);
            if (aTask == null) {
                throw maybeRethrow;
            }
            IUser user = userService.getLoggedOrSystem();
            aTask = taskService.cancelTask(aTask, user).getTask();
            return taskService.assignTask(aTask, user);
        }
    }

    /**
     * Serializes provided object into {@link ByteString}
     *
     * @param object object to be serialized
     *
     * @return serialized object into {@link ByteString}
     * */
    public static ByteString serializeObject(Serializable object) {
        return ByteString.copyFrom(Objects.requireNonNull(SerializationUtils.serialize(object)));
    }

    /**
     * Deserializes provided object as {@link ByteString} into {@link Object}
     *
     * @param object object to be deserialized
     *
     * @return deserialized object into {@link Object}
     * */
    public static Object deserializeObject(ByteString object) {
        return SerializationUtils.deserialize(object.toByteArray());
    }

    /**
     * Creates SHA-256 hash of method name and list of argument types.
     *
     * @param methodName name of the method
     * @param argTypes list of argument types as string
     *
     * @return SHA-256 string hash of the provided input
     * */
    public static String hashMethodSignature(String methodName, List<String> argTypes) throws NoSuchAlgorithmException {
        String original = methodName + argTypes;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(original.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedHash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
