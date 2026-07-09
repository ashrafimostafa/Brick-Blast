package com.mostafa.brickblast.ui.screens.game

import android.annotation.SuppressLint
import android.content.Context
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import com.mostafa.brickblast.domain.model.BoardVisualTheme
import com.mostafa.brickblast.game.renderer.NativeGameRenderer
import com.mostafa.brickblast.ui.viewmodel.GameViewModel

/**
 * Choreographer-driven game surface: simulates and draws without Compose recomposition.
 */
@SuppressLint("ViewConstructor")
class GameCanvasView(context: Context) : View(context) {

    var viewModel: GameViewModel? = null
    var showTrajectory: Boolean = true
    var isDarkTheme: Boolean = true
    var boardTheme: BoardVisualTheme = BoardVisualTheme.Classic
    var persianUi: Boolean = false
    var onCanvasSizeChanged: ((Int, Int) -> Unit)? = null

    private val renderer = NativeGameRenderer(context, resources.displayMetrics.density)
    private val choreographer = Choreographer.getInstance()

    private var lastFrameNanos = 0L
    private var dragging = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isAttachedToWindow) return

            val vm = viewModel
            if (vm != null && vm.isRunning) {
                if (lastFrameNanos != 0L) {
                    val dt = ((frameTimeNanos - lastFrameNanos) / 1_000_000_000f).coerceAtMost(0.033f)
                    vm.tick(dt)
                }
                lastFrameNanos = frameTimeNanos
                invalidate()
            } else {
                lastFrameNanos = 0L
            }
            choreographer.postFrameCallback(this)
        }
    }

    init {
        setWillNotDraw(false)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lastFrameNanos = 0L
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onDetachedFromWindow() {
        choreographer.removeFrameCallback(frameCallback)
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            onCanvasSizeChanged?.invoke(w, h)
        }
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        val engine = viewModel?.gameEngine ?: return
        renderer.render(
            canvas = canvas,
            engine = engine,
            trajectoryPoints = engine.getTrajectoryPoints(),
            showTrajectory = showTrajectory,
            theme = boardTheme,
            isDark = isDarkTheme,
            persianUi = persianUi
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val vm = viewModel ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragging = true
                vm.onDragStart(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (dragging) {
                    vm.onDrag(event.x, event.y)
                    if (viewModel?.gameEngine?.isAiming == true) {
                        invalidate()
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragging) {
                    dragging = false
                    vm.onDragEnd()
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
