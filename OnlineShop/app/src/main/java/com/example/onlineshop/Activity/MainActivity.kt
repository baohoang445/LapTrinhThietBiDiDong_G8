package com.example.onlineshop.Activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.onlineshop.R
import com.example.onlineshop.ViewModel.MainViewModel
import com.example.onlineshop.Model.SliderModel
import com.google.accompanist.pager.*
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.ColorFilter
import com.example.onlineshop.Model.CategoryModel
import com.google.accompanist.pager.ExperimentalPagerApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.onlineshop.Model.ItemsModel
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlineshop.Helper.TinyDB
import androidx.compose.material3.Scaffold

class MainActivity : BaseActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private var user: GoogleSignInAccount? = null
    private var selectedTab by mutableStateOf(0) // 0: Home,1: Cart, 2: Favorite, 3: Orders, 4: Profile
    private var displayName: String = ""

    override fun onResume() {
        super.onResume()
        val tinyDB = TinyDB(this)
        displayName = tinyDB.getString("user_display_name")
        // Gọi lại setContent để cập nhật tên
        setContent {
            MainAppUI(displayName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                user = task.getResult(ApiException::class.java)
            } catch (e: Exception) {
                user = null
            }
        }

        setContent {
            MainAppUI(displayName)
        }
    }

    @Composable
    fun MainAppUI(displayName: String) {
        if (selectedTab == 4) {
            MainProfileScreen(
                user = user,
                onSignInClick = { signInLauncher.launch(googleSignInClient.signInIntent) },
                onSignOutClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        user = null
                    }
                },
                onBackClick = { selectedTab = 0 }
            )
        } else {
            MainActivityScreen(
                displayName = displayName,
                onCartClick = { /* ... */ },
                onProfileClick = { selectedTab = 4 }
            )
        }
    }
}

