## Compose
setContent函数会提供一个Composable作用域，所以在它的闭包中我们就可以随意地调用Composable函数了。
所有的Composable函数还有一个约定俗成的习惯，就是函数的命名首字母需要大写。
Surface函数是Material库中提供的一个通用函数，它的主要作用是为了让应用程序可以更好地适配Material Design，例如控制阴影高度、控制内容颜色、裁剪形状等等。

### 基础控件
#### Text

Text的用法非常简单，只需要给它指定一个text参数，里面传入要显示的内容即可。除此之外，Text还能实现很多其他的功能，通过观察它的参数列表便可略之一二：

```
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    ...
}
```

#### Button
Button应该是仅次于Text之后最常用的控件了。这个相信不需要我介绍大家也都知道是用来做什么的，因为它和View中的Button名字完全相同。不同的是，在View当中，Button是TextView的子类，它们之间是继承的关系。因此，Button实际上是在TextView的基础之上做了功能扩展，使得控件可以点击了。
而在Compose当中，Button和Text之间并没有什么关系。它们是两个独立的控件，并且通常它们还需要配合在一起使用才行。Button的参数列表上还有一个onClick参数，这是一个必填参数，当按钮点击时，就会执行这个参数里指定的逻辑。
为了演示一下这个功能，我们就让点击按钮时弹出一个Toast提示吧。
```
@Composable
fun SimpleWidgetColumn() {
    Column {
        ...
        val context = LocalContext.current
        Button(onClick = {
            Toast.makeText(context, "This is Toast", Toast.LENGTH_SHORT).show()
        }) {
            Text(
                text = "This is Button",
                color = Color.White,
                fontSize = 26.sp
            )
        }
    }
}
```
注意，要想弹出Toast需要有Context参数才行。在Composable函数当中获取Context对象，可以调用LocalContext.current获得。

#### TextField

TextField对应的是View当中的EditText，也就是一个输入框，因此它也是一个非常常用的控件。
首先我们尝试在界面上添加一个TextField吧，代码如下所示：

```
@Composable
fun SimpleWidgetColumn() {
    Column {
        ...
        TextField(value = "", onValueChange = {})
    }
}
```
TextField参数列表上有两个必填参数，其中value参数用于指定当前输入框中显示的文字内容，onValueChange参数用于监听输入框中的文字内容变化。
到这里为止还算比较简单，可是当你尝试在输入框里输入内容时，你会发现不管你在键盘上敲了什么东西，输入框上都不会显示出来。这是和EditText最大的不同点，因为EditText一定是可以显示你输入的内容的。

声明式UI的工作流程有点像是刷新网页一样。即我们去描述一个控件时要附带上它的状态。然后当有任何状态需要发生改变时，只需要像刷新网页一样，让界面上的元素刷新一遍，那么自然状态就能得到更新了。

而TextField中显示的内容就是一种状态，因为随着你的输入，界面上显示的内容也需要跟着更新才行。
那么这里，当在TextField中输入内容时，首先我们并没有去做刷新页面这个操作。其次，就算是做了刷新操作，TextField刷新后发现value参数指定的内容仍然是一个空字符串，因此我们输入的内容还是无法上屏。

现在问题的原因已经解释清楚了，那么要如何解决呢？这就得借助Compose的State组件了。不过这是另外一个知识点，我打算在之后的文章中讲解，本篇文章我不想过于发散，暂时我们还是把精力聚焦在基础控件和布局上，就先跳过这个问题吧。
TextField同样也提供了非常丰富的API来来允许我们对它进行定制。比如，EditText有一个hint属性，用于在输入框里显示一些提示性的文字，然后一旦用户输入了任何内容，这些提示性的文字就会消失。
这里通过placeholder参数来指定一个占位符，其实就是和hint差不多的功能，用户没有在输入框里输入任何内容时就显示placeholder中的内容，一旦用户输入了任何内容，placeholder就会消失。
可以通过TextFieldDefaults调整TextField的输入框背景色：TextFieldDefaults可以支持调整任何你想要调整的颜色，可不仅仅是输入框背景色，具体的用法参考一下它的参数列表你就知道了。
```
@Composable
fun SimpleWidgetColumn() {
    Column {
        ...
        TextField(
            value = "",
            onValueChange = {},
            placeholder = {
                Text(text = "Type something here")
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White
            )
        )
    }
}
```

