package com.example.onlineshop.Activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.shadow
import com.example.onlineshop.Model.ItemsModel
import com.example.onlineshop.ViewModel.MainViewModel

class SearchActivity : ComponentActivity() {
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SearchScreen(
                viewModel = viewModel,
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.loadAllItems().observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredItems = remember(allItems, searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            allItems.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header với nút Back và ô tìm kiếm
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.Black
                )
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Tìm kiếm sản phẩm...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Tìm kiếm",
                        tint = Color(0xFF8D6748)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Xoá",
                                tint = Color(0xFF8D6748)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF8D6748),
                    focusedLabelColor = Color(0xFF8D6748),
                    unfocusedLabelColor = Color(0xFF666666),
                    unfocusedLeadingIconColor = Color(0xFF8D6748),
                    unfocusedTrailingIconColor = Color(0xFF8D6748),
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color(0xFFF5F5F5),
                    errorContainerColor = Color(0xFFF5F5F5),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
        }

        // Kết quả tìm kiếm
        if (searchQuery.isNotBlank()) {
            if (filteredItems.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tìm thấy ${filteredItems.size} sản phẩm cho '$searchQuery'",
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredItems) { item ->
                            PopularItem(listOf(item), 0)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Không tìm thấy sản phẩm nào phù hợp với '$searchQuery'",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            "Tổng số sản phẩm trong cửa hàng: ${allItems.size}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            // Hướng dẫn tìm kiếm
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF8D6748)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Nhập từ khóa để tìm kiếm sản phẩm",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tìm kiếm theo tên sản phẩm hoặc mô tả",
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 