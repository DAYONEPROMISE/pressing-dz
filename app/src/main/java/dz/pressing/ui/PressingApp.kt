package dz.pressing.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dz.pressing.R
import dz.pressing.data.TicketLogic
import dz.pressing.data.TicketStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun statusLabel(status: String): String = when (status) {
    TicketStatus.RECU -> stringResource(R.string.status_recu)
    TicketStatus.PRET -> stringResource(R.string.status_pret)
    TicketStatus.LIVRE -> stringResource(R.string.status_livre)
    else -> status
}

@Composable
fun errorLabel(code: String): String = when (code) {
    "client_required" -> stringResource(R.string.err_client_required)
    "items_required" -> stringResource(R.string.err_items_required)
    "price_positive" -> stringResource(R.string.err_price_positive)
    "price_too_large" -> stringResource(R.string.err_price_too_large)
    else -> code
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PressingApp(vm: TicketViewModel = viewModel()) {
    val tickets by vm.tickets.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { vm.refreshTakings() }

    val visible = remember(tickets, vm.filter) {
        if (vm.filter == null) tickets else tickets.filter { it.status == vm.filter }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("PressingDZ • ${TicketLogic.formatDzd(vm.takingsToday)}") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_ticket_cd))
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = vm.filter == null, onClick = { vm.filter = null }, label = { Text(stringResource(R.string.filter_all)) })
                FilterChip(selected = vm.filter == TicketStatus.RECU, onClick = { vm.filter = TicketStatus.RECU }, label = { Text(stringResource(R.string.status_recu)) })
                FilterChip(selected = vm.filter == TicketStatus.PRET, onClick = { vm.filter = TicketStatus.PRET }, label = { Text(stringResource(R.string.status_pret)) })
                FilterChip(selected = vm.filter == TicketStatus.LIVRE, onClick = { vm.filter = TicketStatus.LIVRE }, label = { Text(stringResource(R.string.status_livre)) })
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visible, key = { it.id }) { t ->
                    val df = remember { SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE) }
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("#${t.id} ${t.clientName}", style = MaterialTheme.typography.titleMedium)
                                Text(TicketLogic.formatDzd(t.priceDzd), style = MaterialTheme.typography.titleMedium)
                            }
                            Text(t.itemsLabel)
                            Text("${stringResource(R.string.label_deposit)}: ${df.format(Date(t.createdAt))} • ${stringResource(R.string.label_ready)}: ${df.format(Date(t.readyAt))}",
                                style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {}, label = { Text(statusLabel(t.status)) })
                                if (t.paid) AssistChip(onClick = {}, label = { Text(stringResource(R.string.label_paid)) })
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val next = TicketLogic.nextStatus(t.status)
                                if (next != null) {
                                    Button(onClick = { vm.advance(t) }) { Text("→ ${statusLabel(next)}") }
                                }
                                if (!t.paid) {
                                    TextButton(onClick = { vm.markPaid(t) }) { Text(stringResource(R.string.action_collect)) }
                                }
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { vm.delete(t) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete_cd))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var client by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var items by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(stringResource(R.string.new_ticket)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(client, { client = it }, label = { Text(stringResource(R.string.field_client)) }, singleLine = true)
                    OutlinedTextField(phone, { phone = it }, label = { Text(stringResource(R.string.field_phone)) }, singleLine = true)
                    OutlinedTextField(items, { items = it }, label = { Text(stringResource(R.string.field_items)) })
                    OutlinedTextField(price, { price = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.field_price)) }, singleLine = true)
                    if (formError != null) Text(errorLabel(formError!!), color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val p = price.toIntOrNull() ?: 0
                    val errs = TicketLogic.validate(client, items, p)
                    if (errs.isNotEmpty()) { formError = errs.first(); return@Button }
                    vm.add(client, phone, items, 1, p, System.currentTimeMillis() + 2 * 86_400_000L) { formError = it }
                    showAdd = false
                }) { Text(stringResource(R.string.action_create)) }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }
}
