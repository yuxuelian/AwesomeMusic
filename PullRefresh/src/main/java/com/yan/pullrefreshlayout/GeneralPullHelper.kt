package com.yishi.refresh

import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.ViewGroup

/**
 * @author 56896
 * @date 2018/10/18 23:54
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
internal class GeneralPullHelper(private val prl: PullRefreshLayout, context: Context) {

    /**
     * default values
     */
    private val minimumFlingVelocity: Int
    private val maximumVelocity: Int
    private val touchSlop: Int

    /**
     * is Being Dragged
     * - use by pullRefreshLayout to know is drag Vertical
     */
    var isDragVertical: Boolean = false

    /**
     * is Drag Horizontal
     * - use by pullRefreshLayout to know is drag Horizontal
     */
    var isDragHorizontal: Boolean = false

    /**
     * is moving direct down
     * - use by pullRefreshLayout to get moving direction
     */
    var isDragMoveTrendDown: Boolean = false

    /**
     * is Refresh Layout has moved
     * - use by prl to get know the layout moved
     */
    var isLayoutDragMoved: Boolean = false

    /**
     * is Disallow Intercept
     * - use by prl to get know need to Intercept event
     */
    var isDisallowIntercept: Boolean = false
    private var lastDisallowIntercept: Boolean = false

    /**
     * is ReDispatch TouchEvent
     */
    private var isReDispatchMoveEvent: Boolean = false

    /**
     * is Dispatch Touch Cancel
     */
    private var isDispatchTouchCancel: Boolean = false

    /**
     * is touch direct down
     * - use by pullRefreshLayout to get drag state
     */
    var dragState: Int = 0

    /**
     * first touch point x
     */
    private var actionDownPointX: Int = 0

    /**
     * first touch point y
     */
    private var actionDownPointY: Int = 0

    /**
     * last Layout Move Distance
     */
    private var lastMoveDistance: Int = 0

    /**
     * motion event child consumed
     */
    private val childConsumed = IntArray(2)
    private var lastChildConsumedY: Int = 0

    /**
     * active pointer id
     */
    private var activePointerId: Int = 0

    /**
     * last drag MotionEvent y
     */
    private var lastDragEventY: Int = 0

    /**
     * touchEvent velocityTracker
     */
    private var velocityTracker: VelocityTracker? = null

    init {
        val configuration = ViewConfiguration.get(context)
        minimumFlingVelocity = configuration.scaledMinimumFlingVelocity
        maximumVelocity = configuration.scaledMaximumFlingVelocity
        touchSlop = configuration.scaledTouchSlop
    }

    fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        initVelocityTrackerIfNotExists()
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = ev.getPointerId(0)
                actionDownPointX = (ev.x + 0.5f).toInt()
                actionDownPointY = (ev.y + 0.5f).toInt()
                lastDragEventY = actionDownPointY

                isLayoutDragMoved = false
                isDisallowIntercept = false
                lastDisallowIntercept = false

                prl.onStartScroll()
                prl.dispatchSuperTouchEvent(ev)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDisallowIntercept) {
                    val pointerIndex = ev.findPointerIndex(activePointerId)
                    if (ev.findPointerIndex(activePointerId) != -1) {
                        val tempY = (ev.getY(pointerIndex) + 0.5f).toInt()
                        if (lastDisallowIntercept) {
                            lastDragEventY = tempY
                        }
                        var deltaY = lastDragEventY - tempY
                        lastDragEventY = tempY

                        if (!isDragVertical || !prl.isTargetNestedScrollingEnabled
                                || !prl.isMoveWithContent && prl.moveDistance != 0) {
                            dellDirection(deltaY)
                        }

                        val movingX = (ev.getX(pointerIndex) + 0.5f).toInt() - actionDownPointX
                        val movingY = (ev.getY(pointerIndex) + 0.5f).toInt() - actionDownPointY
                        if (!isDragVertical && Math.abs(movingY) > touchSlop && Math.abs(movingY) > Math.abs(
                                        movingX)) {
                            val parent = prl.parent
                            parent?.requestDisallowInterceptTouchEvent(true)

                            isDragVertical = true
                            reDispatchMoveEventDrag(ev, deltaY)
                            lastDragEventY = ev.getY(pointerIndex).toInt()
                        } else if (!isDragVertical
                                && !isDragHorizontal
                                && Math.abs(movingX) > touchSlop
                                && Math.abs(movingX) > Math.abs(movingY)) {
                            isDragHorizontal = true
                        }

                        if (isDragVertical) {
                            // ---------- | make sure that the pullRefreshLayout is moved|----------
                            if (lastMoveDistance == 0) {
                                lastMoveDistance = prl.moveDistance
                            }
                            if (lastMoveDistance != prl.moveDistance) {
                                isLayoutDragMoved = true
                            }
                            lastMoveDistance = prl.moveDistance

                            reDispatchMoveEventDragging(ev, deltaY)

                            // make sure that can nested to work or the targetView is move with content
                            // dell the touch logic
                            if (!prl.isTargetNestedScrollingEnabled || !prl.isMoveWithContent) {
                                if (!prl.isMoveWithContent && prl.isTargetNestedScrollingEnabled) {
                                    // when nested scroll the nested event is delay than this logic
                                    // so we need adjust the deltaY
                                    deltaY = (if (isDragMoveTrendDown) -1 else 1) * Math.abs(deltaY)
                                }
                                prl.onPreScroll(deltaY, childConsumed)
                                deltaY = if (prl.parentOffsetInWindow[1] >= Math.abs(deltaY)) 0 else deltaY
                                prl.onScroll(deltaY - (childConsumed[1] - lastChildConsumedY))
                                lastChildConsumedY = childConsumed[1]

                                // -------------------| event reset |--------------------
                                if (!prl.isMoveWithContent) {
                                    ev.offsetLocation(0f, childConsumed[1].toFloat())
                                }
                            }
                        }
                        lastDisallowIntercept = isDisallowIntercept
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                lastDragEventY = ev.getY(index).toInt()
                activePointerId = ev.getPointerId(index)
                reDispatchPointDownEvent()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
                lastDragEventY = ev.getY(ev.findPointerIndex(activePointerId)).toInt()
                reDispatchPointUpEvent(ev)
            }
            MotionEvent.ACTION_UP -> {
                // get know the touchState first
                dragState = 0

                velocityTracker!!.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                val velocityY = (if (isDragMoveTrendDown) 1 else -1) * Math.abs(
                        velocityTracker!!.getYVelocity(activePointerId))
                if (!prl.isTargetNestedScrollingEnabled && isDragVertical && Math.abs(velocityY) > minimumFlingVelocity) {
                    prl.onPreFling((-velocityY.toInt()).toFloat())
                }
                recycleVelocityTracker()

                reDispatchUpEvent(ev)
                prl.onStopScroll()

                isReDispatchMoveEvent = false
                isDispatchTouchCancel = false
                isDragHorizontal = false
                isDragVertical = false

                lastMoveDistance = 0
                lastChildConsumedY = 0
                childConsumed[1] = 0
                activePointerId = -1
                dragState = 0
            }
            MotionEvent.ACTION_CANCEL -> {
                prl.onStopScroll()
                isReDispatchMoveEvent = false
                isDispatchTouchCancel = false
                isDragHorizontal = false
                isDragVertical = false
                lastMoveDistance = 0
                lastChildConsumedY = 0
                childConsumed[1] = 0
                activePointerId = -1
                dragState = 0
            }
        }
        if (velocityTracker != null) {
            velocityTracker!!.addMovement(ev)
        }
        return prl.dispatchSuperTouchEvent(ev)
    }

    fun dellDirection(offsetY: Int) {
        if (offsetY < 0) {
            dragState = 1
            isDragMoveTrendDown = true
        } else if (offsetY > 0) {
            dragState = -1
            isDragMoveTrendDown = false
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker!!.recycle()
            velocityTracker = null
        }
    }

    private fun reDispatchPointDownEvent() {
        if (!prl.isMoveWithContent && isLayoutDragMoved && prl.moveDistance == 0) {
            childConsumed[1] = 0
            lastChildConsumedY = 0
        }
    }

    private fun reDispatchPointUpEvent(event: MotionEvent) {
        if (!prl.isMoveWithContent
                && isLayoutDragMoved
                && prl.moveDistance == 0
                && childConsumed[1] != 0) {
            prl.dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL))
        }
    }

    private fun reDispatchMoveEventDrag(event: MotionEvent, movingY: Int) {
        if ((!prl.isTargetNestedScrollingEnabled || !prl.isMoveWithContent) && (movingY > 0 && prl.moveDistance > 0 || movingY < 0 && prl.moveDistance < 0
                        || isDragHorizontal && (prl.moveDistance != 0
                        || !prl.isTargetAbleScrollUp && movingY < 0
                        || !prl.isTargetAbleScrollDown && movingY > 0))) {
            isDispatchTouchCancel = true
            prl.dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL))
        }
    }

    private fun reDispatchMoveEventDragging(event: MotionEvent, movingY: Int) {
        if ((!prl.isTargetNestedScrollingEnabled || !prl.isMoveWithContent)
                && isDispatchTouchCancel
                && !isReDispatchMoveEvent
                && (movingY > 0 && prl.moveDistance > 0 && prl.moveDistance - movingY < 0 || movingY < 0 && prl.moveDistance < 0 && prl.moveDistance - movingY > 0)) {
            isReDispatchMoveEvent = true
            prl.dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_DOWN))
        }
    }

    private fun reDispatchUpEvent(event: MotionEvent) {
        if ((!prl.isTargetNestedScrollingEnabled || !prl.isMoveWithContent)
                && isDragVertical
                && isLayoutDragMoved) {
            if (!prl.isTargetAbleScrollDown && !prl.isTargetAbleScrollUp) {
                prl.dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL))
            } else if (prl.targetView is ViewGroup) {
                val vp = prl.targetView as ViewGroup
                for (i in 0 until vp.childCount) {
                    vp.getChildAt(i).dispatchTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL))
                }
            }
        }
    }

    private fun getReEvent(event: MotionEvent, action: Int): MotionEvent {
        val reEvent = MotionEvent.obtain(event)
        reEvent.action = action
        return reEvent
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val actionIndex = ev.actionIndex
        if (ev.getPointerId(actionIndex) == activePointerId) {
            val newPointerIndex = if (actionIndex == 0) 1 else 0
            lastDragEventY = ev.getY(newPointerIndex).toInt()
            activePointerId = ev.getPointerId(newPointerIndex)

            if (velocityTracker != null) {
                velocityTracker!!.clear()
            }
        }
    }
}
