package com.example.finlogcalc.mycar.menu

import android.net.Uri
import androidx.core.net.toUri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MinorCrash
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.finlogcalc.R

// --- Data Classes for New Features ---
data class CarHealth(
    val overall: Int,
    val engine: Int,
    val brakes: Int,
    val tires: Int,
    val battery: Int
)

data class CarActivity(
    val id: Int,
    val title: String,
    val date: String,
    val icon: ImageVector,
    val type: String // "fuel", "maintenance", "trip"
)

// --- Main CarInfo Data Class ---
data class CarInfo(
    val id: String = "my_car", // Singleton for now
    var makeModel: String = "",
    var year: String = "",
    var color: String = "",
    var licensePlate: String = "",
    var mileage: String = "",
    var fuelType: String = "",
    var acquisitionDate: String = "",
    var purchasePrice: String = "",
    var notes: String = "",
    var imageUri: String? = null,
    // New fields
    var vinNumber: String = "",
    var transmissionType: String = "",
    var driveType: String = "",
    var engineVolume: String = "", // e.g., "3.0 л"
    var enginePower: String = "",  // e.g., "340 л.с."
    var insuranceEndDate: String = "", // e.g., "10.10.2024"

    // Dummy data for new features (ViewModel will manage these for display)
    var healthStatus: CarHealth = CarHealth(85, 92, 78, 85, 95),
    var recentActivities: List<CarActivity> = listOf(
        CarActivity(1, "Заправка", "2 дня назад", Icons.Filled.LocalGasStation, "fuel"),
        CarActivity(2, "Замена масла", "1 неделя назад", Icons.Filled.OilBarrel, "maintenance"),
        CarActivity(3, "Поездка", "3 дня назад", Icons.Filled.TravelExplore, "trip")
    )
)

// --- ViewModel ---
class MyCarViewModel : ViewModel() {
    var carInfo by mutableStateOf<CarInfo?>(null)
    var isEditing by mutableStateOf(false)

    // Form fields (existing)
    var makeModelInput by mutableStateOf(TextFieldValue())
    var yearInput by mutableStateOf(TextFieldValue())
    var colorInput by mutableStateOf(TextFieldValue())
    var licensePlateInput by mutableStateOf(TextFieldValue())
    var mileageInput by mutableStateOf(TextFieldValue())
    var fuelTypeInput by mutableStateOf(TextFieldValue())
    var acquisitionDateInput by mutableStateOf(TextFieldValue())
    var purchasePriceInput by mutableStateOf(TextFieldValue())
    var notesInput by mutableStateOf(TextFieldValue())
    var imageUriInput by mutableStateOf<Uri?>(null)

    // Form fields (new)
    var vinNumberInput by mutableStateOf(TextFieldValue())
    var transmissionTypeInput by mutableStateOf(TextFieldValue())
    var driveTypeInput by mutableStateOf(TextFieldValue())
    var engineVolumeInput by mutableStateOf(TextFieldValue())
    var enginePowerInput by mutableStateOf(TextFieldValue())
    var insuranceEndDateInput by mutableStateOf(TextFieldValue())


    fun loadCarData() {
        // TODO: Load from SharedPreferences or Database
        if (carInfo == null) {
            // Initialize with default/empty values if no data exists
            val defaultCar = CarInfo()
            carInfo = defaultCar
            isEditing = true // Automatically enter edit mode for first-time setup
            populateFormFields(defaultCar)
        } else {
            populateFormFields(carInfo!!)
            isEditing = false
        }
    }

    private fun populateFormFields(data: CarInfo) {
        makeModelInput = TextFieldValue(data.makeModel)
        yearInput = TextFieldValue(data.year)
        colorInput = TextFieldValue(data.color)
        licensePlateInput = TextFieldValue(data.licensePlate)
        mileageInput = TextFieldValue(data.mileage)
        fuelTypeInput = TextFieldValue(data.fuelType)
        acquisitionDateInput = TextFieldValue(data.acquisitionDate)
        purchasePriceInput = TextFieldValue(data.purchasePrice)
        notesInput = TextFieldValue(data.notes)
        imageUriInput = data.imageUri?.let { it.toUri() }

        // Populate new fields
        vinNumberInput = TextFieldValue(data.vinNumber)
        transmissionTypeInput = TextFieldValue(data.transmissionType)
        driveTypeInput = TextFieldValue(data.driveType)
        engineVolumeInput = TextFieldValue(data.engineVolume)
        enginePowerInput = TextFieldValue(data.enginePower)
        insuranceEndDateInput = TextFieldValue(data.insuranceEndDate)
    }

