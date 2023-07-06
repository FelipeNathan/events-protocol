package br.com.guiabolso.events.server.handler

import br.com.guiabolso.events.model.RequestEvent

interface ConvertingEventHandler<T> : EventHandler {
    fun convert(input: RequestEvent): T

    suspend fun handle(input: RequestEvent, converted: T): EventResponder

    override suspend fun handle(event: RequestEvent): EventResponder = handle(event, convert(event))
}
