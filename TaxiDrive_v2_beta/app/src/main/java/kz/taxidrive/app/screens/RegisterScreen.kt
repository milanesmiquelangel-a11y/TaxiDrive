package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.model.User
import kz.taxidrive.app.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    volverLogin: () -> Unit
) {

    var tipo by remember { mutableStateOf("pasajero") }

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var licencia by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text("Crear cuenta")

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Text("Tipo de usuario")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                RadioButton(
                    selected = tipo == "pasajero",
                    onClick = {
                        tipo = "pasajero"
                    }
                )

                Text("Pasajero")

                Spacer(modifier = Modifier.width(20.dp))

                RadioButton(
                    selected = tipo == "conductor",
                    onClick = {
                        tipo = "conductor"
                    }
                )

                Text("Conductor")

            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                },
                label = {
                    Text("Nombre")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = apellidos,
                onValueChange = {
                    apellidos = it
                },
                label = {
                    Text("Apellidos")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    telefono = it
                },
                label = {
                    Text("Teléfono")
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (tipo == "conductor") {

                Spacer(modifier = Modifier.height(20.dp))

                Text("Datos del vehículo")

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = marca,
                    onValueChange = {
                        marca = it
                    },
                    label = {
                        Text("Marca")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = modelo,
                    onValueChange = {
                        modelo = it
                    },
                    label = {
                        Text("Modelo")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = color,
                    onValueChange = {
                        color = it
                    },
                    label = {
                        Text("Color")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = anio,
                    onValueChange = {
                        anio = it
                    },
                    label = {
                        Text("Año")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = matricula,
                    onValueChange = {
                        matricula = it
                    },
                    label = {
                        Text("Matrícula")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = licencia,
                    onValueChange = {
                        licencia = it
                    },
                    label = {
                        Text("Licencia")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            if (mensaje.isNotEmpty()) {

                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {

                    if (nombre.isBlank() ||
                        apellidos.isBlank() ||
                        telefono.isBlank()
                    ) {

                        mensaje = "Complete todos los campos obligatorios"
                        return@Button
                    }

                    val usuario = User(

                        uid = telefono,

                        nombre = nombre,

                        apellidos = apellidos,

                        telefono = telefono,

                        tipo = tipo,

                        aprobado = tipo == "pasajero",

                        marca = marca,

                        modelo = modelo,

                        color = color,

                        anio = anio,

                        matricula = matricula,

                        licencia = licencia

                    )

                    UserRepository.registerUser(
                        user = usuario,
                        onSuccess = {

                            mensaje = "Cuenta creada correctamente"

                            volverLogin()

                        },
                        onFailure = {

                            mensaje = it.message ?: "Error al registrar el usuario"

                        }
                    )

                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Crear cuenta")

            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = volverLogin,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Volver al inicio de sesión")

            }

        }

    }

}