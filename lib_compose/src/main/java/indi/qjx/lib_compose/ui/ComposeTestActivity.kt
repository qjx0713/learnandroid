package indi.qjx.lib_compose.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import indi.qjx.lib_compose.R
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
                    HighLevelCompose()
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
        SimpleWidgetColumn()
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
        TextField(
            value = "",
            onValueChange = {},
            placeholder = {
                Text(text = "Type something here")
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White
            )
        )
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

