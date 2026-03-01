package com.azzahid.hof.domain.registry

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType

import io.ktor.http.HttpMethod

object BuiltInRouteRegistry {
    val routes = listOf(
        Route(
            id = "built-in-status",
            path = "/api/status",
            method = HttpMethod.Get,
            description = "Server health and status check",
            type = RouteType.StatusRoute,
            isEnabled = true,
            order = -1000
        ),
        Route(
            id = "built-in-openapi",
            path = "/api/json",
            method = HttpMethod.Get,
            description = "OpenAPI JSON specification",
            type = RouteType.OpenApiRoute,
            isEnabled = true,
            order = -999
        ),
        Route(
            id = "built-in-swagger",
            path = "/api/swagger",
            method = HttpMethod.Get,
            description = "Swagger UI documentation",
            type = RouteType.SwaggerRoute,
            isEnabled = true,
            order = -998
        ),
        Route(
            id = "built-in-notification",
            path = "/api/notify",
            method = HttpMethod.Post,
            description = "Trigger device notifications",
            type = RouteType.NotificationRoute,
            isEnabled = true,
            order = -997
        ),
        Route(
            id = "built-in-proxy",
            path = "/api/proxy",
            method = HttpMethod.Get,
            description = "Forward requests to external URLs",
            type = RouteType.ProxyRoute,
            isEnabled = true,
            order = -996
        ),
        Route(
            id = "built-in-echo",
            path = "/api/echo",
            method = HttpMethod.Get,
            description = "Mirror back request details",
            type = RouteType.EchoRoute,
            isEnabled = true,
            order = -995
        ),
        Route(
            id = "built-in-device-info",
            path = "/api/device",
            method = HttpMethod.Get,
            description = "Device model, OS, memory, and storage info",
            type = RouteType.DeviceInfoRoute,
            isEnabled = true,
            order = -994
        ),
        Route(
            id = "built-in-battery",
            path = "/api/battery",
            method = HttpMethod.Get,
            description = "Battery level, charging status, and health",
            type = RouteType.BatteryRoute,
            isEnabled = true,
            order = -993
        ),
        Route(
            id = "built-in-qrcode",
            path = "/api/qr",
            method = HttpMethod.Get,
            description = "Generate a QR code image from text",
            type = RouteType.QrCodeRoute,
            isEnabled = true,
            order = -992
        ),
        Route(
            id = "built-in-app-list",
            path = "/api/apps",
            method = HttpMethod.Get,
            description = "List installed apps on the device",
            type = RouteType.AppListRoute,
            isEnabled = true,
            order = -991
        ),
        Route(
            id = "built-in-clipboard",
            path = "/api/clipboard",
            method = HttpMethod.Get,
            description = "Read or write clipboard text",
            type = RouteType.ClipboardRoute,
            isEnabled = true,
            order = -990
        ),
        Route(
            id = "built-in-volume",
            path = "/api/volume",
            method = HttpMethod.Get,
            description = "Read or set device volume levels",
            type = RouteType.VolumeRoute,
            isEnabled = true,
            order = -989
        ),
        Route(
            id = "built-in-tts",
            path = "/api/speak",
            method = HttpMethod.Post,
            description = "Speak text aloud using text-to-speech",
            type = RouteType.TtsRoute,
            isEnabled = true,
            order = -988
        ),
        Route(
            id = "built-in-wifi-info",
            path = "/api/wifi",
            method = HttpMethod.Get,
            description = "WiFi connection details and signal info",
            type = RouteType.WifiInfoRoute,
            isEnabled = true,
            order = -987
        ),
        Route(
            id = "built-in-vibrate",
            path = "/api/vibrate",
            method = HttpMethod.Post,
            description = "Vibrate the device",
            type = RouteType.VibrateRoute,
            isEnabled = true,
            order = -986
        ),
        Route(
            id = "built-in-flashlight",
            path = "/api/flashlight",
            method = HttpMethod.Post,
            description = "Toggle the device flashlight",
            type = RouteType.FlashlightRoute,
            isEnabled = true,
            order = -985
        ),
        Route(
            id = "built-in-ring",
            path = "/api/ring",
            method = HttpMethod.Post,
            description = "Play alarm sound to find the device",
            type = RouteType.RingRoute,
            isEnabled = true,
            order = -984
        ),
        Route(
            id = "built-in-location",
            path = "/api/location",
            method = HttpMethod.Get,
            description = "Get device GPS location",
            type = RouteType.LocationRoute,
            isEnabled = true,
            order = -983
        ),
        Route(
            id = "built-in-contacts",
            path = "/api/contacts",
            method = HttpMethod.Get,
            description = "Read device contacts",
            type = RouteType.ContactsRoute,
            isEnabled = true,
            order = -982
        ),
        Route(
            id = "built-in-camera",
            path = "/api/camera",
            method = HttpMethod.Post,
            description = "Capture a photo from the camera",
            type = RouteType.CameraRoute,
            isEnabled = true,
            order = -981
        ),
        Route(
            id = "built-in-microphone",
            path = "/api/mic/stream",
            method = HttpMethod.Get,
            description = "Stream live audio from the device microphone",
            type = RouteType.MicrophoneRoute,
            isEnabled = true,
            order = -979
        ),
        Route(
            id = "built-in-app-launch",
            path = "/api/apps/launch",
            method = HttpMethod.Post,
            description = "Launch an installed app by package name",
            type = RouteType.AppLaunchRoute,
            isEnabled = true,
            order = -978
        ),
        Route(
            id = "built-in-app-stop",
            path = "/api/apps/stop",
            method = HttpMethod.Post,
            description = "Stop background processes of an app by package name",
            type = RouteType.AppStopRoute,
            isEnabled = true,
            order = -977
        ),
        Route(
            id = "built-in-dashboard",
            path = "/api/dashboard",
            method = HttpMethod.Get,
            description = "Interactive control panel for all device features",
            type = RouteType.DashboardRoute,
            isEnabled = true,
            order = -976
        )
    )

    fun getPreferenceKey(route: Route): String {
        val name = route.id.removePrefix("built-in-")
        return "enable_$name"
    }

    fun findByRouteType(routeType: RouteType.BuiltInRoute): Route? {
        return routes.find { it.type == routeType }
    }

    fun getAllPreferenceKeys(): List<String> = routes.map { getPreferenceKey(it) }
}