#### Image
Image对应的是View当中的ImageView，也就是用于展示图片的。首先来看一下drawable资源形式的图片要如何加载，代码如下所示：

```
@Composable
fun SimpleWidgetColumn() {
    Column {
        ...
        Image(
            painter = painterResource(id = R.drawable.dog),
            contentDescription = "A dog image"
        )
    }
}
```
Image参数列表上有两个必填参数，其中painter参数用于指定要展示的drawable资源，contentDescription参数用于指定对于这个资源的文字描述。
这个文字描述主要是在accessibility模式下，为有视觉障碍的群体提供发音辅助的。ImageView上也有类型的功能，但只是作为一项可选的属性提供。而到了Compose的Image上，则变成了一个强制性的参数。
当然，如果你就是不想要为图片指定contentDescription，也可以直接传null。
再来看一下bitmap形式的图片要如何加载：

```
@Composable
fun SimpleWidgetColumn() {
    Column {
        ...
        val bitmap: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.dog)
        Image(
            bitmap = bitmap,
            contentDescription = "A dog image"
        )
    }
}
```

先借助ImageBitmap.imageResource函数将drawable资源转换成了一个ImageBitmap对象，然后再将它转给Image控件即可。

需要注意的是，Image接收的是Compose中专有的ImageBitmap对象，而不是传统的Bitmap对象。如果你这里要传入的是一个传统的Bitmap对象，那么还得再额外调用asImageBitmap函数转换一下，如下所示：

```
@Composable
fun SimpleWidgetColumn(bitmap: Bitmap) {
    Column {
        ...
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "A dog image"
        )
    }
}
```
到目前为止，我们展示的都是本地的图片资源。那么如果想要展示一张网络图片资源要怎么办呢？

很遗憾，Compose提供的Image是没有这个能力的，我们需要借助第三方依赖库才行。当然，ImageView也是没有这个能力的，所以我们以前也会使用Glide这样的第三方库。
目前Google比较推荐的第三方Compose图片加载库是Coil和Glide这两个。我知道大家看到Glide一定会感到非常亲切，可能更倾向于使用这个。但实际上Coil是一个基于协程开发的新兴图片加载库，用法更加贴合Kotlin也更加简单，因此我更推荐使用这个新库。

要使用Coil，首先需要将它引入到我们的项目当中：

```
dependencies {
    implementation("io.coil-kt:coil-compose:2.4.0")
}
```
接下来使用Coil提供的AsyncImage控件即可轻松加载网络图片资源了，代码如下所示：

```
@Composable
fun SimpleWidgetColumn() {
    Column {
        ...
        AsyncImage(
            model = "https://img-blog.csdnimg.cn/20200401094829557.jpg",
            contentDescription = "First line of code"
        )
    }
}
```

#### ProgressIndicator

ProgressIndicator对应的是View当中的ProgressBar，也就是用于展示进度条的，这也算是核心基础控件之一了。



我们都知道，View当中的ProgressBar有两种比较常见的形态，分别是圆形ProgressBar和长条形ProgressBar。ProgressIndicator也有这两种形态，对应的控件分别是CircularProgressIndicator和LinearProgressIndicator，我们来逐个学习一下。
首先来看CircularProgressIndicator，它的用法非常简单，如下所示：

```
@Composable
fun SimpleWidgetColumn() {
    Column {
        ...
        CircularProgressIndicator()
    }
}
```
只需要放置一个CircularProgressIndicator控件即可，我们甚至都不需要指定任何的参数。
除了默认的效果外，我们也可以轻松定制进度条的样式，比如通过如下代码就可以修改进度条的颜色和线条粗细：

```
@Composable
fun SimpleWidgetColumn() {
    Column {
        ...
        CircularProgressIndicator(
            color = Color.Green,
            strokeWidth = 6.dp
        )
    }
}
```

接下来再来看一下LinearProgressIndicator的用法
可以使用类似的方法来定制LinearProgressIndicator的样式：

