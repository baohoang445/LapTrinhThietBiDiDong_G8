package com.example.onlineshop.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlineshop.Helper.FavoriteManager
import com.example.onlineshop.Model.ItemsModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.livedata.observeAsState
import com.example.onlineshop.Helper.ManagmentCart
import com.example.onlineshop.ViewModel.MainViewModel

class FavoriteActivity : ComponentActivity() {
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FavoriteScreen(viewModel = viewModel)
        }
    }

    @Composable
    fun FavoriteScreen(viewModel: MainViewModel) {
        val context = LocalContext.current
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val userEmail = account?.email ?: ""

        var favoriteIds by remember { mutableStateOf(setOf<String>()) }
        LaunchedEffect(userEmail) {
            favoriteIds = FavoriteManager.getFavoriteIds(context, userEmail)
        }
        val allProducts by viewModel.loadAllItems().observeAsState(emptyList())
        val favoriteProducts = allProducts.filter { favoriteIds.contains(it.id) }

        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header với nút Back
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { (context as? ComponentActivity)?.finish() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = "Sản phẩm yêu thích",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(Modifier.height(16.dp))

                if (favoriteProducts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bạn chưa có sản phẩm yêu thích nào!")
                    }
                } else {
                    LazyColumn {
                        items(favoriteProducts) { product ->
                            ProductItem(product) {
                                favoriteIds = FavoriteManager.getFavoriteIds(context, userEmail)
                            }
                        }
                    }
                }
            }
            // BottomMenu ở dưới cùng
            BottomMenu(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
                selectedIndex = 2, // Yêu thích
                onItemClick = { index ->
                    when (index) {
                        0 -> context.startActivity(Intent(context, MainActivity::class.java))
                        1 -> context.startActivity(Intent(context, CartActivity::class.java))
                        2 -> {} // Đang ở Yêu thích
                        3 -> context.startActivity(Intent(context, OrdersHistoryActivity::class.java))
                        4 -> context.startActivity(Intent(context, ProfileActivity::class.java))
                    }
                }
            )
        }
    }

    @Composable
    fun ProductItem(item: ItemsModel, onFavoriteChanged: (() -> Unit)? = null) {
        val context = LocalContext.current
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val userEmail = account?.email ?: ""
        var isFavorite by remember {
            mutableStateOf(
                FavoriteManager.isFavorite(
                    context,
                    userEmail,
                    item.id
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    // chi tiet sp
                    val intent = Intent(context, DetailActivity::class.java)
                    intent.putExtra("object", item)
                    context.startActivity(intent)
                }
        ) {
            // anh sp
            Image(
                painter = rememberAsyncImagePainter(model = item.picUrl.firstOrNull()),
                contentDescription = item.title,
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 12.dp)
            )
            // ten & gia sp
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = "%,.0f đ".format(item.price),
                    color = Color(0xFF8D6748),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            // xoa khoi yeu thich
            IconButton(
                onClick = {
                    FavoriteManager.removeFavorite(context, userEmail, item.id)
                    isFavorite = false
                    onFavoriteChanged?.invoke()
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Bỏ yêu thích",
                    tint = Color.Red
                )
            }
            // them vao gio hang
            Button(
                onClick = {
                    val managmentCart = ManagmentCart(context)
                    item.numberInCart = 1
                    managmentCart.insertItem(item)
                    FavoriteManager.removeFavorite(context, userEmail, item.id)
                    Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                    onFavoriteChanged?.invoke()
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Thêm vào giỏ")
            }
        }
    }
}
