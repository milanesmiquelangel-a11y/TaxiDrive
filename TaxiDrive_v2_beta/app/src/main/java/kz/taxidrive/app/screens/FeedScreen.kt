package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.model.Offer
import kz.taxidrive.app.repository.FeedRepository
import kz.taxidrive.app.ui.components.TripCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {

    var offers by remember { mutableStateOf<List<Offer>>(emptyList()) }

    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {

        FeedRepository.loadOffers {
            offers = it
        }

    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text("Buscar viajes")

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)

        ) {

            OutlinedTextField(
                value = origin,
                onValueChange = { origin = it },
                label = { Text("Origen") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destino") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Fecha") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(

                onClick = {

                    FeedRepository.searchOffers(

                        origin = origin,

                        destination = destination,

                        date = date

                    ) {

                        offers = it

                    }

                },

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Buscar")

            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn {

                items(offers) { offer ->

                    TripCard(

                        nombre = offer.nombre,

                        tipo = "🚖 Conductor",

                        origen = offer.origin,

                        destino = offer.destination,

                        fecha = offer.date,

                        hora = offer.time,

                        precio = offer.price,

                        comentario = offer.comment,

                        onCall = {

                            // Próximo sprint

                        },

                        onWhatsApp = {

                            // Próximo sprint

                        }

                    )

                }

            }

        }

    }

}