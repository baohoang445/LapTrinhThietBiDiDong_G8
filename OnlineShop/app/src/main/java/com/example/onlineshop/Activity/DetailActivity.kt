package com.example.onlineshop.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.example.onlineshop.Helper.FavoriteManager
import com.example.onlineshop.Helper.ManagmentCart
import com.example.onlineshop.Model.ItemsModel
import com.example.onlineshop.R
import com.google.android.gms.auth.api.signin.GoogleSignIn


class DetailActivity : BaseActivity() {
    private lateinit var item: ItemsModel
    private lateinit var managementCart: ManagmentCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = intent.getSerializableExtra("object") as ItemsModel
        managementCart = ManagmentCart(this)

        setContent {
            DetailScreen(
                item = item,
                onBackClick = { finish() },
                onAddToCartClick = { selectedModelIndex ->
                    val account = GoogleSignIn.getLastSignedInAccount(this)
                    if (account == null) {
                        android.widget.Toast.makeText(this, "Bạn cần đăng nhập để thêm vào giỏ hàng!", android.widget.Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ProfileActivity::class.java) // chuyển sang màn đăng nhập
                        intent.putExtra("fromAddToCart", true)
                        startActivity(intent)
                        finish()
                    } else{
                        if (selectedModelIndex != -1) {
                            item.numberInCart = 1
                            item.selectedModel = item.model[selectedModelIndex]
                            managementCart.insertItem(item)
                        } else {
                            android.widget.Toast.makeText(this, "Bạn vui lòng chọn size trước!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onCartClick = {
                    startActivity(Intent(this, CartActivity::class.java))
                }
            )
        }
    }

    @Composable
    private fun DetailScreen(
        item: ItemsModel,
        onBackClick: () -> Unit,
        onAddToCartClick: (Int) -> Unit,
        onCartClick: () -> Unit
    ) {
        val context = LocalContext.current
        var selectedImageUrl by remember { mutableStateOf(item.picUrl.first()) }
        var selectedModelIndex by remember { mutableIntStateOf(-1) }
        var showSelectModelWarning by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .height(430.dp)
                    .padding(bottom = 16.dp)
            ) {
                val (back, fav, mainImage, thumbnail) = createRefs()

                Image(
                    painter = rememberAsyncImagePainter(model = selectedImageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            colorResource(R.color.lightBrown),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .constrainAs(mainImage) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )

                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(top = 48.dp, start = 16.dp)
                        .clickable { onBackClick() }
                        .constrainAs(back) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                        }
                )

                // fav
                val account = GoogleSignIn.getLastSignedInAccount(context)
                val userEmail = account?.email ?: ""
                var isFavorite by remember { mutableStateOf(FavoriteManager.isFavorite(context, userEmail, item.id)) }

                IconButton(
                    onClick = {
                      if (isFavorite) {
                          FavoriteManager.removeFavorite(context, userEmail, item.id)
                      } else {
                          FavoriteManager.addFavorite(context, userEmail, item.id)
                      }
                        isFavorite = !isFavorite
                    },
                    modifier = Modifier
                        .padding(top = 48.dp, end = 16.dp)
                        .constrainAs(fav) {
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                        }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Yêu Thích",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }

                LazyRow(
                    modifier = Modifier
                        .background(
                            color = colorResource(R.color.white),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(vertical = 16.dp)
                        .constrainAs(thumbnail) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                ) {
                    items(item.picUrl) { imageUrl ->
                        ImageThumbnail(
                            imageUrl = imageUrl,
                            isSelected = selectedImageUrl == imageUrl,
                            onClick = { selectedImageUrl = imageUrl }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 23.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(end = 16.dp),
                )
                Text(
                    text = "%,.0f đ".format(item.price),
                    fontSize = 22.sp
                )

            }
            RatingBar(rating = item.rating)

            ModelSelector(
                models = item.model,
                selectedModelIndex = selectedModelIndex,
                onModelSelected = {
                    selectedModelIndex = it
                    showSelectModelWarning = false
                }
            )

            Text(
                text = item.description,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier.background(
                        colorResource(R.color.lightBrown),
                        shape = RoundedCornerShape(10.dp)
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.btn_2),
                        contentDescription = "Cart",
                        tint = Color.Black
                    )
                }
                Button(
                    onClick = {
                        if (selectedModelIndex != -1) {
                            onAddToCartClick(selectedModelIndex)
                        } else {
                            showSelectModelWarning = true
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.darkBrown),
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Thêm vào giỏ hàng",
                        fontSize = 18.sp
                    )
                }
            }
            if (showSelectModelWarning) {
                Text(
                    text = "Vui lòng chọn Size trước khi thêm vào giỏ hàng!",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }

    private @Composable
    fun ModelSelector(
        models: ArrayList<String>,
        selectedModelIndex: Int,
        onModelSelected: (Int) -> Unit
    ) {
        LazyRow(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
            itemsIndexed(models) { index, model ->
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .height(40.dp)
                        .then(
                            if (index == selectedModelIndex) {
                                Modifier.border(
                                    1.dp,
                                    colorResource(R.color.darkBrown),
                                    RoundedCornerShape(10.dp)
                                )
                            } else {
                                Modifier.border(
                                    1.dp,
                                    colorResource(R.color.darkBrown),
                                    RoundedCornerShape(10.dp)
                                )
                            }
                        )
                        .background(
                            if (index == selectedModelIndex)
                                colorResource(R.color.darkBrown)
                            else
                                colorResource(R.color.white),
                                shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onModelSelected(index) }
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = model,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (index == selectedModelIndex) colorResource(R.color.white)
                        else colorResource(R.color.black),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    @Composable
    private fun RatingBar(rating: Double) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp).padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Selected Model",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Image(
                painter = painterResource(R.drawable.star),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "$rating Rating", style = MaterialTheme.typography.bodyMedium)
        }
    }

    private @Composable
     fun ImageThumbnail(imageUrl: String, isSelected: Boolean, onClick: () -> Unit) {
        val backColor = if(isSelected) colorResource(R.color.darkBrown) else
            colorResource(R.color.veryLightBrown)

        Box(
            modifier = Modifier
                .padding(4.dp)
                .size(55.dp)
                .then(
                    if (isSelected) Modifier.border(
                        1.dp,
                        colorResource(R.color.darkBrown),
                        RoundedCornerShape(10.dp)
                    ) else {
                        Modifier
                    }
                )
                .background(backColor, shape = RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}