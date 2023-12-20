package indi.qjx.lib_compose.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import indi.qjx.lib_compose.R
import indi.qjx.lib_compose.viewmodel.ComposeTestVM
import kotlin.math.roundToInt

class ComposeTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContent函数会提供一个Composable作用域，所以在它的闭包中我们就可以随意地调用Composable函数了
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //基础控件和布局
//                    SimpleWidgetColumn()

                    //modifier功能1：修改Compose控件的尺寸、布局、行为和样式。
//                    IconImage()

                    //modifier功能2：处理用户的输入
//                    PointerInputEvent()

                    //modifier功能3：使控件可点击、滚动、拖拽
//                    HighLevelCompose()

                    //state
//                    CallCounter()

                    //LazyColumn和LazyRow
//                    ScrollableList2()

                    //  rememberLazyListState
//                    MainLayout()
                    
                    //嵌套滚动情况一，内外滚动方向不一致
//                    VerticalScrollable()

                    //嵌套滚动情况二，内外滚动方向一致
//                    VerticalScrollable2()

                    //拼接不同类型子项
//                    ScrollableList3(rememberLazyListState())

                    //提升Lazy Layout性能
                    SubVerticalScrollable2()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

/**
 * 所有的Composable函数还有一个约定俗成的习惯，就是函数的命名首字母需要大写。
 */
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        CallCounter()
    }
}

/**
 * text和button使用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleWidgetColumn() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        /**
         * Button
         */
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
        /**
         * TextField
         */
        var userInput by remember { mutableStateOf("") }
        TextFieldWidget(userInput, { newValue ->
            userInput = newValue
        })

        /**
         * Image
         */
        Image(
            painter = painterResource(id = R.drawable.zn_img_smart_speaker),
            contentDescription = "A dog image",
            modifier = Modifier
                .wrapContentSize(align = Alignment.CenterStart)
                .border(5.dp, Color.Magenta, CircleShape)
                .clip(CircleShape)
        )

        /**
         * coil库中的网络图片加载
         */
        AsyncImage(
            model = "https://img-blog.csdnimg.cn/20200401094829557.jpg",
            contentDescription = "First line of code"
        )


        /**
         * CircularProgressIndicator
         */
        CircularProgressIndicator(
            color = Color.Green,
            strokeWidth = 6.dp
        )

        /**
         * LinearProgressIndicator
         */
        LinearProgressIndicator(
            color = Color.Blue,
            trackColor = Color.Gray
        )
    }

}

@Composable
fun TextFieldWidget(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
    )
}


/**
 *  modifier功能1：修改Compose控件的尺寸、布局、行为和样式。
 */
@Composable
fun IconImage() {
    Image(
        painter = painterResource(id = R.drawable.zn_img_smart_speaker),
        contentDescription = "Icon Image",
        modifier = Modifier
            .wrapContentSize(align = Alignment.CenterStart)
            .border(5.dp, Color.Magenta, CircleShape)
            .clip(CircleShape)
            .rotate(180f)
    )
}

/**
 *  modifier功能2：处理用户的输入
 */
@Composable
fun PointerInputEvent() {

    Column {
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

        Box(modifier = Modifier
            .requiredSize(200.dp)
            .background(Color.Red)

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
}

/**
 *  modifier功能3：使控件可点击、滚动、拖拽
 */
@Composable
fun HighLevelCompose() {
    //点击
    /*    val context = LocalContext.current
        Box(modifier = Modifier
            .requiredSize(200.dp)
            .background(Color.Blue)
            .clickable {
                Toast
                    .makeText(context, "Box is clicked", Toast.LENGTH_SHORT)
                    .show()
            }
        )*/

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


@Composable
fun Counter(count: Int, onIncrement: () -> Unit, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            fontSize = 50.sp
        )
        Button(
            onClick = { onIncrement() }
        ) {
            Text(
                text = "Click me",
                fontSize = 26.sp
            )
        }
    }
}

@Composable
fun CallCounter(modifier: Modifier = Modifier, viewModel: ComposeTestVM = viewModel()) {
    //使用liveData
    /*    val count by viewModel.count.observeAsState(0)
        val doubleCount by viewModel.doubleCount.observeAsState(0)*/

    //使用Flow
    val count by viewModel.count.collectAsState(0)
    val doubleCount by viewModel.doubleCount.collectAsState(0)
    Column {
        Counter(
            count = count,
            onIncrement = { viewModel.incrementCount() },
            modifier.fillMaxWidth()
        )
        Counter(
            count = doubleCount,
            onIncrement = { viewModel.incrementDoubleCount() },
            modifier.fillMaxWidth()
        )
    }
}

//不带下标
@Composable
fun ScrollableList() {
    val list = ('A'..'Z').map { it.toString() }
    LazyColumn {
        items(list.size) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Text(
                    text = list[index],
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}


//带下标
@Composable
fun ScrollableList2() {
    val list = ('A'..'Z').map { it.toString() }
    LazyColumn(contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        itemsIndexed(list) { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Text(
                    text = item,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}


//实现当A元素在屏幕上可见的时候，Fab按钮也是可见的。当A元素滑出了屏幕，Fab按钮也会随之消失
@Composable
fun MainLayout() {
    val state = rememberLazyListState()
    Box {
        ScrollableList(state)
        val shouldShowAddButton = state.firstVisibleItemIndex  == 0
        AddButton(shouldShowAddButton)
    }
}

@Composable
fun ScrollableList(state: LazyListState) {
    val list = ('A'..'Z').map { it.toString() }
    LazyColumn(state = state) {
        items(list) { letter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = letter,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun BoxScope.AddButton(isVisible: Boolean) {
    if (isVisible) {
        FloatingActionButton(
            onClick = { /*TODO*/},
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, "Add Button")
        }
    }
}



//嵌套滚动情况一，内外滚动方向不一致
@Composable
fun VerticalScrollable() {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        HorizontalScrollable()
        for (i in 1..10) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Item $i",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun HorizontalScrollable() {
    val list = ('A'..'Z').map { it.toString() }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        items(list) { letter ->
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .height(200.dp)
            ) {
                Text(
                    text = letter,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}


//嵌套滚动情况二，内外滚动方向一致
@Composable
fun VerticalScrollable2() {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SubVerticalScrollable()
        for (i in 1..10) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Item $i",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun SubVerticalScrollable() {
    val list = ('A'..'Z').map { it.toString() }
    LazyColumn(modifier = Modifier.height(300.dp)) {
        items(list) { letter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = letter,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun ImageHeader() {
    Image(
        painterResource(id = R.drawable.zn_img_smart_speaker),
        contentDescription = "Header Image",
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    )
}

@Composable
fun ImageFooter() {
    Image(
        painterResource(id = R.drawable.zn_img_smart_speaker),
        contentDescription = "Header Image",
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    )
}

@Composable
fun ScrollableList3(state: LazyListState) {
    val list = (1..10).map { it.toString() }
    LazyColumn(state = state) {
        item {
            ImageHeader()
        }
        items(list) { letter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = letter,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
        item {
            ImageFooter()
        }
    }
}


@Composable
fun SubVerticalScrollable2() {
    val list = ('A'..'Z').map { it.toString() }
    LazyColumn(modifier = Modifier.height(300.dp)) {
        items(list, key = { it }) { letter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = letter,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}
