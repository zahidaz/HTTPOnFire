package com.azzahid.hof.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: String,
    val path: String,
    val method: String,
    val description: String,
    val type: String, // JSON serialized RouteType
    val isEnabled: Boolean,
    val order: Int
)

private val RouteTypeModule = SerializersModule {
    polymorphic(RouteType::class) {
        subclass(RouteType.ApiRoute::class)
        subclass(RouteType.StaticFile::class)
        subclass(RouteType.Directory::class)
        subclass(RouteType.RedirectRoute::class)
        subclass(RouteType.ProxyRoute::class)
    }
}

private val json = Json {
    serializersModule = RouteTypeModule
}

fun Route.toEntity(): RouteEntity {
    return RouteEntity(
        id = id,
        path = path,
        method = method.value,
        description = description,
        type = json.encodeToString(type),
        isEnabled = isEnabled,
        order = order
    )
}

fun RouteEntity.toDomain(): Route {
    return Route(
        id = id,
        path = path,
        method = HttpMethod.parse(method),
        description = description,
        type = json.decodeFromString<RouteType>(type),
        isEnabled = isEnabled,
        order = order
    )
}