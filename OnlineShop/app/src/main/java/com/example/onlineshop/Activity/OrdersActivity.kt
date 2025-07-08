package com.example.onlineshop.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineshop.Helper.ManagmentCart
import com.example.onlineshop.Model.ItemsModel
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlineshop.Model.OrderModel
import com.example.onlineshop.Helper.TinyDB
import com.google.gson.Gson
import android.app.Activity
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.onlineshop.R
import androidx.compose.ui.text.input.KeyboardType
import com.google.android.gms.auth.api.signin.GoogleSignIn

class OrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val managmentCart = ManagmentCart(this)
        val cartItems = managmentCart.getListCart()

        setContent {
            MaterialTheme {
                OrdersScreen(
                    cartItems = cartItems,
                    onOrderConfirm = {name, phone, address, paymentMethod ->
                        val account = GoogleSignIn.getLastSignedInAccount(this)
                        val email = account?.email ?: ""
                        val order = OrderModel(
                            items = cartItems,
                            name = name,
                            phone = phone,
                            address = address,
                            paymentMethod = paymentMethod,
                            status = "Đang xử lý",
                            userEmail = email,
                            paymentStatus = if (paymentMethod == "Chuyển khoản") "Chờ chuyển khoản" else "Đã thanh toán"
                        )
                        if (paymentMethod == "Chuyển khoản") {
                            val intent = Intent(this, BankingActivity::class.java)
                            intent.putExtra("order", order)
                            startActivity(intent)
                        } else {
                            val tinyDB = TinyDB(this)
                            val gson = Gson()
                            val ordersJsonList = tinyDB.getListString("OrdersList")
                            val orders = ordersJsonList.map { gson.fromJson(it, OrderModel::class.java) }.toMutableList()
                            orders.add(order)
                            val newOrdersJsonList = orders.map { gson.toJson(it) }
                            tinyDB.putListString("OrdersList", ArrayList(newOrdersJsonList))
                            managmentCart.clearCart()
                            Toast.makeText(
                                this,
                                "Đặt hàng thành công!\nTên: $name\nSĐT: $phone\nĐịa chỉ: $address\nPTTT: $paymentMethod",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this, OrdersHistoryActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OrdersScreen(
    cartItems: List<ItemsModel>,
    onOrderConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Thanh toán bằng tiền mặt khi nhận hàng") }
    val context = LocalContext.current
    val activity = remember { context as? Activity }
    var phoneError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5F2))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 8.dp, end = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { activity?.finish() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                    contentDescription = "Back",
                    tint = Color(0xFF3A2D19)
                )
            }
            Text(
                text = "Xác nhận đặt hàng",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        // danh sach sp
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Sản phẩm:", fontWeight = FontWeight.Bold)
                LazyColumn {
                    items(cartItems) { item ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.title} x${item.numberInCart}")
                            Text("%,.0f đ".format(item.price * item.numberInCart))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // Form dien thong tin khach
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Họ tên người nhận") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = {
                if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                    phone = it
                }
            },
            label = { Text("Số điện thoại") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = phoneError,
            supportingText = {
                if (phoneError) Text("Số điện thoại phải đủ 10 số và chỉ chứa số!", color = Color.Red, fontSize = 12.sp)
            }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = address,
            onValueChange = {
                if (it.length <= 100) {
                    address = it
                    addressError = false
                } else {
                    addressError = true
                }
            },
            label = { Text("Địa chỉ nhận hàng") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            isError = addressError,
            supportingText = {
                if (addressError) Text("Địa chỉ tối đa 100 ký tự", color = Color.Red, fontSize = 12.sp)
            }
        )
        Spacer(Modifier.height(16.dp))
        Text("Phương thức thanh toán:", fontWeight = FontWeight.Bold)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = paymentMethod == "Tiền mặt khi nhận",
                    onClick = { paymentMethod = "Tiền mặt khi nhận" }
                )
                Text("Thanh toán bằng tiền mặt khi nhận hàng")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = paymentMethod == "Chuyển khoản",
                    onClick = { paymentMethod = "Chuyển khoản" }
                )
                Text("Chuyển khoản")
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                phoneError = phone.length != 10 || !phone.all { it.isDigit() }
                addressError = address.length > 100
                if (name.isBlank() || phoneError || address.isBlank() || addressError) {
                    return@Button
                }
                onOrderConfirm(name, phone, address, paymentMethod)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6748))
        ) {
            Text("Đặt hàng", fontSize = 18.sp, color = Color.White)
        }
    }
}
