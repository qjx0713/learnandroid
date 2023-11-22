# View相关

## View基础知识

### View的位置参数

View 的位置主要由它的四个顶点来决定,分别对应于 View 的四个属性:top，left，right，bottom，其中top是左上角纵标，left 是左上角横标，right 是右下角横标，bottom是右下角纵坐标。需要注意的是，这些坐标都是相对于 View 的父容器来说的，因此它是一种相对坐标。

    width = right-left
    height = bottom - top

在View的源码中它们对应于mLeft、mRight、mTop和mBottom这四个成员变量，获取方式如下所示

    Left = getLeft();
    Right=getRight();
    Top= getTop();
    Bottom =getBottom()

从Android3.0开始，View 增加了额外的几个参数:x、y、translationX和translationY其中x和y是View左上角的坐标，而translationX和translationY 是 View左上角相对于父容器的偏移量。这几个参数也是相对于父容器的坐标，并且translationX和translationY的默认值是0，和 View 的四个基本的位置参数一样，View 也为它们提供了 get/set 方法，这几个参数的换算关系如下所示。

    x = left + translationX
    y = top + translationY

需要注意的是，View 在平移的过程中，top 和 left 表示的是原始左上角的位置信息其值并不会发生改变，此时发生改变的是x、y、translationX和translationY 这四个参数

### 基础概念

#### MotionEvent

在手指接触屏幕后所产生的一系列事件中，典型的事件类型有如下几种

- ACTION_DOWN--手指刚接触屏幕:
- ACTION_MOVE-手指在屏幕上移动:
- ACTION_UP-手机从屏幕上松开的一瞬间

正常情况下，一次手指触摸屏幕的行为会触发一系列点击事件，考虑如下几种情况:

点击屏幕后离开松开，事件序列为 DOWN->UP;

点击屏幕滑动一会再松开，事件序列为 DOWN->MOVE->...MOVE->UP

上述三种情况是典型的事件序列，同时通过 MotionEvent 对象我们可以得到点击事件发生的x和y坐标。为此，系统提供了两组方法:getX/getY 和getRawX/getRawY。它们的区别其实很简单，getX/getY 返回的是相对于当前 Vew 左上角的x和y标，而getRawX/getRawY返回的是相对于手机屏幕左上角的x和y坐标。

#### TouchSlop 
 
TouchSlop是系统所能识别出的被认为是滑动的最小距离，换句话说，当手指在屏幕上滑动时，如果两次滑动之间的距离小于这个常量，那么系统就不认为你是在进行滑动操作。原因很简单:滑动的距离太短，系统不认为它是滑动。这是一个常量，和设备有关，在不同设备上这个值可能是不同的，通过如下方式即可获取这个常量:ViewConfiguration.get(getContext()).getScaledTouchSlop()。当我们在处理滑动时，可以利用这个常量来做一些过滤，比如当两次滑动事件的滑动距离小于这个值，我们就可以认为未达到滑动距离的临界值，因此就可以认为它们不是滑动，这样做可以有更好的用户体验。

#### VelocityTracker

速度追踪，用于追踪手指在滑动过程中的速度，包括水平和竖直方向的速度。它的使用过程很简单，首先，在View的onTouchEvent 方法中追踪当前单击事件的速度:
    
    VelocityTracker velocityTracker = VelocityTracker.obtain();
    velocityTracker.addMovement(event);

接着，当我们先知道当前的滑动速度时，这个时候可以采用如下方式来获得当前的速度:

    velocityTracker.computeCurrentVelocity(1000);
    int xVelocity=(int)velocityTracker.getXVelocity()
    int yVelocity=(int) velocityTracker.getYVelocity();

