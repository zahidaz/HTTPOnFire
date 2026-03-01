package com.azzahid.hof.features.http.routing.routes.builtin

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addAppLaunchRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val packageName = call.request.queryParameters["package"]

        if (packageName.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Missing required query parameter: package")
            )
            return@post
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            call.respond(
                HttpStatusCode.NotFound,
                Failure(error = "App not found or has no launchable activity: $packageName")
            )
            return@post
        }

        try {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            call.respond(Success(data = mapOf("launched" to packageName)))
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Failed to launch app: ${e.message}")
            )
        }
    }
}

internal fun io.ktor.server.routing.Route.addAppStopRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val packageName = call.request.queryParameters["package"]

        if (packageName.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Missing required query parameter: package")
            )
            return@post
        }

        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.killBackgroundProcesses(packageName)
            call.respond(Success(data = mapOf("stopped" to packageName)))
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Failed to stop app: ${e.message}")
            )
        }
    }
}
