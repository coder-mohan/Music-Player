package com.ldt.musicr.ui.widget.bubblepicker

import com.ldt.musicr.ui.widget.bubblepicker.model.PickerItem


interface BubblePickerListener {

    fun onBubbleSelected(item: PickerItem, position: Int)

    fun onBubbleDeselected(item: PickerItem, position: Int)

}