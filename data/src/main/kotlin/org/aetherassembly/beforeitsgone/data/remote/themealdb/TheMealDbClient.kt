package org.aetherassembly.beforeitsgone.data.remote.themealdb

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class MealSummary(
    @SerialName("idMeal") val id: String,
    @SerialName("strMeal") val name: String,
    @SerialName("strMealThumb") val thumbnailUrl: String
)

@Serializable
private data class MealFilterResponse(
    val meals: List<MealSummary>? = null
)

class TheMealDbClient @Inject constructor(
    private val httpClient: HttpClient
) {
    private val base = "https://www.themealdb.com/api/json/v1/1"

    suspend fun searchByIngredient(ingredient: String): List<MealSummary> = try {
        httpClient.get("$base/filter.php") {
            parameter("i", ingredient)
        }.body<MealFilterResponse>().meals ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}
