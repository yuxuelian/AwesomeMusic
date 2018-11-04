package com.yan.pullrefreshlayout

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.ListView
import android.widget.OverScroller
import android.widget.ScrollView
import androidx.core.view.*
import androidx.core.widget.ListViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.kaibo.core.util.deviceHeight
import org.jetbrains.anko.dip

/**
 * @author 56896
 * @date 2018/10/18 23:54
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class PullRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        ViewGroup(context, attrs, defStyleAttr),
        NestedScrollingParent,
        NestedScrollingChild {

    private val parentHelper: NestedScrollingParentHelper
    private val childHelper: NestedScrollingChildHelper
    private val parentScrollConsumed = IntArray(2)
    internal val parentOffsetInWindow = IntArray(2)

    /**
     * view children
     * - use by showGravity to dell onLayout
     */
    internal var headerView: View? = null
    internal var footerView: View? = null

    /**
     * - use by generalHelper to dell cancel event
     */
    internal var targetView: View? = null
    private var pullContentLayout: View? = null

    //-------------------------START| values part |START-----------------------------
    /**
     * trigger distance
     * - use by showGravity to control the layout move
     */
    var refreshTriggerDistance = -1
    var loadTriggerDistance = -1

    /**
     * max drag distance
     */
    var pullDownMaxDistance = -1
    var pullUpMaxDistance = -1

    /**
     * refresh Animation total during
     */
    private var refreshAnimationDuring = 180

    /**
     * 回弹动画时长
     */
    private var resetAnimationDuring = 600

    /**
     * over scroll top start offset
     */
    private var topOverScrollMaxTriggerOffset = 60

    /**
     * over scroll bottom start offset
     */
    private var bottomOverScrollMaxTriggerOffset = 60

    /**
     * over scroll min during
     */
    private var overScrollMinDuring = 65

    /**
     * target view id
     */
    private var targetViewId = -1

    /**
     * the ratio for final distance for drag
     */
    private var dragDampingRatio = 0.6f

    /**
     * over scroll adjust value
     */
    private var overScrollAdjustValue = 1f

    /**
     * move distance ratio for over scroll
     */
    private var overScrollDampingRatio = 0.35f

    /**
     * switch
     */
    var isRefreshEnable = true
    var isTwinkEnable = true
    var isLoadMoreEnable = false
    var isAutoLoadingEnable = false

    /**
     * dispatch control
     * dispatchTouchAble:is able prl dispatch touch event
     * dispatchPullTouchAble:is able prl dell pull logic
     * dispatchChildrenEventAble:is the views in prl able to dispatch touch event
     */
    private var dispatchTouchAble = true
    private var dispatchPullTouchAble = true
    private var dispatchChildrenEventAble = true

    /**
     * move with
     * isMoveWithContent:- use by generalHelper dell touch logic
     */
    private var isMoveWithFooter = true
    private var isMoveWithHeader = true
    internal var isMoveWithContent = true

    /**
     * view front
     */
    private var isHeaderFront = false
    private var isFooterFront = false

    //--------------------START|| values can modify in the lib only ||START------------------

    /**
     * current refreshing state 1:refresh 2:loadMore
     */
    private var refreshState = 0

    /**
     * last Scroll Y
     */
    private var lastScrollY = 0

    /**
     * over scroll state
     */
    private var overScrollState = 0

    /**
     * drag move distance
     * - use by generalHelper dell touch logic
     */
    var moveDistance = 0
        private set

    /**
     * final scroll distance
     */
    private var finalScrollDistance = 0f

    /**
     * make sure header or footer hold trigger one time
     */
    private var pullStateControl = true

    /**
     * refreshing state trigger
     */
    var isHoldingTrigger = false
        private set

    var isHoldingFinishTrigger = false
        private set

    private var isResetTrigger = false

    var isOverScrollTrigger = false
        private set

    /**
     * refresh with action
     */
    private var refreshWithAction = true

    /**
     * is scroll able when view scroll back
     */
    private var isScrollAbleViewBackScroll = false

    /**
     * is is target nested
     */
    private var isTargetNested = false

    private var isAttachWindow = false

    //--------------------END|| values can modify in class only ||END------------------
    //--------------------END| values part |END------------------

    private val showGravity: ShowGravity
    private val generalPullHelper: GeneralPullHelper

    private var onRefreshListener: OnRefreshListener? = null

    /**
     * use this can instead of isTargetAbleScrollUp and isTargetAbleScrollDown
     */
    private var onTargetScrollCheckListener: OnTargetScrollCheckListener? = null

    private var onMoveTargetViewListener: OnMoveTargetViewListener? = null

    private var scroller: OverScroller? = null

    private var startRefreshAnimator: ValueAnimator? = null
    private var resetHeaderAnimator: ValueAnimator? = null
    private var startLoadMoreAnimator: ValueAnimator? = null
    private var resetFooterAnimator: ValueAnimator? = null
    private var overScrollAnimator: ValueAnimator? = null

    private var scrollInterpolator: Interpolator? = null
    private var animationMainInterpolator: Interpolator? = null
    private var animationOverScrollInterpolator: Interpolator? = null

    private var delayHandleActionRunnable: Runnable? = null

    private val recyclerDefaultInterpolator: Interpolator
        get() = Interpolator { t ->
            var t = t
            t -= 1.0f
            t * t * t * t * t + 1.0f
        }

    /**
     * - use by generalHelper to dell touch logic
     */
    val isTargetNestedScrollingEnabled: Boolean
        get() = isTargetNested && ViewCompat.isNestedScrollingEnabled(targetView!!)

    val isTargetAbleScrollUp: Boolean
        get() = if (onTargetScrollCheckListener != null) {
            onTargetScrollCheckListener!!.onScrollUpAbleCheck()
        } else PRLCommonUtils.canChildScrollUp(targetView)

    val isTargetAbleScrollDown: Boolean
        get() = if (onTargetScrollCheckListener != null) {
            onTargetScrollCheckListener!!.onScrollDownAbleCheck()
        } else PRLCommonUtils.canChildScrollDown(targetView)

    /**
     * state animation
     */
    private val resetHeaderAnimationListener = object : PullAnimatorListenerAdapter() {
        override fun animationStart() {
            if (isResetTrigger && isRefreshing && !isHoldingFinishTrigger && onHeaderPullFinish(
                            isFlag)) {
                isHoldingFinishTrigger = true
            }
        }

        override fun animationEnd() {
            if (isResetTrigger) {
                resetRefreshState()
            }
        }
    }

    private val resetFooterAnimationListener = object : PullAnimatorListenerAdapter() {
        override fun animationStart() {
            if (isResetTrigger && isLoading && !isHoldingFinishTrigger && onFooterPullFinish(
                            isFlag)) {
                isHoldingFinishTrigger = true
            }
        }

        override fun animationEnd() {
            if (isResetTrigger) {
                resetLoadMoreState()
            }
        }
    }

    private val refreshStartAnimationListener = object : PullAnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            if (refreshState == 0) {
                refreshState = 1
                if (footerView != null) {
                    footerView!!.visibility = View.GONE
                }
                if (onRefreshListener != null && refreshWithAction) {
                    onRefreshListener!!.onRefresh()
                }
            }
        }
    }

    private val loadingStartAnimationListener = object : PullAnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            if (refreshState == 0) {
                refreshState = 2
                if (headerView != null) {
                    headerView!!.visibility = View.GONE
                }
                if (onRefreshListener != null && refreshWithAction) {
                    onRefreshListener!!.onLoading()
                }
            }
        }
    }

    private val overScrollAnimatorListener = object : PullAnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            super.onAnimationStart(animation)
            onNestedScrollAccepted(pullContentLayout!!, targetView!!, ViewCompat.SCROLL_AXIS_VERTICAL)
        }

        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            handleAction()
            onStopNestedScroll(targetView!!)
            overScrollState = 0
            isOverScrollTrigger = false
        }
    }

    /**
     * animator update listener
     */
    private val headerAnimationUpdate = ValueAnimator.AnimatorUpdateListener { animation ->
        moveChildren(animation.animatedValue as Int)
        onHeaderPullChange()
    }

    private val footerAnimationUpdate = ValueAnimator.AnimatorUpdateListener { animation ->
        moveChildren(animation.animatedValue as Int)
        onFooterPullChange()
    }

    private val overScrollAnimatorUpdate = ValueAnimator.AnimatorUpdateListener { animation ->
        val offsetY = (animation.animatedValue as Int * overScrollDampingRatio).toInt()
        onScrollAny(offsetY + parentOffsetInWindow[1])
    }

    val isOverScrollUp: Boolean
        get() = overScrollState == 1

    val isOverScrollDown: Boolean
        get() = overScrollState == 2

    val isRefreshing: Boolean
        get() = refreshState == 0 && startRefreshAnimator != null && startRefreshAnimator!!.isRunning || refreshState == 1

    val isLoading: Boolean
        get() = refreshState == 0 && startLoadMoreAnimator != null && startLoadMoreAnimator!!.isRunning || refreshState == 2

    val isDragDown: Boolean
        get() = generalPullHelper.dragState == 1

    val isDragUp: Boolean
        get() = generalPullHelper.dragState == -1

    val isDragMoveTrendDown: Boolean
        get() = generalPullHelper.isDragMoveTrendDown

    val isDragVertical: Boolean
        get() = generalPullHelper.isDragVertical

    val isDragHorizontal: Boolean
        get() = generalPullHelper.isDragHorizontal

    val isLayoutDragMoved: Boolean
        get() = generalPullHelper.isLayoutDragMoved

    val isLayoutMoving: Boolean
        get() {
            if (startRefreshAnimator != null && startRefreshAnimator!!.isRunning) {
                return true
            } else if (resetHeaderAnimator != null && resetHeaderAnimator!!.isRunning) {
                return true
            } else if (startLoadMoreAnimator != null && startLoadMoreAnimator!!.isRunning) {
                return true
            } else if (resetFooterAnimator != null && resetFooterAnimator!!.isRunning) {
                return true
            } else if (overScrollAnimator != null && overScrollAnimator!!.isRunning) {
                return true
            }
            return false
        }

    init {
        showGravity = ShowGravity(this)
        generalPullHelper = GeneralPullHelper(this, context)
        parentHelper = NestedScrollingParentHelper(this)
        childHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
        loadAttribute(context, attrs)
    }

    private fun loadAttribute(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.PullRefreshLayout)
        isRefreshEnable = ta.getBoolean(R.styleable.PullRefreshLayout_prl_refreshEnable, isRefreshEnable)
        isLoadMoreEnable = ta.getBoolean(R.styleable.PullRefreshLayout_prl_loadMoreEnable, isLoadMoreEnable)
        isTwinkEnable = ta.getBoolean(R.styleable.PullRefreshLayout_prl_twinkEnable, isTwinkEnable)
        isAutoLoadingEnable = ta.getBoolean(R.styleable.PullRefreshLayout_prl_autoLoadingEnable, isAutoLoadingEnable)
        isHeaderFront = ta.getBoolean(R.styleable.PullRefreshLayout_prl_headerFront, isHeaderFront)
        isFooterFront = ta.getBoolean(R.styleable.PullRefreshLayout_prl_footerFront, isFooterFront)
        refreshTriggerDistance = ta.getDimensionPixelOffset(R.styleable.PullRefreshLayout_prl_refreshTriggerDistance, refreshTriggerDistance)
        loadTriggerDistance = ta.getDimensionPixelOffset(R.styleable.PullRefreshLayout_prl_loadTriggerDistance, loadTriggerDistance)
        pullDownMaxDistance = ta.getDimensionPixelOffset(R.styleable.PullRefreshLayout_prl_pullDownMaxDistance, pullDownMaxDistance)
        pullUpMaxDistance = ta.getDimensionPixelOffset(R.styleable.PullRefreshLayout_prl_pullUpMaxDistance, pullUpMaxDistance)
        resetAnimationDuring = ta.getInt(R.styleable.PullRefreshLayout_prl_resetAnimationDuring, resetAnimationDuring)
        refreshAnimationDuring = ta.getInt(R.styleable.PullRefreshLayout_prl_refreshAnimationDuring, refreshAnimationDuring)
        overScrollMinDuring = ta.getInt(R.styleable.PullRefreshLayout_prl_overScrollMinDuring, overScrollMinDuring)
        dragDampingRatio = ta.getFloat(R.styleable.PullRefreshLayout_prl_dragDampingRatio, dragDampingRatio)
        overScrollAdjustValue = ta.getFloat(R.styleable.PullRefreshLayout_prl_overScrollAdjustValue, overScrollAdjustValue)
        overScrollDampingRatio = ta.getFloat(R.styleable.PullRefreshLayout_prl_overScrollDampingRatio, overScrollDampingRatio)
        topOverScrollMaxTriggerOffset = ta.getDimensionPixelOffset(R.styleable.PullRefreshLayout_prl_topOverScrollMaxTriggerOffset, dip(topOverScrollMaxTriggerOffset))
        bottomOverScrollMaxTriggerOffset = ta.getDimensionPixelOffset(R.styleable.PullRefreshLayout_prl_downOverScrollMaxTriggerOffset, dip(bottomOverScrollMaxTriggerOffset))
        showGravity.headerShowGravity = ta.getInteger(R.styleable.PullRefreshLayout_prl_headerShowGravity, ShowGravity.FOLLOW)
        showGravity.footerShowGravity = ta.getInteger(R.styleable.PullRefreshLayout_prl_footerShowGravity, ShowGravity.FOLLOW)
        targetViewId = ta.getResourceId(R.styleable.PullRefreshLayout_prl_targetId, targetViewId)
        headerView = initRefreshView(context, ta.getResourceId(R.styleable.PullRefreshLayout_prl_headerViewId, -1))
        footerView = initRefreshView(context, ta.getResourceId(R.styleable.PullRefreshLayout_prl_footerViewId, -1))
        ta.recycle()
    }

    private fun initRefreshView(context: Context, resourceId: Int): View? {
        return if (resourceId != -1) {
            LayoutInflater.from(context).inflate(resourceId, null, false)
        } else null
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initContentView()
        // make sure that targetView able to scroll after targetView has set
        dellNestedScrollCheck()
        readyScroller()
    }

    private fun initContentView() {
        for (i in 0 until childCount) {
            if (getChildAt(i) !== headerView && getChildAt(i) !== footerView) {
                pullContentLayout = getChildAt(i)
                break
            }
        }

        if (pullContentLayout == null) {
            throw RuntimeException("PullRefreshLayout should have a child")
        }

        // ---------| targetView ready |----------
        if (targetViewId != -1) {
            targetView = findViewById(targetViewId)
        }

        if (targetView == null) {
            targetView = pullContentLayout
        }

        setHeaderView(headerView)
        setFooterView(footerView)
    }

    fun dispatchSuperTouchEvent(ev: MotionEvent): Boolean {
        return dispatchChildrenEventAble && super.dispatchTouchEvent(ev)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        try {
            //dell Exception - java.lang.IllegalArgumentException: pointerIndex out of range
            return dispatchTouchAble && (!dispatchPullTouchAble && super.dispatchTouchEvent(ev) || generalPullHelper.dispatchTouchEvent(ev))
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }

        return false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        for (i in 0 until childCount) {
            measureChildWithMargins(getChildAt(i), widthMeasureSpec, 0, heightMeasureSpec, 0)
        }
        if (headerView != null && refreshTriggerDistance == -1) {
            refreshTriggerDistance = headerView!!.measuredHeight
        }
        if (footerView != null && loadTriggerDistance == -1) {
            loadTriggerDistance = footerView!!.measuredHeight
        }

        if (pullDownMaxDistance == -1) {
            pullDownMaxDistance = measuredHeight
        }

        if (pullUpMaxDistance == -1) {
            pullUpMaxDistance = measuredHeight
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        showGravity.layout(0, 0, measuredWidth, measuredHeight)
        layoutContentView()
    }

    private fun layoutContentView() {
        val lp = pullContentLayout!!.layoutParams as ViewGroup.MarginLayoutParams
        pullContentLayout!!.layout(
                paddingLeft + lp.leftMargin,
                paddingTop + lp.topMargin,
                paddingLeft + lp.leftMargin + pullContentLayout!!.measuredWidth,
                paddingTop + lp.topMargin + pullContentLayout!!.measuredHeight)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttachWindow = true
        handleAction()
    }

    override fun onDetachedFromWindow() {
        isAttachWindow = false
        cancelAllAnimation()
        abortScroller()
        startRefreshAnimator = null
        resetHeaderAnimator = null
        startLoadMoreAnimator = null
        resetFooterAnimator = null
        overScrollAnimator = null
        delayHandleActionRunnable = null
        super.onDetachedFromWindow()
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is ViewGroup.MarginLayoutParams
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return ViewGroup.MarginLayoutParams(-1, -1)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return ViewGroup.MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return ViewGroup.MarginLayoutParams(context, attrs)
    }

    override fun requestDisallowInterceptTouchEvent(b: Boolean) {
        if ((android.os.Build.VERSION.SDK_INT >= 21 || targetView !is AbsListView) && (targetView == null || ViewCompat.isNestedScrollingEnabled(targetView!!))) {
            super.requestDisallowInterceptTouchEvent(b)
        }
    }

    override fun computeScroll() {
        val isFinish = scroller == null || !scroller!!.computeScrollOffset() || scroller!!.isFinished
        if (!isFinish) {
            val currY = scroller!!.currY
            val currScrollOffset = currY - lastScrollY
            lastScrollY = currY
            if (scrollOver(currScrollOffset)) {
                return
            } else if (isScrollAbleViewBackScroll && targetView is ListView) {
                // ListView scroll back scroll to normal
                ListViewCompat.scrollListBy((targetView as ListView?)!!, currScrollOffset)
            }

            if (!isOverScrollTrigger
                    && !isTargetAbleScrollUp
                    && currScrollOffset < 0
                    && moveDistance >= 0) {
                overScrollDell(1, currScrollOffset)
            } else if (!isOverScrollTrigger
                    && !isTargetAbleScrollDown
                    && currScrollOffset > 0
                    && moveDistance <= 0) {
                overScrollDell(2, currScrollOffset)
            }
            // invalidate View ,the method invalidate() sometimes not work , so i use ViewCompat.postInvalidateOnAnimation(this) instead of invalidate()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun scrollOver(currScrollOffset: Int): Boolean {
        return (isTwinkEnable && (overScrollFlingState() == 1 || overScrollFlingState() == 2) && overScrollBackDell(overScrollFlingState(), currScrollOffset))
    }

    /**
     * overScroll Back Dell
     *
     * @param tempDistance temp move distance
     * @return need continue
     */
    private fun overScrollBackDell(type: Int, tempDistance: Int): Boolean {
        if (type == 1 && finalScrollDistance > moveDistance * 2 || type == 2 && finalScrollDistance < moveDistance * 2) {
            cancelAllAnimation()
            if (type == 1 && moveDistance <= tempDistance || type == 2 && moveDistance >= tempDistance) {
                dellScroll((-moveDistance).toFloat())
                return kindsOfViewsToNormalDell(type, tempDistance)
            }
            dellScroll((-tempDistance).toFloat())
            return false
        } else {
            abortScroller()
            handleAction()
            return true
        }
    }

    /**
     * kinds of view dell back scroll to normal state
     */
    private fun kindsOfViewsToNormalDell(type: Int, tempDistance: Int): Boolean {
        if (!dispatchChildrenEventAble) {
            return false
        }
        val sign = if (type == 1) 1 else -1
        val velocity = (sign * Math.abs(scroller!!.currVelocity)).toInt()
        if (targetView is ScrollView && !isScrollAbleViewBackScroll) {
            (targetView as ScrollView).fling(velocity)
        } else if (targetView is WebView && !isScrollAbleViewBackScroll) {
            (targetView as WebView).flingScroll(0, velocity)
        } else if (targetView is RecyclerView && !isTargetNestedScrollingEnabled && !isScrollAbleViewBackScroll) {
            (targetView as RecyclerView).fling(0, velocity)
        } else if (targetView is NestedScrollView && !isTargetNestedScrollingEnabled && !isScrollAbleViewBackScroll) {
            (targetView as NestedScrollView).fling(velocity)
        } else if (!PRLCommonUtils.canChildScrollUp(targetView!!) && !PRLCommonUtils.canChildScrollDown(targetView!!)
                || targetView is ListView && !isScrollAbleViewBackScroll
                || targetView is RecyclerView
                || targetView is NestedScrollView) {
            // this case just dell overScroll normal,without any operation
        } else {
            // the target is able to scrollUp or scrollDown but have not the fling method
            // ,so dell the view just like normal view
            overScrollDell(type, tempDistance)
            return true
        }
        isScrollAbleViewBackScroll = true
        return false
    }

    /**
     * dell over scroll to move children
     */
    private fun startOverScrollAnimation(type: Int, distanceMove: Int) {
        val mDistanceMove = if (type == 1) {
            Math.max(-topOverScrollMaxTriggerOffset, distanceMove)
        } else {
            Math.min(bottomOverScrollMaxTriggerOffset, distanceMove)
        }
        val finalDistance = scroller!!.finalY - scroller!!.currY
        abortScroller()
        cancelAllAnimation()
        if (overScrollAnimator == null) {
            if (animationOverScrollInterpolator == null) {
                animationOverScrollInterpolator = LinearInterpolator()
            }
            overScrollAnimator = getAnimator(mDistanceMove, 0, overScrollAnimatorUpdate, overScrollAnimatorListener, animationOverScrollInterpolator!!)
        } else {
            overScrollAnimator!!.setIntValues(mDistanceMove, 0)
        }
        overScrollAnimator!!.duration = getOverScrollTime(finalDistance)
        overScrollAnimator!!.start()
    }

    private fun onTopOverScroll() {
        overScrollState = 1
    }

    private fun onBottomOverScroll() {
        overScrollState = 2
        autoLoadingTrigger()
    }

    private fun autoLoadingTrigger() {
        if (isAutoLoadingEnable && !isHoldingTrigger && onRefreshListener != null) {
            isHoldingTrigger = true
            loadingStartAnimationListener.onAnimationEnd(null)
        }
    }

    private fun readyScroller() {
        if (scroller == null && (isTwinkEnable || isAutoLoadingEnable)) {
            if (targetView is RecyclerView) {
                if (scrollInterpolator == null) {
                    scrollInterpolator = recyclerDefaultInterpolator
                }
                scroller = OverScroller(context, scrollInterpolator)
                return
            }
            scroller = OverScroller(context, BezierInterpolator())
        }
    }

    private fun readyMainInterpolator(): Interpolator {
        if (animationMainInterpolator == null) {
            animationMainInterpolator = ViscousInterpolator()
        }
        return animationMainInterpolator!!
    }

    /**
     * dell the nestedScroll
     *
     * @param distanceY move distance of Y
     */
    private fun dellScroll(distanceY: Float) {
        if (distanceY == 0f) {
            return
        }
        var tempDistance = (moveDistance + distanceY).toInt()
        tempDistance = Math.min(tempDistance, pullDownMaxDistance)
        tempDistance = Math.max(tempDistance, -pullUpMaxDistance)
        if (!isTwinkEnable && (isRefreshing && tempDistance < 0 || isLoading && tempDistance > 0)) {
            if (moveDistance == 0) {
                return
            }
            tempDistance = 0
        }
        if (isLoadMoreEnable && tempDistance <= 0 || isRefreshEnable && tempDistance >= 0 || isTwinkEnable) {
            moveChildren(tempDistance)
        } else {
            moveDistance = 0
            return
        }
        if (moveDistance >= 0 && headerView != null) {
            onHeaderPullChange()
            if (!isHoldingTrigger && moveDistance >= refreshTriggerDistance) {
                if (pullStateControl) {
                    pullStateControl = false
                    onHeaderPullHoldTrigger()
                }
                return
            }
            if (!isHoldingTrigger && !pullStateControl) {
                pullStateControl = true
                onHeaderPullHoldUnTrigger()
            }
            return
        }
        if (footerView == null) {
            return
        }
        onFooterPullChange()
        if (!isHoldingTrigger && moveDistance <= -loadTriggerDistance) {
            if (pullStateControl) {
                pullStateControl = false
                onFooterPullHoldTrigger()
            }
            return
        }
        if (!isHoldingTrigger && !pullStateControl) {
            pullStateControl = true
            onFooterPullHoldUnTrigger()
        }
    }

    private fun overScrollDell(type: Int, offset: Int) {
        if (parentOffsetInWindow[1] != 0 || (isTwinkEnable && (!isTargetAbleScrollUp && isTargetAbleScrollDown && moveDistance < 0 || isTargetAbleScrollUp && !isTargetAbleScrollDown && moveDistance > 0))) {
            return
        }
        if (type == 1) {
            onTopOverScroll()
        } else {
            onBottomOverScroll()
        }
        if (!isTwinkEnable) {
            abortScroller()
            return
        }
        isOverScrollTrigger = true
        startOverScrollAnimation(type, offset)
    }

    /**
     * decide on the action refresh or loadMore
     */
    private fun handleAction() {
        if (isRefreshEnable
                && headerView != null
                && !isLoading
                && !isResetTrigger
                && moveDistance >= refreshTriggerDistance) {
            startRefresh(moveDistance, -1, true)
        } else if (isLoadMoreEnable
                && footerView != null
                && !generalPullHelper.isDragMoveTrendDown
                && !isRefreshing
                && !isResetTrigger
                && moveDistance <= -loadTriggerDistance) {
            startLoadMore(moveDistance, -1, true)
        } else if (!isHoldingTrigger && moveDistance > 0 || isRefreshing && (moveDistance < 0 || isResetTrigger)) {
            resetHeaderView(moveDistance)
        } else if (!isHoldingTrigger && moveDistance < 0 || isLoading && (moveDistance > 0 || isResetTrigger)) {
            resetFootView(moveDistance)
        }
    }

    private fun startRefresh(headerCurrentDistance: Int, toRefreshDistance: Int, withAction: Boolean) {
        if (refreshTriggerDistance == -1) {
            return
        }
        cancelAllAnimation()
        if (!isHoldingTrigger && onHeaderPullHolding()) {
            isHoldingTrigger = true
        }
        val refreshTriggerHeight = if (toRefreshDistance != -1) toRefreshDistance else refreshTriggerDistance
        if (headerCurrentDistance == refreshTriggerHeight) {
            refreshStartAnimationListener.onAnimationEnd(null)
            return
        }
        if (startRefreshAnimator == null) {
            startRefreshAnimator = getAnimator(headerCurrentDistance, refreshTriggerHeight, headerAnimationUpdate, refreshStartAnimationListener, readyMainInterpolator())
        } else {
            startRefreshAnimator!!.setIntValues(headerCurrentDistance, refreshTriggerHeight)
        }
        refreshWithAction = withAction
        startRefreshAnimator!!.duration = refreshAnimationDuring.toLong()
        startRefreshAnimator!!.start()
    }

    private fun resetHeaderView(headerViewHeight: Int) {
        cancelAllAnimation()
        if (headerViewHeight == 0) {
            resetHeaderAnimationListener.onAnimationStart(null)
            resetHeaderAnimationListener.onAnimationEnd(null)
            return
        }
        if (resetHeaderAnimator == null) {
            resetHeaderAnimator = getAnimator(headerViewHeight, 0, headerAnimationUpdate, resetHeaderAnimationListener, readyMainInterpolator())
        } else {
            resetHeaderAnimator!!.setIntValues(headerViewHeight, 0)
        }
        resetHeaderAnimator!!.duration = resetAnimationDuring.toLong()
        resetHeaderAnimator!!.start()
    }

    private fun resetRefreshState() {
        if (isHoldingFinishTrigger) {
            onHeaderPullReset()
        }
        if (footerView != null) {
            footerView!!.visibility = View.VISIBLE
        }
        resetState()
    }

    private fun startLoadMore(loadCurrentDistance: Int, toLoadDistance: Int, withAction: Boolean) {
        if (loadTriggerDistance == -1) {
            return
        }
        cancelAllAnimation()
        if (!isHoldingTrigger && onFooterPullHolding()) {
            isHoldingTrigger = true
        }
        val loadTriggerHeight = if (toLoadDistance != -1) toLoadDistance else loadTriggerDistance
        if (loadCurrentDistance == -loadTriggerHeight) {
            loadingStartAnimationListener.onAnimationEnd(null)
            return
        }
        if (startLoadMoreAnimator == null) {
            startLoadMoreAnimator = getAnimator(loadCurrentDistance, -loadTriggerHeight, footerAnimationUpdate, loadingStartAnimationListener, readyMainInterpolator())
        } else {
            startLoadMoreAnimator!!.setIntValues(loadCurrentDistance, -loadTriggerHeight)
        }
        refreshWithAction = withAction
        startLoadMoreAnimator!!.duration = refreshAnimationDuring.toLong()
        startLoadMoreAnimator!!.start()
    }

    private fun resetFootView(loadMoreViewHeight: Int) {
        cancelAllAnimation()
        if (loadMoreViewHeight == 0) {
            resetFooterAnimationListener.onAnimationStart(null)
            resetFooterAnimationListener.onAnimationEnd(null)
            return
        }
        if (resetFooterAnimator == null) {
            resetFooterAnimator = getAnimator(loadMoreViewHeight, 0, footerAnimationUpdate, resetFooterAnimationListener, readyMainInterpolator())
        } else {
            resetFooterAnimator!!.setIntValues(loadMoreViewHeight, 0)
        }
        resetFooterAnimator!!.duration = resetAnimationDuring.toLong()
        resetFooterAnimator!!.start()
    }

    private fun resetLoadMoreState() {
        if (isHoldingFinishTrigger) {
            onFooterPullReset()
        }
        if (headerView != null) {
            headerView!!.visibility = View.VISIBLE
        }
        resetState()
    }

    private fun resetState() {
        isHoldingFinishTrigger = false
        isHoldingTrigger = false
        pullStateControl = true
        isResetTrigger = false
        refreshState = 0
    }

    private fun getAnimator(firstValue: Int,
                            secondValue: Int,
                            updateListener: ValueAnimator.AnimatorUpdateListener,
                            animatorListener: Animator.AnimatorListener,
                            interpolator: Interpolator): ValueAnimator {
        val animator = ValueAnimator.ofInt(firstValue, secondValue)
        animator.addUpdateListener(updateListener)
        animator.addListener(animatorListener)
        animator.interpolator = interpolator
        return animator
    }

    private fun abortScroller() {
        if (scroller != null && !scroller!!.isFinished) {
            scroller!!.abortAnimation()
        }
    }

    private fun cancelAnimation(animator: ValueAnimator?) {
        if (animator != null && animator.isRunning) {
            animator.cancel()
        }
    }

    private fun getOverScrollTime(distance: Int): Long {
        val ratio = Math.abs(distance.toFloat() / context.deviceHeight)
        return Math.max(overScrollMinDuring.toLong(), (Math.pow((2000 * ratio).toDouble(), 0.44) * overScrollAdjustValue).toLong())
    }

    private fun dellNestedScrollCheck() {
        var target = targetView
        while (target !== pullContentLayout) {
            if (target !is NestedScrollingChild) {
                isTargetNested = false
                return
            }
            target = target.parent as View
        }
        isTargetNested = target is NestedScrollingChild
    }

    private fun removeDelayRunnable() {
        if (delayHandleActionRunnable != null) {
            removeCallbacks(delayHandleActionRunnable)
        }
    }

    /**
     * the fling may execute after onStopNestedScroll , so while overScrollBack try delay to handle
     * action
     */
    private fun getDelayHandleActionRunnable(): Runnable {
        return Runnable {
            if (!isTwinkEnable || (scroller != null && scroller!!.isFinished && overScrollState == 0)) {
                handleAction()
            }
        }
    }

    private fun setViewFront(firstFront: Boolean, secondFront: Boolean, firstView: View?, secondView: View?) {
        if (firstFront) {
            bringViewToFront(firstView)
        } else {
            bringViewToFront(pullContentLayout)
            if (secondFront) {
                bringViewToFront(secondView)
            }
        }
    }

    private fun bringViewToFront(view: View?) {
        view?.bringToFront()
    }

    private fun dellDetachComplete(): Boolean {
        if (isAttachWindow) {
            return true
        }
        isResetTrigger = true
        isHoldingFinishTrigger = true
        return false
    }

    private fun nestedAble(target: View): Boolean {
        return isTargetNestedScrollingEnabled || target !is NestedScrollingChild
    }

    private fun overScrollFlingState(): Int {
        if (moveDistance == 0) {
            return 0
        }
        return if (!generalPullHelper.isDragMoveTrendDown) {
            if (moveDistance > 0) 1 else -1
        } else {
            if (moveDistance < 0) 2 else -1
        }
    }

    private fun getRefreshView(v: View): View {
        var lp: ViewGroup.LayoutParams? = v.layoutParams
        if (v.parent != null) {
            (v.parent as ViewGroup).removeView(v)
        }
        if (lp == null) {
            lp = ViewGroup.LayoutParams(-1, -2)
            v.layoutParams = lp
        }
        return v
    }

    private fun onHeaderPullChange() {
        if (headerView is OnPullListener && !isLoading) {
            (headerView as OnPullListener).onPullChange(moveDistance.toFloat() / refreshTriggerDistance)
        }
    }

    private fun onHeaderPullHoldTrigger() {
        if (headerView is OnPullListener) {
            (headerView as OnPullListener).onPullHoldTrigger()
        }
    }

    private fun onHeaderPullHoldUnTrigger() {
        if (headerView is OnPullListener) {
            (headerView as OnPullListener).onPullHoldUnTrigger()
        }
    }

    private fun onHeaderPullHolding(): Boolean {
        if (headerView is OnPullListener) {
            (headerView as OnPullListener).onPullHolding()
            return true
        }
        return false
    }

    private fun onHeaderPullFinish(flag: Boolean): Boolean {
        if (headerView is OnPullListener) {
            (headerView as OnPullListener).onPullFinish(flag)
            return true
        }
        return false
    }

    private fun onHeaderPullReset() {
        if (headerView is OnPullListener) {
            (headerView as OnPullListener).onPullReset()
        }
    }

    private fun onFooterPullChange() {
        if (footerView is OnPullListener && !isRefreshing) {
            (footerView as OnPullListener).onPullChange(moveDistance.toFloat() / loadTriggerDistance)
        }
    }

    private fun onFooterPullHoldTrigger() {
        if (footerView is OnPullListener) {
            (footerView as OnPullListener).onPullHoldTrigger()
        }
    }

    private fun onFooterPullHoldUnTrigger() {
        if (footerView is OnPullListener) {
            (footerView as OnPullListener).onPullHoldUnTrigger()
        }
    }

    private fun onFooterPullHolding(): Boolean {
        if (footerView is OnPullListener) {
            (footerView as OnPullListener).onPullHolding()
            return true
        }
        return false
    }

    private fun onFooterPullFinish(flag: Boolean): Boolean {
        if (footerView is OnPullListener) {
            (footerView as OnPullListener).onPullFinish(flag)
            return true
        }
        return false
    }

    private fun onFooterPullReset() {
        if (footerView is OnPullListener) {
            (footerView as OnPullListener).onPullReset()
        }
    }

    internal fun onStartScroll() {
        abortScroller()
        cancelAllAnimation()
        isScrollAbleViewBackScroll = false
    }

    internal fun onPreScroll(dy: Int, consumed: IntArray) {
        if (dy > 0 && moveDistance > 0) {
            if (dy > moveDistance) {
                consumed[1] += moveDistance
                dellScroll((-moveDistance).toFloat())
                return
            }
            consumed[1] += dy
            dellScroll((-dy).toFloat())
        } else if (dy < 0 && moveDistance < 0) {
            if (dy < moveDistance) {
                consumed[1] += moveDistance
                dellScroll((-moveDistance).toFloat())
                return
            }
            consumed[1] += dy
            dellScroll((-dy).toFloat())
        }
    }

    internal fun onScroll(dy: Int) {
        if (generalPullHelper.isDragMoveTrendDown && !isTargetAbleScrollUp || !generalPullHelper.isDragMoveTrendDown && !isTargetAbleScrollDown) {
            onScrollAny(dy)
        }
    }

    private fun onScrollAny(dy: Int) {
        var dy = dy
        if (dy < 0
                && dragDampingRatio < 1
                && pullDownMaxDistance > 0
                && moveDistance - dy > pullDownMaxDistance * dragDampingRatio) {
            dy = (dy * (1 - moveDistance / pullDownMaxDistance.toFloat())).toInt()
        } else if (dy > 0
                && dragDampingRatio < 1
                && pullUpMaxDistance > 0
                && -moveDistance + dy > pullUpMaxDistance * dragDampingRatio) {
            dy = (dy * (1 - -moveDistance / pullUpMaxDistance.toFloat())).toInt()
        } else {
            dy = (dy * dragDampingRatio).toInt()
        }
        dellScroll((-dy).toFloat())
    }

    internal fun onStopScroll() {
        removeDelayRunnable()
        if (!isTwinkEnable) {
            handleAction()
        } else if ((overScrollFlingState() == 1 || overScrollFlingState() == 2) && !isOverScrollTrigger) {
            if (delayHandleActionRunnable == null) {
                delayHandleActionRunnable = getDelayHandleActionRunnable()
            }
            postDelayed(delayHandleActionRunnable, 50)
        } else if (scroller != null && scroller!!.isFinished) {
            handleAction()
        }

        // just make that custom header and footer easier
        if (generalPullHelper.isLayoutDragMoved) {
            if (isRefreshing || moveDistance > 0) {
                onHeaderPullChange()
            } else if (isLoading || moveDistance < 0) {
                onFooterPullChange()
            }
        }
    }

    internal fun onPreFling(velocityY: Float) {
        if ((isTwinkEnable || isAutoLoadingEnable) && overScrollFlingState() != -1) {
            readyScroller()
            lastScrollY = 0
            scroller!!.fling(0, 0, 0, velocityY.toInt(), 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE)
            finalScrollDistance = (scroller!!.finalY - scroller!!.currY).toFloat()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes)
        startNestedScroll(axes and ViewCompat.SCROLL_AXIS_VERTICAL)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (nestedAble(target)) {
            generalPullHelper.dellDirection(dy)
            if (isMoveWithContent) {
                onPreScroll(dy, consumed)
            }
            val parentConsumed = parentScrollConsumed
            if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
                consumed[0] += parentConsumed[0]
                consumed[1] += parentConsumed[1]
            }
        }
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
                                dyUnconsumed: Int) {
        if (nestedAble(target)) {
            dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                    parentOffsetInWindow)
            if (isMoveWithContent) {
                onScroll(dyUnconsumed + parentOffsetInWindow[1])
            }
        }
    }

    override fun onStopNestedScroll(child: View) {
        parentHelper.onStopNestedScroll(child)
        stopNestedScroll()
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        if (nestedAble(target)) {
            onPreFling(velocityY)
        }
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun getNestedScrollAxes(): Int {
        return parentHelper.nestedScrollAxes
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return childHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        childHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return childHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
                                      dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    interface OnPullListener {
        fun onPullChange(percent: Float)

        fun onPullHoldTrigger()

        fun onPullHoldUnTrigger()

        fun onPullHolding()

        fun onPullFinish(flag: Boolean)

        fun onPullReset()
    }

    interface OnTargetScrollCheckListener {
        fun onScrollUpAbleCheck(): Boolean

        fun onScrollDownAbleCheck(): Boolean
    }

    interface OnRefreshListener {
        fun onRefresh() {}

        fun onLoading() {}
    }

    /**
     * 用于监听TargetView的移动
     */
    interface OnMoveTargetViewListener {
        fun onMoveDistance(distance: Int)
    }

    private open inner class PullAnimatorListenerAdapter : AnimatorListenerAdapter() {
        var isFlag = true
        private var isCancel: Boolean = false

        override fun onAnimationStart(animation: Animator?) {
            if (!isAttachWindow) {
                return
            }
            animationStart()
        }

        override fun onAnimationCancel(animation: Animator) {
            isCancel = true
        }

        override fun onAnimationEnd(animation: Animator?) {
            if (!isAttachWindow) {
                return
            }
            if (!isCancel) {
                animationEnd()
            }
            isCancel = false
        }

        protected open fun animationStart() {}

        protected open fun animationEnd() {}
    }

    // ------------------| open api |------------------

    /**
     * move children
     */
    fun moveChildren(distance: Int) {
        moveDistance = distance
        if (moveDistance <= 0 && !isTargetAbleScrollDown) {
            autoLoadingTrigger()
        }
        if (isMoveWithFooter) {
            showGravity.dellFooterMoving(moveDistance)
        }
        if (isMoveWithHeader) {
            showGravity.dellHeaderMoving(moveDistance)
        }
        if (isMoveWithContent) {
            pullContentLayout!!.translationY = moveDistance.toFloat()
            // 回调移动了多少距离
            if (onMoveTargetViewListener != null) {
                onMoveTargetViewListener!!.onMoveDistance(moveDistance)
            }
        }
    }

    fun setOnMoveTargetViewListener(onMoveTargetViewListener: OnMoveTargetViewListener) {
        this.onMoveTargetViewListener = onMoveTargetViewListener
    }

    fun cancelAllAnimation() {
        cancelAnimation(overScrollAnimator)
        cancelAnimation(startRefreshAnimator)
        cancelAnimation(resetHeaderAnimator)
        cancelAnimation(startLoadMoreAnimator)
        cancelAnimation(resetFooterAnimator)
        removeDelayRunnable()
    }

    @JvmOverloads
    fun refreshComplete(flag: Boolean = true) {
        if (dellDetachComplete() && !isLoading) {
            isResetTrigger = true
            resetHeaderAnimationListener.isFlag = flag
            if (resetHeaderAnimator != null && resetHeaderAnimator!!.isRunning) {
                resetHeaderAnimationListener.onAnimationStart(null)
                return
            }
            resetHeaderView(moveDistance)
        }
    }

    @JvmOverloads
    fun loadMoreComplete(flag: Boolean = true) {
        if (dellDetachComplete() && !isRefreshing) {
            isResetTrigger = true
            resetFooterAnimationListener.isFlag = flag
            if (resetFooterAnimator != null && resetFooterAnimator!!.isRunning) {
                resetFooterAnimationListener.onAnimationStart(null)
                return
            }
            resetFootView(moveDistance)
        }
    }

    fun autoLoading(toLoadDistance: Int) {
        autoLoading(true, toLoadDistance)
    }

    @JvmOverloads
    fun autoLoading(withAction: Boolean = true, toLoadDistance: Int = -1) {
        if (!isLoadMoreEnable || isHoldingTrigger) {
            return
        }
        startLoadMore(moveDistance, toLoadDistance, withAction)
    }

    fun autoRefresh(toRefreshDistance: Int) {
        autoRefresh(true, toRefreshDistance)
    }

    @JvmOverloads
    fun autoRefresh(withAction: Boolean = true, toRefreshDistance: Int = -1) {
        if (isRefreshEnable && !isLoading && headerView != null) {
            cancelAllAnimation()
            resetState()
            startRefresh(moveDistance, toRefreshDistance, withAction)
        }
    }

    fun requestPullDisallowInterceptTouchEvent(b: Boolean) {
        generalPullHelper.isDisallowIntercept = b
        requestDisallowInterceptTouchEvent(b)
    }

    fun setHeaderView(header: View?) {
        if (headerView != null && headerView !== header) {
            removeView(headerView)
        }
        headerView = header
        if (header == null) {
            return
        }
        addView(getRefreshView(header))

        if (!isHeaderFront) {
            setViewFront(false, isFooterFront, null, footerView)
        }
    }

    fun setFooterView(footer: View?) {
        if (footerView != null && footerView !== footer) {
            removeView(footerView)
        }
        footerView = footer
        if (footer == null) {
            return
        }
        addView(getRefreshView(footer))

        if (!isFooterFront) {
            setViewFront(false, isHeaderFront, null, headerView)
        }
    }

    fun setTargetView(targetView: View) {
        this.targetView = targetView
        cancelTouchEvent()
        dellNestedScrollCheck()
        if (targetView is RecyclerView && (isTwinkEnable || isAutoLoadingEnable)) {
            if (scrollInterpolator == null) {
                scrollInterpolator = recyclerDefaultInterpolator
                scroller = OverScroller(context, scrollInterpolator)
            }
        }
    }

    fun setOnRefreshListener(onRefreshListener: OnRefreshListener) {
        this.onRefreshListener = onRefreshListener
    }

    fun setOnTargetScrollCheckListener(
            onTargetScrollCheckListener: OnTargetScrollCheckListener) {
        this.onTargetScrollCheckListener = onTargetScrollCheckListener
    }

    fun setScrollInterpolator(interpolator: Interpolator) {
        this.scrollInterpolator = interpolator
        scroller = OverScroller(context, scrollInterpolator)
    }

    fun setOverScrollAdjustValue(overScrollAdjustValue: Float) {
        this.overScrollAdjustValue = overScrollAdjustValue
    }

    fun setTopOverScrollMaxTriggerOffset(topOverScrollMaxTriggerOffset: Int) {
        this.topOverScrollMaxTriggerOffset = topOverScrollMaxTriggerOffset
    }

    fun setBottomOverScrollMaxTriggerOffset(bottomOverScrollMaxTriggerOffset: Int) {
        this.bottomOverScrollMaxTriggerOffset = bottomOverScrollMaxTriggerOffset
    }

    fun setOverScrollMinDuring(overScrollMinDuring: Int) {
        this.overScrollMinDuring = overScrollMinDuring
    }

    fun setOverScrollDampingRatio(overScrollDampingRatio: Float) {
        this.overScrollDampingRatio = overScrollDampingRatio
    }

    fun setRefreshAnimationDuring(refreshAnimationDuring: Int) {
        this.refreshAnimationDuring = refreshAnimationDuring
    }

    fun setResetAnimationDuring(resetAnimationDuring: Int) {
        this.resetAnimationDuring = resetAnimationDuring
    }

    fun setDragDampingRatio(dragDampingRatio: Float) {
        this.dragDampingRatio = dragDampingRatio
    }

    fun setRefreshShowGravity(@ShowGravity.ShowState headerShowGravity: Int, @ShowGravity.ShowState footerShowGravity: Int) {
        setHeaderShowGravity(headerShowGravity)
        setFooterShowGravity(footerShowGravity)
    }

    fun setHeaderShowGravity(@ShowGravity.ShowState headerShowGravity: Int) {
        showGravity.headerShowGravity = headerShowGravity
        requestLayout()
    }

    fun setFooterShowGravity(@ShowGravity.ShowState footerShowGravity: Int) {
        showGravity.footerShowGravity = footerShowGravity
        requestLayout()
    }

    fun setHeaderFront(headerFront: Boolean) {
        if (isHeaderFront != headerFront) {
            isHeaderFront = headerFront
            setViewFront(isHeaderFront, isFooterFront, headerView, footerView)
        }
    }

    fun setFooterFront(footerFront: Boolean) {
        if (isFooterFront != footerFront) {
            isFooterFront = footerFront
            setViewFront(isFooterFront, isHeaderFront, footerView, headerView)
        }
    }

    fun setMoveWithFooter(moveWithFooter: Boolean) {
        this.isMoveWithFooter = moveWithFooter
    }

    fun setMoveWithContent(moveWithContent: Boolean) {
        this.isMoveWithContent = moveWithContent
    }

    fun setMoveWithHeader(moveWithHeader: Boolean) {
        this.isMoveWithHeader = moveWithHeader
    }

    fun cancelTouchEvent() {
        if (generalPullHelper.dragState != 0) {
            super.dispatchTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(), MotionEvent.ACTION_CANCEL, 0f, 0f, 0))
        }
    }

    fun setDispatchTouchAble(dispatchTouchAble: Boolean) {
        this.dispatchTouchAble = dispatchTouchAble
    }

    fun setDispatchPullTouchAble(dispatchPullTouchAble: Boolean) {
        this.dispatchPullTouchAble = dispatchPullTouchAble
    }

    fun setDispatchChildrenEventAble(dispatchChildrenEventAble: Boolean) {
        this.dispatchChildrenEventAble = dispatchChildrenEventAble
    }

    fun setAnimationMainInterpolator(animationMainInterpolator: Interpolator) {
        this.animationMainInterpolator = animationMainInterpolator
    }

    fun setAnimationOverScrollInterpolator(animationOverScrollInterpolator: Interpolator) {
        this.animationOverScrollInterpolator = animationOverScrollInterpolator
    }

    fun <T : View> getHeaderView(): T {
        return headerView as T
    }

    fun <T : View> getFooterView(): T {
        return footerView as T
    }

    fun <T : View> getTargetView(): T {
        return targetView as T
    }
}