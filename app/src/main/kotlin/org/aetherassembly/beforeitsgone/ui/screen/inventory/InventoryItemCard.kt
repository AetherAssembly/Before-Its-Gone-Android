package org.aetherassembly.beforeitsgone.ui.screen.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.aetherassembly.beforeitsgone.domain.model.ExpiryStatus
import org.aetherassembly.beforeitsgone.domain.model.InventoryItem

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    expiryStatus: ExpiryStatus?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onWaste: () -> Unit
) {
    val statusColor = when (expiryStatus) {
        ExpiryStatus.EXPIRED -> Color(0xFFD32F2F)
        ExpiryStatus.EXPIRING_SOON -> Color(0xFFF57C00)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = item.expiresAt.take(10),
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
                Text(
                    text = "${item.quantity} · ${item.location}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onWaste) {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = "Log ${item.name} as wasted",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete ${item.name}")
            }
        }
    }
}