在这一步中有两点需要注意，第一点，获取速度之前必须先计算速度，即getXVelocity和getYVelocity这两个方法的前面必须要调用computeCurrentVelocity 方法;第二点速度可以为负数，手指逆着坐标系的正方向滑动，所产生的速度就为负值。另外，computeCurrentVelocity 这个方法的参数表示的是一个时间单元或者说时间间隔，它的单位是毫秒(ms，计算速度时得到的速度就是在这个时间间隔内手指在水平或竖直方向上所滑动的像素数。针对上面的例子，如果我们通过velocityTracker.computeCurrentVelocity(100)来获取速度，那么得到的速度就是手指在100ms内所滑过的像素数，因此水平速度就成了 10像素/每100ms(这里假设滑动过程是匀速的)，最后，当不需要使用它的时候，需要调用 clear 方法来重置并回收内存:

    velocityTracker.clear();
    velocityTracker.recycle();



#### GestureDetector

手势检测，用于辅助检测用户的单击、滑动、长按、双击等行为。要使用 GestureDetector也不复杂，参考如下过程。
首先，需要创建一个GestureDetector对象并实现OnGestureListener 接口，根据需要我们还可以实现OnDoubleTapListener 从而能够监听双击行为:

    GestureDetector mGestureDetector = new GestureDetector(this);
	//解决长按屏幕后无法拖动的现象
    mGestureDetector.setIsLongpressEnabled(false);

接着，接管目标 View 的onTouchEvent 方法，在待监听 View 的onTouchEvent 方法中添加如下实现:

    boolean consume= mGestureDetector.onTouchEvent(event);
    return consume;

OnGestureListener中，比较常用的有:onSingleTapUp(单击)、 onFling(快速滑动)、onScroll(拖动)、onLongPress(长按)和onDoubleTap (双击)。另外这里要说明的是，实际开发中，可以不使用GestureDetector，完全可以自己在 View的onTouchEvent 方法中实现所需的监听，这个就看个人的喜好了。这里有一个建议供读者参考:如果只是监听滑动相关的，建议自己在onTouchEvent中实现，如果要监听双击这种行为的话，那么就使用GestureDetector。

#### Scroller

弹性滑动对象用于实现View的弹性滑动。我们知道当使用 View的scrollTo/scrollBy方法来进行滑动时，其过程是瞬间完成的，这个没有过渡效果的滑动用户体验不好。这个时候就可以使用 Scroller 来实现有过渡效果的滑动，其过程不是瞬间完成的，而是在一定的时间间隔内完成的。Scroller 本身无法让 View 弹性滑动，它需要和 View的computeScroll方法配合使用才能共同完成这个功能。

    Scroller scroller =new Scroller(mContext);
    //缓慢滚动到指定位置
    private void smoothscrollTo(int destX，int destY){
	    int scrollX=getScrollX();
	    int delta=destx-scrollX;
	    //1000ms内滑向destx，效果就是慢慢滑动
	    mScroller.startscroll(scrollx，0，delta，0，1000);
		invalidate();
	}

    @Override
    public void computeScroll() {
    	if (mScroller.computescrollOffset()){
			scrollTo(mScroller.getCurrX()，mScroller.getCurrY());
			postInvalidate(); 
		}
	}


### View的滑动

通过三种方式可以实现View的滑动:第一种是通过View本身提供的scrollTo/scrollBy方法来实现滑动;第二种是通过动画给 View施加平移效果来实现滑动:第三种是通过改变View的 LayoutParams 使得 View 重新布局从而实现滑动。

#### scrollTo/scrollBy

在滑动过程中mScrollX 的值总是等于 View左边缘和View内容左边缘在水平方向的距离而mScrollY的值总是等于View上边缘和View内容上边缘在坚直方向的距离。View 边缘是指 Vew 的位置，由四个顶点组成，而 View 内容边缘是指View中的内容的边缘，scrollTo和scrollBy 只能改变View 内容的位置而不能改变View在布局中的位置。mScrollX和mScrollY 的单位为像素，mScrollX为正值，View 内容向左移动，mScrollX为负值，View 内容向右移动；
mScrolly为正值，View 内容向上移动，mScrollX为负值，View 内容向下移动。

#### 动画

View动画是对 View 的影像做操作，它并不能真正改变View的位置参数包括宽/高,并且如果希望动画后的状态得以保留还必须将fillAfter属性设置为 true，否则动画完成后其动画结果会消失。比如我们要把 View 向右移动 100像素，如果fllAfter 为 false，那么在动画完成的一刹那，View 会瞬间恢复到动画前的状态:如果fllAfter 为true，在动画完成后，View 停留在距原始位置 100 像素的右边。使用属性动画并不会存在上述问题，但是在 Android 3.0以下无法使用属性动画，这个时候我们可以使用动画兼容库 nineoldandroids 来实现属性动画，尽管如此，在Android 3.0以下的手机通过nineoldandroids来实现的属性动画本质上仍然是View动画。

#### LayoutParams
    
    MarginLayoutParams params = (MarginLayoutParams)mButtonl,getlayoutParams();
    params.width +=100;
    params.leftMargin +=100;
    mButton1 requestlayout();
    //或者mButton1.setLayoutParams(params);

## View事件分发

    public boolean dispatchTouchEvent(MotionEvent ev)

用来进行事件的分发。如果事件能够传递给当前 View，那么此方法一定会被调用，返回结果受当前 View的onTouchEvent 和下级 View的dispatchTouchEvent方法的影响，表示是否消耗当前事件。

    public boolean onInterceptTouchEvent(MotionEvent event)

在上述方法内部调用，用来判断是否拦截某个事件，如果当前 View 拦截了某个事件那么在同一个事件序列当中，此方法不会被再次调用，返回结果表示是否拦截当前事件。

    public boolean onTouchEvent(MotionEvent event)

在dispatchTouchEvent 方法中调用，用来处理点击事件，返回结果表示是否消耗当前事件，如果不消耗，则在同一个事件序列中，当前 View 无法再次接收到事件。

其实它们的关系可以用如下伪代码表示:

    public boolean dispatchTouchEvent(MotionEvent ev){
		boolean consume = false;
	    if (onInterceptTouchEvent(ev)) {
		    consume = onTouchEvent(ev);
		} else {
		    consume = child.dispatchTouchEvent(ev);
		}
	    return consume;
	}

对于一个根 ViewGroup 来说，点击事件产生后，首先会传递给它,这时它的 dispatchTouchEvent就会被调用,如果这个ViewGroup 的onInterceptTouchEvent方法返回true就表示它要拦截当前事件，接着事件就会交给这个 ViewGroup 处理，即它的onTouchEvent方法就会被调用;如果这个 ViewGroup 的onInterceptTouchEvent方法返回false就表示它不拦截当前事件，这时当前事件就会继续传递给它的子元素，接着子元素的dispatchTouchEvent 方法就会被调用，如此反复直到事件被最终处理。


当一个 View 需要处理事件时，如果它设置了 OnTouchListener，那么OnTouchListener中的onTouch 方法会被回调。这时事件如何处理还要看 onTouch 的返回值，如果返回false则当前 View的onTouchEvent 方法会被调用;如果返回 true，那么onTouchEvent 方法将不会被调用。由此可见，给 View 设置的 OnTouchListener，其优先级比onTouchEvent 要高。在onTouchEvent 方法中，如果当前设置的有 OnClickListener，那么它的 onClick 方法会被调用。可以看出，平时我们常用的 OnClickListener，其优先级最低，即处于事件传递的尾端。


当一个点击事件产生后，它的传递过程遵循如下顺序:Activity-> Window->View，即事件总是先传递给Activity，Activity再传递给 Window，最后 Window 再传递给顶级 View。顶级 View 接收到事件后，就会按照事件分发机制去分发事件。考虑一种情况，如果一个View的onTouchEvent返回false，那么它的父容器的onTouchEvent 将会被调用，依此类推如果所有的元素都不处理这个事件,那么这个事件将会最终传递给Activity处理，即Activity的onTouchEvent 方法会被调用。这个过程其实也很好理解，我们可以换一种思路，假如点击事件是一个难题，这个难题最终被上级领导分给了一个程序员去处理(这是事件分发过程)，结果这个程序员搞不定 (onTouchEvent 返回了 false)，现在该怎么办呢?难题必须要解决，那只能交给水平更高的上级解决(上级的onTouchEvent 被调用)，如果上级再搞不定，那只能交给上级的上级去解决，就这样将难题一层层地向上抛，这是公司内部一种很常见的处理问题的过程。从这个角度来看，View 的事件传递过程还是很贴近现实的，毕竞程序员也生活在现实中。

关于事件传递的机制，这里给出一些结论，根据这些结论可以更好地理解整个传递机制，如下所示。

(1)同一个事件序列是指从手指接触屏幕的那一刻起，到手指离开屏幕的那一刻结束，在这个过程中所产生的一系列事件，这个事件序列以 down 事件开始，中间含有数量不定的move 事件，最终以up 事件结束。

(2)正常情况下，一个事件序列只能被一个 View 拦截且消耗。这一条的原因可以参考(3)，因为一旦一个元素拦截了某此事件，那么同一个事件序列内的所有事件都会直接交给它处理，因此同一个事件序列中的事件不能分别由两个 View 同时处理，但是通过特殊手段可以做到，比如一个 View 将本该自已处理的事件通过onTouchEvent 强行传递给其他 View处理。

(3)某个 View 一旦决定拦截，那么这一个事件序列都只能由它来处理(如果事件序列能够传递给它的话)，并且它的 onInterceptTouchEvent 不会再被调用。这条也很好理解，就是说当一个 View 决定拦截一个事件后，那么系统会把同一个事件序列内的其他方法都直接交给它来处理，因此就不用再调用这个 View的onInterceptTouchEvent 去询问它是否要拦截了。

(4)某个 View 一旦开始处理事件,如果它不消耗ACTION_DOWN 事件(onTouchEvent返回了 false)，那么同一事件序列中的其他事件都不会再交给它来处理，并且事件将重新交由它的父元素去处理，即父元素的 onTouchEvent 会被调用。意思就是事件一旦交给一个View 处理，那么它就必须消耗掉，否则同一事件序列中剩下的事件就不再交给它来处理了这就好比上级交给程序员一件事，如果这件事没有处理好，短期内上级就不敢再把事情交给这个程序员做了，二者是类似的道理。

(5)如果 View 不消耗除 ACTION_DOWN 以外的其他事件那么这个点击事件会消失此时父元素的 onTouchEvent 并不会被调用，并且当前 View 可以持续收到后续的事件，最终这些消失的点击事件会传递给 Activity 处理。

(6)ViewGroup 默认不拦截任何事件。Android 源码中 ViewGroup 的 onInterceptTouch-Event方法默认返回false。

(7) View 没有 onInterceptTouchEvent 方法，一旦有点击事件传递给它，那么它的onTouchEvent 方法就会被调用。

(8)View的onTouchEvent默认都会消耗事件(返回true),除非它是不可点击的(clickable和longClickable 同时为 false)。View 的 longClickable 属性默认都为 false，clickable 属性要分情况,比如 Button 的clickable 属性默认为 true,而 TextView 的 clickable 属性默认为 false。

(9)View 的enable 属性不影响 onTouchEvent 的默认返回值。哪怕一个 View 是disable状态的，只要它的 clickable 或者 ongClickable 有一个为 true，那么它的 onTouchEvent 就返回true

(10) onClick 会发生的前提是当前 View 是可点击的，并且它收到了 down 和up 的事件。

(11)事件传递过程是由外向内的，即事件总是先传递给父元素，然后再由父元素分发给子 View，通过 requestDisallowInterceptTouchEvent 方法可以在子元素中干预父元素的事件分发过程，但是ACTION_DOWN 事件除外。

## View事件分发

![图片](./image/2-1.png)

### 外部拦截法

所谓外部拦截法是指点击事情都先经过父容器的拦截处理，如果父容器需要此事件就拦截，如果不需要此事件就不拦截，这样就可以解决滑动冲突的问题，这种方法比较符合点击事件的分发机制。外部拦截法需要重写父容器的方法，在内部做相应的拦截即可。

### 内部拦截法
内部拦截法是指父容器不拦截任何事件，所有的事件都传递给子元素，如果子元素需要此事件就直接消耗掉，否则就交由父容器进行处理，这种方法和 Android 中的事件分发机制不一致，需要配合 requestDisallowInterceptTouchEvent 方法才能正常工作，使用起来较外部拦截法稍显复杂。

除了子元素需要做处理以外，父元素也要默认拦截除了 ACTION_DOWN 以外的其他事件，这样当子元素调用 parent.requestDisallowInterceptTouchEvent(false)方法时，父元素才能继续拦截所需的事件。为什么父容器不能拦截ACTION_DOWN 事件呢?那是因为ACTION_DOWN事件并不受FLAG_DISALLOW_INTERCEPT这个标记位的控制，所以一旦父容器拦截ACTION DOWN事件，那么所有的事件都无法传递到子元素中去，这样内部拦截就无法起作用了。