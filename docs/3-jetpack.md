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

## WorkManager

Android的后台机制是一个很复杂的话题，在很早之前，Android系统的后台功能是非常开放的，Service的优先级也很高，仅次于Activity，那个时候可以在Service中做很多事情。但由于后台功能太过于开放，每个应用都想无限地占用后台资源，导致手机的内存越来越紧张，耗电越来越快，也变得越来越卡。为了解决这些情况，基本上Android系统每发布一个新版本，后台权限都会被进一步收紧。
我印象中与后台相关的API变更大概有这些：从4.4系统开始AlarmManager的触发时间由原来的精准变为不精准，5.0系统中加入了JobScheduler来处理后台任务，6.0系统中引入了Doze和App Standby模式用于降低手机被后台唤醒的频率，从8.0系统开始直接禁用了Service的后台功能，只允许使用前台Service。当然，还有许许多多小细节的修改。这么频繁的功能和API变更，让开发者就很难受了，到底该如何编写后台代码才能保证应用程序在不同系统版本上的兼容性呢？为了解决这个问题，Google推出了WorkManager组件。WorkManager很适合用于处理一些要求定时执行的任务，它可以根据操作系统的版本自动选择底层是使用AlarmManager实现还是JobScheduler实现，从而降低了我们的使用成本。另外，它还支持周期性任务、链式任务处理等功能，是一个非常强大的工具。
不过，我们还得先明确一件事情：WorkManager和Service并不相同，也没有直接的联系。Service是Android系统的四大组件之一，它在没有被销毁的情况下是一直保持在后台运行的。而WorkManager只是一个处理定时任务的工具，它可以保证即使在应用退出甚至手机重启的情况下，之前注册的任务仍然将会得到执行，因此WorkManager很适合用于执行一些定期和服务器进行交互的任务，比如周期性地同步数据，等等。
另外，使用WorkManager注册的周期性任务不能保证一定会准时执行，这并不是bug，而是系统为了减少电量消耗，可能会将触发时间临近的几个任务放在一起执行，这样可以大幅度地减少CPU被唤醒的次数，从而有效延长电池的使用时间。

### WorkManager的基本用法
要想使用WorkManager，需要先在app/build.gradle文件中添加如下的依赖：
```
dependencies {
 ...
 implementation "androidx.work:work-runtime:2.2.0"
}
```
WorkManager的基本用法其实非常简单，主要分为以下3步：
1. 定义一个后台任务，并实现具体的任务逻辑；
2. 配置该后台任务的运行条件和约束信息，并构建后台任务请求；
3. 将该后台任务请求传入WorkManager的enqueue()方法中，系统会在合适的时间运行。

第一步要定义一个后台任务，这里创建一个SimpleWorker类，代码如下所示：
```
class SimpleWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("SimpleWorker", "do work in SimpleWorker")
        return Result.success()
    }

}
```
后台任务的写法非常固定，也很好理解。首先每一个后台任务都必须继承自Worker类，并调用它唯一的构造函数。然后重写父类中的doWork()方法，在这个方法中编写具体的后台任务逻辑即可。
doWork()方法不会运行在主线程当中，因此你可以放心地在这里执行耗时逻辑，不过这里简单起见只是打印了一行日志。另外，doWork()方法要求返回一个Result对象，用于表示任务的运行结果，成功就返回Result.success()，失败就返回Result.failure()。除此之外，还有一个Result.retry()方法，它其实也代表着失败，只是可以结合WorkRequest.Builder的setBackoffCriteria()方法来重新执行任务

第二步，配置该后台任务的运行条件和约束信息。
这一步其实也是最复杂的一步，因为可配置的内容非常多，不过目前我们还只是学习WorkManager的基本用法，因此只进行最基本的配置就可以了，代码如下所示：
```
val request = OneTimeWorkRequest.Builder(SimpleWorker::class.java).build()
```
可以看到，只需要把刚才创建的后台任务所对应的Class对象传入OneTimeWorkRequest.Builder的构造函数中，然后调用build()方法即可完成构建。OneTimeWorkRequest.Builder是WorkRequest.Builder的子类，用于构建单次运行的后台任务请求。WorkRequest.Builder还有另外一个子类PeriodicWorkRequest.Builder，可用于构建周期性运行的后台任务请求，但是为了降低设备性能消耗，PeriodicWorkRequest.Builder构造函数中传入的运行周期间隔不能短于15分钟，示例代码如下：
```
val request = PeriodicWorkRequest.Builder(SimpleWorker::class.java, 15,TimeUnit.MINUTES).build()
```
最后一步，将构建出的后台任务请求传入WorkManager的enqueue()方法中，系统就会在合
适的时间去运行了：
```
WorkManager.getInstance(context).enqueue(request)
```

