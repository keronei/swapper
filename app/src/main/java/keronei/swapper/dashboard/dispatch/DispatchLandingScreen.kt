package keronei.swapper.dashboard.dispatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import keronei.swapper.dashboard.DashboardViewModel
import keronei.swapper.data.domain.RequestsWithUpdatesModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatchLandingScreen(dashboardViewModel: DashboardViewModel, modifier: Modifier) {

    Scaffold(
        modifier = modifier,
        content = { paddingValues ->
            var selectedCategory by remember { mutableStateOf<String?>(null) }

            val showSheet = selectedCategory != null

            val requests = dashboardViewModel.requests.collectAsState()

            val newRequests = requests.value.filter { it.request.status == "New" }
            val assignedRequests = requests.value.filter { it.request.status == "Assigned" }
            val pickupsRequests = requests.value.filter { it.request.status == "Pickups" }
            val completedRequests = requests.value.filter { it.request.status == "Completed" }

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            val cardItems = listOf(
                Triple("New", newRequests.size, Icons.Default.MailOutline),
                Triple("Assigned", assignedRequests.size, Icons.Default.AddCircle),
                Triple("Pickups", pickupsRequests.size, Icons.Default.LocationOn),
                Triple("Completed", completedRequests.size, Icons.Default.CheckCircle)
            )

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Greeting Section
                Box(
                    modifier = Modifier
                        .weight(.3f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Hello, Keronei 👋",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cards Section
                Column(
                    modifier = Modifier
                        .weight(.7f)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        cardItems.forEach { (label, count, icon) ->
                            item {
                                HighlightCard(label, count, {
                                    selectedCategory = label
                                }, icon)
                            }
                        }
                    }

                    if (showSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { selectedCategory = null },
                            sheetState = sheetState
                        ) {
                           val selected = requests.value.filter { it.request.status == selectedCategory}

                            RequestList(
                                category = selectedCategory!!,
                                requests = selected
                            )
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun HighlightCard(label: String, count: Int, onClick: () -> Unit, icon: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Makes it square-ish
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ExpandableRequestCard(request: RequestsWithUpdatesModel) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(12.dp)
    ) {
        val parsedInstant = Instant.parse(request.request.createdTime)

        Column(Modifier.padding(16.dp)) {
            Text(request.request.requestedByStation, fontWeight = FontWeight.Bold)
            Text("${request.request.requestCount} batteries requested")

            Text(text = timeAgo(parsedInstant), style = MaterialTheme.typography.labelSmall)

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(text = request.request.comment, style = MaterialTheme.typography.bodySmall)

                Button({
                }) {
                    Text("Update")
                }
            }
        }
    }
}

fun timeAgo(instant: Instant): String {
    val now = Clock.System.now()
    val duration = now - instant

    return when {
        duration.inWholeMinutes < 1 -> "just now"
        duration.inWholeHours < 1 -> "${duration.inWholeMinutes} minutes ago"
        duration.inWholeDays < 1 -> "${duration.inWholeHours} hours ago"
        else -> "${duration.inWholeDays} days ago"
    }
}

@Composable
fun RequestList(category: String, requests: List<RequestsWithUpdatesModel>) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
        Text(text = category, style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(requests) { request ->
                ExpandableRequestCard(request)
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