    fun saveCarInfo() {
        carInfo = CarInfo(
            makeModel = makeModelInput.text,
            year = yearInput.text,
            color = colorInput.text,
            licensePlate = licensePlateInput.text,
            mileage = mileageInput.text,
            fuelType = fuelTypeInput.text,
            acquisitionDate = acquisitionDateInput.text,
            purchasePrice = purchasePriceInput.text,
            notes = notesInput.text,
            imageUri = imageUriInput?.toString(),
            // Save new fields
            vinNumber = vinNumberInput.text,
            transmissionType = transmissionTypeInput.text,
            driveType = driveTypeInput.text,
            engineVolume = engineVolumeInput.text,
            enginePower = enginePowerInput.text,
            insuranceEndDate = insuranceEndDateInput.text,
            // Retain dummy health and activities if not edited
            healthStatus = carInfo?.healthStatus ?: CarHealth(85, 92, 78, 85, 95),
            recentActivities = carInfo?.recentActivities ?: listOf(
                CarActivity(1, "Заправка", "2 дня назад", Icons.Filled.LocalGasStation, "fuel"),
                CarActivity(2, "Замена масла", "1 неделя назад", Icons.Filled.OilBarrel, "maintenance"),
                CarActivity(3, "Поездка", "3 дня назад", Icons.Filled.TravelExplore, "trip")
            )
        )
        isEditing = false
        // TODO: Persist carInfo (e.g., SharedPreferences, Database)
    }

    fun startEditing() {
        if (carInfo != null) {
            populateFormFields(carInfo!!)
        } else {
            populateFormFields(CarInfo())
        }
        isEditing = true
    }

    fun cancelEditing() {
        if (carInfo != null) {
            populateFormFields(carInfo!!)
            isEditing = false
        }
    }

    fun updateImageUri(uri: Uri?) {
        imageUriInput = uri
    }
}

// --- Navigation Routes ---
sealed class MyCarScreenRoute(val route: String) {
    object CarDetails : MyCarScreenRoute("car_details")
    object CarInput : MyCarScreenRoute("car_input")
}

