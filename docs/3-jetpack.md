# JetPack

## LiveData

### LiveData的使用

注册观察者： LiveData 支持两种注册观察者的方式：

- LiveData#observe(LifecycleOwner, Observer) 带生命周期感知的注册： 更常用的注册方式，这种方式能够获得 LiveData 自动取消订阅和安全地回调数据的特性；
- LiveData#observeForever(Observer) 永久注册： LiveData 会一直持有观察者的引用，只要数据更新就会回调，因此这种方式必须在合适的时机手动移除观察者。

设置数据： LiveData 设置数据需要利用子类 MutableLiveData 提供的接口：setValue() 为同步设置数据，postValue() 为异步设置数据，内部将 post 到主线程再修改数据。


### LiveData的局限

1. LiveData 只能在主线程更新数据： 只能在主线程 setValue，即使 postValue 内部也是切换到主线程执行；
1. LiveData 数据重放问题： 注册新的订阅者，会重新收到 LiveData 存储的数据，这在有些情况下不符合预期；
1. LiveData 不防抖问题： 重复 setValue 相同的值，订阅者会收到多次 onChanged() 回调（可以使用 distinctUntilChanged() 优化）；
1. LiveData 丢失数据问题： 在数据生产速度 > 数据消费速度时，LiveData 无法观察者能够接收到全部数据。比如在子线程大量 postValue 数据但主线程消费跟不上时，中间就会有一部分数据被忽略。

### LiveData的原理

#### 注册观察者的执行过程
LiveData 支持使用 observe() 或 observeForever() 两种方式注册观察者，其内部会分别包装为 2 种包装对象：

1、observe()： 将观察者包装为 LifecycleBoundObserver 对象，它是 Lifecycle 框架中 LifecycleEventObserver 的实现类，因此它可以绑定到宿主（参数 owner）的生命周期上，这是实现 LiveData 自动取消订阅和安全地回调数据的关键；
2、observeForever()： 将观察者包装为 AlwaysActiveObserver，不会关联宿主生命周期，当然你也可以理解为全局生命周期。
注意： LiveData 内部会禁止一个观察者同时使用 observe() 和 observeForever() 两种注册方式。但同一个 LiveData 可以接收 observe() 和 observeForever() 两种观察者。
LiveData.java
```
private SafeIterableMap<Observer<? super T>, ObserverWrapper> mObservers = new SafeIterableMap<>();

// 注册方式 1：带生命周期感知的注册方式
@MainThread
public void observe(LifecycleOwner owner, Observer<? super T> observer) {
    // 1.1 主线程检查
    assertMainThread("observe");
    // 1.2 宿主生命周期状态是 DESTROY，则跳过
    if (owner.getLifecycle().getCurrentState() == DESTROYED) {
        return;
    }
    // 1.3 将 Observer 包装为 LifecycleBoundObserver
    LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
    ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
    // 1.4 禁止将 Observer 绑定到不同的宿主上
    if (existing != null && !existing.isAttachedTo(owner)) {
        throw new IllegalArgumentException("Cannot add the same observer with different lifecycles");
    }
    if (existing != null) {
        return;
    }
    // 1.5 将包装类注册到宿主声明周期上
    owner.getLifecycle().addObserver(wrapper);
}

// 注册方式 2：永久注册的方式
@MainThread
public void observeForever(Observer<? super T> observer) {
    // 2.1 主线程检查
    assertMainThread("observeForever");
    // 2.2 将 Observer 包装为 AlwaysActiveObserver
    AlwaysActiveObserver wrapper = new AlwaysActiveObserver(observer);
    // 2.3 禁止将 Observer 注册到生命周期宿主后又进行永久注册
    ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
    if (existing instanceof LiveData.LifecycleBoundObserver) {
        throw new IllegalArgumentException("Cannot add the same observer with different lifecycles");
    }
    if (existing != null) {
        return;
    }
    // 2.4 分发最新数据
    wrapper.activeStateChanged(true);
}

// 注销观察者
@MainThread
public void removeObserver(@NonNull final Observer<? super T> observer) {
    // 主线程检查
    assertMainThread("removeObserver");
    // 移除
    ObserverWrapper removed = mObservers.remove(observer);
    if (removed == null) {
        return;
    }
    // removed.detachObserver() 方法：
    // LifecycleBoundObserver 最终会调用 Lifecycle#removeObserver()
    // AlwaysActiveObserver 为空实现
    removed.detachObserver();
    removed.activeStateChanged(false);
}
```

