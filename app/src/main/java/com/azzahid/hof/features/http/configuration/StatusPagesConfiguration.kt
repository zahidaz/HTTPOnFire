package com.azzahid.hof.features.http.configuration

import com.azzahid.hof.domain.model.Failure
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

internal fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = cause.message ?: "Invalid request")
            )
        }
        exception<IllegalStateException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                Failure(error = cause.message ?: "Request could not be completed")
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(
                    error = mapOf(
                        "type" to cause::class.simpleName,
                        "message" to cause.message,
                        "localizedMessage" to cause.localizedMessage,
                        "stackTrace" to cause.stackTrace.take(5).map { it.toString() },
                        "cause" to cause.cause?.let {
                            mapOf(
                                "type" to it::class.simpleName,
                                "message" to it.message
                            )
                        }
                    ).toString())
            )
        }
    }
}