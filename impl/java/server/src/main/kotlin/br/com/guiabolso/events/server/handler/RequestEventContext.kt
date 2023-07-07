package br.com.guiabolso.events.server.handler

import br.com.guiabolso.events.builder.EventBuilder
import br.com.guiabolso.events.builder.EventTemplate
import br.com.guiabolso.events.json.JsonAdapter
import br.com.guiabolso.events.model.EventErrorType
import br.com.guiabolso.events.model.EventMessage
import br.com.guiabolso.events.model.RequestEvent
import br.com.guiabolso.events.model.ResponseEvent

data class RequestEventContext(
    val event: RequestEvent,
    val jsonAdapter: JsonAdapter,
) {
    private val eventBuilder = EventBuilder(jsonAdapter)

    inline fun <reified T> payloadAs(): T = event.payloadAs(jsonAdapter)

    suspend fun response(
        operations: suspend EventTemplate.() -> Unit,
    ): ResponseEvent {
        return eventBuilder.responseFor(event, operations)
    }

    fun error(
        type: EventErrorType,
        message: EventMessage,
    ): ResponseEvent {
        return eventBuilder.errorFor(event, type, message)
    }
}
