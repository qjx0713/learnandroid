## 协程
### 协程的基本用法
1. 添加依赖
```
dependencies {
 ...
 implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1"
 implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1"
}
```

2. 开启协程
```
GlobalScope.launch {
}
```
可以在任意地方调用，每次创建的都是顶层协程
```
runBlocking {
}
```
可以在任意地方调用，会阻塞线程，只建议在测试环境下使用
```
launch {
}
```
只能在协程作用域中调用
```
coroutineScope {
}
```
可以在协程作用域或挂起函数中调用

在 Kotlin 当中创建协程主要有两种方式，分别是 launch 和 async 两个函数，launch 是较通用的一种方式，它这个理念更类似于是一种叫 fire and forgot 的方式，我创建了你，然后你就去运行吧，之后就不管你了，有点像创建线程的方式。然后 async 函数则不同了，async 函数和 launch 函数一样，也会创建一个协程，但是它会有一个 Deferred 类型的返回值。
```
val deferred = async {

}
deferred.await()
```
调用 async 函数代码会执行，之后可以调用 deferred.await 函数来获取函数执行的返回结果。async 会开启协程去执行代码块里的代码，同时代码块最后一行代码会作为返回值返回，可以调用 await 函数来去获取返回的返回值。如果调用 await 函数的时候，协程还没有运行完，调用 await 函数的协程就会被挂起，一直等到 async 函数执行结束之后， await 函数才会重新被恢复。

主要可以通过这两个函数可以创建一个协程，但是这两个函数都不可以直接调用，而是要在协程作用域当中才能调用，同时在挂起函数中也不可以调用，所以还有一个核心的问题要解决，就是如何创建一个协程作用域，创建协程作用域的方式有很多种，但是这里不推荐其他那些使用方式，因为建议的方式就只有一种：CoroutineScop。除了这个函数之外，如果你比较了解协程，还会知道有 viewModelScop 的或者 lifecycleScop 这些方式，也可以创建协程作用域，这些方式是推荐使用的，但是他们都是在 CoroutineScop的基础上做的封装。
```
val scope = CoroutineScope(Dispatchers.Main + Job())
```
CoroutineScope 函数的参数列表中有一个叫 CoroutineContext 的参数，可以将 CoroutineContext 简单理解成是一种 Set 集合，又因为 CoroutineContext 里面重载了加号运算符，所以多个 CoroutineContext 元素之间可以使用加号来连接。
 
上面的示例代码中，Dispatchers.Main 和 Job 其实都是 CoroutineContext 对象。

#### Dispatcher
Dispatcher 用于告知协程，应该在哪个线程当中去运行。Dispatchers.Main 就是在 Android 的主线程当中运行，除了 Main 之外，我们还可以指定：
- Dispatchers.Default 开启低并发的子线程，去执行一些计算密集型的操作
- Dispatchers.IO 开启高并发的子线程，然后去进行一些阻塞密集型操作

#### Job
Job 是作为协程身份唯一标识的存在，每一个协程内部都会有一个唯一的标识。通过 Job 对可以控制协程的生命周期，比如：
- 可以判断协程是否正在运行
- 可以判断协程是否已经被取消
- 可以判断协程是否运行结束
  
下面的示例，定义一个 getUserFromDb 函数，从数据库中获取数据。由于数据库的读取操作也是一个耗时操作，所以需要将它放到一个子线程当中去运行，否则它会阻塞当前的主线程，影响到界面的交互。
```
suspend fun getUserFromDb(): User {
    return withContext(Dispatchers.IO)
     {
        val user = // load user from db
        user
    }
}

suspend fun getAndShowUser() {
  val user = getUserFromDb()
  showUser(user)
}
```
这里借助了 withContext 函数来实现， withContext 也是协程当中内置的一个非常常用的函数。在 withContext 参数当中指定了 Dispatchers.IO，withContext 函数代码块当中的代码就都会在子线程当中运行，代码块中就可以去执行任意的耗时操作，然后当 withContext 运行结束之后，它的最后一行代码会作为返回值返回，然后拿到返回的 User 对象，并且直接去 return withContext 函数，这样 getUserFromDB 函数就可以获取到从数据库读取的 User 对象。

### 协程更多的特性
#### 协程是结构化的
上面有提到 launch 函数是需要在协程的作用域当中才能调用，同时 launch 函数它又会创建一个协程的作用域，那是不是意味着可以在 launch 当中继续调用 launch，然后再继续调用 launch 函数呢？
```
val scope = CoroutineScope(Dispatchers.Main + Job())
scope.launch {
    launch {
        launch {
        }
    }
    launch {
    }
}
```
上面这段示例代码是合法的，协程支持嵌套调用，但是协程的嵌套调用和线程又不一样，协程的嵌套调用是有父子结构的，也就是说这里顶层的 scope.launch 函数首先创建了一个协程，然后在它的协程下面又创建了两个子协程，这两个子协程是有父协程概念的。在第一个子协程当中又创建了一个子协程，这是它的父协程，就是刚才创建的第一个子协程。而反观线程，他们之间是没有任何父子关系的，不管在线程当中开启了多少个线程，都是一个个独立的子线程，跟创建它的外层线程之间没有任何的关联。
 
