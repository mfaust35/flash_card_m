package com.faust.m.flashcardm.presentation.view_library_booklet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.faust.m.flashcardm.R


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
                defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    // Used to draw rectangles on canvas representing the different cards
    private val paint = Paint()
    private val rect = Rect()
    private val colors = listOf(
        ContextCompat.getColor(context, R.color.colorNewCard),
        ContextCompat.getColor(context, R.color.colorTrainingCard),
        ContextCompat.getColor(context, R.color.colorFamiliarCard)
    )

    // Customization for view
    private val customSpacing: Int
    private val spacingMatchSquareSize: Boolean
    private val baselineAlignBottom: Boolean
    private val shrink: Int
    private val adjustedHeight: Int
        get() = height - 2*shrink
    private val adjustedWidth: Int
        get() = width - 2*shrink

    init {
        if (null == attrs) {
            customSpacing = 0
            spacingMatchSquareSize = true
            baselineAlignBottom = false
            shrink = 0
        }
        else {
            context.obtainStyledAttributes(attrs, R.styleable.CardStatisticView).also {
                // If we define a custom spacing, automatically set spacingMatchSquareSize to false
                // and get the custom spacing value
                if (it.hasValue(R.styleable.CardStatisticView_spacing)) {
                    customSpacing = it.getDimensionPixelSize(
                        R.styleable.CardStatisticView_spacing,
                        0
                    )
                    spacingMatchSquareSize = false
                }
                // If there is no custom spacing, get the custom spacingMatchSquareSize value if any
                else {
                    customSpacing = 0
                    spacingMatchSquareSize = it.getBoolean(
                        R.styleable.CardStatisticView_spacing_match_square_size,
                        true
                    )
                }
                // Get the other attribute values anyways
                baselineAlignBottom = it.getBoolean(
                    R.styleable.CardStatisticView_android_baselineAlignBottom,
                    false
                )
                shrink = it.getDimensionPixelSize(
                    R.styleable.CardStatisticView_shrinking,
                    0
                )
            }.recycle()
        }
    }

    // Data to draw
    var countNew = 0
    var countInReview = 0
    var countLearned = 0

    /**
     * 0 based index of square to magnify.
     * Default to no magnified square.
     * If the index is outside of the total of square to, nothing will be magnified.
     */
    private var magnify = -1


    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            val totalCount = countNew + countInReview + countLearned
            if (0 == totalCount) return // Nothing to display
            val squareSideSize = adjustedHeight
            val availableWidthForSpacing = adjustedWidth - totalCount*squareSideSize

            // Init rectangle: Start with a rectangle with no width, indented on left by shrink,
            // top = 0+shrink , bottom = top+adjustedHeight
            rect.set(shrink, shrink, shrink, adjustedHeight+shrink)

            // Decide whether to draw each square separately or all as one block
            if (0 >= availableWidthForSpacing) {
                // Not enough space to draw each square
                drawBlocks(it, totalCount)
            }
            else {
                // Check how much space we can have between each square
                val availableIndividualSpace = when (totalCount) {
                    1 -> 0 // No space when only 1 square
                    else -> availableWidthForSpacing / (totalCount-1)
                }
                val idealSpace = when {
                    spacingMatchSquareSize -> squareSideSize
                    else -> customSpacing
                }.coerceAtMost(availableIndividualSpace)

                drawSquares(it, idealSpace, squareSideSize)
            }
        }
    }

    /**
     * Draw 3 block (new, in review, learned)
     * @param canvas: The canvas to draw on
     * @param totalCount: the total number of card
     */
    private fun drawBlocks(canvas: Canvas, totalCount: Int) {
        val widthForNew = adjustedWidth*countNew/totalCount
        val widthForInReview = adjustedWidth*countInReview/totalCount
        val widths = mutableListOf(
            widthForNew,
            widthForInReview,
            adjustedWidth-widthForNew-widthForInReview // Leftover space
        )
        val magnifiedSquareIndex = magnifiedSquareIndex()

        widths.forEachIndexed { i, width ->
            paint.color = colors[i]
            val isMagnifiedBlock = magnifiedSquareIndex[i] != -1
            drawNSquares(canvas, rect, paint, 1, 0, width, isMagnifiedBlock)
        }
    }

    private fun magnifiedSquareIndex(): Array<Int> {
        val result = arrayOf(-1,-1,-1) // Index of magnified square for
                                                  // new / inReview / learned, initialize to -1
        if (0 <= magnify) {
            when {
                magnify < countNew ->
                    result[0] = magnify
                magnify < countInReview+countNew ->
                    result[1] = magnify - countNew
                magnify < countLearned+countInReview+countNew ->
                    result[2] = magnify - countNew - countInReview
            }
        }
        return result
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
        val magnifiedSquareIndex = magnifiedSquareIndex()

        listOf(countNew, countInReview, countLearned).forEachIndexed { i, count ->
            paint.color = colors[i]
            val mag = magnifiedSquareIndex[i]
            // Draw squares before magnified square if any
            drawNSquares(canvas, rect, paint, mag, customSpacing, squareSize)
            // Draw magnified square if any
            drawNSquares(
                canvas, rect, paint,
                (mag+1).coerceAtMost(1), customSpacing, squareSize, true
            )
            // Draw squares after magnified square if any
            drawNSquares(canvas, rect, paint, count - (mag+1), customSpacing, squareSize)
        }
    }

    /**
     * Repeat (Move rectangle to new position and draw square) times n
     */
    private fun drawNSquares(canvas: Canvas, rect: Rect, paint: Paint, n: Int,
                             customSpacing: Int, squareSize: Int, magnify: Boolean = false) {
        repeat(n) {
            // Modify rect and create a copy in case we need to magnify it
            var updatedRect = rect.apply {
                left = right
                right += squareSize
            }
            if (magnify) updatedRect = updatedRect.magnify()
            // Draw rect
            canvas.drawRect(updatedRect, paint)
            // Prep rect for the next square to draw
            rect.right = (rect.right + customSpacing).coerceAtLeast(updatedRect.right)
        }
    }

    /**
     * Copy rectangle, update its bound to make it large by shrinkValue, and return copy
     * This method does not modify the initial rect
     */
    private fun Rect.magnify(): Rect =
        Rect(this).apply {
            left -= shrink
            top -= shrink
            right += shrink
            bottom += shrink
        }

    /**
     * Need to override this method to be able to align the baseline of this view with
     * the baseline of a textView. Code taken from android's implementation of imageView
     */
    override fun getBaseline(): Int = if (baselineAlignBottom) measuredHeight - shrink else -1
}
