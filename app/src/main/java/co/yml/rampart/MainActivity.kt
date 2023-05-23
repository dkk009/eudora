package co.yml.rampart

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.rampart.MainActivity.Companion.TAG
import coil.compose.AsyncImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class MainActivity : ComponentActivity() {

    companion object{
        const val TAG = "DETECTED STRING"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HomeScreen(
                context = this@MainActivity

            )
        }
    }
}

@Composable
fun HomeScreen(context: Context) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var detectedObjects by remember { mutableStateOf("") }
    var detectedStr by remember { mutableStateOf("") }
    var finalTxt by remember { mutableStateOf("") }


    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri
            detectedObjects = ""
            detectedStr = ""
            finalTxt = ""
            if (uri != null) {
                val image = InputImage.fromFilePath(context, uri)
                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        for (label in labels) {
                            val text = label.text
                            val confidence = label.confidence
                            val index = label.index
                            println("Found $text with confidence $confidence")
                            detectedObjects = "$detectedObjects\n$text - ${confidence.toString().percent()}"
//                            detectedObjects = "$detectedObjects\n$text-$confidence"
                        }
                    }
                    .addOnFailureListener { e -> e.printStackTrace() }
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->

                        for (block in visionText.textBlocks) {
                            val boundingBox = block.boundingBox
                            val cornerPoints = block.cornerPoints
                            var description = ""
                            Log.d(TAG, "Bounding box : $boundingBox")
                            val text = block.text
                            Log.d(TAG, "Block is : $block")
                            Log.d(TAG, "LIne inside Block is : ${block.lines}")
                            for (line in block.lines) {
                                var sentence = ""
                                Log.d(TAG, "Line is : $line")
                                for (element in line.elements) {
                                    Log.d(TAG, "Element is : $element")
                                    var word = ""
                                    for (symbol in element.symbols) {
                                        word += symbol.text
                                        println(symbol.text)
                                        detectedStr += symbol.text
                                    }
                                    sentence += " $word"
                                    Log.d(TAG, "word is : $word")
                                }
                                Log.d(TAG, "Sentence is : $sentence")
                                description += "$sentence\n"
                            }
                            Log.d(TAG, "Description is : $description")
                           finalTxt += description
                        }
                    }
                    .addOnFailureListener {

                    }

            }


        }
    )

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = selectedImageUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            singlePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text(text = "Pick photo")
        }

        Text(
            text = detectedObjects,
            style = TextStyle(
                color = Color.DarkGray,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.W800,
                fontStyle = FontStyle.Normal,
            ),
        )

        Text(
            text = finalTxt,
            style = TextStyle(
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.W800,
                fontStyle = FontStyle.Normal,
            )
        )
    }
}


private fun processTextBlock(result: Text) {
    val resultText = result.text
    for (block in result.textBlocks) {
        val blockText = block.text
        val blockCornerPoints = block.cornerPoints
        val blockFrame = block.boundingBox
        for (line in block.lines) {
            val lineText = line.text
            val lineCornerPoints = line.cornerPoints
            val lineFrame = line.boundingBox
            for (element in line.elements) {
                val elementText = element.text
                val elementCornerPoints = element.cornerPoints
                val elementFrame = element.boundingBox
                for (symbol in element.symbols) {
                    val symbolText = symbol.text
                    val symbolCornerPoints = symbol.cornerPoints
                    val symbolFrame = symbol.boundingBox
                }
            }
        }
    }
}

fun String.percent(): String {
    if (this.isEmpty()) return ""
    return this.take(4).drop(2)+"%"

}