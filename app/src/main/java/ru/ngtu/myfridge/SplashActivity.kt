package ru.ngtu.myfridge

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import ru.ngtu.myfridge.ui.theme.MyFridgeTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyFridgeTheme {
                SplashScreen()
            }
        }

        // Проверка состояния авторизации
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        Handler(Looper.getMainLooper()).postDelayed({
            if (user != null) {
                // Если пользователь авторизован, сразу переходим в MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // Если не авторизован, показываем экран логина
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish() // Завершаем SplashActivity
        }, 2000)
    }
}

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDEFFF6)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.display),
            contentDescription = "Splash Image",
            modifier = Modifier
                .wrapContentSize()
                .padding(bottom = 16.dp)
        )

        Text(
            text = "My Fridge",
            style = TextStyle(
                fontSize = 36.sp,
                fontFamily = FontFamily(Font(R.font.harlow_solid_italic)),
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        )
    }
}