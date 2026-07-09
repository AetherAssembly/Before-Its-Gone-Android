package org.aetherassembly.beforeitsgone.data.remote.openfoodfacts

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

data class OFFProduct(
    val name: String,
    val categories: List<String>,
    val imageUrl: String?
)

class OpenFoodFactsClient @Inject constructor(
    private val client: HttpClient
) {
    suspend fun lookupBarcode(barcode: String): OFFProduct? = runCatching {
        val response: OFFResponse = client.get(
            "https://world.openfoodfacts.org/api/v2/product/$barcode.json?fields=product_name,categories_tags,image_front_url"
        ) {
            header("User-Agent", "BeforeItsGone-Android/1.0 (support@aetherassembly.org)")
        }.body()

        if (response.status != 1 || response.product == null) return null

        OFFProduct(
            name = response.product.productName ?: return null,
            categories = response.product.categoriesTags
                .map { it.removePrefix("en:").replace("-", " ") },
            imageUrl = response.product.imageFrontUrl
        )
    }.getOrNull()
}

@Serializable
private data class OFFResponse(
    val status: Int = 0,
    val product: OFFProductRaw? = null
)

@Serializable
private data class OFFProductRaw(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("categories_tags") val categoriesTags: List<String> = emptyList(),
    @SerialName("image_front_url") val imageFrontUrl: String? = null
)
