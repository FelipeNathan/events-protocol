package br.com.guiabolso.events.server

import br.com.guiabolso.events.EventBuilderForTest
import br.com.guiabolso.events.builder.EventBuilder
import br.com.guiabolso.events.json.JsonAdapterProducer
import br.com.guiabolso.events.model.RequestEvent
import br.com.guiabolso.events.server.handler.ConvertingEventHandler
import br.com.guiabolso.events.server.handler.EventHandler
import br.com.guiabolso.events.server.handler.EventResponder
import br.com.guiabolso.events.server.handler.SimpleEventHandlerRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SimpleEventHandlerRegistryTest {
    private lateinit var handler1: Handler1
    private lateinit var handler2: Handler2
    val builder = EventBuilder(JsonAdapterProducer.mapper)

    @BeforeEach
    fun beforeEach() {

        handler1 = Handler1()
        handler2 = Handler2()
    }

    @Test
    fun `test can add multiple events (collection)`() {
        val eventHandlerDiscovery = SimpleEventHandlerRegistry()

        val handlers = listOf(handler1, handler2)

        eventHandlerDiscovery.addAll(handlers)

        val handler1 = eventHandlerDiscovery.eventHandlerFor(handler1.eventName, handler1.eventVersion)
        val handler2 = eventHandlerDiscovery.eventHandlerFor(handler2.eventName, handler2.eventVersion)
        assertEquals(handler1, handler1)
        assertEquals(handler2, handler2)
    }

    @Test
    fun `test can add multiple events (vararg)`() {
        val eventHandlerDiscovery = SimpleEventHandlerRegistry()

        val handlers = listOf(handler1, handler2)

        eventHandlerDiscovery.addAll(*handlers.toTypedArray())

        val handler1 = eventHandlerDiscovery.eventHandlerFor(handler1.eventName, handler1.eventVersion)
        val handler2 = eventHandlerDiscovery.eventHandlerFor(handler2.eventName, handler2.eventVersion)
        assertEquals(handler1, handler1)
        assertEquals(handler2, handler2)
    }

    @Test
    fun testCanHandleEvent() = runBlocking {
        val eventHandlerDiscovery = SimpleEventHandlerRegistry()

        eventHandlerDiscovery.add("event:name", 1) {
            EventBuilderForTest.buildResponseEvent()
        }

        val handler = eventHandlerDiscovery.eventHandlerFor("event:name", 1)!!

        val eventResponder = handler.handle(EventBuilderForTest.buildRequestEvent())
        assertEquals(EventBuilderForTest.buildResponseEvent(), eventResponder(builder))
    }

    @Test
    fun testReturnsNullWhenEventNotFound() {
        val eventHandlerDiscovery = SimpleEventHandlerRegistry()

        val handler = eventHandlerDiscovery.eventHandlerFor("event:name", 1)

        assertNull(handler)
    }

    @Test
    fun testThrowsExceptionWhenRegisteringDuplicatedEvent() {
        val eventHandlerDiscovery = SimpleEventHandlerRegistry()

        val handlers = listOf(handler1, handler1)

        assertThrows(IllegalStateException::class.java) {
            eventHandlerDiscovery.addAll(*handlers.toTypedArray())
        }
    }
}

private class Handler1 : EventHandler {
    var handles = 0

    override val eventName = "Dummy1"
    override val eventVersion = 1

    override suspend fun handle(event: RequestEvent): EventResponder {
        handles++
        return { responseFor(event) { } }
    }
}

private class Handler2 : ConvertingEventHandler<Unit> {
    var handles = 0

    override val eventName = "Dummy2"
    override val eventVersion = 1

    override fun convert(input: RequestEvent) {
    }

    override suspend fun handle(input: RequestEvent, converted: Unit): EventResponder = {
        handles++
        responseFor(input) { }
    }
}