```
@Composable
fun SimpleWidgetColumn() {
    Column {
        ...
        LinearProgressIndicator(
            color = Color.Blue,
            backgroundColor = Color.Gray
        )
    }
}
```

### 基础布局
Compose虽然也有很多的布局，但是最核心的主要就只有三个，Column、Row和Box，Column就是让控件纵向排列，这个我们刚才已经体验过了。Row就是让控件横向排列。Column和Row对应的其实就是View当中的LinearLayout。而Box对应的是View当中的FrameLayout
其实Compose当中也有ConstraintLayout，但并不是非常推荐使用。原因在于，我们之前在View当中之所以使用ConstraintLayout，主要是因为View在布局嵌套过深的情况下性能会急剧下降，而ConstraintLayout则可以使用一层布局嵌套来完成复杂的界面编写，这是它最大的价值所在。
而Compose则完全没有了这个问题，使用Compose来编写界面，你可以进行任意深度的布局嵌套，性能是丝毫不会受影响的。
也正是因为这个原因，Compose中的ConstraintLayout就没有太大的优势了，毕竟使用Column和Row编写出来的布局，在可读性方面要更好一些。

#### Column

目前Column中的所有控件都是居左对齐的，那么我们有没有办法让它们居中对齐呢？
代码其实非常简单，如下所示：

```
@Composable
fun SimpleWidgetColumn() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ...
    }
}
```
这里我们给Column增加了两个参数。

modifier参数是Compose当中非常灵魂的一个组件，所有的Compose控件都需要依赖它，我们接下来的一篇文章就会对Modifier进行详细的讨论。目前你只要知道它调用了fillMaxSize()函数之后可以让Column的大小充满父布局即可。
horizontalAlignment参数可以指定Column当中的子控件在水平方向上的对齐方式，CenterHorizontally表示居中对齐，另外你还可以选择Start和End。

如果我的需要是Column中的每个子控件的对齐方式各不相同怎么办呢？当然没问题，Compose提供了非常高的可定制性，我们只需要在相应子控件的modifier参数中进行对齐方式覆写即可：

```
@Composable
fun SimpleWidgetColumn() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.align(Alignment.End),
            text = "This is Text",
            color = Color.Blue,
            fontSize = 26.sp
        )
        ...
    }
}
```
这样的话，只有第一个Text控件会变成居右对齐，剩下的其他控件会仍然保持居中对齐

除了可以指定子控件在水平方向上的对齐方式外，我们还可以指定子控件在垂直方向上的分布方式，这是什么意思呢？来观看如下代码：

```
@Composable
fun SimpleWidgetColumn() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ...
    }
}
```
这里我们又指定了一个verticalArrangement参数，并给它设置成SpaceEvenly。SpaceEvenly的意思是，让Column中的每个子控件平分Column在垂直方向上的空间。
这个可能是会让大家耳目一新的功能，因为LinearLayout当中是没有类似的功能的，或者LinearLayout要借助layout_weight参数才能实现同样的效果。

但verticalArrangement参数可指定的分布方式非常丰富，LinearLayout想要进行完全类似的模拟还是相当困难的。由于可指定的分布方式比较多，这里我无法给大家一一演示，因此我们来看一张Google官方的动图示例就能快速了解每种分布方式的效果了：

![](./image/7-1.gif)

#### Row

掌握了Column之后再来看Row，那就相当简单了，因为它们基本是就是完全一样的东西，只是方向上有所区别。

如何允许用户通过滚动的方式来查看超出屏幕的内容。首先，如果你是想要寻求RecyclerView或者是ListView在Compose当中的替代品，那么很遗憾，这不在我们今天这篇文章的覆盖范围内，这部分内容我会专门写一篇文章进行讲解。

而像我们当前遇到的这种情况，在View中的话，通常可以在需要滚动的内容之外再嵌套一层ScrollView布局，这样ScrollView中的内容就可以滚动了。

而Compose则不需要再进行额外的布局嵌套，只需要借助modifier参数即可，代码如下所示：

