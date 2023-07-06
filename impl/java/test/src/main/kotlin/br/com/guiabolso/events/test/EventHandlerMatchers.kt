package br.com.guiabolso.events.test

import br.com.guiabolso.events.json.JsonAdapter
import br.com.guiabolso.events.json.MapperHolder
import br.com.guiabolso.events.model.RequestEvent
import br.com.guiabolso.events.server.handler.ConvertingEventHandler
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny

fun <T> ConvertingEventHandler<T>.shouldConvert(
    inputEvent: RequestEvent,
    jsonAdapter: JsonAdapter = MapperHolder.mapper,
) = shouldNotThrowAny { convert(inputEvent).invoke(jsonAdapter) }

fun ConvertingEventHandler<*>.shouldNotConvert(
    inputEvent: RequestEvent,
    jsonAdapter: JsonAdapter = MapperHolder.mapper,
) = shouldThrowAny { convert(inputEvent).invoke(jsonAdapter) }
