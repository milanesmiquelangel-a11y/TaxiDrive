package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.model.TripPost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsScreen(

    volver: () -> Unit

) {

    val publicaciones = remember {

        mutableStateListOf<TripPost>()

    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text("Mis publicaciones")

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)

        ) {

            Button(

                onClick = volver,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("← Volver")

            }

            Spacer(modifier = Modifier.height(16.dp))

            if (publicaciones.isEmpty()) {

                Text(

                    text = "Todavía no tienes publicaciones.",

                    style = MaterialTheme.typography.titleMedium

                )

            } else {

                LazyColumn(

                    verticalArrangement = Arrangement.spacedBy(12.dp)

                ) {

                    items(publicaciones) { viaje ->

                        Card(

                            modifier = Modifier.fillMaxWidth(),

                            elevation = CardDefaults.cardElevation(4.dp)

                        ) {

                            Column(

                                modifier = Modifier.padding(16.dp)

                            ) {

                                Text(

                                    text = "${viaje.origin} → ${viaje.destination}",

                                    fontWeight = FontWeight.Bold

                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text("Fecha: ${viaje.date}")

                                Text("Hora: ${viaje.time}")

                                Text("Precio: ${viaje.price} ₸")

                                Text("Estado: ${viaje.status}")

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(

                                    onClick = {

                                        // TODO Editar publicación

                                    },

                                    modifier = Modifier.fillMaxWidth()

                                ) {

                                    Text("Editar")

                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(

                                    onClick = {

                                        // TODO Marcar como completado

                                    },

                                    modifier = Modifier.fillMaxWidth()

                                ) {

                                    Text("Finalizar viaje")

                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(

                                    onClick = {

                                        // TODO Eliminar publicación

                                    },

                                    modifier = Modifier.fillMaxWidth()

                                ) {

                                    Text("Eliminar")

                                }

                            }

                        }

                    }

                }

            }

        }

    }

}

