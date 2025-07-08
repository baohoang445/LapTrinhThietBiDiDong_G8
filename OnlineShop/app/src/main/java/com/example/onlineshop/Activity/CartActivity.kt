package com.example.onlineshop.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.example.onlineshop.Helper.ChangeNumberItemsListener
import com.example.onlineshop.Helper.ManagmentCart
import com.example.onlineshop.Model.ItemsModel
import com.example.onlineshop.R

class CartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CartScreen ( ManagmentCart(this),
                onBackClick = {
                    finish()
                })
        }
    }
}

fun calculatorCart(managmentCart: ManagmentCart, tax: MutableState<Double>) {
    val percentTax = 0.02
    tax.value = Math.round((managmentCart.getTotalFee() * percentTax) * 100) / 100.0
}

@Composable
private fun CartScreen(
    managmentCart: ManagmentCart = ManagmentCart(LocalContext.current),
    onBackClick:() -> Unit
) {
    val cartItems = remember{ mutableStateOf(managmentCart.getListCart()) }
    val tax = remember{ mutableDoubleStateOf(0.0) }

    // Reload gio hang
    LaunchedEffect(Unit) {
        cartItems.value = managmentCart.getListCart()
        calculatorCart(managmentCart, tax)
    }
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ConstraintLayout(modifier = Modifier.padding(top = 36.dp)) {
            val (backBtn, cartTxt) = createRefs()
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(cartTxt) {centerTo(parent)}
                , text = "Giỏ hàng của bạn",
                    textAlign = TextAlign.Center,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
            )
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = null,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .constrainAs(backBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
            )
        }
        if(cartItems.value.isEmpty()){
            Text(
                text = "Giỏ hàng của bạn đang trống",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            CartList(cartItems = cartItems.value, managmentCart) {
                cartItems.value = managmentCart.getListCart()
                calculatorCart(managmentCart, tax)
            }
            CartSummary(
                itemTotal = managmentCart.getTotalFee(),
                tax = tax.value,
                delivery = 10.0
            )
        }
    }
}

@Composable
fun CartSummary(itemTotal: Double,
                tax: Double,
                delivery: Double) {
    val total = itemTotal + tax + delivery
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Thành Tiền:",
                Modifier.weight(1f),
                color = colorResource(R.color.darkBrown),
                fontWeight = FontWeight.Bold
            )
            Text(text = "%,.0f đ".format(itemTotal))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Tiền Thuế:",
                Modifier.weight(1f),
                color = colorResource(R.color.darkBrown),
                fontWeight = FontWeight.Bold
            )
            Text(text = "%,.0f đ".format(tax))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Phí giao hàng:",
                Modifier.weight(1f),
                color = colorResource(R.color.darkBrown),
                fontWeight = FontWeight.Bold
            )
            Text(text = "%,.0f đ".format(delivery))
        }
        Box(
            modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Tổng Cộng:",
                Modifier.weight(1f),
                color = colorResource(R.color.darkBrown),
                fontWeight = FontWeight.Bold
            )
            Text(text = "%,.0f đ".format(total))
        }
        Button(
            onClick = {
                // chuyen sang OrdersActivity

                context.startActivity(Intent(context, OrdersActivity::class.java))
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.darkBrown)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(50.dp)
        ) {
            Text(
                text = "Đặt Hàng Ngay",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun CartList(cartItems: ArrayList<ItemsModel>,
             managmentCart: ManagmentCart,
             onItemChange:() -> Unit
) {
    LazyColumn(
        Modifier.padding(top = 16.dp)
    ) {
            items(cartItems) { item->
                CartItem(
                    cartItems,
                    item = item,
                    managmentCart = managmentCart,
                    onItemChange = onItemChange
                )
            }
        }
}

@Composable
fun CartItem(
    cartItems: ArrayList<ItemsModel>,
    item: ItemsModel,
    managmentCart: ManagmentCart,
    onItemChange: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        val(pic, titleTxt, feeEachTime, totalEachItem, Quantity) = createRefs()

        Image(
            painter = rememberAsyncImagePainter(item.picUrl[0]),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(90.dp)
                .background(
                    colorResource(R.color.lightBrown),
                    shape = RoundedCornerShape(10.dp)
                )

                .constrainAs(pic) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )
        Text(
            text = item.title,
            modifier = Modifier
                .constrainAs(titleTxt) {
                    start.linkTo(pic.end)
                    top.linkTo(pic.top)
                }
                .padding(start = 8.dp, top = 8.dp)
        )
        Text(
            text = "%,.0f đ".format(item.price), color = colorResource(R.color.darkBrown)
            , modifier = Modifier
                .constrainAs(feeEachTime) {
                    start.linkTo(titleTxt.start)
                    top.linkTo(titleTxt.bottom)
                }
                .padding(start = 8.dp, top = 8.dp)
        )
        Text(
            text = "%,.0f đ".format(item.numberInCart*item.price),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .constrainAs(totalEachItem) {
                    start.linkTo(titleTxt.start)
                    bottom.linkTo(pic.bottom)
                }
                .padding(start = 8.dp)
        )
        ConstraintLayout (
            modifier = Modifier
                .width(100.dp)
                .constrainAs(Quantity) {
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .background(
                    colorResource(R.color.lightBrown),
                    shape = RoundedCornerShape(100.dp)
                )
        ) {
            val (plusCartBtn, minusCartBtn, numberItemText) = createRefs()
            Text(
                text = item.numberInCart.toString(),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.constrainAs(numberItemText) {
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
            )
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(28.dp)
                    .background(
                        colorResource(R.color.darkBrown),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .constrainAs(plusCartBtn) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .clickable {
                        managmentCart.plusItem(
                            managmentCart.getListCart(),
                            managmentCart.getListCart().indexOf(item),
                            object: ChangeNumberItemsListener{
                                override fun onChanged() {
                                    onItemChange()
                                }

                            }
                        )
                    }

            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(28.dp)
                    .background(
                        colorResource(R.color.white),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .constrainAs(minusCartBtn) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .clickable {
                        managmentCart.minusItem(
                            managmentCart.getListCart(),
                            managmentCart.getListCart().indexOf(item),
                            object: ChangeNumberItemsListener{
                                override fun onChanged() {
                                    onItemChange()
                                }
                            })
                    }
            ) {
                Text(
                    text = "-",
                    color = colorResource(R.color.darkBrown),
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}