协程的这种特性又被称作为结构化并发，这种结构化并发的特性让整个协程变得非常利于管理。
 
比如现在有一个 Activity 界面，要发起网络请求或者执行很多耗时的逻辑操作。然后在这些逻辑操作还没有完成的情况下，Activity 被用户关闭，那么现在进行的一些逻辑操作其实没有必要再进行下去的。因为进行了也没有办法将结果反馈到界面上，所以这种情况下最佳的解决方案是将这些正在运行的逻辑全部都取消掉。
 
在使用线程时，如果你创建了很多个线程，想要管理他们是非常困难，因为需要追踪到每一个线程，然后想办法将他们一一取消，而协程的结构化并发特性使得管理协程变得非常简单：只需要调用最顶层协程的 cancel 函数，就可以将它下面的所有子协程一起取消掉，非常方便。
如果现在只想要取消部分的子协程怎么办？注意：每一个 launch 函数它都会返回一个 Job 对象，Job 是一个协程的唯一标识，所以 launch 函数返回的 Job 对象其实就是用于标示它的唯一 ID。有了 Job 对象之后，你就可以在任何地方调用 job.cancel 函数，只会取消掉当前协程以及它自己下面的所有子协程，而它外面的父协程，还有它的兄弟协程不会受到影响。

#### 协程取消的注意点
协程的取消是需要协作完成。通过一个具体的示例来进行解释，下面定义了一个 doSomethingHeavy 函数：
```
suspend fun doSomethingHeavy() {
    // logic before withContext
    withContext(Dispatchers.IO) {
        // do heavy logic
    }
    // logic after withContext
}
```
在这个函数当中先执行了一些逻辑，有注释标识。然后在调用完这一段逻辑之后，开始执行 withContext。如果当我在执行 logic before withContext 的时候，当前的协程被取消掉了，withContext 函数在执行之前会检查协程是不是还在运运行状态，如果发现当前协程已经取消了，withContext 代码块当中的代码就不会得到执行，并且 withContext 之后的 logic after withContext 也不会执行。
 
但是如果我们 logic before withContext 运行完了，已经进入到 withContext 代码块里面，这个时候我们的协程被取消了，那么 withContext 代码块中的代码一定会全部执行完，我们的 cancel 函数是不会影响到 withContext 代码块中代码执行的。然后 withContext 这个函数会帮我们在协程结束之后再做一次检查，看看当前的协程是不是还在正常运行，如果不在正常运行，那么 logic after withContext 这部分的代码就不会继续执行。这个是因为 withContext 帮我们做这样的代码检查，所以它才能够比较好的帮我们完成协程取消的操作。如果现在执行的代码并没有调用 withContext，或者是在 withContext 函数的代码块当中循环去执行一段非常耗时的逻辑操作，那我们的取消是没有办法正常实现的，需要代码块中的代码全部执行完，才有办法取消。
```
suspend fun doSomethingHeavy() {
    // logic before withContext
    withContext(Dispatchers.IO) {
        for (file in files) {
            // do heavy logic
        }
    }
    // logic after withContext
}
```
考虑下面的示例代码，在withContext 的代码块中循环遍历文件。假设在执行到第二次的循环时，当前协程被取消，希望后面的循环不要再继续运行了，但是实际上并不会，只有整个耗时的循环，每一个 file 遍历执行完之后，协程才能得到取消。所以为什么说协程的取消是需要协作是完成的，我们需要清楚它的取消机制，并且在每次执行耗时逻辑之前都来做一次协程是否还处于运行状态的检查。
```
suspend fun doSomethingHeavy() {
    // logic before withContext
    withContext(Dispatchers.IO) {
        for (file in files) {
            ensureActive()
            // do heavy logic
        }
    }
    // logic after withContext
}
```
示例中使用了 ensureActive 函数来帮我们检查协程是否还处于运行状态。

#### 处理协程的异常
 
##### 传统方式
try-catch 是传统的异常处理方式。如果在一个 launch 函数当中创建了一个协程，然后去使用 try-catch 来去捕获协程当中的异常，这种方式和平时编写代码使用 try-catch 的方式是完全一样的，可以捕获到协程里面逻辑出现的异常。
```
launch {
    try {
        // do something
        throw Exception("unhandled exception")
    } catch (e: Exception) {
        // caught exception
    }
}
```
现在将这种捕获的方式来稍微换一种写法，把 try-catch 移到 launch 函数的外面，然后捕获协程当中的异常，这种方式一定是捕获不到的。
```
try {
    launch {
        // do something
        throw Exception("unhandled exception")
    }
} catch (e: Exception) {
    // caught exception
}
```
因为 launch 函数它并不是调用它之后，整个代码就会阻塞在这里不动，而是会继续向下执行的。当继续向下执行之后，很快就会超出 try-catch 的作用域，而超出作用域之后，如果 launch 内部的代码块 throw 一个 Exception，try-catch 就不可能捕获。
如果再换一种更加特殊的场景，使用 async 函数，然后在里面 throw 一个 Exception，并且 deferred.await 这个方法是在 try-catch 作用域当中的，那么这种方式能够捕获到协程的异常吗？不一定，有些情况的话能够捕获到，而有些捕获不到。
```
try {
    val deferred = async {
        // do something
        throw Exception("unhandled exception")
    }
    deferred.await()
} catch (e: Exception) {
    // caught exception
}
```
总结一下协程的异常处理：协程内部的异常通过传统的 try-catch 方式捕获没有问题，但是永远不要去做跨协程的异常捕获。
 
