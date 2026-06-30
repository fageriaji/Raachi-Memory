package com.raachi.memory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.raachi.memory.domain.model.Gender
import com.raachi.memory.domain.model.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val gender: Gender?,
    val age: Int?,
    val birthday: Long?,
    val email: String?,
    val mobile: String?,
    @ColumnInfo(name = "height_cm") val heightCm: Float?,
    @ColumnInfo(name = "weight_kg") val weightKg: Float?,
    @ColumnInfo(name = "profile_photo_uri") val profilePhotoUri: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
) {
    fun toDomain() = UserProfile(id, name, gender, age, birthday, email, mobile, heightCm, weightKg, profilePhotoUri, createdAt, updatedAt)

    companion object {
        fun fromDomain(model: UserProfile) = UserProfileEntity(model.id, model.name, model.gender, model.age, model.birthday, model.email, model.mobile, model.heightCm, model.weightKg, model.profilePhotoUri, model.createdAt, model.updatedAt)
    }
}