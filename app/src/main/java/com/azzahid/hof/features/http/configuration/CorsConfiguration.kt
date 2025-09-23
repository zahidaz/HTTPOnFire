package com.azzahid.hof.features.http.configuration

import com.azzahid.hof.domain.model.CorsConfiguration
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

internal fun Application.configureCors(corsConfig: CorsConfiguration) {
    install(CORS) {
        if (corsConfig.allowAnyHost) {
            anyHost()
        } else {
            corsConfig.allowedHosts.forEach { host ->
                allowHost(host)
            }
        }

        corsConfig.allowedMethods.forEach { method ->
            when (method.uppercase()) {
                "GET" -> allowMethod(io.ktor.http.HttpMethod.Get)
                "POST" -> allowMethod(io.ktor.http.HttpMethod.Post)
                "PUT" -> allowMethod(io.ktor.http.HttpMethod.Put)
                "DELETE" -> allowMethod(io.ktor.http.HttpMethod.Delete)
                "PATCH" -> allowMethod(io.ktor.http.HttpMethod.Patch)
                "OPTIONS" -> allowMethod(io.ktor.http.HttpMethod.Options)
            }
        }

        corsConfig.allowedHeaders.forEach { header ->
            allowHeader(header)
        }

        allowCredentials = corsConfig.allowCredentials
    }
}