##### 全局捕获方式
 
除了传统的 try-catch, 协程还提供了一种全局捕获异常的方式：CoroutineExceptionHandler。
```
val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
    // caught exception
}
```
调用 CoroutineExceptionHandler 函数，并且给它传递一个 lambda 表达式，然后在 lambda 中接收异常的返回信息，lambda 中的 throwable 参数就是具体抛出的异常。可以将 CoroutineExceptionHandler 应用到 CoroutineScope 函数当中，因为 CoroutineExceptionHandler 实际上也是一个 CoroutineContext，所以它可以用加号进行连接。
```
val scope = CoroutineScope(Dispatchers.Main + Job() + handler)
```
比如说使用 ViewModelScope 时，是没有 CoroutineScope 编写权限的，这种情况下可以在调用 launch 函数时加入 handler。
```
viewModelScope.launch(handler) {

}
```
如果调用 scope.launch 其中的一个子协程，在子协程 launch 时并传入了 handler，能捕获到吗？这种情况下是捕获不到的，CorountineExceptionHandler 只能放到顶层协程当中，所以在子协程当中不要使用它。
```
scope.launch {
    launch(handler) {
    }
}
```
#### 协程失败时的行为

接下来我们再来讨论一下协程失败的行为。

什么是失败？
就是当程序出现了未捕获的异常时就叫做失败。但是如果你没有使用，刚才介绍 CorountineExceptionHandler 将异常捕获，那么你的程序就会崩溃，崩溃的情况下也就谈不上失败。所以前提是要将异常捕获住，然后才能谈协程失败的行为。

那么当一个协程失败的话，它会做出什么样的反应？
 
首先失败事件会冒泡到上一层，也就是 Parent 的这层协程当中，然后 Parent 层会先将自己的子协程全部 cancel 掉，接着再将自己 cancel 掉，最后再将这个事件继续冒泡到上一层。可以简单理解：假如一个协程失败的话，它的整个协程栈，所有的协程全都会被取消。
 
那么取消会造成什么样的后果呢？
一个协程一旦它被取消之后，它就无法再次 launch，所以这对程序来说可能是一个比较大的影响，避免全部取消的方法就是使用 SupervisorJob 。SupervisorJob 和之前提到的 Job 类似，只不过它有一个额外的行为：如果一个子协程失败时，SupervisorJob 不会对子协程或者它自己做任何其他的处理，你自己失败就可以了，这个是 SupervisorJob 它的作用。接下来我们来看一下它的用法，很简单，其实就是将我们刚才 Job 的部分替换成 SupervisorJob。 
```
val scope = CoroutineScope(Dispatchers.Main + SupervisorJob() + handler)
scope.launch {
    throw Exception("coroutine failed")
}
scope.launch {

}
```
然后我再跟大家演示一段程序，现在的话我换成了 SupervisorJob  之后，我们使用 CoroutineScope launch 两个协程，我在第一个协程里面去 throw 了一个 Exception，那么第二个协程会受到影响吗？--- 答案是不会的。
```
val scope = CoroutineScope(Dispatchers.Main + SupervisorJob() + handler)
scope.launch {
    launch {
        throw Exception("coroutine failed")
    }
    launch {
    }
}
```
现在如果我在 scope.launch 里面 launch 一个子协程，然后在它里面又去 launch 两个子协程。这个时候，第一个子协程里去 throw 了一个 Exception，那么第二个子协程会受到影响吗？--- 答案是会的。因为我们就是内部的两个子协程，它的父协程的 Job 类型已经变成了 Job，而不是 SupervisorJob。那么这种情况的话，如果我们想要让他互相之间不会受影响，就要借助一个的supervisorScope  函数来去包裹一下这两个子协程。这样他们的 Job 类型就变成了 SupervisorJob。 