#### 生命周期感知源码分析

LifecycleBoundObserver 是 LifecycleEventObserver 的实现类，当宿主生命周期变化时，会回调其中的 LifecycleEventObserve#onStateChanged() 方法：
LiveData$ObserverWrapper.java
```
private abstract class ObserverWrapper {
    final Observer<? super T> mObserver;
    boolean mActive;
    // 观察者持有的版本号
    int mLastVersion = START_VERSION; // -1

    ObserverWrapper(Observer<? super T> observer) {
        mObserver = observer;
    }

    abstract boolean shouldBeActive();

    boolean isAttachedTo(LifecycleOwner owner) {
        return false;
    }

    void detachObserver() {
    }

    void activeStateChanged(boolean newActive) {
        // 同步宿主的生命状态
        if (newActive == mActive) {
            return;
        }
        mActive = newActive;
        changeActiveCounter(mActive ? 1 : -1);
        // STARTED 状态以上才会尝试分发数据
        if (mActive) {
            dispatchingValue(this);
        }
    }
}
```
Livedata$LifecycleBoundObserver.java
```// 注册方式：observe()
class LifecycleBoundObserver extends ObserverWrapper implements LifecycleEventObserver {
    @NonNull
    final LifecycleOwner mOwner;

    LifecycleBoundObserver(@NonNull LifecycleOwner owner, Observer<? super T> observer) {
        super(observer);
        mOwner = owner;
    }

    // 宿主的生命周期大于等于可见状态（STARTED），认为活动状态
    @Override
    boolean shouldBeActive() {
        return mOwner.getLifecycle().getCurrentState().isAtLeast(STARTED);
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        Lifecycle.State currentState = mOwner.getLifecycle().getCurrentState();
        // 宿主生命周期进入 DESTROYED 时，会移除观察者
        if (currentState == DESTROYED) {
            removeObserver(mObserver);
            return;
        }
        Lifecycle.State prevState = null;
        while (prevState != currentState) {
            prevState = currentState;
            // 宿主从非可见状态转为可见状态（STARTED）时，会尝试触发数据分发
            activeStateChanged(shouldBeActive());
            currentState = mOwner.getLifecycle().getCurrentState();
        }
    }

    @Override
    boolean isAttachedTo(LifecycleOwner owner) {
        return mOwner == owner;
    }

    @Override
    void detachObserver() {
        mOwner.getLifecycle().removeObserver(this);
    }
}
```

AlwaysActiveObserver.java
```
// 注册方式：observeForever()
private class AlwaysActiveObserver extends ObserverWrapper {

    AlwaysActiveObserver(Observer<? super T> observer) {
        super(observer);
    }

    @Override
    boolean shouldBeActive() {
        return true;
    }
}
```

#### 同步设置数据的执行过程
LiveData 使用 setValue() 方法进行同步设置数据（必须在主线程调用），需要注意的是，设置数据后并不一定会回调 Observer#onChanged() 分发数据，而是需要同时 2 个条件：

条件 1： 观察者绑定的生命周期处于活跃状态；
observeForever() 观察者：一直处于活跃状态；
observe() 观察者：owner 宿主生命周期处于活跃状态。
条件 2： 观察者的持有的版本号小于 LiveData 的版本号时。
LiveData.java
```
// LiveData 持有的版本号
private int mVersion;

// 异步设置数据 postValue() 最终也是调用到 setValue()
@MainThread
protected void setValue(T value) {
    // 主线程检查
    assertMainThread("setValue");
    // 版本号加一
    mVersion++;
    mData = value;
    // 数据分发
    dispatchingValue(null);
}

// 数据分发
void dispatchingValue(ObserverWrapper initiator) {
    // 这里的标记位和嵌套循环是为了处理在 Observer#onChanged() 中继续调用 setValue()，
    // 而产生的递归设置数据的情况，此时会中断旧数据的分发，转而分发新数据，这是丢失数据的第 2 种情况。
    if (mDispatchingValue) {
        mDispatchInvalidated = true;
        return;
    }
    mDispatchingValue = true;
    do {
        mDispatchInvalidated = false;
        if (initiator != null) {
            // onStateChanged() 走这个分支，只需要处理单个观察者
            considerNotify(initiator);
            initiator = null;
        } else {
            // setValue() 走这个分支，需要遍历所有观察者
            for (Iterator<Map.Entry<Observer<? super T>, ObserverWrapper>> iterator = mObservers.iteratorWithAdditions(); iterator.hasNext(); ) {
                considerNotify(iterator.next().getValue());
                if (mDispatchInvalidated) {
                    break;
                }
            }
        }
    } while (mDispatchInvalidated);
    mDispatchingValue = false;
}

// 尝试触发回调，只有观察者持有的版本号小于 LiveData 持有版本号，才会分发回调
private void considerNotify(ObserverWrapper observer) {
    // STARTED 状态以上才会尝试分发数据
    if (!observer.mActive) {
        return;
    }
    if (!observer.shouldBeActive()) {
        observer.activeStateChanged(false);
        return;
    }
    // 版本对比
    if (observer.mLastVersion >= mVersion) {
        return;
    }
    observer.mLastVersion = mVersion;
    // 分发回调
    observer.mObserver.onChanged((T) mData);
}
```