```
@Composable
fun SimpleWidgetColumn() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ...
    }
}
```
这里我们在modifier参数上面又串接了一个horizontalScroll函数，这个函数有一个ScrollState参数是必填参数，它是用于保证在手机横竖屏旋转的情况下滚动位置不会丢失的，通常可以调用rememberScrollState函数获得。
remember系列的函数是Compose当中非常重要的一个部分，我也会在后续的文章当中会进行详细的介绍。当Column中的内容显示不下时，让Column滚动的方式也是类似的，只需要将horizontalScroll改成verticalScroll即可，这里就不演示了。

#### Box
Box对应的是View当中的FrameLayout，它没有丰富的定位方式，所有的控件都会默认摆放在布局的左上角。让我们通过例子来看一看吧：

```
@Composable
fun SimpleWidgetColumn() {
    Box {
        ...
    }
}
```
这里我们将最外层的布局修改成了Box，重新运行一下程序，效果如下图所示：

可以看到，所有子控件都出现在了布局的左上角，并且后添加的控件是会压在先添加的控件上面的。

当然除了这种默认效果之外，我们还可以通过修改子控件的modifier参数来指定控件在布局中的对齐方式，这和Column中的用法是相似的。
修改SimpleWidgetColumn函数中的代码，如下所示：

```
@Composable
fun SimpleWidgetColumn() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.align(Alignment.TopStart),
            ...
        )

        Button(
            modifier = Modifier.align(Alignment.TopEnd),
            ...
        })

        TextField(
            modifier = Modifier.align(Alignment.CenterStart),
            ...
        )

        Image(
            modifier = Modifier.align(Alignment.CenterEnd),
            ...
        )

        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.BottomStart),
            ...
        )

        LinearProgressIndicator(
            modifier = Modifier.align(Alignment.BottomEnd),
            ...
        )
    }
}
```
首先我们通过给Box指定Modifier.fillMaxSize()让它充满父布局，接下来给每个子控件都增加了一个modifier参数，并且通过Modifier.align分别指定了它们各自的对齐方式，这样就不会重叠到一起了。


### Modifier

#### Modifier的作用

Compose对于Modifier能做的事情规定的很明确，Modifier主要负责以下4个大类的功能：
- 修改Compose控件的尺寸、布局、行为和样式。
- 为Compose控件增加额外的信息，如无障碍标签。
- 处理用户的输入
- 添加上层交互功能，如让控件变得可点击、可滚动、可拖拽。

#### 修改Compose控件的尺寸、布局、行为和样式
```
@Composable
fun IconImage() {
    Image(
        painter = painterResource(id = R.drawable.icon),
        contentDescription = "Icon Image",
    )
```
这里定义了一个IconImage()函数，然后在里面放置了一个Image()，用于显示一张图片
这张图片的像素是500*500，而我的手机分辨率显然是大于这个像素数的，但这张图片却可以横向充满全屏。因此说明，在没有进行任何Modifier指定的情况下，Image默认是使用了fillMaxSize()的效果。
接下来我们通过手动指定Modifier来修改一下默认样式：

```
@Composable
fun IconImage() {
    Image(
        painter = painterResource(id = R.drawable.icon),
        contentDescription = "Icon Image",
        modifier = Modifier.wrapContentSize()
    )
}
```

这里调用了Modifier.wrapContentSize()，从而让Image根据自身内容来决定控件的大小。
除此之外，我们还可以非常轻松地对图片进行裁剪和增加边框，代码如下：

```
@Composable
fun IconImage() {
    Image(
        painter = painterResource(id = R.drawable.icon),
        contentDescription = "Icon Image",
        modifier = Modifier
            .wrapContentSize(align = Alignment.CenterStart)
            .border(5.dp, Color.Magenta, CircleShape)
            .clip(CircleShape)
    )
}
```

这里将图片裁剪成了圆形，同时给它增加了一个5dp的边框。我们也可以借助Modifier修改控件的行为，如偏移、旋转等等。比如通过如下代码让图片旋转180度：

```
@Composable
fun IconImage() {
    Image(
        painter = painterResource(id = R.drawable.icon),
        contentDescription = "Icon Image",
        modifier = Modifier
            .wrapContentSize(align = Alignment.CenterStart)
            .border(5.dp, Color.Magenta, CircleShape)
            .clip(CircleShape)
            .rotate(180f)
    )
}
```
#### 为Compose控件增加额外的信息
国内的开发者绝大部分对于Accessibility和Test都不怎么感兴趣。虽然也有一些文章会讲解如何使用Accessibility，但目标应用场景基本都是做一些自动化脚本，甚至是流氓软件之类的东西，可能真的鲜有人关注Accessibility具体是用来做什么的。

