package com.netgrif.application.engine.menu.utils;

import com.netgrif.application.engine.menu.domain.MenuItemConstants;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.TaskPair;
import com.netgrif.application.engine.menu.services.interfaces.IMenuItemService;

import java.text.Normalizer;
import java.util.List;

public class MenuItemUtils {

    /**
     * Sanitizes input. Removes any diacritical marks, replaces any special character with delimiter and lowers the
     * characters
     *
     * @param input input string to be sanitized
     *
     * @return sanitized input string
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
     * Finds task id in the provided case instance by transition id
     *
     * @param useCase case instance containing the task id to be found
     * @param transId transition identifier of the task
     *
     * @return id of found task or null otherwise
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
     * This method is mainly used for {@link IMenuItemService#moveItem(Case, String)}
     *
     * @param folderItem case instance of folder menu item
     * @param destUri path of the uri node
     *
     * @return true, if the nodePath would become cyclic to folderItem's current nodePath after item move. F.e.
     * "/node1/node2" would be cyclic to "/node1/node2/node3" after move
     * */
    public static boolean isCyclicNodePath(Case folderItem, String destUri) {
        String oldNodePath = (String) folderItem.getFieldValue(MenuItemConstants.FIELD_NODE_PATH);
        return destUri.contains(oldNodePath);
    }

    /**
     * @param useCase case instance where the caseRef exists
     * @param caseRefId id of caseRef field
     *
     * @return List of case ids inside caseRef field. Returns null if the field doesn't exist or the field's value is
     * null.
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
     * @param useCase case instance where the caseRef exists
     * @param caseRefId id of caseRef field
     *
     * @return Case id inside caseRef field. Returns null if the field doesn't exist or the caseRef is empty.
     * */
    public static String getCaseIdFromCaseRef(Case useCase, String caseRefId) {
        List<String> caseIds = getCaseIdsFromCaseRef(useCase, caseRefId);
        if (caseIds == null || caseIds.isEmpty()) {
            return null;
        }
        return caseIds.get(0);
    }

    /**
     * @param menuItemCase case instance of menu item
     *
     * @return true if the menu item contains view
     * */
    public static boolean hasView(Case menuItemCase) {
        return getCaseIdFromCaseRef(menuItemCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID) != null;
    }

    /**
     * @param folderCase case instance of folder menu item
     *
     * @return true if the folder case contains any child menu item cases
     * */
    public static boolean hasFolderChildren(Case folderCase) {
        List<String> childIds = MenuItemUtils.getCaseIdsFromCaseRef(folderCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        return childIds != null && !childIds.isEmpty();
    }

}
