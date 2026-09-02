package com.netgrif.application.engine.action

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase
import com.netgrif.application.engine.objects.petrinet.domain.I18nString
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemConstants
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemView
import com.netgrif.application.engine.menu.services.interfaces.IMenuItemService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.MenuProcessRunner
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class MenuItemUploadTest {

    private static final String TEST_NET = "menu_item_upload_test.xml"

    private static final List<String> ORDER_ITEM_IDENTIFIERS = (1..15).collect {
        "menu_test_order_${it}" as String
    }

    private static final String NO_ORDER_ITEM_IDENTIFIER = "menu_test_order_missing"
    private static final String CLEARED_ORDER_ITEM_IDENTIFIER = "menu_test_order_cleared"
    private static final String I18N_ORDER_ITEM_IDENTIFIER = "menu_test_order_i18n"

    private static final List<Integer> RANDOM_CREATION_ORDER = (0..<100).collect { index ->
        ((index * 37 + 17) % 100) + 1
    }

    private static final List<String> RANDOM_ITEM_IDENTIFIERS = (1..100).collect {
        randomItemIdentifier(it)
    }

    private static final String CHILD_IDS_ONLY_DEMO_IDENTIFIER = "menu_test_child_ids_only_demo"

    private static final List<Integer> CHILD_IDS_ONLY_CREATION_ORDER = [7, 2, 9, 1, 5, 3, 10, 4, 8, 6]

    private static final List<String> CHILD_IDS_ONLY_ITEM_IDENTIFIERS = (1..10).collect {
        childIdsOnlyItemIdentifier(it)
    }

    private static final List<String> MENU_IDENTIFIERS = [
            "menu_test_root",
            "menu_test_order_demo",
            NO_ORDER_ITEM_IDENTIFIER,
            CLEARED_ORDER_ITEM_IDENTIFIER,
            I18N_ORDER_ITEM_IDENTIFIER,
            "menu_test_random_demo",
            CHILD_IDS_ONLY_DEMO_IDENTIFIER,
            "menu_test_overview",
            "menu_test_sales",
            "menu_test_customers",
            "menu_test_offers",
            "menu_test_operations",
            "menu_test_work_queue",
            "menu_test_settings",
            "menu_test_users",
            "menu_test_audit"
    ] + ORDER_ITEM_IDENTIFIERS + RANDOM_ITEM_IDENTIFIERS + CHILD_IDS_ONLY_ITEM_IDENTIFIERS

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IMenuItemService menuItemService

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void uploadCreatesCompleteNestedMenuAndReuploadIsIdempotent() {
        assert importHelper.createNet(TEST_NET).isPresent()

        Case root = menuItem("menu_test_root")
        Case orderDemo = menuItem("menu_test_order_demo")
        Case noOrderItem = menuItem(NO_ORDER_ITEM_IDENTIFIER)
        Case clearedOrderItem = menuItem(CLEARED_ORDER_ITEM_IDENTIFIER)
        Case i18nOrderItem = menuItem(I18N_ORDER_ITEM_IDENTIFIER)
        List<Case> orderItems = ORDER_ITEM_IDENTIFIERS.collect { menuItem(it) }
        Case randomDemo = menuItem("menu_test_random_demo")
        Map<Integer, Case> randomItems = (1..100).collectEntries { itemOrder ->
            [(itemOrder): menuItem(randomItemIdentifier(itemOrder))]
        }
        List<Case> randomItemsInCreationOrder = RANDOM_CREATION_ORDER.collect { randomItems[it] }
        Case childIdsOnlyDemo = menuItem(CHILD_IDS_ONLY_DEMO_IDENTIFIER)
        Map<Integer, Case> childIdsOnlyItems = (1..10).collectEntries { itemNumber ->
            [(itemNumber): menuItem(childIdsOnlyItemIdentifier(itemNumber))]
        }
        List<Case> childIdsOnlyItemsInCreationOrder = CHILD_IDS_ONLY_CREATION_ORDER.collect {
            childIdsOnlyItems[it]
        }
        Case overview = menuItem("menu_test_overview")
        Case sales = menuItem("menu_test_sales")
        Case customers = menuItem("menu_test_customers")
        Case offers = menuItem("menu_test_offers")
        Case operations = menuItem("menu_test_operations")
        Case workQueue = menuItem("menu_test_work_queue")
        Case settings = menuItem("menu_test_settings")
        Case users = menuItem("menu_test_users")
        Case audit = menuItem("menu_test_audit")

        assertMenuItem(root, "/menu_test_root", null)
        assertMenuItem(orderDemo, "/menu_test_root/menu_test_order_demo", root)
        assertMenuItem(
                noOrderItem,
                "/menu_test_root/menu_test_order_demo/${NO_ORDER_ITEM_IDENTIFIER}" as String,
                orderDemo
        )
        assertMenuItem(
                clearedOrderItem,
                "/menu_test_root/menu_test_order_demo/${CLEARED_ORDER_ITEM_IDENTIFIER}" as String,
                orderDemo
        )
        assertMenuItem(
                i18nOrderItem,
                "/menu_test_root/menu_test_order_demo/${I18N_ORDER_ITEM_IDENTIFIER}" as String,
                orderDemo
        )
        orderItems.eachWithIndex { item, index ->
            assertMenuItem(
                    item,
                    "/menu_test_root/menu_test_order_demo/menu_test_order_${index + 1}" as String,
                    orderDemo
            )
        }
        assertMenuItem(randomDemo, "/menu_test_root/menu_test_random_demo", root)
        randomItems.each { itemOrder, item ->
            assertMenuItem(
                    item,
                    "/menu_test_root/menu_test_random_demo/${randomItemIdentifier(itemOrder)}" as String,
                randomDemo
            )
        }
        assertMenuItem(childIdsOnlyDemo, "/menu_test_root/menu_test_child_ids_only_demo", root)
        childIdsOnlyItems.each { itemNumber, item ->
            assertMenuItem(
                    item,
                    "/menu_test_root/menu_test_child_ids_only_demo/${childIdsOnlyItemIdentifier(itemNumber)}" as String,
                    childIdsOnlyDemo
            )
        }
        assertMenuItem(overview, "/menu_test_root/menu_test_overview", root)
        assertMenuItem(sales, "/menu_test_root/menu_test_sales", root)
        assertMenuItem(customers, "/menu_test_root/menu_test_sales/menu_test_customers", sales)
        assertMenuItem(offers, "/menu_test_root/menu_test_sales/menu_test_offers", sales)
        assertMenuItem(operations, "/menu_test_root/menu_test_operations", root)
        assertMenuItem(workQueue, "/menu_test_root/menu_test_operations/menu_test_work_queue", operations)
        assertMenuItem(settings, "/menu_test_root/menu_test_operations/menu_test_settings", operations)
        assertMenuItem(users, "/menu_test_root/menu_test_operations/menu_test_settings/menu_test_users", settings)
        assertMenuItem(audit, "/menu_test_root/menu_test_operations/menu_test_settings/menu_test_audit", settings)

        assert RANDOM_CREATION_ORDER.toSet() == (1..100).toSet()
        assert RANDOM_CREATION_ORDER != (1..100).toList()

        assertChildren(root, [orderDemo, randomDemo, childIdsOnlyDemo, overview, sales, operations])
        assertChildren(orderDemo, [noOrderItem, clearedOrderItem, i18nOrderItem] + orderItems)
        assertChildren(randomDemo, randomItemsInCreationOrder)
        assertChildren(childIdsOnlyDemo, childIdsOnlyItemsInCreationOrder)
        assert menuItemService.getOrderedMenuItemChildren(childIdsOnlyDemo)*.stringId ==
                childIdsOnlyItemsInCreationOrder*.stringId
        assertChildren(sales, [customers, offers])
        assertChildren(operations, [workQueue, settings])
        assertChildren(settings, [users, audit])

        assertOrder(root, 0)
        assertOrder(orderDemo, 1)
        orderItems.eachWithIndex { item, index -> assertOrder(item, index + 1) }
        assertOrder(noOrderItem, null)
        assertOrder(clearedOrderItem, null)
        assertOrder(i18nOrderItem, 6.5)
        assertOrder(randomDemo, 2)
        randomItems.each { itemOrder, item -> assertOrder(item, itemOrder) }
        assertOrder(childIdsOnlyDemo, null)
        childIdsOnlyItems.values().each { assertOrder(it, null) }
        assertOrder(overview, null)
        assertOrder(sales, 20)
        assertOrder(customers, 5)
        assertOrder(offers, 5)
        assertOrder(operations, 10)
        assertOrder(workQueue, null)
        assertOrder(settings, 0)
        assertOrder(users, null)
        assertOrder(audit, 0)

        I18nString i18nMenuName = i18nOrderItem.dataSet[MenuItemConstants.FIELD_MENU_NAME].value as I18nString
        assert i18nMenuName.defaultValue == "I18N menu test"
        assert i18nMenuName.getTranslation("sk") == "I18N test – Slovenčina - 6.5"
        assert i18nMenuName.getTranslation("en") == "I18N test – English - 6.5"

        assert overview.dataSet[MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE].value ==
                MenuItemView.TABBED_CASE_VIEW.identifier
        assert customers.dataSet[MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE].value ==
                MenuItemView.TABBED_CASE_VIEW.identifier
        assert workQueue.dataSet[MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE].value ==
                MenuItemView.TABBED_TASK_VIEW.identifier
        assert overview.dataSet[MenuItemConstants.FIELD_ALLOWED_ROLES].options
                .containsKey("menu_test_viewer:menu_upload_test")

        Map<String, String> originalCaseIds = MENU_IDENTIFIERS.collectEntries {
            [(it): menuItem(it).stringId]
        }

        assert importHelper.createNet(TEST_NET).isPresent()

        MENU_IDENTIFIERS.each { identifier ->
            Case itemAfterReupload = menuItem(identifier)
            assert itemAfterReupload.stringId == originalCaseIds[identifier]
            assert countMenuItems(identifier) == 1
        }

        assertChildren(menuItem("menu_test_root"), [
                menuItem("menu_test_order_demo"),
                menuItem("menu_test_random_demo"),
                menuItem(CHILD_IDS_ONLY_DEMO_IDENTIFIER),
                menuItem("menu_test_overview"),
                menuItem("menu_test_sales"),
                menuItem("menu_test_operations")
        ])
        assertChildren(
                menuItem("menu_test_order_demo"),
                [
                        menuItem(NO_ORDER_ITEM_IDENTIFIER),
                        menuItem(CLEARED_ORDER_ITEM_IDENTIFIER),
                        menuItem(I18N_ORDER_ITEM_IDENTIFIER)
                ] +
                        ORDER_ITEM_IDENTIFIERS.collect { menuItem(it) }
        )
        assertChildren(
                menuItem("menu_test_random_demo"),
                RANDOM_CREATION_ORDER.collect { menuItem(randomItemIdentifier(it)) }
        )
        assertChildren(
                menuItem(CHILD_IDS_ONLY_DEMO_IDENTIFIER),
                CHILD_IDS_ONLY_CREATION_ORDER.collect { menuItem(childIdsOnlyItemIdentifier(it)) }
        )
    }

    private Case menuItem(String identifier) {
        Case result = workflowService.searchOne(menuItemPredicate(identifier))
        assert result != null: "Menu item [$identifier] was not created by the upload event"
        return result
    }

    private long countMenuItems(String identifier) {
        return workflowService.searchAll(menuItemPredicate(identifier)).totalElements
    }

    private static def menuItemPredicate(String identifier) {
        return QCase.case$.processIdentifier.eq(MenuProcessRunner.MENU_NET_IDENTIFIER)
                .and(QCase.case$.dataSet.get(MenuItemConstants.FIELD_IDENTIFIER).value.eq(identifier))
    }

    private static String randomItemIdentifier(Number itemNumber) {
        return "menu_test_random_${itemNumber.toString().padLeft(3, '0')}" as String
    }

    private static String childIdsOnlyItemIdentifier(Number itemNumber) {
        return "menu_test_child_ids_only_${itemNumber.toString().padLeft(2, '0')}" as String
    }

    private static void assertMenuItem(Case item, String expectedPath, Case expectedParent) {
        assert item.dataSet[MenuItemConstants.FIELD_NODE_PATH].value == expectedPath
        if (expectedParent != null) {
            assert item.dataSet[MenuItemConstants.FIELD_PARENT_ID].value == [expectedParent.stringId]
        }
    }

    private static void assertChildren(Case parent, List<Case> expectedChildren) {
        assert parent.dataSet[MenuItemConstants.FIELD_CHILD_ITEM_IDS].value ==
                expectedChildren.collect { it.stringId }
    }

    private static void assertOrder(Case item, Number expectedOrder) {
        assert item.dataSet[MenuItemConstants.FIELD_ORDER].value == expectedOrder
    }
}
