package kz.taxidrive.app.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.repository.AuthRepository
import kz.taxidrive.app.repository.OfferMessageRepository
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferChatScreen(offerId: Long, volver: () -> Unit) {
 val context=LocalContext.current
 val scope=rememberCoroutineScope()
 var myId by remember { mutableStateOf<Long?>(null) }
 var messages by remember { mutableStateOf(emptyList<kz.taxidrive.app.data.network.OfferMessageDto>()) }
 var draft by remember { mutableStateOf("") }
 var status by remember { mutableStateOf("Cargando conversación…") }
 var sending by remember { mutableStateOf(false) }
 fun refresh() { scope.launch { try { messages=OfferMessageRepository.list(context,offerId); status=if(messages.isEmpty()) "Todavía no hay mensajes." else "" } catch(e:Exception) { status=e.message ?: "Chat no disponible para esta oferta." } } }
 LaunchedEffect(offerId) { try { myId=AuthRepository.restoreSession(context)?.id } catch(_:Exception) {}; refresh() }
 Scaffold(topBar={ TopAppBar(title={ Text("Chat del viaje") }) }) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
   OutlinedButton(onClick=volver,modifier=Modifier.fillMaxWidth()){Text("← Volver")}
   if(status.isNotBlank()) Text(status,color=MaterialTheme.colorScheme.secondary)
   LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
    items(messages) { m ->
     val mine=m.sender_user_id==myId
     Card(Modifier.fillMaxWidth().padding(vertical=4.dp),colors=CardDefaults.cardColors(containerColor=if(mine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
      Column(Modifier.padding(10.dp)) { Text(if(mine) "Tú" else "Otro participante",style=MaterialTheme.typography.labelSmall); Text(m.message); m.created_at?.let { Text(it,style=MaterialTheme.typography.labelSmall) } }
     }
    }
   }
   Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
    OutlinedTextField(value=draft,onValueChange={draft=it.take(2000)},modifier=Modifier.weight(1f),label={Text("Escribe un mensaje")},enabled=!sending,maxLines=3)
    Spacer(Modifier.width(8.dp))
    Button(enabled=draft.isNotBlank() && !sending,onClick={ val msg=draft.trim(); scope.launch { sending=true; try { OfferMessageRepository.send(context,offerId,msg); draft=""; refresh() } catch(e:Exception) { status=e.message ?: "No se pudo enviar." } finally { sending=false } } }) { Text(if(sending) "…" else "Enviar") }
   }
   TextButton(onClick={ { refresh() } },modifier=Modifier.fillMaxWidth()){Text("Actualizar mensajes")}
  }
 }
}