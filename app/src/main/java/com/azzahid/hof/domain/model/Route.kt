package com.azzahid.hof.domain.model

import androidx.core.net.toUri
import com.azzahid.hof.domain.model.serialization.HttpMethodSerializer
import com.azzahid.hof.features.http.routing.routes.addApi
import com.azzahid.hof.features.http.routing.routes.addDirectory
import com.azzahid.hof.features.http.routing.routes.addRedirect
import com.azzahid.hof.features.http.routing.routes.addStaticFile
import com.azzahid.hof.features.http.routing.routes.builtin.addAppLaunchRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addAppListRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addAppStopRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addBatteryRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addCameraRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addDashboardRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addClipboardRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addContactsRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addDeviceInfoRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addEchoRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addFlashlightRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addLocationRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addMicrophoneRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addNotificationRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addOpenApiRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addProxyRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addQrCodeRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addRingRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addStatusRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addSwaggerRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addTtsRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addVibrateRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addVolumeRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addWifiInfoRoute
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import io.ktor.server.routing.Route as ServerRoute

@Serializable
data class Route(
    val id: String,
    val path: String,
    @Serializable(with = HttpMethodSerializer::class)
    val method: HttpMethod,
    val description: String = "",
    val type: RouteType,
    val isEnabled: Boolean = true,
    val order: Int = 0
)


@Serializable
sealed class RouteType {
    abstract fun handler(route: Route): ServerRoute.() -> Unit

    @Serializable
    sealed class BuiltInRoute : RouteType()

    @Serializable
    data class ApiRoute(
        val responseBody: String = "",
        val statusCode: Int = 200,
        val headers: Map<String, String> = emptyMap()
    ) : RouteType() {
        fun getKtorStatusCode(): HttpStatusCode = HttpStatusCode.fromValue(statusCode)
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addApi(route, headers, responseBody, getKtorStatusCode())
        }
    }

    @Serializable
    data class StaticFile(
        val fileUri: String,
        val mimeType: String? = null
    ) : RouteType() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addStaticFile(route, fileUri.toUri())
        }
    }

    @Serializable
    data class Directory(
        val directoryUri: String,
        val allowBrowsing: Boolean = true,
        val indexFile: String? = "index.html",
        val allowUpload: Boolean = false
    ) : RouteType() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addDirectory(route, directoryUri.toUri(), allowBrowsing, indexFile, allowUpload)
        }
    }

    @Serializable
    data class RedirectRoute(
        val targetUrl: String,
        val statusCode: Int = 302
    ) : RouteType() {
        fun isPermanentRedirect(): Boolean = statusCode == HttpStatusCode.MovedPermanently.value

        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addRedirect(route, isPermanentRedirect(), targetUrl)
        }
    }

    @Serializable
    object StatusRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addStatusRoute(route)
        }
    }

    @Serializable
    object OpenApiRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addOpenApiRoute(route)
        }
    }

    @Serializable
    object SwaggerRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addSwaggerRoute(route)
        }
    }

    @Serializable
    object NotificationRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addNotificationRoute(route)
        }
    }

    @Serializable
    object ProxyRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addProxyRoute(route)
        }
    }

    @Serializable
    object EchoRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addEchoRoute(route)
        }
    }

    @Serializable
    object DeviceInfoRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addDeviceInfoRoute(route)
        }
    }

    @Serializable
    object BatteryRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addBatteryRoute(route)
        }
    }

    @Serializable
    object QrCodeRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addQrCodeRoute(route)
        }
    }

    @Serializable
    object AppListRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addAppListRoute(route)
        }
    }

    @Serializable
    object ClipboardRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addClipboardRoute(route)
        }
    }

    @Serializable
    object VolumeRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addVolumeRoute(route)
        }
    }

    @Serializable
    object TtsRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addTtsRoute(route)
        }
    }

    @Serializable
    object WifiInfoRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addWifiInfoRoute(route)
        }
    }

    @Serializable
    object VibrateRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addVibrateRoute(route)
        }
    }

    @Serializable
    object FlashlightRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addFlashlightRoute(route)
        }
    }

    @Serializable
    object RingRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addRingRoute(route)
        }
    }

    @Serializable
    object LocationRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addLocationRoute(route)
        }
    }

    @Serializable
    object ContactsRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addContactsRoute(route)
        }
    }

    @Serializable
    object CameraRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addCameraRoute(route)
        }
    }

    @Serializable
    object MicrophoneRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addMicrophoneRoute(route)
        }
    }

    @Serializable
    object AppLaunchRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addAppLaunchRoute(route)
        }
    }

    @Serializable
    object AppStopRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addAppStopRoute(route)
        }
    }

    @Serializable
    object DashboardRoute : BuiltInRoute() {
        override fun handler(route: Route): ServerRoute.() -> Unit = {
            addDashboardRoute(route)
        }
    }

}