## Flow
### Flow基本用法
因为Flow是构建在Kotlin协程基础之上的，因此协程依赖库必不可少。
使用：
```
val timeFlow = flow {
        var time = 0
        while (true) {
            emit(time)
            delay(1000)
            time++
        }
    }
```
```
button.setOnClickListener {
    lifecycleScope.launch {
        mainViewModel.timeFlow.collect { time ->
            textView.text = time.toString()
        }
    }
}
```
使用flow构建函数构建出的Flow是属于Cold Flow，也叫做冷流。所谓冷流就是在没有任何接受端的情况下，Flow是不会工作的。只有在有接受端（水龙头打开）的情况下，Flow函数体中的代码就会自动开始执行。
由于Flow的collect函数是一个挂起函数，因此必须在协程作用域或其他挂起函数中才能调用。这里我们借助lifecycleScope启动了一个协程作用域来实现。

另外，只要调用了collect函数之后就相当于进入了一个死循环，它的下一行代码是永远都不会执行到的。因此，如果你的代码中有多个Flow需要collect，下面这种写法就是完全错误的：
```
lifecycleScope.launch {
    mainViewModel.flow1.collect {
        ...
    }
    mainViewModel.flow2.collect {
        ...
    }
}
```
这种写法flow2中的数据是无法得到更新的，因为它压根就执行不到。
正确的写法应该是借助launch函数再启动子协程去collect，这样不同子协程之间就互不影响了：
```
lifecycleScope.launch {
    launch {
        mainViewModel.flow1.collect {
            ...
        }
    }
    launch {
        mainViewModel.flow2.collect {
            ...
        }
    }
}
```

### 操作符函数进阶
#### map
```
val flow = flowOf(1, 2, 3, 4, 5)
flow.map {
    it * it
}.collect {
    println(it)
}
```
#### filter
```
val flow = flowOf(1, 2, 3, 4, 5)
flow.filter { 
    it % 2 == 0
}.map {
    it * it
}.collect {
    println(it)
}
```
#### onEach
```
val flow = flowOf(1, 2, 3, 4, 5)
flow.onEach {
    println(it)
}.collect {
}
```
#### debounce
debounce函数可以用来确保flow的各项数据之间存在一定的时间间隔，如果是时间点过于临近的数据只会保留最后一条。
```
flow {
    emit(1)
    emit(2)
    delay(600)
    emit(3)
    delay(100)
    emit(4)
    delay(100)
    emit(5)
}
.debounce(500)
.collect {
    println(it)
}
```
可以看到，我们调用了debounce函数，并且传入了500作为参数，意义就是说只有两条数据之间的间隔超过500毫秒才能发送成功。

这里使用emit()函数依次发送了1、2、3、4、5这几条数据。其中，1和2是连续发送的，2和3之间存在600毫秒的间隔，因此2可以发送成功。3和4之间、4和5之间间隔只有100毫秒，因此都无法发送成功。5由于是最后一条数据，因此可以发送成功。那么打印结果应该是2和5

#### sample
sample操作符函数和debounce稍微有点类似，它们的用法也比较接近，同样都是接收一个时间参数。sample是采样的意思，也就是说，它可以从flow的数据流当中按照一定的时间间隔来采样某一条数据。这个函数在某些源数据量很大，但我们又只需展示少量数据的时候比较有用。
```
flow {
    while (true) {
        emit("发送一条弹幕")
    }
}
.sample(1000)
.flowOn(Dispatchers.IO)
.collect {
    println(it)
}
```
可以看到，虽然弹幕的发送量无限大，但是我们每秒钟只会打印出一条弹幕

#### reduce
reduce函数会通过参数给我们一个Flow的累积值和一个Flow的当前值，我们可以在函数体中对它们进行一定的运算，运算的结果会作为下一个累积值继续传递到reduce函数当中。
```
val result = flow {
    for (i in (1..100)) {
        emit(i)
    }
}.reduce { acc, value -> acc + value}
println(result)
```
这里需要注意的是，reduce函数是一个终端操作符函数，它的后面不可以再接其他操作符函数了，而是只能获取最终的运行结果。
#### fold
fold函数和reduce函数基本上是完全类似的，它也是一个终端操作符函数。

主要的区别在于，fold函数需要传入一个初始值，这个初始值会作为首个累积值被传递到fold的函数体当中
```
val result = flow {
    for (i in ('A'..'Z')) {
        emit(i.toString())
    }
}.fold("Alphabet: ") { acc, value -> acc + value}
println(result)
```
#### flatMapConcat
flatMap的核心，就是将两个flow中的数据进行映射、合并、压平成一个flow，最后再进行输出。
```
flowOf(1, 2, 3)
.flatMapConcat {
    flowOf("a$it", "b$it")
}
.collect {
    println(it)
}
//输出a1、b1、a2、b2、a3、b3
```
这里的第一个flow会依次发送1、2、3这几个数据。然后在flatMapConcat函数中，我们传入了第二个flow。第二个flow会依次发送a、b这两个数据，但是在a、b的后面又各自拼接了一个it。这个it就是来自第一个flow中的数据。所以，flow1中的1、2、3会依次与flow2中的a、b进行组合，这样就能组合出a1、b1、a2、b2、a3、b3这样几条数据。而collect函数最终收集到的就是这些组合后的数据。

