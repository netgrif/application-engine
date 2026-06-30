package com.netgrif.application.engine.action

import com.netgrif.application.engine.importer.service.FieldFactory
import com.netgrif.application.engine.objects.petrinet.domain.Transition
import com.netgrif.application.engine.objects.petrinet.domain.dataset.NumberField
import com.netgrif.application.engine.objects.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action
import com.netgrif.application.engine.objects.workflow.domain.DataField
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

class ActionDelegateUnitTest {

    @Test
    void initPopulatesFieldAndTransitionAliasesAndClearRemovesExecutionState() {
        def delegate = new ActionDelegate()
        def fieldFactory = mock(FieldFactory)
        def useCase = caseWithNet()
        def textField = new TextField()
        textField.importId = "text"
        def transition = new Transition()
        transition.importId = "submit"
        useCase.petriNet.transitions = ["transition-id": transition] as LinkedHashMap
        delegate.fieldFactory = fieldFactory
        when(fieldFactory.buildFieldWithoutValidation(useCase, "text-id", null)).thenReturn(textField)

        def action = new Action()
        action.fieldIds = [text: "text-id"]
        action.transitionIds = [submit: "transition-id"]
        delegate.init(action, useCase, Optional.empty(), mock(FieldActionsRunner), [source: "unit"])

        assertSame(textField, delegate.get("text"))
        assertSame(transition, delegate.get("submit"))
        assertSame(action, delegate.action)
        assertSame(useCase, delegate.useCase)
        assertEquals([source: "unit"], delegate.params)
        assertNotNull(delegate.Frontend)
        assertNotNull(delegate.NaeModule)
        assertNotNull(delegate.Plugin)
        assertTrue(delegate.outcomes.isEmpty())

        delegate.clearAfterExecution()

        assertNull(delegate.action)
        assertNull(delegate.useCase)
        assertNull(delegate.task)
        assertEquals([source: "unit"], delegate.params)
        assertNull(delegate.map)
        assertNull(delegate.outcomes)
        assertNull(delegate.Frontend)
        assertNull(delegate.NaeModule)
        assertNull(delegate.Plugin)
    }

    @Test
    void cacheOperationsAreScopedByCaseId() {
        def delegate = new ActionDelegate()
        def runner = mock(FieldActionsRunner)
        def useCase = caseWithNet()
        delegate.@useCase = useCase
        delegate.@actionsRunner = runner
        when(runner.getFromCache("${useCase.stringId}-answer")).thenReturn(42)

        delegate.cache("answer", 42)
        def cached = delegate.cache("answer")
        delegate.cacheFree("answer")

        assertEquals(42, cached)
        verify(runner).addToCache("${useCase.stringId}-answer", 42)
        verify(runner).getFromCache("${useCase.stringId}-answer")
        verify(runner).removeFromCache("${useCase.stringId}-answer")
    }

    @Test
    void changeFieldValueWritesChangedValueAndOutcome() {
        def delegate = delegateWithDataService()
        def useCase = caseWithNet()
        delegate.@useCase = useCase
        def field = new TextField()
        field.importId = "text"
        field.value = "old"
        useCase.dataSet.put("text", new DataField("old"))
        when(delegate.@dataService.applyFieldConnectedChanges(useCase, field)).thenReturn(useCase)

        delegate.changeFieldValue(field, { "new" }, useCase, Optional.empty())

        assertEquals("new", useCase.dataSet.get("text").value)
        assertEquals(1, delegate.outcomes.size())
        assertTrue(delegate.outcomes.first().changedFields.containsKey("text"))
    }

    @Test
    void changeFieldValueClearsNullAndCoercesNumbers() {
        def delegate = delegateWithDataService()
        def useCase = caseWithNet()
        delegate.@useCase = useCase
        def text = new TextField()
        text.importId = "text"
        text.value = "old"
        useCase.dataSet.put("text", new DataField("old"))
        doAnswer { invocation -> invocation.getArgument(0) }
                .when(delegate.@dataService)
                .applyFieldConnectedChanges(
                        any(com.netgrif.application.engine.objects.workflow.domain.Case.class),
                        any(com.netgrif.application.engine.objects.petrinet.domain.dataset.Field.class)
                )

        delegate.changeFieldValue(text, { null }, useCase, Optional.empty())

        assertNull(useCase.dataSet.get("text").value)

        def number = new NumberField()
        number.importId = "amount"
        useCase.dataSet.put("amount", new DataField(0.0d))

        delegate.changeFieldValue(number, { "42.5" }, useCase, Optional.empty())

        assertEquals(42.5d, useCase.dataSet.get("amount").value)
        assertEquals(2, delegate.outcomes.size())
    }

    @Test
    void helpersReturnDslConstantsAndMappedData() {
        def delegate = new ActionDelegate()
        def text = new TextField()
        text.importId = "text"

        delegate.set("key", "value")

        assertEquals("value", delegate.get("key"))
        assertEquals(ActionDelegate.UNCHANGED_VALUE, delegate.unchanged())
        assertEquals(ActionDelegate.ALWAYS_GENERATE, delegate.always())
        assertEquals(ActionDelegate.ONCE_GENERATE, delegate.once())
        assertEquals(ActionDelegate.TRANSITIONS, delegate.transitions())
        assertEquals("wrapped", delegate.nullable("wrapped").get())
        assertEquals([text: text], delegate.mapData([text]))
    }

    private ActionDelegate delegateWithDataService() {
        def delegate = new ActionDelegate()
        delegate.@dataService = mock(IDataService, { invocation ->
            if (invocation.method.name == "applyFieldConnectedChanges") {
                return invocation.getArgument(0)
            }
            return RETURNS_DEFAULTS.answer(invocation)
        } as org.mockito.stubbing.Answer)
        delegate.@outcomes = []
        delegate.@task = Optional.empty()
        return delegate
    }

    private com.netgrif.application.engine.adapter.spring.workflow.domain.Case caseWithNet() {
        def useCase = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case()
        useCase.set_id(new ProcessResourceId())
        useCase.petriNet = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet()
        useCase.petriNet.transitions = new LinkedHashMap<>()
        useCase.dataSet = new LinkedHashMap<>()
        return useCase
    }
}
