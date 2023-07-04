package br.com.guiabolso.events.builder

import br.com.guiabolso.events.exception.MissingEventInformationException
import br.com.guiabolso.events.json.JsonAdapter
import br.com.guiabolso.events.json.JsonNull
import br.com.guiabolso.events.json.MapperHolder
import br.com.guiabolso.events.json.TreeNode
import br.com.guiabolso.events.json.treeNodeOrNull
import br.com.guiabolso.events.model.EventErrorType
import br.com.guiabolso.events.model.EventErrorType.BadProtocol
import br.com.guiabolso.events.model.EventErrorType.EventNotFound
import br.com.guiabolso.events.model.EventMessage
import br.com.guiabolso.events.model.RedirectPayload
import br.com.guiabolso.events.model.RequestEvent
import br.com.guiabolso.events.model.ResponseEvent
import br.com.guiabolso.events.utils.EventUtils
import java.util.UUID

class EventTemplate(private val jsonAdapter: JsonAdapter) {
    var name: String? = null
    var version: Int? = null
    var id = EventUtils.eventId
    var flowId = EventUtils.flowId
    var payload: Any? = null
    var identity: Any? = null
    var auth: Any? = null
    var metadata: Any? = null

    fun toRequestEvent() = RequestEvent(
        name = this.name ?: throw MissingEventInformationException("Missing event name."),
        version = this.version ?: throw MissingEventInformationException("Missing event version."),
        id = this.id ?: throw MissingEventInformationException("Missing event id."),
        flowId = this.flowId ?: throw MissingEventInformationException("Missing event flowId."),
        payload = convertPayload(),
        identity = convertToJsonObjectOrEmpty(this.identity),
        auth = convertToJsonObjectOrEmpty(this.auth),
        metadata = convertToJsonObjectOrEmpty(this.metadata)
    )

    fun toResponseEvent() = ResponseEvent(
        name = this.name ?: throw MissingEventInformationException("Missing event name."),
        version = this.version ?: throw MissingEventInformationException("Missing event version."),
        id = this.id ?: throw MissingEventInformationException("Missing event id."),
        flowId = this.flowId ?: throw MissingEventInformationException("Missing event flowId."),
        payload = convertPayload(),
        identity = convertToJsonObjectOrEmpty(this.identity),
        auth = convertToJsonObjectOrEmpty(this.auth),
        metadata = convertToJsonObjectOrEmpty(this.metadata)
    )

    private fun convertPayload() = when (this.payload) {
        null -> JsonNull
        else -> jsonAdapter.toJsonTree(this.payload)
    }

    private fun convertToJsonObjectOrEmpty(value: Any?) = when (value) {
        null -> TreeNode()
        JsonNull -> TreeNode()
        else -> jsonAdapter.toJsonTree(value).treeNodeOrNull ?: TreeNode()
    }
}

class EventBuilder(private val jsonAdapter: JsonAdapter = MapperHolder.mapper) {

    fun event(operations: EventTemplate.() -> Unit): RequestEvent {
        return EventTemplate(jsonAdapter).apply(operations).toRequestEvent()
    }

    fun responseEvent(
        operations: EventTemplate.() -> Unit,
    ): ResponseEvent {
        return EventTemplate(jsonAdapter).apply(operations).toResponseEvent()
    }

    suspend fun responseFor(
        event: RequestEvent,
        operations: suspend EventTemplate.() -> Unit,
    ): ResponseEvent {
        return EventTemplate(jsonAdapter).apply {
            operations()

            name = "${event.name}:response"
            version = event.version
            id = id ?: event.id
            flowId = flowId ?: event.flowId
        }.toResponseEvent()
    }

    fun errorFor(
        event: RequestEvent,
        type: EventErrorType,
        message: EventMessage,
    ): ResponseEvent {
        if (type is EventErrorType.Unknown) {
            throw IllegalArgumentException(
                "This error type should not be used to send events. This error error type only exists to provide " +
                        "future compatibility with newer versions of this API."
            )
        }

        return EventTemplate(jsonAdapter).apply {
            this.name = "${event.name}:${type.typeName}"
            this.version = event.version
            this.payload = message
            this.id = this.id ?: event.id
            this.flowId = this.flowId ?: event.flowId
        }.toResponseEvent()
    }

    fun redirectFor(
        requestEvent: RequestEvent,
        payload: RedirectPayload,
    ): ResponseEvent {
        return EventTemplate(jsonAdapter).apply {
            this.name = "${requestEvent.name}:redirect"
            this.version = requestEvent.version
            this.payload = payload
            this.id = id ?: requestEvent.id
            this.flowId = flowId ?: requestEvent.flowId
        }.toResponseEvent()
    }

    fun eventNotFound(event: RequestEvent): ResponseEvent {
        return EventTemplate(jsonAdapter).apply {
            this.name = EventNotFound.typeName
            this.version = 1
            this.id = id ?: event.id
            this.flowId = flowId ?: event.flowId
            this.payload = EventMessage(
                code = "NO_EVENT_HANDLER_FOUND",
                parameters = mapOf("event" to event.name, "version" to event.version)
            )
        }.toResponseEvent()
    }

    fun badProtocol(message: EventMessage): ResponseEvent {
        return EventTemplate(jsonAdapter).apply {
            name = BadProtocol.typeName
            version = 1
            id = UUID.randomUUID().toString()
            flowId = UUID.randomUUID().toString()
            payload = message
        }.toResponseEvent()
    }
}