总结一下回调 Observer#onChanged() 的情况：

1、注册观察者时，观察者绑定的生命处于活跃状态，并且 LiveData 存在已设置的旧数据；
2、调用 setValue() / postValue() 设置数据时，观察者绑定的生命周期处于活跃状态；
3、观察者绑定的生命周期由非活跃状态转为活跃状态，并且 LiveData 存在未分发到该观察者的数据（即观察者持有的版本号小于 LiveData 持有的版本号）；

提示： observeForever() 虽然没有直接绑定生命周期宿主，但可以理解为绑定的生命周期是全局的，因此在移除观察者之前都是活跃状态。

#### 异步设置数据的执行过程
LiveData 使用 postValue() 方法进行异步设置数据（允许在子线程调用），内部会通过一个临时变量 mPendingData 存储数据，再通过 Handler 将切换到主线程并调用 setValue(临时变量)。因此，当在子线程连续 postValue() 时，可能会出现中间的部分数据不会被观察者接收到。
LiveData.java
```
final Object mDataLock = new Object();

static final Object NOT_SET = new Object();

// 临时变量
volatile Object mPendingData = NOT_SET;

private final Runnable mPostValueRunnable = new Runnable() {
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Object newValue;
        synchronized (mDataLock) {
            newValue = mPendingData;
            // 重置临时变量
            mPendingData = NOT_SET;
        }
        // 真正修改数据的地方，也是统一到 setValue() 设置数据
        setValue((T) newValue);
    }
};

protected void postValue(T value) {
    boolean postTask;
    synchronized (mDataLock) {
        // 临时变量被重置时，才会发送修改的 Message，这是出现背压的第 1 种情况
        postTask = mPendingData == NOT_SET;
        mPendingData = value;
    }
    if (!postTask) {
        return;
    }
    ArchTaskExecutor.getInstance().postToMainThread(mPostValueRunnable);
}
```

总结一下 LiveData 可能丢失数据的场景，此时观察者可能不会接收到所有的数据：

情况 1（背压问题）： 使用 postValue() 异步设置数据，并且观察者的消费速度小于数据生产速度；
情况 2： 在观察者处理回调（Observer#obChanged()）的过程中重新设置新数据，此时会中断旧数据的分发，部分观察者将无法接收到旧数据；
情况 3： 观察者绑定的生命周期处于非活跃状态时，连续使用 setValue() / postValue() 设置数据时，观察将无法接收到中间的数据。

####  LiveData 数据重放原因分析
LiveData 的数据重放问题也叫作数据倒灌、粘性事件，核心源码在 LiveData#considerNotify(Observer) 中：

首先，LiveData 和观察者各自会持有一个版本号 version，每次 LiveData#setValue 或 postValue 后，LiveData 持有的版本号会自增 1。在 LiveData#considerNotify(Observer) 尝试分发数据时，会判断观察者持有版本号是否小于 LiveData 的版本号（Observer#mLastVersion >= LiveData#mVersion 是否成立），如果成立则说明这个观察者还没有消费最新的数据版本。
而观察者的持有的初始版本号是 -1，因此当注册新观察者并且正好宿主的生命周期是大于等于可见状态（STARTED）时，就会尝试分发数据，这就是数据重放。

为什么 Google 要把 LiveData 设计为粘性呢？LiveData 重放问题需要区分场景来看 —— 状态适合重放，而事件不适合重放：

当 LiveData 作为一个状态使用时，在注册新观察者时重放已有状态是合理的；
当 LiveData 作为一个事件使用时，在注册新观察者时重放已经分发过的事件就是不合理的。