package br.com.guiabolso.events.server.handler

import br.com.guiabolso.events.json.JsonAdapter
import br.com.guiabolso.events.model.RequestEvent

typealias EventConverter<T> = JsonAdapter.() -> T

interface ConvertingEventHandler<T> : EventHandler {

    fun convert(input: RequestEvent): EventConverter<T>

    suspend fun handle(input: RequestEvent, converted: T): EventResponder

    override suspend fun handle(event: RequestEvent): EventResponder {
        return {
            val converter = convert(event)
            val eventResponder = handle(event, converter.invoke(this))
            eventResponder(this)
        }
    }
}
