package kz.taxidrive.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kz.taxidrive.app.screens.AdminScreen
import kz.taxidrive.app.screens.DriverScreen
import kz.taxidrive.app.screens.LoginScreen
import kz.taxidrive.app.screens.PassengerScreen
import kz.taxidrive.app.screens.RegisterScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {

            LoginScreen(
                abrirRegistro = {
                    navController.navigate(Routes.REGISTER)
                },
                loginCorrecto = {
                    navController.navigate(Routes.HOME)
                }
            )

        }

        composable(Routes.REGISTER) {

            RegisterScreen(
                volverLogin = {
                    navController.popBackStack()
                }
            )

        }

        composable(Routes.PASSENGER) {

            PassengerScreen(
                volver = {
                    navController.popBackStack()
                }
            )

        }

        composable(Routes.DRIVER) {

            DriverScreen(
                volver = {
                    navController.popBackStack()
                }
            )

        }

        composable(Routes.ADMIN) {

            AdminScreen(
                volver = {
                    navController.popBackStack()
                }
            )

        }

    }

}