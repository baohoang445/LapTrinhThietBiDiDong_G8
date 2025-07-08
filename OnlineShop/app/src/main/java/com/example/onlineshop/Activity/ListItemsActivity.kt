package com.example.onlineshop.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.ui.platform.LocalContext
import com.example.onlineshop.Helper.FavoriteManager
import com.example.onlineshop.Model.ItemsModel
import com.example.onlineshop.R
import com.example.onlineshop.ViewModel.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.lazy.items

class ListItemsActivity : BaseActivity() {
    private val viewModel = MainViewModel()
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = intent.getStringExtra("id") ?: ""
        title = intent.getStringExtra("title") ?: ""

        setContent {
            ListItemsScreen(
                title = title,
                onBackClick = { finish() },
                viewModel = viewModel,
                id = id
            )
        }
    }

    @Composable
    private fun ListItemsScreen(
        title: String,
        onBackClick: () -> Unit,
        viewModel: MainViewModel,
        id: String
    ) {
        val items by viewModel.loadFiltered(id).observeAsState(emptyList())

        LaunchedEffect(id) {
            viewModel.loadFiltered(id)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ConstraintLayout(
                modifier = Modifier
                    .padding(top = 36.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                val (backBtn, cartTxt) = createRefs()

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(cartTxt) { centerTo(parent) },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    text = title
                )

                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBackClick() }
                        .constrainAs(backBtn) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                )
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ListItemsFullSize(items)
            }
        }
    }

    @Composable
    fun ListItemsFullSize(items: List<ItemsModel>) {
        val context = LocalContext.current
        LazyColumn {
            items(items) { item ->
                ProductItem(item, context)
            }
        }
    }

    @Composable
    fun ProductItem(item: ItemsModel, context: android.content.Context) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val userEmail = account?.email ?: ""
        var isFavorite by remember { mutableStateOf(FavoriteManager.isFavorite(context, userEmail, item.id)) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 8.dp)
                .clickable {
                    val intent = Intent(context, DetailActivity::class.java)
                    intent.putExtra("object", item)
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
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
                // Icon tym
                IconButton(
                    onClick = {
                        if (isFavorite) {
                            FavoriteManager.removeFavorite(context, userEmail, item.id)
                        } else {
                            FavoriteManager.addFavorite(context, userEmail, item.id)
                        }
                        isFavorite = !isFavorite
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }
        }
    }
}
