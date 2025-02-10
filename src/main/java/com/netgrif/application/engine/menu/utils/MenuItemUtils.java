package com.netgrif.application.engine.menu.utils;

import com.netgrif.application.engine.menu.domain.MenuItemConstants;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.TaskPair;

import java.text.Normalizer;
import java.util.List;

public class MenuItemUtils {

    /**
     * todo javadoc
     * */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[\\W-]+", "-")
                .toLowerCase();
    }

    /**
     * todo javadoc
     */
    public static String findTaskIdInCase(Case useCase, String transId) {
        if (useCase == null || transId == null) {
            return null;
        }

        TaskPair resultPair = useCase.getTasks().stream()
                .filter(taskPair -> taskPair.getTransition().equals(transId))
                .findFirst().orElse(null);

        if (resultPair == null) {
            return null;
        }

        return resultPair.getTask();
    }

    /**
     * todo javadoc
     * */
    public static boolean isCyclicNodePath(Case folderItem, String destUri) {
        String oldNodePath = (String) folderItem.getFieldValue(MenuItemConstants.FIELD_NODE_PATH);
        return destUri.contains(oldNodePath);
    }

    /**
     * todo javadoc
     * */
    @SuppressWarnings("unchecked")
    public static List<String> getCaseIdsFromCaseRef(Case useCase, String caseRefId) {
        try {
            return (List<String>) useCase.getFieldValue(caseRefId);
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    /**
     * todo javadoc
     * */
    public static String getCaseIdFromCaseRef(Case useCase, String caseRefId) {
        List<String> caseIds = getCaseIdsFromCaseRef(useCase, caseRefId);
        if (caseIds == null || caseIds.isEmpty()) {
            return null;
        }
        return caseIds.get(0);
    }

    /**
     * todo javadoc
     * */
    public static boolean hasView(Case menuItemCase) {
        return getCaseIdFromCaseRef(menuItemCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID) != null;
    }

    /**
     * todo javadoc
     * */
    public static boolean hasFolderChildren(Case folderCase) {
        List<String> childIds = MenuItemUtils.getCaseIdsFromCaseRef(folderCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        return childIds != null && !childIds.isEmpty();
    }

}