比如说我们想要获取用户的数据，但是获取用户数据必须要有token授权信息才行，因此我们得先发起一个请求去获取token信息，然后再发起另一个请求去获取用户数据。

这种两个网络请求之间存在依赖关系的代码其实挺不好写的，稍微一不注意就可能会陷入嵌套地狱：
```
public void getUserInfo() {
    sendGetTokenRequest(new Callback() {
        @Override
        public void result(String token) {
            sendGetUserInfoRequest(token, new Callback() {
                @Override
                public void result(String userInfo) {
                    // handle with userInfo
                }
            });
        }
    });
}
```
可以看出来，网终请求代码由于需要开线程执行，然后在回调中获取结果，通常会嵌套得比较深。而这个问题我们就可以借助flatMapConcat函数来解决。

假设我们将sendGetTokenRequest()函数和sendGetUserInfoRequest()函数都使用flow的写法进行改造：
```
fun sendGetTokenRequest(): Flow<String> = flow {
    // send request to get token
    emit(token)
}

fun sendGetUserInfoRequest(token: String): Flow<String> = flow {
    // send request with token to get user info
    emit(userInfo)
}
```
那么接下来就可以用flatMapConcat函数将它们串连成一条链式执行的任务了：
```
fun main() {
    runBlocking {
        sendGetTokenRequest()
            .flatMapConcat { token ->
                sendGetUserInfoRequest(token)
            }
            .flowOn(Dispatchers.IO)
            .collect { userInfo ->
                println(userInfo)
            }
    }
}
```
当然，这个用法并不仅限于只能将两个flow串连成一条链式任务，如果你有更多的任务需要串到这同一条链上，只需要不断连缀flatMapConcat即可：
```
fun main() {
    runBlocking {
        flow1.flatMapConcat { flow2 }
             .flatMapConcat { flow3 }
             .flatMapConcat { flow4 }
             .collect { userInfo ->
                 println(userInfo)
             }
    }
}
```
可以看到，这种写法，不管串连多少任务，都可以用完全平级的写法搞定，完全不会遇到之前嵌套地狱的困扰。

#### flatMapMerge
多人觉得flatMap这几个操作符函数难以理解，其中一个原因就是，不管代码怎么写，flatMapConcat和flatMapMerge的效果好像都是一样的。
没错，如果只是用我们上面学习的代码示例，你会发现不管是用flatMapConcat还是flatMapMerge，最终的结果都是相同的。
这两个函数最主要的区别其实就在字面上。concat是连接的意思，merge是合并的意思。连接一定会保证数据是按照原有的顺序连接起来的，而合并则只保证将数据合并到一起，并不会保证顺序。因此，flatMapMerge函数的内部是启用并发来进行数据处理的，它不会保证最终结果的顺序。

当然，刚才我们所使用的示例并不能演示出这种场景，下面我来对代码稍微进行一下改造：
```
flowOf(300, 200, 100)
    .flatMapConcat {
        flow {
            delay(it.toLong())
            emit("a$it")
            emit("b$it")
        }
    }
    .collect {
        println(it)
    }
//运行结果: a300 b300 a200 b200 a100 b100
```
变化主要在于，我将第一个flow发送的数据改成了300、200、100。然后第二个flow中，在发送数据之前，我们要先去delay相对应的毫秒数。
可以看到，最终的结果仍然是按照flow1中数据发送的顺序输出的，即使第一个数据被delay了300毫秒，后面的数据也没有优先执行权。这就是flatMapConcat函数所代表的涵义。

而到了这里，flatMapMerge函数的区别也就呼之欲出了，它是可以并发着去处理数据的，而并不保证顺序。那么哪条数据被delay的时间更短，它就可以更优先地得到处理。

将flatMapConcat函数替换成flatMapMerge函数，如下所示：
```
fun main() {
    runBlocking {
        flowOf(300, 200, 100)
            .flatMapMerge {
                flow {
                    delay(it.toLong())
                    emit("a$it")
                    emit("b$it")
                }
            }
            .collect {
                println(it)
            }
    }
}
//运行结果:a100 b100 a200 b200 a300 b300
```

#### flatMapLatest
先来回顾一下collectLatest函数的特性，它只接收处理最新的数据。如果有新数据到来了而前一个数据还没有处理完，则会将前一个数据剩余的处理逻辑全部取消。

flatMapLatest函数也是类似的，flow1中的数据传递到flow2中会立刻进行处理，但如果flow1中的下一个数据要发送了，而flow2中上一个数据还没处理完，则会直接将剩余逻辑取消掉，开始处理最新的数据。