事实上，Accessibility的最主要目的，是结合Talkback为那些有视觉障碍的群体提供发音辅助的，以保证即使他们的眼睛看不见或看不清，也可以正常地使用手机和各类App。
Accessibility在国内非常小众，相信大部分朋友应该都不知道如何打开Talkback，所以对这部分进行实战演示可能意义并不大。如果感兴趣或者有需要的话，请自行深入学习。

#### 处理用户的输入

这里的用户输入并不是指的文本输入框的输入，那个是由TextField控件处理的，和Modifier关系不大。
这里的用户输入指的是，当用户的手指在屏幕上进行滑动、点击各种操作时，会认为这是用户的一种输入，而我们则需要对这类输入进行处理。
其实Compose已经提供了许多上层的API，使得开发者能够非常轻松地处理用户的各种输入，这个我们待会就会看到具体的例子。
但如果这些上层API都无法满足你的需求，那么可能你就得使用偏底层的API来进行一些特殊的定制了，而这也是Modifier的其中一个功能领域。
下面我们直接看代码：

```
@Composable
fun PointerInputEvent() {
    Box(modifier = Modifier
        .requiredSize(200.dp)
        .background(Color.Blue)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    Log.d("PointerInputEvent", "event: ${event.type}")
                }
            }
        }
    )
}
```

这里定义了一个PointerInputEvent函数，里面封装了一个Box，并指定它的大小是200dp，颜色是蓝色。
Compose中的Box基本就相当于View中的FrameLayout，它们默认是不能影响用户的点击或其他输入事件的。
而这里，我们调用了Modifier.pointerInput()函数，使用偏底层的API来允许Box可以对用户的输入事件进行处理。
pointerInput()函数至少要传入一个参数，这个参数的作用是，当参数的值发生变化时，pointerInput()函数会重新执行。这是一种声明式编程的思维，我们之前也提到过，以后还会再反复提及。而如果你并没有需求需要pointerInput()函数重新执行，那么传入一个Unit参数就可以了。
在pointerInput()函数的代码块当中，这里调用awaitPointerEventScope启动了一个协程作用域，我们在协程作用域里编写一个死循环，并调用awaitPointerEvent()函数来等待用户输入事件到来。
如果用户没有输入任何事件，这里就会一直挂起等待，直到有用户输入事件之后才会恢复执行，执行完之后又会进入死循环等待下一次用户输入事件的到来。

可以看到，当手指在屏幕上按下并拖动时，我们就能捕获到这些用户输入事件了。

当然这个写法有点过于底层了，基本没有太多场景我们需要使用如此底层的事件处理API。Compose给我们提供了一系列非常好用的辅助API，可以轻松应对绝大部分的事件处理场景。
观察如下代码：

```
@Composable
fun PointerInputEvent() {
    Box(modifier = Modifier
        .requiredSize(200.dp)
        .background(Color.Blue)
        .pointerInput(Unit) {
            detectTapGestures {
                Log.d("PointerInputEvent", "Tap")
            }
            // Never reach
        }
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                Log.d("PointerInputEvent", "Dragging")
            }
            // Never reach
        }
    )
}
```
这里我们在pointerInput()函数中使用了detectTapGestures，用来监听用户的点击事件。又在另一个pointerInput()函数中使用detectDragGestures，用来监听用户的拖拽事件。

注意这两个事件不能在同一个pointerInput()函数中监听，因为detectTapGestures和detectDragGestures函数都是阻塞性的，调用了之后下面的一行代码就永远不会执行到了。
pointerInput()函数当中能做的事情还非常非常多，但是这个展开那又可以写一篇很长的文章了，所以我们就此打往。本篇文章的目的是讲解Modifier，而不是针对每一个知识点都无限发散展开。

