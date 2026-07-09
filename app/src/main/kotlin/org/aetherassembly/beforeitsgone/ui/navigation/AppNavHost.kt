package org.aetherassembly.beforeitsgone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.aetherassembly.beforeitsgone.ui.screen.additem.AddEditItemScreen
import org.aetherassembly.beforeitsgone.ui.screen.inventory.InventoryScreen
import org.aetherassembly.beforeitsgone.ui.screen.recipes.RecipesScreen
import org.aetherassembly.beforeitsgone.ui.screen.scanner.BarcodeScannerScreen
import org.aetherassembly.beforeitsgone.ui.screen.settings.SettingsScreen
import org.aetherassembly.beforeitsgone.ui.screen.shopping.ShoppingListScreen
import org.aetherassembly.beforeitsgone.ui.screen.wastelog.WasteLogScreen

sealed class Screen(val route: String) {
    data object Inventory : Screen("inventory")
    data object AddItem : Screen("add_item")
    data object EditItem : Screen("edit_item/{itemId}") {
        fun withId(id: String) = "edit_item/$id"
    }
    data object BarcodeScanner : Screen("scanner")
    data object Settings : Screen("settings")
    data object WasteLog : Screen("waste_log")
    data object ShoppingList : Screen("shopping_list")
    data object Recipes : Screen("recipes")
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Inventory.route) {
        composable(Screen.Inventory.route) {
            InventoryScreen(
                onAddItem = { navController.navigate(Screen.AddItem.route) },
                onEditItem = { id -> navController.navigate(Screen.EditItem.withId(id)) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenWasteLog = { navController.navigate(Screen.WasteLog.route) },
                onOpenShoppingList = { navController.navigate(Screen.ShoppingList.route) },
                onOpenRecipes = { navController.navigate(Screen.Recipes.route) }
            )
        }
        composable(Screen.AddItem.route) {
            AddEditItemScreen(
                itemId = null,
                onScan = { navController.navigate(Screen.BarcodeScanner.route) },
                onDone = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditItem.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStack ->
            AddEditItemScreen(
                itemId = backStack.arguments?.getString("itemId"),
                onScan = { navController.navigate(Screen.BarcodeScanner.route) },
                onDone = { navController.popBackStack() }
            )
        }
        composable(Screen.BarcodeScanner.route) {
            BarcodeScannerScreen(
                onBarcodeDetected = { barcode ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_barcode", barcode)
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.WasteLog.route) {
            WasteLogScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ShoppingList.route) {
            ShoppingListScreen(
                onBack = { navController.popBackStack() },
                onOpenItem = { id -> navController.navigate(Screen.EditItem.withId(id)) }
            )
        }
        composable(Screen.Recipes.route) {
            RecipesScreen(onBack = { navController.popBackStack() })
        }
    }
}
