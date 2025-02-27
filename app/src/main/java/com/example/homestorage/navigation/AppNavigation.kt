package com.example.homestorage.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.homestorage.screens.AddContainerScreen
import com.example.homestorage.screens.AddRoomScreen
import com.example.homestorage.screens.AddSubContainerScreen
import com.example.homestorage.screens.AddThirdContainerScreen
import com.example.homestorage.screens.BatchEditScreen
import com.example.homestorage.screens.ContainerScreen
import com.example.homestorage.screens.ExportImportScreen
import com.example.homestorage.screens.HomeScreen
import com.example.homestorage.screens.ItemCategoryFormScreen
import com.example.homestorage.screens.ItemCategoryManagementScreen
import com.example.homestorage.screens.ItemFormScreen
import com.example.homestorage.screens.ManageSubContainerScreen
import com.example.homestorage.screens.RoomManagementScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    // 统一的物品表单页面：若 itemId 为 0 则为添加；否则为编辑
    data object ItemForm : Screen(
        "item_form/{itemId}?defaultRoom={defaultRoom}&defaultContainer={defaultContainer}" +
                "&defaultCategory={defaultCategory}&defaultSubContainer={defaultSubContainer}" +
                "&defaultThirdContainer={defaultThirdContainer}"
    ) {
        fun createRoute(
            itemId: Int = 0,
            defaultRoom: String = "",
            defaultContainer: String = "",
            defaultCategory: String = "",
            defaultSubContainer: String = "",
            defaultThirdContainer: String = ""
        ): String {
            return "item_form/$itemId" +
                    "?defaultRoom=$defaultRoom" +
                    "&defaultContainer=$defaultContainer" +
                    "&defaultCategory=$defaultCategory" +
                    "&defaultSubContainer=$defaultSubContainer" +
                    "&defaultThirdContainer=$defaultThirdContainer"
        }
    }
    data object BatchEdit : Screen("batch_edit/{location}/{ids}") {
        fun createRoute(location: String, ids: String) = "batch_edit/$location/$ids"
    }
    data object AddRoom : Screen("add_room?roomName={roomName}") {
        fun createRoute(roomName: String? = null): String =
            if (!roomName.isNullOrBlank()) "add_room?roomName=$roomName" else "add_room"
    }
    data object AddContainer : Screen("add_container?defaultRoom={defaultRoom}&containerNameToEdit={containerNameToEdit}") {
        fun createRoute(defaultRoom: String, containerNameToEdit: String = "") =
            "add_container?defaultRoom=$defaultRoom&containerNameToEdit=$containerNameToEdit"
    }
    data object AddSubContainer : Screen("add_subcontainer") {
        fun createRoute(room: String, containerName: String) = "add_subcontainer/$room/$containerName"
    }
    data object AddItemCategory : Screen("item_category_form?room={room}&container={container}") {
        fun createRoute(room: String, container: String) = "item_category_form?room=$room&container=$container"
    }
    data object ExportImport : Screen("export_import")
    data object ContainerScreen : Screen("container_screen/{room}/{container}") {
        fun createRoute(room: String, container: String) = "container_screen/$room/$container"
    }
    data object ManageSubContainer : Screen("manage_subcontainer/{room}/{container}") {
        fun createRoute(room: String, container: String) = "manage_subcontainer/$room/$container"
    }
    data object AddThirdContainer : Screen("add_third_container/{room}/{container}/{subContainer}") {
        fun createRoute(room: String, container: String, subContainer: String) =
            "add_third_container/$room/$container/$subContainer"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = "item_form/{itemId}?" +
                    "defaultRoom={defaultRoom}&" +
                    "defaultContainer={defaultContainer}&" +
                    "defaultCategory={defaultCategory}&" +
                    "defaultSubContainer={defaultSubContainer}&" +
                    "defaultThirdContainer={defaultThirdContainer}",
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType; defaultValue = 0 },
                navArgument("defaultRoom") { type = NavType.StringType; defaultValue = "" },
                navArgument("defaultContainer") { type = NavType.StringType; defaultValue = "" },
                navArgument("defaultCategory") { type = NavType.StringType; defaultValue = "" },
                navArgument("defaultSubContainer") { type = NavType.StringType; defaultValue = "" },
                navArgument("defaultThirdContainer") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            val defaultRoom = backStackEntry.arguments?.getString("defaultRoom") ?: ""
            val defaultContainer = backStackEntry.arguments?.getString("defaultContainer") ?: ""
            val defaultCategory = backStackEntry.arguments?.getString("defaultCategory") ?: ""
            val defaultSubContainer = backStackEntry.arguments?.getString("defaultSubContainer") ?: ""
            val defaultThirdContainer = backStackEntry.arguments?.getString("defaultThirdContainer") ?: ""

            ItemFormScreen(
                navController = navController,
                itemId = itemId,
                defaultRoom = defaultRoom,
                defaultContainer = defaultContainer,
                defaultCategory = defaultCategory,
                defaultSubContainer = defaultSubContainer,
                defaultThirdContainer = defaultThirdContainer,
            )
        }
        composable(
            route = Screen.BatchEdit.route,
            arguments = listOf(
                navArgument("location") { type = NavType.StringType }, // 位置参数
                navArgument("ids") { type = NavType.StringType }        // ID列表
            )
        ) { backStackEntry ->
            val location = backStackEntry.arguments?.getString("location") ?: ""
            val ids = backStackEntry.arguments?.getString("ids") ?: ""

            BatchEditScreen(
                navController = navController,
                location = location,
                ids = ids
            )
        }
        composable(
            route = "add_room?roomName={roomName}",
            arguments = listOf(
                navArgument("roomName") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val roomNameParam = backStackEntry.arguments?.getString("roomName")
            // 你可以根据 roomNameParam 来判断是编辑还是创建
            AddRoomScreen(navController = navController)
        }
        composable(
            route = "add_container?defaultRoom={defaultRoom}&containerNameToEdit={containerNameToEdit}",
            arguments = listOf(
                navArgument("defaultRoom") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("containerNameToEdit") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val defaultRoom = backStackEntry.arguments?.getString("defaultRoom") ?: ""
            val containerNameToEdit = backStackEntry.arguments?.getString("containerNameToEdit") ?: ""

            AddContainerScreen(
                navController = navController,
                defaultRoom = defaultRoom,
                containerNameToEdit = containerNameToEdit
            )
        }
        composable(
            route = "item_category_form?categoryNameToEdit={categoryName}",
            arguments = listOf(navArgument("categoryName") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            ItemCategoryFormScreen(navController, categoryName)
        }
        composable(Screen.ExportImport.route) {
            ExportImportScreen(navController = navController)
        }
        composable(
            route = "container_screen/{room}/{container}",
            arguments = listOf(
                navArgument("room") { type = NavType.StringType },
                navArgument("container") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val room = backStackEntry.arguments?.getString("room") ?: ""
            val container = backStackEntry.arguments?.getString("container") ?: ""
            ContainerScreen(navController = navController, room = room, container = container)
        }
        composable(
            route = "manage_subcontainer/{room}/{container}",
            arguments = listOf(
                navArgument("room") { type = NavType.StringType },
                navArgument("container") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val room = backStackEntry.arguments?.getString("room") ?: ""
            val container = backStackEntry.arguments?.getString("container") ?: ""
            ManageSubContainerScreen(
                navController = navController,
                room = room,
                container = container
            )
        }
        composable(
            route = "add_subcontainer/{room}/{containerName}",
            arguments = listOf(
                navArgument("room") { type = NavType.StringType },
                navArgument("containerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val room = backStackEntry.arguments?.getString("room") ?: ""
            val containerName = backStackEntry.arguments?.getString("containerName") ?: ""
            AddSubContainerScreen(
                navController = navController,
                room = room,
                containerName = containerName
            )
        }
        composable(
            route = "add_third_container/{room}/{container}/{subContainer}",
            arguments = listOf(
                navArgument("room") { type = NavType.StringType },
                navArgument("container") { type = NavType.StringType },
                navArgument("subContainer") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val room = backStackEntry.arguments?.getString("room") ?: ""
            val container = backStackEntry.arguments?.getString("container") ?: ""
            val subContainer = backStackEntry.arguments?.getString("subContainer") ?: ""

            AddThirdContainerScreen(
                navController = navController,
                room = room,
                containerName = container,
                subContainerName = subContainer
            )
        }
        composable("room_management") {
            RoomManagementScreen(navController)
        }
        composable("item_category_management") {
            ItemCategoryManagementScreen(navController)
        }
    }
}
