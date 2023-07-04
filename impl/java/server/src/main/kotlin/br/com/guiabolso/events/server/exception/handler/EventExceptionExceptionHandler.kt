package br.com.guiabolso.events.server.exception.handler

import br.com.guiabolso.events.exception.EventException
import br.com.guiabolso.events.model.EventMessage
import br.com.guiabolso.events.model.ResponseEvent
import br.com.guiabolso.events.server.handler.RequestEventContext
import br.com.guiabolso.tracing.Tracer
import br.com.guiabolso.tracing.utils.ExceptionUtils
import datadog.trace.api.DDTags

object EventExceptionExceptionHandler : EventExceptionHandler<EventException> {

    override suspend fun handleException(
        exception: EventException,
        event: RequestEventContext,
        tracer: Tracer,
    ): ResponseEvent {
        tracer.notifyRootError(
            exception.code,
            exception.parameters.mapValues { it.value?.toString() },
            exception.expected
        )
        tracer.addRootProperty(DDTags.ERROR_TYPE, exception.code)
        tracer.addRootProperty(DDTags.ERROR_STACK, ExceptionUtils.getStackTrace(exception))
        return event.error(exception.type, EventMessage(exception.code, exception.parameters))
    }
}
