package com.faust.m.flashcardm.presentation.view_library_booklet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.faust.m.flashcardm.R
import org.jetbrains.anko.dimen


@BindingAdapter(value = ["count_new", "count_in_review", "count_learned"], requireAll = false)
fun setCounts(view: CardStatisticView,
              newCount: Int?,
              inReviewCount: Int?,
              learnedCount: Int?) {
    newCount?.let { view.countNew = newCount }
    inReviewCount?.let { view.countInReview = inReviewCount }
    learnedCount?.let { view.countLearned = learnedCount }
    view.invalidate()
}

class CardStatisticView
    @JvmOverloads
    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyleAttr: Int = 0) : ImageView(context, attrs, defStyleAttr) {

    // Used to draw rectangles on canvas representing the different cards
    private val paint = Paint()
    private val rect = Rect()
    private val colors = listOf(
        ContextCompat.getColor(context, R.color.colorNewCard),
        ContextCompat.getColor(context, R.color.colorInReviewCard),
        ContextCompat.getColor(context, R.color.colorLearnedCard)
    )

    // Customization for view
    private val spacing = attrs?.let {
        context
            .obtainStyledAttributes(it, R.styleable.CardStatisticView)
            .getDimensionPixelSize(
                R.styleable.CardStatisticView_spacing,
                context.dimen(R.dimen.large_margin)
            )
    } ?: context.dimen(R.dimen.large_margin)

    // Data to draw
    var countNew = 0
    var countInReview = 0
    var countLearned = 0


    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            val totalCount = countNew + countInReview + countLearned
            val squareSideSize = height

            // Decide whether to draw each square separately or all as one block
            if (totalCount*squareSideSize > width) {
                // Not enough space to draw each square
                drawBlocks(it, totalCount)
            }
            else {
                // Check how much space we can have between each square
                val totalWidthForSpacing = width - (totalCount*squareSideSize)
                val customSpacing = when (totalCount) {
                    1 -> 0 // No spacing when there is only one square
                    else -> totalWidthForSpacing / (totalCount-1)
                }
                // Draw square either with custom spacing or with default spacing
                when {
                    customSpacing < spacing -> drawSquares(it, customSpacing, squareSideSize)
                    else -> drawSquares(it, spacing, squareSideSize)
                }
            }
        }
    }

    /**
     * Draw 3 block (new, in review, learned)
     * @param canvas: The canvas to draw on
     * @param totalCount: the total number of card
     */
    private fun drawBlocks(canvas: Canvas, totalCount: Int) {
        val widthForNew = width*countNew/totalCount
        val widthForInReview = width*countInReview/totalCount
        val widths = mutableListOf(
            widthForNew,
            widthForInReview,
            width-widthForNew-widthForInReview // Leftover space
        )

        rect.set(0, 0, 0, height)

        widths.forEachIndexed { i, width ->
            paint.color = colors[i]
            drawNSquares(canvas, rect, paint, 1, 0, width)
        }
    }

    /**
     * Draw a certain number of square, depending on the number of card on of each type
     * When drawing all squares with this method, there might be a bit of extra space at the
     * end due to the custom spacing not being perfect
     * @param canvas: the canvas to draw on
     * @param customSpacing: the space to leave between each square
     * @param squareSize: the size of each square
     */
    private fun drawSquares(canvas: Canvas, customSpacing: Int, squareSize: Int) {
        // Reset rect
        rect.set(0, 0, 0, squareSize)

        val dataToDraw = listOf(countNew, countInReview, countLearned)
        for((index, count) in dataToDraw.withIndex()) {
            paint.color = colors[index]
            drawNSquares(canvas, rect, paint, count, customSpacing, squareSize)
        }
    }

    private fun drawNSquares(canvas: Canvas, rect: Rect, paint: Paint, n: Int,
                             customSpacing: Int, squareSize: Int) {
        repeat(n) {
            rect.apply {
                left = right
                right += squareSize
            }
            canvas.drawRect(rect, paint)
            rect.right += customSpacing
        }
    }
}