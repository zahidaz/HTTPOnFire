package com.azzahid.hof.features.http.configuration

import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.domain.model.serialization.AnySerializer
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

internal fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        val module = SerializersModule {
            contextual(Success::class as KClass<*>) {
                Success.serializer(AnySerializer)
            }
        }

        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            serializersModule = module
        })
    }
}