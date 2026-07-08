package org.aetherassembly.beforeitsgone.domain.usecase

import org.aetherassembly.beforeitsgone.domain.model.InventoryItem

/**
 * Matches desktop's documented sync contract (docs/cloud-sync.md in the desktop
 * repo): "pushes all local items to Supabase, then pulls any remote items that
 * are newer than the local copy." Conflict resolution is last-write-wins by
 * `updatedAt`. This is a deliberate mirror of desktop's behavior, including its
 * known limitation that a push always overwrites the remote row for that id
 * before the newer-remote-wins pull check runs on the *next* sync.
 *
 * Deletions are never propagated either direction — an item removed locally
 * reappears on the next sync from a device that still has it, same as desktop.
 */
fun resolveSyncConflicts(
    local: List<InventoryItem>,
    remote: List<InventoryItem>
): List<InventoryItem> {
    val localById = local.associateBy { it.id }
    val merged = local.associateBy { it.id }.toMutableMap()

    for (remoteItem in remote) {
        val localItem = localById[remoteItem.id]
        if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
            merged[remoteItem.id] = remoteItem
        }
    }

    return merged.values.toList()
}