```
flow {
    emit(1)
    delay(150)
    emit(2)
    delay(50)
    emit(3)
}.flatMapLatest {
    flow {
        delay(100)
        emit("$it")
    }
}
.collect {
    println(it)
}
//输出1 3
```
这里我们在flow1中依次发送了1、2、3这几条数据。其中，1和2之间间隔了150毫秒，2和3之间间隔了50毫秒。而在flow2中，每次处理数据需要消耗100毫秒。
那么由此我们可以分析出，当flow1中的第2条数据发送过来时，flow2中的第1条数据肯定已经处理完了。但是当flow1中的第3条数据发送过来时，flow2中的第2条数据并没有处理完。那么根据collectLatest函数的规则，这条数据的剩余处理逻辑会被取消掉。因此，2不会被打印出来。最终我们看到的打印结果应该是1和3
#### zip
使用zip连接的两个flow，它们之间是并行的运行关系。这点和flatMap差别很大，因为flatMap的运行方式是一个flow中的数据流向另外一个flow，是串行的关系。
```
val flow1 = flowOf("a", "b", "c")
val flow2 = flowOf(1, 2, 3, 4, 5)
flow1.zip(flow2) { a, b ->
    a + b
}.collect {
    println(it)
}
//输出a1 b2 c3 
```

### buffer

buffer函数和我们上篇文章学到的collectLatest函数，以及接下来要学习的conflate函数都是类似的，那就是解决Flow流速不均匀的问题。
```
flow {
    emit(1);
    delay(1000);
    emit(2);
    delay(1000);
    emit(3);
}.onEach {
    println("$it is ready")
}.collect {
    delay(1000)
    println("$it is handled")
}
```
默认情况下，collect函数和flow函数会运行在同一个协程当中，因此collect函数中的代码没有执行完，flow函数中的代码也会被挂起等待。

也就是说，我们在collect函数中处理数据需要花费1秒，flow函数同样就要等待1秒。collect函数处理完成数据之后，flow函数恢复运行，发现又要等待1秒，这样2秒钟就过去了才能发送下一条数据。
略微改造一下上述代码:
```
flow {
    emit(1);
    delay(1000);
    emit(2);
    delay(1000);
    emit(3);
}
.onEach {
    println("$it is ready")
}
.buffer()
.collect {
    delay(1000)
    println("$it is handled")
}
```
buffer函数会让flow函数和collect函数运行在不同的协程当中，这样flow中的数据发送就不会受collect函数的影响了。buffer函数其实就是一种背压的处理策略，它提供了一份缓存区，当Flow数据流速不均匀的时候，使用这份缓存区来保证程序运行效率。flow函数只管发送自己的数据，它不需要关心数据有没有被处理，反正都缓存在buffer当中。而collect函数只需要一直从buffer中获取数据来进行处理就可以了。

#### conflate
buffer函数最大的问题在于，不管怎样调整它缓冲区的大小（buffer函数可以通过传入参数来指定缓冲区大小），都无法完全地保障程度的运行效果。究其原因，主要还是因为buffer函数不会丢弃数据。

而在某些场景下，我们可能并不需要保留所有的数据。

比如拿股票软件举例，服务器端会将股票价格的实时数据源源不断地发送到客户端这边，而客户端这边只需要永远显示最新的股票价格即可，将过时的数据展示给用户是没有意义的。

因此，这种场景下使用buffer函数来提升运行效率就完全不合理，它会缓存太多完全没有必要保留的数据。

那么针对这种场景，其中一个可选的方案就是借助我们在上篇文章中学习的collectLatest函数。

它的特性是，只接收处理最新的数据，如果有新数据到来了而前一个数据还没有处理完，则会将前一个数据剩余的处理逻辑全部取消。

我们通过以下例子来验证这个函数的特性
```
flow {
    var count = 0
    while (true) {
        emit(count)
        delay(1000)
        count++
    }
}.collectLatest {
    println("start handle $it")
    delay(2000)
    println("finish handle $it")
}
```
通过日志打印我们发现，每条数据都是有输出的，但是每条数据都只输出了start部分，而finish部分则都没有输出。
这就充分说明了collectLatest函数的特性，当有新数据到来时而前一个数据还没有处理完，则会将前一个数据剩余的处理逻辑全部取消。所以，finish部分的日志是永远得不到输出的。

对于这种行为结果，我个人认为是有点反直觉的。我的第一直觉是，当前正在处理的数据无论如何都应该处理完，然后准备去处理下一条数据时，直接处理最新的数据即可，中间的数据就都可以丢弃掉了。
稍微对以上代码进行一些修改
```
flow {
    var count = 0
    while (true) {
        emit(count)
        delay(1000)
        count++
    }
}
.conflate()
.collect {
    println("start handle $it")
    delay(2000)
    println("finish handle $it")
}
```

### Flow的生命周期管理
#### launchWhenStarted
lifecycleScope除了launch函数可以用于启动一个协程之外，还有几个与Activity生命周期关联的launch函数可以使用。比如说，launchWhenStarted函数就是用于保证只有在Activity处于Started状态的情况下，协程中的代码才会执行。
当我们将程序从后台切回到前台时，计时器会接着之前切出去的时间继续计时。这说明了什么？说明程序在后台的时候，Flow的管道中一直会暂存着一些的旧数据，这些数据不仅可能已经失去了时效性，而且还会造成一些内存上的问题。

