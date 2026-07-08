package org.aetherassembly.beforeitsgone.domain.usecase

private data class ShelfLifeByLocation(val fridge: Int, val freezer: Int, val pantry: Int)

private val CATEGORY_SHELF_LIFE = mapOf(
    "dairy"      to ShelfLifeByLocation(10,  90,   3),
    "milk"       to ShelfLifeByLocation(10,  90,   3),
    "cheese"     to ShelfLifeByLocation(21,  180,  7),
    "yogurt"     to ShelfLifeByLocation(14,  60,   3),
    "butter"     to ShelfLifeByLocation(30,  365,  7),
    "eggs"       to ShelfLifeByLocation(35,  365,  7),
    "meat"       to ShelfLifeByLocation(4,   120,  1),
    "poultry"    to ShelfLifeByLocation(3,   120,  1),
    "fish"       to ShelfLifeByLocation(3,   120,  1),
    "seafood"    to ShelfLifeByLocation(3,   120,  1),
    "fruits"     to ShelfLifeByLocation(14,  365,  7),
    "vegetables" to ShelfLifeByLocation(10,  365,  5),
    "produce"    to ShelfLifeByLocation(10,  365,  5),
    "bread"      to ShelfLifeByLocation(14,  90,   7),
    "bakery"     to ShelfLifeByLocation(14,  90,   5),
    "pasta"      to ShelfLifeByLocation(730, 730,  730),
    "rice"       to ShelfLifeByLocation(730, 730,  730),
    "grains"     to ShelfLifeByLocation(365, 730,  365),
    "cereals"    to ShelfLifeByLocation(365, 365,  365),
    "canned"     to ShelfLifeByLocation(7,   1095, 1095),
    "frozen"     to ShelfLifeByLocation(7,   365,  1),
    "beverages"  to ShelfLifeByLocation(30,  365,  365),
    "juice"      to ShelfLifeByLocation(14,  365,  365),
    "snacks"     to ShelfLifeByLocation(180, 365,  180),
    "condiments" to ShelfLifeByLocation(365, 365,  365),
    "sauces"     to ShelfLifeByLocation(180, 365,  365),
    "spices"     to ShelfLifeByLocation(730, 730,  730),
    "oils"       to ShelfLifeByLocation(365, 365,  365),
    "nuts"       to ShelfLifeByLocation(180, 365,  90),
    "chocolate"  to ShelfLifeByLocation(365, 730,  365),
    "sweets"     to ShelfLifeByLocation(180, 365,  180),
)

private val DEFAULTS = ShelfLifeByLocation(14, 365, 30)

private val OFB_CATEGORY_MAP: List<Pair<Regex, String>> = listOf(
    Regex("dairy|milk|cream|fromage|cheese|yogurt|butter", RegexOption.IGNORE_CASE) to "dairy",
    Regex("egg", RegexOption.IGNORE_CASE)                                           to "eggs",
    Regex("meat|beef|pork|lamb|veal|deli", RegexOption.IGNORE_CASE)                to "meat",
    Regex("poultry|chicken|turkey|duck", RegexOption.IGNORE_CASE)                  to "poultry",
    Regex("fish|salmon|tuna|cod|seafood|shellfish", RegexOption.IGNORE_CASE)        to "fish",
    Regex("fruit|berr|apple|orange|banana|grape|melon", RegexOption.IGNORE_CASE)   to "fruits",
    Regex("vegetable|veggie|salad|greens|spinach|carrot", RegexOption.IGNORE_CASE) to "vegetables",
    Regex("bread|bakery|biscuit|cracker|pastry", RegexOption.IGNORE_CASE)          to "bread",
    Regex("pasta|noodle|spaghetti|macaroni", RegexOption.IGNORE_CASE)              to "pasta",
    Regex("rice|grain|quinoa|oat|cereal", RegexOption.IGNORE_CASE)                 to "cereals",
    Regex("canned|tinned|conserv", RegexOption.IGNORE_CASE)                        to "canned",
    Regex("frozen", RegexOption.IGNORE_CASE)                                        to "frozen",
    Regex("beverage|drink|juice|water|soda|coffee|tea", RegexOption.IGNORE_CASE)   to "beverages",
    Regex("snack|chip|crisp|popcorn", RegexOption.IGNORE_CASE)                     to "snacks",
    Regex("sauce|condiment|ketchup|mustard|mayo", RegexOption.IGNORE_CASE)         to "condiments",
    Regex("spice|herb|seasoning", RegexOption.IGNORE_CASE)                         to "spices",
    Regex("oil|vinegar", RegexOption.IGNORE_CASE)                                   to "oils",
    Regex("nut|almond|cashew|peanut|walnut", RegexOption.IGNORE_CASE)              to "nuts",
    Regex("chocolate|candy|sweet|confection", RegexOption.IGNORE_CASE)             to "sweets",
)

fun matchShelfLifeCategory(ofbCategories: List<String>): String? {
    val combined = ofbCategories.joinToString(" ").lowercase()
    return OFB_CATEGORY_MAP.firstOrNull { (pattern, _) -> pattern.containsMatchIn(combined) }?.second
}

fun predictShelfLife(ofbCategories: List<String>, location: String): Int {
    val table = matchShelfLifeCategory(ofbCategories)?.let { CATEGORY_SHELF_LIFE[it] } ?: DEFAULTS
    return when (location) {
        "fridge"  -> table.fridge
        "freezer" -> table.freezer
        else      -> table.pantry
    }
}
