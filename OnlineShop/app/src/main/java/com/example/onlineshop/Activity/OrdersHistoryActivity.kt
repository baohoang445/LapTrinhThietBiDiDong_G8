package com.example.onlineshop.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.onlineshop.R
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlineshop.Helper.TinyDB
import com.example.onlineshop.Model.OrderModel
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import kotlin.random.Random
import kotlin.math.absoluteValue
import com.google.android.gms.auth.api.signin.GoogleSignIn

class OrdersHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tinyDB = TinyDB(this)
        val gson = Gson()
        val ordersJsonList = tinyDB.getListString("OrdersList")
        val account = GoogleSignIn.getLastSignedInAccount(this)
        val email = account?.email ?: ""
        val orders = ordersJsonList.map { gson.fromJson(it, OrderModel::class.java) }
            .filter { it.userEmail == email }
        setContent {
            MaterialTheme {
                OrdersHistoryScreen(orders)
            }
        }
    }
}

@Composable
fun OrdersHistoryScreen(orders: List<OrderModel>) {
    val context = LocalContext.current
    val activity = context as? Activity

    val currentTime by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    var selectedOrder by remember { mutableStateOf<OrderModel?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F5F2))
                .padding(16.dp)
        ) {
            // Tiêu đề + Back
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
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
                    text = "Lịch sử đơn hàng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (orders.isEmpty()) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF8D6748)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Bạn chưa có đơn hàng nào", color = Color.Gray, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(context, MainActivity::class.java))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6748))
                    ) {
                        Text("Mua sắm ngay", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                LazyColumn {
                    items(orders.reversed()) { order ->
                        val timeDiff = currentTime - order.time
                        val minutesPassed = timeDiff / (1000 * 60)

                        // Chỉ đổi trạng thái nếu chưa bị hủy
                        val (status, statusColor) = when {
                            order.status == "Đã hủy" -> "Đã hủy" to Color(0xFFD32F2F)
                            minutesPassed < 1 -> "Đang xử lý" to Color(0xFFFFA000)
                            minutesPassed < 3 -> "Đang giao" to Color(0xFF2196F3)
                            else -> "Đã giao" to Color(0xFF4CAF50)
                        }
                        val total = order.items.sumOf { it.price * it.numberInCart }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    selectedOrder = order
                                },
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(12.dp)
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = order.items.firstOrNull()?.picUrl?.firstOrNull(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Mã đơn: ${order.id.takeLast(6)}", fontWeight = FontWeight.Bold)
                                    Text(
                                        "Ngày: " + SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.time)),
                                        fontSize = 13.sp, color = Color.Gray
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(statusColor, shape = CircleShape)
                                        )
                                        Text(
                                            text = status,
                                            color = statusColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }
                                    Text(
                                        "Tổng tiền: %,.0f đ".format(total),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8D6748),
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Chi tiết",
                                    tint = Color(0xFF8D6748),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pop-up chi tiết đơn hàng
        selectedOrder?.let { order ->
            val timeDiff = currentTime - order.time
            val minutesPassed = timeDiff / (1000 * 60)
            val (status, statusColor) = when {
                order.status == "Đã hủy" -> "Đã hủy" to Color(0xFFD32F2F)
                minutesPassed < 1 -> "Đang xử lý" to Color(0xFFFFA000)
                minutesPassed < 3 -> "Đang giao" to Color(0xFF2196F3)
                else -> "Đã giao" to Color(0xFF4CAF50)
            }
            AlertDialog(
                onDismissRequest = { selectedOrder = null },
                confirmButton = {
                    Row {
                        // Nút Hủy đơn hàng chỉ hiện nếu chưa giao và chưa hủy
                        if (order.status != "Đã hủy" && status != "Đã giao") {
                            Button(
                                onClick = {
                                    // Cập nhật status
                                    order.status = "Đã hủy"
                                    // Lưu lại vào TinyDB
                                    val tinyDB = TinyDB(context)
                                    val gson = Gson()
                                    val ordersJsonList = tinyDB.getListString("OrdersList")
                                    val orders = ordersJsonList.map { gson.fromJson(it, OrderModel::class.java) }.toMutableList()
                                    val idx = orders.indexOfFirst { it.id == order.id }
                                    if (idx != -1) {
                                        orders[idx] = order
                                        val newOrdersJsonList = orders.map { gson.toJson(it) }
                                        tinyDB.putListString("OrdersList", ArrayList(newOrdersJsonList))
                                    }
                                    selectedOrder = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6748))
                            ) {
                                Text("Hủy đơn hàng", color = Color.White)
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        TextButton(onClick = { selectedOrder = null }) {
                            Text("Đóng")
                        }
                    }
                },
                title = { Text("Chi tiết đơn hàng") },
                text = {
                    Column {
                        Text("Tên: ${order.name}")
                        Text("SĐT: ${order.phone}")
                        Text("Địa chỉ: ${order.address}")
                        Text("PTTT: ${order.paymentMethod}")
                        Spacer(Modifier.height(6.dp))
                        Text("Trạng thái: ", fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(statusColor, shape = CircleShape)
                            )
                            Text(
                                text = status,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Sản phẩm:", fontWeight = FontWeight.Bold)
                        order.items.forEach {
                            Text("- ${it.title} x${it.numberInCart}")
                        }
                    }
                }
            )
        }

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
                    3 -> {}
                    4 -> context.startActivity(Intent(context, ProfileActivity::class.java))
                }
            }
        )
    }
}