要知道，我们使用flow构建函数构建出的Flow是属于冷流，也就是在没有任何接受端的情况下，Flow是不会工作的。但是上述例子当中，即使程序切到了后台，Flow依然没有中止，还是为它保留了过期数据，这就是一种内存上的浪费。

当然，我们这个例子非常简单，在实际项目中一个Flow可能又是由多个上游Flow合并而成的。在这种情况下，如果程序进入了后台，却仍有大量Flow依然处于活跃的状态，那么内存问题会变得更加严重。

#### repeatOnLifecycle
repeatOnLifecycle函数接受一个Lifecycle.State参数，这里我们传入Lifecycle.State.STARTED，同样表示只有在Activity处于Started状态的情况下，协程中的代码才会执行。
我们将程序切到后台之后，日志打印就停止了。当我们将程序重新切回前台时，计时器会从零开始重新计时。

这说明什么？说明Flow在程序进入后台之后就完全停止了，不会保留任何数据。程序回到前台之后Flow又从头开始工作，所以才会从零开始计时。正确使用repeatOnLifecycle函数，这样才能让我们的程序在使用Flow的时候更加安全。

总结：launcherwhenstart 继续计时的原因是退到后台，协程suspend 在collect的那一行，collect 被 suspend，emit 不会触发的。当恢复到前台时 collect 会resume 继续计时。
repeatwhenxxx 不继续计时是因为退到后台协程直接被cancel 了，回到前台重新启动协程再collect

### StateFlow
#### 基本用法
可以说，StateFlow的基本用法甚至能够做到与LiveData完全一致。
```
class MainViewModel : ViewModel() {

    private val _stateFlow = MutableStateFlow(0)

    val stateFlow = _stateFlow.asStateFlow()

    fun startTimer() {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                _stateFlow.value += 1
            }
        }, 0, 1000)
    }
}
```

```
class MainActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.text_view)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            mainViewModel.startTimer()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.stateFlow.collect {
                    textView.text = it.toString()
                }
            }
        }
    }
}
```
#### 高级用法
借助stateIn函数，可以将其他的Flow转换成StateFlow。
为了能够更好地讲解stateIn函数，我们还需要对之前的例子进行一下改造。

首先将MainViewModel中的代码还原到最初版本：
```
class MainViewModel : ViewModel() {

    val timeFlow = flow {
        var time = 0
        while (true) {
            emit(time)
            delay(1000)
            time++
        }
    }
    
}
```
然后修改MainActivity中的代码：
```
class MainActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.text_view)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.timeFlow.collect { time ->
                    textView.text = time.toString()
                }
            }
        }
    }
}
```
这里我们移除了对Button点击事件的监听，而是在onCreate函数中直接让计时器就开始工作。原来除了程序进入后台之外，手机发生横竖屏切换也会让计时器重新开始计时。出现这个情况的原因是，手机横竖屏切换会导致Activity重新创建，重新创建就会使得timeFlow重新被collect，而冷流每次被collect都是要重新执行的。那么该怎么解决呢？现在终于可以引入stateIn函数了
```
class MainViewModel : ViewModel() {

    private val timeFlow = flow {
        var time = 0
        while (true) {
            emit(time)
            delay(1000)
            time++
        }
    }

    val stateFlow =
        timeFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000), 
            0
        )
}
```
stateIn函数接收3个参数，其中第1个参数是作用域，传入viewModelScope即可。第3个参数是初始值，计时器的初始值传入0即可。而第2个参数则是最有意思的了。刚才有说过，当手机横竖屏切换的时候，我们不希望Flow停止工作。但是再之前又提到了，当程序切到后台时，我们希望Flow停止工作。
这该怎么区分分别是哪种场景呢？Google给出的方案是使用超时机制来区分。

因为横竖屏切换通常很快就能完成，这里我们通过stateIn函数的第2个参数指定了一个5秒的超时时长，那么只要在5秒钟内横竖屏切换完成了，Flow就不会停止工作。反过来讲，这也使得程序切到后台之后，如果5秒钟之内再回到前台，那么Flow也不会停止工作。但是如果切到后台超过了5秒钟，Flow就会全部停止了。

### SharedFlow
StateFlow和LiveData具有高度一致性，因此可想而知，StateFlow也是粘性的。
假设我们现在正在开发一个登录功能，点击按钮开始执行登录操作，登录成功之后弹出一个Toast告知用户。
```
class MainViewModel : ViewModel() {

    private val _loginFlow = MutableStateFlow("")

    val loginFlow = _loginFlow.asStateFlow()

    fun startLogin() {
        // Handle login logic here.
        _loginFlow.value = "Login Success"
    }
}
```
这里我们定义了一个startLogin函数，当调用这个函数时开始执行登录逻辑操作，登录成功之后向loginFlow进行赋值来告知用户登录成功了。

