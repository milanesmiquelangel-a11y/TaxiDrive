package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.model.User
import kz.taxidrive.app.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(

    uid: String,

    volver: () -> Unit

) {

    var usuario by remember {

        mutableStateOf(User())

    }

    LaunchedEffect(Unit) {

        UserRepository.getUser(uid) {

            if (it != null) {

                usuario = it

            }

        }

    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text("Mi perfil")

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Text(

                text = usuario.nombre,

                style = MaterialTheme.typography.headlineSmall,

                fontWeight = FontWeight.Bold

            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(

                modifier = Modifier.fillMaxWidth()

            ) {

                Column(

                    modifier = Modifier.padding(16.dp)

                ) {

                    Text("Teléfono: ${usuario.telefono}")

                    Text("Tipo: ${usuario.tipo}")

                    if (usuario.tipo == "conductor") {

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Marca: ${usuario.marca}")

                        Text("Modelo: ${usuario.modelo}")

                        Text("Color: ${usuario.color}")

                        Text("Año: ${usuario.anio}")

                        Text("Matrícula: ${usuario.matricula}")

                        Text("Licencia: ${usuario.licencia}")

                    }

                }

            }

            Spacer(modifier = Modifier.height(25.dp))

            Button(

                onClick = {

                    // Sprint siguiente

                },

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Editar perfil")

            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(

                onClick = volver,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Volver")

            }

        }

    }

}