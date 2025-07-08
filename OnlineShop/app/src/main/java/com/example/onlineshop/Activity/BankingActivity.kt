package com.example.onlineshop.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlineshop.Helper.TinyDB
import com.example.onlineshop.Model.OrderModel
import com.google.gson.Gson
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import androidx.compose.foundation.text.KeyboardOptions
import android.graphics.Color as GColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.delay

class BankingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val order = intent.getSerializableExtra("order") as? OrderModel
        setContent {
            if (order != null) {
                BankingScreen(order = order, onPaymentSuccess = {
                    // Lưu đơn hàng với trạng thái đã thanh toán
                    order.paymentStatus = "Đã thanh toán"
                    val tinyDB = TinyDB(this)
                    val gson = Gson()
                    val ordersJsonList = tinyDB.getListString("OrdersList")
                    val orders = ordersJsonList.map { gson.fromJson(it, OrderModel::class.java) }.toMutableList()
                    orders.add(order)
                    val newOrdersJsonList = orders.map { gson.toJson(it) }
                    tinyDB.putListString("OrdersList", ArrayList(newOrdersJsonList))
                    // Xóa giỏ hàng
                    val managmentCart = com.example.onlineshop.Helper.ManagmentCart(this)
                    managmentCart.clearCart()
                    // Quay về lịch sử đơn hàng
                    val intent = Intent(this, OrdersHistoryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                })
            } else {
                Text("Không tìm thấy đơn hàng", color = Color.Red)
            }
        }
    }
}

@Composable
fun BankingScreen(order: OrderModel, onPaymentSuccess: () -> Unit) {
    var paid by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showInputCode by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(5) }
    val amount = order.items.sumOf { it.price * it.numberInCart }
    val momoPhone = "0389590258"
    val qrContent = "momo://?action=transfer&amount=${amount.toInt()}&receiver=$momoPhone&comment=ORDER-${order.id.takeLast(6)}"
    val qrBitmap = remember(qrContent) { generateQrCode(qrContent) }

    // Countdown effect
    if (paid && countdown > 0) {
        LaunchedEffect(countdown) {
            delay(1000)
            countdown--
        }
    }
    // Tự động chuyển về lịch sử đơn hàng khi countdown == 0
    if (paid && countdown == 0) {
        LaunchedEffect(Unit) {
            onPaymentSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5F2))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!paid) {
            Text("Quét mã QR để thanh toán Chuyển khoản", fontSize = 20.sp, color = Color(0xFF8D6748))
            Spacer(Modifier.height(24.dp))
            if (qrBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Chuyển khoản QR",
                    modifier = Modifier.size(220.dp)
                )
            } else {
                Text("Không tạo được mã QR", color = Color.Red)
            }
            Spacer(Modifier.height(24.dp))
            Text("Số tiền: %,.0f đ".format(amount), fontSize = 18.sp, color = Color.Black)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { showConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6748))
            ) {
                Text("Tôi đã chuyển khoản", color = Color.White)
            }
        } else {
            Text("Đơn hàng sẽ được xác nhận sau khi shop kiểm tra giao dịch. Vui lòng giữ lại biên lai chuyển khoản.", color = Color(0xFF8D6748), fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            if (countdown > 0) {
                Text("Bạn sẽ được chuyển về lịch sử đơn hàng sau $countdown giây...", color = Color.Gray)
            }
        }
    }

    // Dialog xác nhận
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        showInputCode = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6748))
                ) { Text("Đúng, tôi đã chuyển khoản", color = Color.White) }
            },
            dismissButton = {
                Button(onClick = { showConfirmDialog = false }) { Text("Huỷ") }
            },
            title = { Text("Xác nhận chuyển khoản") },
            text = { Text("Bạn chắc chắn đã chuyển khoản thành công cho số tiền này chứ?") }
        )
    }
    // Dialog nhập 4 số cuối giao dịch
    if (showInputCode) {
        AlertDialog(
            onDismissRequest = { showInputCode = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.length == 4 && code.all { it.isDigit() }) {
                            showInputCode = false
                            paid = true
                            codeError = false
                        } else {
                            codeError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6748))
                ) { Text("Xác nhận", color = Color.White) }
            },
            dismissButton = {
                Button(onClick = { showInputCode = false }) { Text("Huỷ") }
            },
            title = { Text("Nhập 4 số cuối mã giao dịch Chuyển khoản") },
            text = {
                Column {
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            if (it.length <= 4) code = it
                        },
                        label = { Text("4 số cuối giao dịch") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = codeError,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (codeError) Text("Vui lòng nhập đúng 4 số!", color = Color.Red, fontSize = 12.sp)
                }
            }
        )
    }
}

fun generateQrCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) GColor.BLACK else GColor.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
} 