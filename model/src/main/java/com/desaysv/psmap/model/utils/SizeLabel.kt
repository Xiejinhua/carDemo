package com.desaysv.psmap.model.utils

import android.text.Editable
import android.text.Html.TagHandler
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import org.xml.sax.XMLReader
import java.util.Locale

class SizeLabel : TagHandler {
    private var size = 24
    private lateinit var sizeList: IntArray
    private var startIndex = 0
    private var stopIndex = 0
    private var sizeCount = 0
    private var moreSize = false

    constructor(size: Int) {
        this.size = size
    }

    constructor(size: IntArray) {
        if (size.isNotEmpty()) {
            sizeList = size
            moreSize = true
        }
    }

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
        if (tag.lowercase(Locale.getDefault()) == "size") {
            if (opening) {
                startIndex = output.length
            } else {
                stopIndex = output.length
                if (moreSize) {
                    if (sizeCount >= sizeList.size) {
                        output.setSpan(
                            AbsoluteSizeSpan(sizeList[sizeList.size - 1]),
                            startIndex,
                            stopIndex,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        output.setSpan(
                            AbsoluteSizeSpan(sizeList[sizeCount]),
                            startIndex,
                            stopIndex,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        sizeCount++
                    }
                } else {
                    output.setSpan(
                        AbsoluteSizeSpan(size),
                        startIndex,
                        stopIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }
}