#### 使控件可点击、滚动、拖拽
总体来说，使用pointerInput()函数来处理用户输入是比较偏底层的，就像是在View系统中处理TouchEvent一样。
事实上，我们并不需要总是使用这么底层的API。Modifier提供了足够多的上层API来处理诸如点击、滚动、拖拽等用户输入事件。使用这些上层API能让开发者的工作变得非常简单，下面我们就来逐个学习下吧。
首先看点击。事实上，有些控件默认就是可以点击的，如Button。而有些则不能，如Box。

让一个默认不能点击的控件变得可以点击，并不一定非要使用pointerInput()函数，clickable()函数也能做到，并且代码会更加简洁。

```
@Composable
fun HighLevelCompose() {
    val context = LocalContext.current
    Box(modifier = Modifier
        .requiredSize(200.dp)
        .background(Color.Blue)
        .clickable {
            Toast.makeText(context, "Box is clicked", Toast.LENGTH_SHORT).show()
        }
    )
}
```
这里我们给Box添加了一个clickable()函数，那么当Box被点击的时候，clickable()函数闭包中的代码就会执行了
接下来是滚动。其实我们在上篇文章中已经演示过如何让一个控件布局可以滚动了,借助verticalScroll()函数就可以快速让Column布局可以在垂直方向上滚动了

再来看拖拽。draggable()函数允许让一个控件在水平或垂直方向上拖拽，并可以监听用户的拖拽距离，我们再根据返回的拖拽距离对控件进行相应的偏移，就可以实现拖拽效果了。

```
@Composable
fun HighLevelCompose() {
    var offsetX by remember { mutableStateOf(0f) }
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .requiredSize(200.dp)
            .background(Color.Blue)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    offsetX += delta
                })
    )
}
```

这里为了让控件能够偏移，引入了一个我们还没学过的知识点，State。关于这个知识点下篇文章中就会讲解，如果现在还看不懂的话也没关系，目前你只要了解draggable()函数的作用就足够了。

不过draggable()函数有一个弊端，它只能允许控件在水平或垂直方向上拖拽，不可以同时在水平和垂直方向上拖拽。所以如果你有这种特殊需求的话，那么就可以使用更加底层的pointerInput()函数来实现：

```
@Composable
fun HighLevelCompose() {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .requiredSize(200.dp)
            .background(Color.Blue)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    )
}
```

在pointerInput()函数内部，我们调用detectDragGestures来监听用户的拖拽手势，这样就可以同步获得用户在水平和垂直方向上的拖拽距离，并对控件进行相应的偏移了。
另外要记得，由于这是底层API，所以很多事情要自己做，比如事件处理完了，要记得调用consume()函数将它消费掉。

#### 串接顺序有影响

开篇的时候有提到过，Modifier是一个非常特殊的参数，它可以通过链式调用的方式串接无限多的API，从而实现各种你想要的效果。
而Modifier的链式调用模式对于串接的顺序是有要求的，不同的串接顺序可能实现的是不同的效果。这点和xml的区别非常大，因为xml对于属性的指定是没有顺序要求的，每个属性写在上面还是写在下面都无所谓

但是不用担心，这并不会导致Modifier变得更难使用，反而能够让你更加清楚自己在做什么。我们通过一个例子就可以快速了解了。

回到一开始IconImage()函数的例子，现在我们通过串接一个background()函数给它添加一个灰色的背景：

```
@Composable
fun IconImage() {
    Image(
        painter = painterResource(id = R.drawable.icon),
        contentDescription = "Icon Image",
        modifier = Modifier
            .wrapContentSize()
            .background(Color.Gray)
            .border(5.dp, Color.Magenta, CircleShape)
            .clip(CircleShape)
    )
}
```

运行一下程序，效果如下图所示：

![图片](/image/7-2.png)

其实这里的代码就已经开始有讲究了。

如果想要给图片增加一个背景色，background()函数一定要在border()和clip()函数之前调用才行，这样Compose的执行逻辑就是，先为图片指定了一个矩形灰色背景，然后再将图片裁剪成圆形，就出现了上图所示的效果。

如果把background()函数放在border()和clip()函数之后调用，Compose的执行逻辑就会变成，先把图片裁剪成圆形，然后再在圆形的基础上添加背景色，那么这个背景色也是圆形的，从而就完全看不到了。