@Composable
fun MainActivityScreen(displayName: String, onCartClick:()->Unit, onProfileClick: () -> Unit) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()
    val tinyDB = remember { TinyDB(context) }
    var displayName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        displayName = tinyDB.getString("user_display_name")
    }

    val banners = remember { mutableStateListOf<SliderModel> () }
    val categories = remember { mutableStateListOf<CategoryModel>() }
    val Popular = remember { mutableStateListOf<ItemsModel>() }

    var showBannerLoading by remember { mutableStateOf(true) }
    var showCategoryLoading by remember { mutableStateOf(true) }
    var showPopularLoading by remember { mutableStateOf(true) }

    //banner
    LaunchedEffect(Unit) {
        viewModel.loadBanner().observeForever{
            banners.clear()
            banners.addAll(it)
            showBannerLoading = false
        }
    }
    //category
    LaunchedEffect(Unit) {
        viewModel.loadCategory().observeForever{
            categories.clear()
            categories.addAll(it)
            showCategoryLoading = false
        }
    }
    //Popular
    LaunchedEffect(Unit) {
        viewModel.loadPopular().observeForever{
            Popular.clear()
            Popular.addAll(it)
            showPopularLoading = false
        }
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BottomMenu(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
                    .background(
                        colorResource(R.color.darkBrown),
                        shape = RoundedCornerShape(10.dp)
                    ),
                selectedIndex = 0,
                onItemClick = { index ->
                    when (index) {
                        0 -> {}
                        1 -> context.startActivity(Intent(context, CartActivity::class.java))
                        2 -> context.startActivity(Intent(context, FavoriteActivity::class.java))
                        3 -> context.startActivity(Intent(context, OrdersHistoryActivity::class.java))
                        4 -> context.startActivity(Intent(context, ProfileActivity::class.java))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            // Header, Banner, Brand, Popular, ...
            // --- Header ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.veryLightBrown))
                    .padding(top = 48.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    val account = GoogleSignIn.getLastSignedInAccount(context)
                    if (account?.photoUrl != null) {
                        AsyncImage(
                            model = account.photoUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(64.dp),
                            tint = colorResource(R.color.darkBrown)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    // Greeting chiếm hết không gian còn lại
                    val name = account?.displayName ?: displayName.ifBlank { "Khách" }
                    Text(
                        text = "Chào mừng trở lại, $name!",
                        color = colorResource(R.color.darkBrown),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    // Icon Search và Bell sát phải, cách đều
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    context.startActivity(Intent(context, SearchActivity::class.java))
                                }
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.bell_icon),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            // Banner
            if(showBannerLoading){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()
                }
            }else{
                Banners(banners)
            }
            // Official Brand
            Text(
                text="Thương hiệu",
                color=Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .padding(horizontal = 16.dp)
            )
            if(showCategoryLoading){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()
                }
            }else{
                CategoryList(categories)
            }
            // Sản phẩm phổ biến
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(title = "Phổ biến nhất", actionText = "Tất cả")
                if(showPopularLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }else{
                    ListItems(Popular, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun CategoryList(categories: SnapshotStateList<CategoryModel>) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val context = LocalContext.current

    LazyRow(modifier = Modifier
        .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp)
    ) {

        items(categories.size) {index: Int ->
            CategoryItem(item = categories[index], isSelected = selectedIndex == index,
                onItemClick = {
                    selectedIndex = index
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(context, ListItemsActivity::class.java).apply {
                            putExtra("id", categories[index].id.toString())
                            putExtra("title", categories[index].title)
                        }
                        startActivity(context, intent, null)
                    }, 500)
                }
            )

         }
    }
}

@Composable
fun CategoryItem(item:CategoryModel, isSelected:Boolean, onItemClick:()->Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onItemClick), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = (item.picUrl),
            contentDescription = item.title,
            modifier = Modifier
                .size(if(isSelected) 60.dp else 50.dp)
                .background(
                    color = if(isSelected) colorResource(R.color.darkBrown)else colorResource(R.color.lightBrown),
                    shape = RoundedCornerShape(100.dp)
                ),
            contentScale = ContentScale.Inside,
            colorFilter = if(isSelected) {
                ColorFilter.tint(Color.White)
            }else{
                ColorFilter.tint(Color.Black)
            }
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Text(
            text = item.title,
            color = colorResource(R.color.darkBrown),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }

}

@Composable
fun SectionTitle(title: String, actionText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = title,
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = actionText,
            color = colorResource(R.color.darkBrown),
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Banners(banners: List<SliderModel>) {
    AutoSlidingCarousel(banners = banners)
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AutoSlidingCarousel(
    modifier: Modifier = Modifier.padding(top = 16.dp),
    pagerState: PagerState = remember { PagerState() },
    banners: List<SliderModel>
) {
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(count = banners.size, state = pagerState) { page ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(banners[page].url)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
                    .height(150.dp),
                contentScale = ContentScale.FillBounds
            )
                    }
        DotIndicator(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.CenterHorizontally),
            totalDots = banners.size,
            selectedIndex = if(isDragged) pagerState.currentPage else pagerState.currentPage,
            dotSize = 8.dp
        )
    }
}
@Composable
fun DotIndicator(
    modifier: Modifier = Modifier,
    totalDots:Int,
    selectedIndex:Int,
    selectedColor: Color = colorResource(R.color.darkBrown),
    unSelectedColor: Color = colorResource(R.color.grey),
    dotSize:Dp
){
    LazyRow(
        modifier = modifier
            .wrapContentSize()
    ) {
       items(totalDots){index ->
           IndicatorDot(
               color = if(index == selectedIndex) selectedColor else unSelectedColor,
               size = dotSize
           )
           if(index != totalDots-1){
               Spacer(modifier = Modifier.padding(horizontal = 2.dp))
           }
       }
    }
}
@Composable
    fun IndicatorDot(
        modifier: Modifier = Modifier,
        size: Dp,
        color: Color
    ){
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )

    }


@Composable
fun BottomMenu(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    onItemClick: (Int) -> Unit
) {
    val items = listOf(
        Pair(R.drawable.btn_1, "Khám Phá"),
        Pair(R.drawable.btn_2, "Giỏ Hàng"),
        Pair(R.drawable.btn_3, "Yêu Thích"),
        Pair(R.drawable.btn_4, "Đơn Hàng"),
        Pair(R.drawable.btn_5, "Tài Khoản")
    )
    Row(
        modifier = modifier
            .height(70.dp)
            .background(
                color = Color(0xFF3A2D19),
                shape = RoundedCornerShape(16.dp)
            ),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onItemClick(index) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = item.first),
                    contentDescription = item.second,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = item.second,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun MainProfileScreen(
    user: GoogleSignInAccount?,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        if (user?.photoUrl != null) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                modifier = Modifier.size(96.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user?.displayName ?: "Khách",
            fontSize = 22.sp,
            color = Color.Black
        )
        Text(
            text = user?.email ?: "Chưa đăng nhập",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        if (user == null) {
            Button(onClick = onSignInClick) {
                Text("Đăng nhập với Google")
            }
        } else {
            Button(onClick = onSignOutClick) {
                Text("Đăng xuất")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
            Text("Quay lại trang chủ", color = Color.Black)
        }
    }
}

