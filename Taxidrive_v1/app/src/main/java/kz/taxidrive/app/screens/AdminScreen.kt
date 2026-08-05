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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    volver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Panel de Administración")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Button(
                onClick = { volver() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← Volver")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aprobar conductores")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver solicitudes")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver ofertas")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestionar usuarios")
            }
        }
    }
}