接着修改MainActivity中的代码，如下所示：
```
class MainActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            mainViewModel.startLogin()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.loginFlow.collect {
                    if (it.isNotBlank()) {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
```
这里当点击按钮时，我们调用MainViewModel中的startLogin函数开始执行登录。
然后在对loginFlow进行collect的地方，通过弹出一个Toast来告知用户登录已经成功了。可以看到，当点击按钮开始执行登录时，弹出了一个Login Success的Toast，说明登录成功了。到这里都还挺正常的。接下来当我们尝试去旋转一下屏幕，此时又会弹出一个Login Success的Toast，这就不对劲了。而这，就是粘性所导致的问题。
现在我们明白了在某些场景下粘性特性是不太适用的，接下来我们就学习一下如何使用SharedFlow这个非粘性的版本来解决这个问题。
修改MainViewModel中的代码，如下所示：
```
class MainViewModel : ViewModel() {

    private val _loginFlow = MutableSharedFlow<String>()

    val loginFlow = _loginFlow.asSharedFlow()

    fun startLogin() {
        // Handle login logic here.
        viewModelScope.launch {
            _loginFlow.emit("Login Success")
        }
    }
}
```
SharedFlow和StateFlow的用法还是略有不同的。
首先，MutableSharedFlow是不需要传入初始值参数的。因为非粘性的特性，它本身就不要求观察者在观察的那一刻就能收到消息，所以也没有传入初始值的必要。
SharedFlow 的构造函数允许我们配置三个参数：
```
public fun <T> MutableSharedFlow(
    // 重放数据个数
    replay: Int = 0,
    // 额外缓存容量
    extraBufferCapacity: Int = 0,
    // 缓存溢出策略
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
): MutableSharedFlow<T> {
    val bufferCapacity0 = replay + extraBufferCapacity
    val bufferCapacity = if (bufferCapacity0 < 0) Int.MAX_VALUE else bufferCapacity0 // coerce to MAX_VALUE on overflow
    return SharedFlowImpl(replay, bufferCapacity, onBufferOverflow)
}

public enum class BufferOverflow {
    // 挂起
    SUSPEND,
    // 丢弃最早的一个
    DROP_OLDEST,
    // 丢弃最近的一个
    DROP_LATEST
}
```

| 参数 | 描述 |
|----|----|
| reply | 重放数据个数，当新订阅者时注册时会重放缓存的 replay 个数据 |
| extraBufferCapacity | 额外缓存容量，在 replay 之外的额外容量，SharedFlow 的缓存容量 capacity = replay + extraBufferCapacity（实在想不出额外容量有什么用，知道可以告诉我）|
| onBufferOverflow | 缓存溢出策略，即缓存容量 capacity 满时的处理策略（SUSPEND、DROP_OLDEST、DROP_LAST）|

另外就是，SharedFlow无法像StateFlow那样通过给value变量赋值来发送消息，而是只能像传统Flow那样调用emit函数。而emit函数又是一个挂起函数，所以这里需要调用viewModelScope的launch函数启动一个协程，然后再发送消息。
当然，其实SharedFlow的用法还远不止这些，我们可以通过一些参数的配置来让SharedFlow在有观察者开始工作之前缓存一定数量的消息，甚至还可以让SharedFlow模拟出StateFlow的效果。
但是我觉得这些配置会让SharedFlow更难理解，就不打算讲了。还是让它们之间的区别更纯粹一些，通过粘性和非粘性的需求来选择你所需要的那个版本即可。


## Channel 通道

在协程的基础能力上使用数据流，除了上文提到到 Flow API，还有一个 Channel API。Channel 是 Kotlin 中实现跨协程数据传输的数据结构，类似于 Java 中的 BlockQueue 阻塞队列。不同之处在于 BlockQueue 会阻塞线程，而 Channel 是挂起线程。Google 的建议 是优先使用 Flow 而不是 Channel，主要原因是 Flow 会更自动地关闭数据流，而一旦 Channel 没有正常关闭，则容易造成资源泄漏。此外，Flow 相较于 Channel 提供了更明确的约束和操作符，更灵活。
Channel 主要的操作如下：

- 创建 Channel： 通过 Channel(Channel.UNLIMITED) 创建一个 Channel 对象，或者直接使用 produce{} 创建一个生产者协程；
- 关闭 Channel： Channel#close()；
- 发送数据： Channel#send() 往 Channel 中发送一个数据，在 Channel 容量不足时 send() 操作会挂起，Channel 默认容量 capacity 是 1；
- 接收数据： 通过 Channel#receive() 从 Channel 中取出一个数据，或者直接通过 actor 创建一个消费者协程，在 Channel 中数据不足时 receive() 操作会挂起。
- 广播通道 BroadcastChannel（废弃，使用 SharedFlow）： 普通 Channel 中一个数据只会被一个消费端接收，而 BroadcastChannel 允许多个消费端接收。


## MVI 
- 事件（Event）： 事件是一次有效的，新订阅者不应该收到旧的事件，因此事件数据适合用 SharedFlow(replay=0)；
- 状态（State）： 状态是可以恢复的，新订阅者允许收到旧的状态数据，因此状态数据适合用 StateFlow。