// --- Main Feature Composable ---
@Composable
fun MyCarFeatureNavHost(
    modifier: Modifier = Modifier,
    mainNavController: NavHostController,
    scaffoldPadding: PaddingValues
) {
    val internalNavController = rememberNavController()
    val carViewModel: MyCarViewModel = viewModel()

    LaunchedEffect(Unit) {
        carViewModel.loadCarData()
    }

    val startDestination = if (carViewModel.carInfo != null && !carViewModel.isEditing && carViewModel.carInfo?.makeModel?.isNotEmpty() == true) {
        MyCarScreenRoute.CarDetails.route
    } else {
        MyCarScreenRoute.CarInput.route
    }

    NavHost(
        navController = internalNavController,
        startDestination = startDestination,
        modifier = modifier.padding(scaffoldPadding)
    ) {
        composable(MyCarScreenRoute.CarInput.route) {
            CarInputScreen(
                viewModel = carViewModel,
                onSave = {
                    carViewModel.saveCarInfo()
                    internalNavController.navigate(MyCarScreenRoute.CarDetails.route) {
                        popUpTo(MyCarScreenRoute.CarInput.route) { inclusive = true }
                    }
                },
                onCancel = {
                    carViewModel.cancelEditing()
                    if (carViewModel.carInfo != null && carViewModel.carInfo?.makeModel?.isNotEmpty() == true) {
                         internalNavController.navigate(MyCarScreenRoute.CarDetails.route) {
                            popUpTo(MyCarScreenRoute.CarInput.route) { inclusive = true }
                        }
                    } else {
                        mainNavController.popBackStack()
                    }
                },
                mainNavController = mainNavController
            )
        }
        composable(MyCarScreenRoute.CarDetails.route) {
            val car = carViewModel.carInfo
            if (car != null) {
                CarDetailsScreen(
                    carInfo = car,
                    onEdit = {
                        carViewModel.startEditing()
                        internalNavController.navigate(MyCarScreenRoute.CarInput.route)
                    },
                    mainNavController = mainNavController,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LaunchedEffect(Unit) {
                    internalNavController.navigate(MyCarScreenRoute.CarInput.route) {
                        popUpTo(MyCarScreenRoute.CarDetails.route) { inclusive = true }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarInputScreen(
    viewModel: MyCarViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateImageUri(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.carInfo?.makeModel?.isNotEmpty() == true) stringResource(
                    R.string.common_edit) + " " + stringResource(R.string.main_menu_button_my_car) else stringResource(
                    R.string.my_car_add_info_button)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.carInfo != null && viewModel.carInfo?.makeModel?.isNotEmpty() == true) {
                            onCancel()
                        } else {
                            mainNavController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
                            R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.imageUriInput != null) {
                        AsyncImage(
                            model = viewModel.imageUriInput,
                            contentDescription = stringResource(R.string.my_car_image_description),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.ic_car_placeholder),
                            placeholder = painterResource(id = R.drawable.ic_car_placeholder)
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddAPhoto,
                                contentDescription = stringResource(R.string.my_car_add_image_action_desc),
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.my_car_tap_to_add_image_text),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.makeModelInput,
                    onValueChange = { viewModel.makeModelInput = it },
                    label = { Text(stringResource(R.string.my_car_brand_label) + "/" + stringResource(
                        R.string.my_car_model_label)) },
                    placeholder = { Text(stringResource(R.string.my_car_brand_hint) + "/" + stringResource(
                        R.string.my_car_model_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.DirectionsCar, contentDescription = stringResource(
                        R.string.my_car_make_model_icon_desc)) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.yearInput,
                    onValueChange = { viewModel.yearInput = it },
                    label = { Text(stringResource(R.string.my_car_year_label)) },
                    placeholder = { Text(stringResource(R.string.my_car_year_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = stringResource(
                        R.string.my_car_year_icon_desc)) },
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                 OutlinedTextField(
                    value = viewModel.acquisitionDateInput,
                    onValueChange = { viewModel.acquisitionDateInput = it },
                    label = { Text(stringResource(R.string.my_car_acquisition_date_label)) },
                    placeholder = { Text(stringResource(R.string.my_car_acquisition_date_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.Event, contentDescription = stringResource(R.string.my_car_acquisition_date_icon_desc)) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.purchasePriceInput,
                    onValueChange = { viewModel.purchasePriceInput = it },
                    label = { Text(stringResource(R.string.my_car_purchase_price_label)) },
                    placeholder = { Text(stringResource(R.string.my_car_purchase_price_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.MonetizationOn, contentDescription = stringResource(
                        R.string.my_car_purchase_price_icon_desc)) },
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.colorInput,
                    onValueChange = { viewModel.colorInput = it },
                    label = { Text("Цвет") },
                    leadingIcon = { Icon(Icons.Outlined.ColorLens, contentDescription = stringResource(
                        R.string.my_car_color_icon_desc)) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.licensePlateInput,
                    onValueChange = { viewModel.licensePlateInput = it },
                    label = { Text(stringResource(R.string.my_car_license_plate_label)) },
                    placeholder = { Text(stringResource(R.string.my_car_license_plate_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.ConfirmationNumber, contentDescription = stringResource(
                        R.string.my_car_license_plate_icon_desc)) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.mileageInput,
                    onValueChange = { viewModel.mileageInput = it },
                    label = { Text("Пробег (км)") },
                    leadingIcon = { Icon(Icons.Outlined.Speed, contentDescription = stringResource(R.string.my_car_mileage_icon_desc)) },
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                 OutlinedTextField(
                    value = viewModel.fuelTypeInput,
                    onValueChange = { viewModel.fuelTypeInput = it },
                    label = { Text(stringResource(R.string.my_car_fuel_type_label)) },
                    placeholder = { Text(stringResource(R.string.my_car_fuel_type_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.LocalGasStation, contentDescription = stringResource(
                        R.string.my_car_fuel_type_icon_desc)) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // New Input Fields
                OutlinedTextField(
                    value = viewModel.vinNumberInput,
                    onValueChange = { viewModel.vinNumberInput = it },
                    label = { Text("VIN номер") },
                    placeholder = { Text("Введите VIN номер") },
                    leadingIcon = { Icon(Icons.Filled.QuestionMark, contentDescription = "VIN") }, 
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.transmissionTypeInput,
                    onValueChange = { viewModel.transmissionTypeInput = it },
                    label = { Text("Коробка передач") },
                    placeholder = { Text("Например, Автомат") },
                    leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = "Коробка передач") }, 
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.driveTypeInput,
                    onValueChange = { viewModel.driveTypeInput = it },
                    label = { Text("Привод") },
                    placeholder = { Text("Например, Полный") },
                    leadingIcon = { Icon(Icons.Filled.MinorCrash, contentDescription = "Привод") }, 
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.engineVolumeInput,
                    onValueChange = { viewModel.engineVolumeInput = it },
                    label = { Text("Объем двигателя") },
                    placeholder = { Text("Например, 3.0 л") },
                    leadingIcon = { Icon(Icons.Filled.Build, contentDescription = "Объем двигателя") }, 
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.enginePowerInput,
                    onValueChange = { viewModel.enginePowerInput = it },
                    label = { Text("Мощность") },
                    placeholder = { Text("Например, 340 л.с.") },
                    leadingIcon = { Icon(Icons.Filled.Power, contentDescription = "Мощность двигателя") }, 
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.insuranceEndDateInput,
                    onValueChange = { viewModel.insuranceEndDateInput = it },
                    label = { Text("Страховка до (ДД.ММ.ГГГГ)") },
                    placeholder = { Text("Например, 10.10.2024") },
                    leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Дата окончания страховки") }, 
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                OutlinedTextField(
                    value = viewModel.notesInput,
                    onValueChange = { viewModel.notesInput = it },
                    label = { Text("Дополнительные заметки") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = stringResource(R.string.my_car_notes_icon_desc)) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Spacer(modifier = Modifier.weight(1f, fill = false))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text(stringResource(R.string.common_cancel))
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text(stringResource(R.string.common_save))
                    }
                }
                 Spacer(modifier = Modifier.height(8.dp))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailsScreen(
    carInfo: CarInfo,
    onEdit: () -> Unit,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(carInfo.makeModel.ifEmpty { stringResource(R.string.my_car_details_title) }) },
                navigationIcon = {
                    IconButton(onClick = { mainNavController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
                            R.string.common_back))
                    }
                },
                 colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onEdit,
                icon = { Icon(Icons.Filled.Edit, stringResource(R.string.common_edit)) },
                text = { Text(stringResource(R.string.common_edit)) }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        content = { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Main Card with Gradient Image ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E293B), Color(0xFF1E3A8A)),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        ) {
                            AsyncImage(
                                model = carInfo.imageUri?.let { it.toUri() } ?: R.drawable.ic_car_placeholder,
                                contentDescription = stringResource(R.string.my_car_image_description),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.ic_car_placeholder),
                                placeholder = painterResource(id = R.drawable.ic_car_placeholder)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                            startY = 0.5f * 220.dp.value
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = carInfo.makeModel.ifEmpty { "Мой Автомобиль" },
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${carInfo.year} ${stringResource(R.string.my_car_model_year_suffix)}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.my_car_mileage_label_short),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "${carInfo.mileage} км",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Fuel & Insurance Badges within the main card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .padding(top = 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.EvStation, contentDescription = "Топливо", tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Топливо", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(carInfo.fuelType.ifEmpty { "-" }, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Страховка", tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Страховка", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(carInfo.insuranceEndDate.ifEmpty { "-" }, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // --- Health Status Section ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Состояние автомобиля",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Overall Health", tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${carInfo.healthStatus.overall}%", color = Color(0xFF22C55E), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        HealthProgressBar(
                            label = "Двигатель",
                            value = carInfo.healthStatus.engine,
                            icon = Icons.Filled.Build,
                            iconTint = Color(0xFF3B82F6)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HealthProgressBar(
                            label = "Тормоза",
                            value = carInfo.healthStatus.brakes,
                            icon = Icons.Filled.CarCrash,
                            iconTint = Color(0xFFF97316)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HealthProgressBar(
                            label = "Шины",
                            value = carInfo.healthStatus.tires,
                            icon = Icons.Filled.TireRepair,
                            iconTint = Color(0xFF22C55E)
                        )
                    }
                }

                // --- Quick Actions ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Filled.LocalGasStation,
                        text = "Заправка",
                        onClick = { /* TODO: Navigate to refueling feature */ },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Build,
                        text = "ТО",
                        onClick = { /* TODO: Navigate to maintenance feature */ },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Map,
                        text = "Поездка",
                        onClick = { /* TODO: Navigate to trip feature */ },
                        modifier = Modifier.weight(1f)
                    )
                }

                // --- Recent Activities ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Последние активности",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            carInfo.recentActivities.forEach { activity ->
                                RecentActivityItem(activity = activity)
                            }
                        }
                    }
                }

                // --- Additional Information ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Информация о машине",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = onEdit) {
                                Icon(Icons.Filled.Edit, contentDescription = "Изменить", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Изменить")
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CarDetailItemNew(label = "VIN номер", value = carInfo.vinNumber)
                            CarDetailItemNew(label = "Цвет", value = carInfo.color)
                            CarDetailItemNew(label = "Коробка передач", value = carInfo.transmissionType)
                            CarDetailItemNew(label = "Привод", value = carInfo.driveType)
                            CarDetailItemNew(label = "Объем двигателя", value = carInfo.engineVolume)
                            CarDetailItemNew(label = "Мощность", value = carInfo.enginePower)
                            CarDetailItemNew(label = stringResource(R.string.my_car_year_label), value = carInfo.year)
                            CarDetailItemNew(label = stringResource(R.string.my_car_license_plate_label), value = carInfo.licensePlate)
                            CarDetailItemNew(label = stringResource(R.string.my_car_acquisition_date_label), value = carInfo.acquisitionDate)
                            CarDetailItemNew(label = stringResource(R.string.my_car_purchase_price_label), value = carInfo.purchasePrice)
                            if (carInfo.notes.isNotBlank()) {
                                CarDetailItemNew(label = "Заметки", value = carInfo.notes)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    )
}

@Composable
fun CarDetailItemNew(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value.ifEmpty { "-" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.55f)
        )
    }
}

@Composable
fun HealthProgressBar(label: String, value: Int, icon: ImageVector, iconTint: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Text("$value%", style = MaterialTheme.typography.bodyLarge, color = if (value < 80) Color(0xFFF97316) else Color(0xFF22C55E))
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (value < 80) Color(0xFFF97316) else Color(0xFF22C55E),
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        }
    }
}

@Composable
fun RecentActivityItem(activity: CarActivity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { /* TODO: Handle activity click */ }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val backgroundColor = when (activity.type) {
            "fuel" -> Color(0xFFDBEAFE) // blue-100
            "maintenance" -> Color(0xFFFEF3C7) // orange-100
            "trip" -> Color(0xFFD1FAE5) // green-100
            else -> MaterialTheme.colorScheme.primaryContainer
        }
        val contentColor = when (activity.type) {
            "fuel" -> Color(0xFF2563EB) // blue-600
            "maintenance" -> Color(0xFFEA580C) // orange-600
            "trip" -> Color(0xFF059669) // green-600
            else -> MaterialTheme.colorScheme.onPrimaryContainer
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(activity.icon, contentDescription = activity.title, tint = contentColor, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(activity.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(activity.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Подробнее", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
