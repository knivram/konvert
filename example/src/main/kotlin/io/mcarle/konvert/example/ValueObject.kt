package io.mcarle.konvert.example

import io.mcarle.konvert.api.Konverter
import java.util.UUID

data class UserId(val value: UUID)
data class User(val id: UserId)

data class UserDto(val id: UUID)

@Konverter
interface UserMapper {
    fun toDto(user: User): UserDto
    fun toDto(id: UserId): UUID = id.value

    fun toDomain(userDto: UserDto): User
    fun toDomain(id: UUID): UserId = UserId(id)
}
