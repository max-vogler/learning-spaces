package de.maxvogler.learningspaces.helpers

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import java.util.*

fun Menu.itemsSequence(): Sequence<MenuItem> = MenuItemsSequence(this)

private class MenuItemsSequence(private val menu: Menu) : Sequence<MenuItem> {
    override fun iterator(): Iterator<MenuItem> {
        return IndexBasedIterator(count = { menu.size() }, getItem = { menu.getItem(it) })
    }
}

private class IndexBasedIterator<O>(
        private val count: () -> Int,
        private val getItem: (Int) -> O
) : Iterator<O> {

    private var index = 0
    private val initialCount = count()

    override fun next(): O {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        return getItem(index++)
    }

    override fun hasNext(): Boolean {
        if (initialCount != count()) {
            throw ConcurrentModificationException()
        }

        return index < initialCount
    }
}
