package com.example.onlineshop.Activity


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import android.app.Activity
import androidx.activity.compose.LocalActivityResultRegistryOwner.current
import androidx.compose.foundation.clickable
import com.example.onlineshop.R
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Help
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

class ProfileActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private var user by mutableStateOf<GoogleSignInAccount?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Lấy lại tài khoản đã đăng nhập nếu có( đã đăng nhập bằng mail trước ấy)
        user = GoogleSignIn.getLastSignedInAccount(this)

        val fromAddToCart = intent.getBooleanExtra("fromAddToCart", false)

        val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                user = task.getResult(ApiException::class.java)
                if (
                    fromAddToCart && user!= null) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                user = null
            }
        }

        setContent {
            MaterialTheme {
                ProfileScreen(
                    user = user,
                    onSignInClick = { signInLauncher.launch(googleSignInClient.signInIntent) },
                    onSignOutClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            user = null
                        }
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    user: GoogleSignInAccount?,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = current as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8F5F2), Color(0xFFEDE0D4))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            // Greeting
            Text(
                text = if (user != null) "Chào mừng trở lại, ${user.displayName}!" else "Chào mừng trở lại!",
                fontSize = 20.sp,
                color = Color(0xFF8D6748),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Avatar
            Card(
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.size(110.dp)
            ) {
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color(0xFF8D6748)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = user?.displayName ?: "Khách",
                fontSize = 22.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user?.email ?: "Chưa đăng nhập",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Shortcut chức năng
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    ProfileShortcutItem(icon = Icons.Default.List, text = "Đơn hàng của tôi") { /* TODO: chuyển sang OrdersHistoryActivity */ }
                    Divider()
                    ProfileShortcutItem(icon = Icons.Default.LocationOn, text = "Địa chỉ giao hàng") { /* TODO: chuyển sang màn địa chỉ */ }
                    Divider()
                    ProfileShortcutItem(icon = Icons.Default.Help, text = "Hỗ trợ/FAQ") { /* TODO: chuyển sang hỗ trợ */ }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (user == null) {
                Button(onClick = onSignInClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6748))) {
                    Text("Đăng nhập với Google", color = Color.White)
                }
            } else {
                Button(onClick = onSignOutClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6748))) {
                    Text("Đăng xuất", color = Color.White)
                }
            }
        }

        // Bottom Menu
        BottomMenu(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            onItemClick = { index ->
                when (index) {
                    0 -> context.startActivity(Intent(context, MainActivity::class.java))
                    1 -> context.startActivity(Intent(context, CartActivity::class.java))
                    2 -> context.startActivity(Intent(context, FavoriteActivity::class.java))
                    3 -> context.startActivity(Intent(context, OrdersHistoryActivity::class.java))
                    4 -> {}
                }
            }
        )
    }
}

@Composable
fun ProfileShortcutItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF8D6748))
        Spacer(Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, color = Color.Black)
    }
}
