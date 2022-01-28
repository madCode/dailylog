package com.app.dailylog.ui.settings

import androidx.recyclerview.widget.ItemTouchHelper


/**
 * Interface to notify an item ViewHolder of relevant callbacks from [ ].
 *
 * @author Paul Burke (ipaulpro)
 */
interface ShortcutTouchHelperViewHolder {
    /**
     * Called when the [ItemTouchHelper] first registers an item as being moved or swiped.
     * Implementations should update the item view to indicate it's active state.
     */
    fun onItemSelected()

    /**
     * Called when the [ItemTouchHelper] has completed the move or swipe, and the active item
     * state should be cleared.
     */
    fun onItemClear()
}