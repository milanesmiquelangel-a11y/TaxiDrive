package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(

    abrirBuscar: () -> Unit,

    abrirPasajero: () -> Unit,

    abrirConductor: () -> Unit,

    abrirPerfil: () -> Unit,

    cerrarSesion: () -> Unit

) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text("TaxiDrive.kz")

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),

            verticalArrangement = Arrangement.Center,

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Text(

                text = "Bienvenido",

                fontSize = 28.sp,

                fontWeight = FontWeight.Bold,

                color = MaterialTheme.colorScheme.primary

            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(

                onClick = abrirBuscar,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("🔍 Buscar viajes")

            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(

                onClick = abrirPasajero,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("➕ Publicar solicitud")

            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(

                onClick = abrirConductor,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("🚖 Publicar oferta")

            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(

                onClick = abrirPerfil,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("👤 Mi perfil")

            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(

                onClick = cerrarSesion,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Cerrar sesión")

            }

        }

    }

}