package com.azzahid.hof.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azzahid.hof.domain.model.Endpoint
import com.azzahid.hof.domain.model.EndpointType
import com.azzahid.hof.domain.model.HttpMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Entity(tableName = "endpoints")
data class EndpointEntity(
    @PrimaryKey val id: String,
    val path: String,
    val method: String,
    val description: String,
    val type: String, // JSON serialized EndpointType
    val isEnabled: Boolean,
    val order: Int
)

private val endpointTypeModule = SerializersModule {
    polymorphic(EndpointType::class) {
        subclass(EndpointType.ApiEndpoint::class)
        subclass(EndpointType.StaticFile::class)
        subclass(EndpointType.Directory::class)
        subclass(EndpointType.RedirectEndpoint::class)
        subclass(EndpointType.ProxyEndpoint::class)
    }
}

private val json = Json {
    serializersModule = endpointTypeModule
}

fun Endpoint.toEntity(): EndpointEntity {
    return EndpointEntity(
        id = id,
        path = path,
        method = method.name,
        description = description,
        type = json.encodeToString(type),
        isEnabled = isEnabled,
        order = order
    )
}

fun EndpointEntity.toDomain(): Endpoint {
    return Endpoint(
        id = id,
        path = path,
        method = HttpMethod.valueOf(method),
        description = description,
        type = json.decodeFromString<EndpointType>(type),
        isEnabled = isEnabled,
        order = order
    )
}