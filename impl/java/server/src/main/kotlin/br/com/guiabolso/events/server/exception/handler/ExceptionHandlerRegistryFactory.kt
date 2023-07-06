package br.com.guiabolso.events.server.exception.handler

import br.com.guiabolso.events.builder.EventBuilder
import br.com.guiabolso.events.exception.EventException
import br.com.guiabolso.events.exception.EventValidationException
import br.com.guiabolso.events.server.exception.EventNotFoundException

object ExceptionHandlerRegistryFactory {

    @JvmStatic
    fun emptyExceptionHandler(eventBuilder: EventBuilder) = ExceptionHandlerRegistry(eventBuilder)

    @JvmStatic
    fun exceptionHandler(eventBuilder: EventBuilder) =
        ExceptionHandlerRegistry(eventBuilder).apply {
            register(EventValidationException::class.java, BadProtocolExceptionHandler(eventBuilder))
            register(EventNotFoundException::class.java, EventNotFoundExceptionHandler(eventBuilder))
            register(EventException::class.java, EventExceptionExceptionHandler(eventBuilder))
        }

    @JvmStatic
    fun bypassAllExceptionHandler(eventBuilder: EventBuilder, wrapExceptionAndEvent: Boolean = true) =
        ExceptionHandlerRegistry(eventBuilder).apply {
            register(Exception::class.java, BypassExceptionHandler(wrapExceptionAndEvent))
        }
}