下面继续对这个例子进行改造，现在我们想要为图片增加一些边距。Compose中为控件增加边距是借助Modifier.padding()函数实现的，如下所示：

```
@Composable
fun IconImage() {
    Image(
        painter = painterResource(id = R.drawable.icon),
        contentDescription = "Icon Image",
        modifier = Modifier
            .wrapContentSize()
            .background(Color.Gray)
            .border(5.dp, Color.Magenta, CircleShape)
            .padding(18.dp)
            .clip(CircleShape)
    )
}
```

这里我们调用Modifier.padding()函数给图片增加了18dp的边距。重新运行程序，效果如下图所示：

![图片](/image/7-3.png)

你会发现，增加的边距是属于内边距，边框的位置并没有变，只是里面内容的边距增加了。

出现这种现象的原因是，我们先调用的border()函数，再调用的padding()函数，因此边框的位置已经在设置边距之前就固定下来了，也就形成了内边距的效果。

那么很明显，改成先调用padding()函数，再调用border()函数，就可以实现外边距的效果：

```
@Composable
fun IconImage() {
    Image(
        painter = painterResource(id = R.drawable.icon),
        contentDescription = "Icon Image",
        modifier = Modifier
            .wrapContentSize()
            .background(Color.Gray)
            .padding(18.dp)
            .border(5.dp, Color.Magenta, CircleShape)
            .clip(CircleShape)
    )
}
```

重新运行一下程序看看吧：

![图片](/image/7-4.png)

借助Modifier的这个特性，其实我们只需要调整一下padding()函数的调用顺序，就能非常容易地控制控件的内外边距。在View系统中需要借助layout_marging和padding两个属性才能完成的工作，在Compose当中只需要一个padding()函数就能实现了。

因此你会发现，在Compose当中根本就没有layout_marging这个属性所对应的概念，因为它是不需要的。

#### 增加Modifier参数

开篇的时候还提到过，任何一个Composable函数都应该有一个Modifier参数才对，如果没有的话，那么就说明这个Composable函数写的有问题。

根据Google官方推荐的Compose编码规范，任何一个Composable函数它的第一个非强制参数都应该是Modifier，就像这样：

```
@Composable
fun TestComposable(a: Int, b: String, modifier: Modifier = Modifier) {
    
}
```

这个规范非常有讲究，因为Modifier是一个可选参数，因此它需要放到所有强制性参数的后面。这样调用方可以选择指定Modifier参数，也可以选择不指定。

而如果Modifier参数被放到了强制性参数的前面，那么就必须先指定Modifier参数，然后才能接着去指定强制性参数，或者就得使用参数名传参法，用法就变得不方便了。
现在我们明白了为什么Modifier参数要放到第一个非强制参数的位置，那么为什么每个Composable函数都应该有一个Modifier参数呢？这主要还是为了灵活性考虑的。

还是以刚才的IconImage()函数举例，IconImage()的作用应该是提供一个头像控件，所以它可以控制头像的形状、背景、边框、边距等等，但是它不应该控制头像的对齐方式。
这点应该很好理解，总不能说一个头像控件只能居中或者居左显示吧？

控件的对齐方式应该由它的父布局决定，父布局可以根据其自身的显示需求决定如何对齐这个头像控件，那么为了让IconImage()函数拥有这个灵活性，我们就需要为其添加一个Modifier参数，如下所示：

```
@Composable
fun ParentLayout(modifier: Modifier = Modifier) {
    Column {
        IconImage(Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun IconImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.icon),
        contentDescription = "Icon Image",
        modifier = modifier
            .wrapContentSize()
            .background(Color.Gray)
            .padding(18.dp)
            .border(5.dp, Color.Magenta, CircleShape)
            .clip(CircleShape)
    )
}
```

除了给IconImage()函数增加了Modifier参数之外，在为内部Image()控件指定行为的时候也要使用这个参数，而不是创建一个新的Modifier对象。
这样我们在任何调用IconImage()的地方，就都可以根据实际需求来指定它的对齐方式了。
这个例子充分展示了拥有Modifier参数的Composable函数具备更高的灵活性，Google提供的所有内置Composable函数都遵循了这个规范，因此希望你也能遵守吧。