### 使用WorkManager处理复杂的任务
让后台任务在指定的延迟时间后运行，只需要借助setInitialDelay()方法就可以了，代码如下所示：
```
val request = OneTimeWorkRequest.Builder(SimpleWorker::class.java)
                        .setInitialDelay(5, TimeUnit.MINUTES)
                        .build()
 ```
 说给后台任务请求添加标签：
 ```
 val request = OneTimeWorkRequest.Builder(SimpleWorker::class.java)
 ...
 .addTag("simple")
 .build()
 ```
那么添加了标签有什么好处呢？最主要的一个功能就是我们可以通过标签来取消后台任务请求：
```
WorkManager.getInstance(this).cancelAllWorkByTag("simple")
```
当然，即使没有标签，也可以通过id来取消后台任务请求：
```
WorkManager.getInstance(this).cancelWorkById(request.id)
```
但是，使用id只能取消单个后台任务请求，而使用标签的话，则可以将同一标签名的所有后台任务请求全部取消，这个功能在逻辑复杂的场景下尤其有用。
除此之外，我们也可以使用如下代码来一次性取消所有后台任务请求：
```
WorkManager.getInstance(this).cancelAllWork()
```
如果后台任务的doWork()方法中返回了Result.retry()，那么是可以结合setBackoffCriteria()方法来重新执行任务的
```
val request = OneTimeWorkRequest.Builder(SimpleWorker::class.java)
 ...
 .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
 .build()
```
setBackoffCriteria()方法接收3个参数：第二个和第三个参数用于指定在多久之后重新执行任务，时间最短不能少于10秒钟；第一个参数则用于指定如果任务再次执行失败，下次重试的时间应该以什么样的形式延迟。这其实很好理解，假如任务一直执行失败，不断地重新执行似乎并没有什么意义，只会徒增设备的性能消耗。而随着失败次数的增多，下次重试的时间也应该进行适当的延迟，这才是更加合理的机制。第一个参数的可选值有两种，分别是LINEAR和EXPONENTIAL，前者代表下次重试时间以线性的方式延迟，后者代表下次重试时间以指数的方式延迟。
Result.success()和Result.failure()又有什么作用？这两个返回值其实就是用于通知任务运行结果的，我们可以使用如下代码对后台任务的运行结果进行监听：
```
WorkManager.getInstance(this)
    .getWorkInfoByIdLiveData(request.id)
    .observe(this) { workInfo ->
        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
            Log.d("MainActivity", "do work succeeded")
        } else if (workInfo.state == WorkInfo.State.FAILED) {
            Log.d("MainActivity", "do work failed")
        }
 }
 ```
这里调用了getWorkInfoByIdLiveData()方法，并传入后台任务请求的id，会返回一个LiveData对象。然后我们就可以调用LiveData对象的observe()方法来观察数据变化了，以此监听后台任务的运行结果。另外，你也可以调用getWorkInfosByTagLiveData()方法。

假设这里定义了3个独立的后台任务：同步数据、压缩数据和上传数据。现在我们想要实现先同步、再压缩、最后上传的功能，就可以借助链式任务来实现，代码示例如下：
```
val sync = ...
val compress = ...
val upload = ...
WorkManager.getInstance(this)
 .beginWith(sync)
 .then(compress)
 .then(upload)
 .enqueue()
```
这段代码还是比较好理解的，相信你一看就能懂。beginWith()方法用于开启一个链式任务，至于后面要接上什么样的后台任务，只需要使用then()方法来连接即可。另外WorkManager还要求，必须在前一个后台任务运行成功之后，下一个后台任务才会运行。也就是说，如果某个后台任务运行失败，或者被取消了，那么接下来的后台任务就都得不到运行了。
前面所介绍的WorkManager的所有功能，在国产手机上都有可能得不到正确的运行。这是因为绝大多数的国产手机厂商在进行Android系统定制的时候会增加一个一键关闭的功能，允许用户一键杀死所有非白名单的应用程序。而被杀死的应用程序既无法接收广播，也无法运行WorkManager的后台任务。这个功能虽然与Android原生系统的设计理念并不相符，但是我们也没有什么解决办法。或许就是因为有太多恶意应用总是想要无限占用后台，国产手机厂商才增加了这个功能吧。因此，这里给你的建议就是，WorkManager可以用，但是千万别依赖它去实现什么核心功能，因为它在国产手机上可能会非常不稳定。