# Technical Specifications - Kotlin Implementation

## Overview
Detailed technical specifications for implementing the CEO Ticket Issuance Android application using modern Kotlin technologies and best practices.

## Architecture

### App Architecture Pattern
- **MVVM (Model-View-ViewModel)**: Clean separation of concerns
- **Repository Pattern**: Data access abstraction
- **Dependency Injection**: Hilt for DI
- **Clean Architecture**: Layered approach with domain, data, and presentation layers

### Project Structure
```
app/
├── src/main/java/com/ceo/ticketissuance/
│   ├── data/
│   │   ├── database/
│   │   │   ├── entities/
│   │   │   ├── dao/
│   │   │   └── AppDatabase.kt
│   │   ├── repository/
│   │   └── sync/
│   ├── domain/
│   │   ├── model/
│   │   ├── repository/
│   │   └── usecase/
│   ├── presentation/
│   │   ├── ui/
│   │   │   ├── login/
│   │   │   ├── dashboard/
│   │   │   ├── observation/
│   │   │   ├── countdown/
│   │   │   ├── issuance/
│   │   │   └── sync/
│   │   ├── viewmodel/
│   │   └── navigation/
│   ├── di/
│   ├── util/
│   └── MainActivity.kt
```

## Dependencies

### Core Dependencies
```kotlin
// build.gradle.kts (Module: app)
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose
    implementation("androidx.compose.ui:ui:1.5.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.8")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Camera
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // ML Kit
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
}
```

## Database Implementation

### Room Database Setup
```kotlin
@Database(
    entities = [
        User::class,
        Street::class,
        Contravention::class,
        VehicleMake::class,
        VehicleModel::class,
        Observation::class,
        Ticket::class,
        SyncQueueEntry::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun streetDao(): StreetDao
    abstract fun contraventionDao(): ContraventionDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun observationDao(): ObservationDao
    abstract fun ticketDao(): TicketDao
    abstract fun syncQueueDao(): SyncQueueDao
}
```

### Entity Examples
```kotlin
@Entity(tableName = "observations")
data class Observation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vrm: String,
    val streetId: Long,
    val contraventionId: Long,
    val makeId: Long,
    val modelId: Long,
    val valvePositionFront: Int?,
    val valvePositionRear: Int?,
    val observationStartTime: LocalDateTime,
    val observationEndTime: LocalDateTime?,
    val status: ObservationStatus,
    val photoPath: String?,
    val userId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticketNumber: String,
    val vrm: String,
    val streetId: Long,
    val contraventionId: Long,
    val makeId: Long,
    val modelId: Long,
    val color: String,
    val valvePositionFront: Int?,
    val valvePositionRear: Int?,
    val observationId: Long?,
    val issueTime: LocalDateTime,
    val penaltyAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val photoPath: String,
    val notes: String?,
    val status: TicketStatus,
    val userId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

### DAOs
```kotlin
@Dao
interface ObservationDao {
    @Query("SELECT * FROM observations WHERE status = :status")
    fun getObservationsByStatus(status: ObservationStatus): Flow<List<Observation>>
    
    @Insert
    suspend fun insertObservation(observation: Observation): Long
    
    @Update
    suspend fun updateObservation(observation: Observation)
    
    @Query("SELECT * FROM observations WHERE id = :id")
    suspend fun getObservationById(id: Long): Observation?
}

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<Ticket>>
    
    @Insert
    suspend fun insertTicket(ticket: Ticket): Long
    
    @Query("SELECT COUNT(*) FROM tickets WHERE DATE(createdAt) = :date")
    suspend fun getTicketCountByDate(date: String): Int
}
```

## Repository Implementation

### Repository Pattern
```kotlin
interface ObservationRepository {
    fun getActiveObservations(): Flow<List<Observation>>
    suspend fun createObservation(observation: Observation): Long
    suspend fun updateObservation(observation: Observation)
    suspend fun getObservationById(id: Long): Observation?
}

@Singleton
class ObservationRepositoryImpl @Inject constructor(
    private val observationDao: ObservationDao,
    private val syncQueueManager: SyncQueueManager
) : ObservationRepository {
    
    override fun getActiveObservations(): Flow<List<Observation>> {
        return observationDao.getObservationsByStatus(ObservationStatus.ACTIVE)
    }
    
    override suspend fun createObservation(observation: Observation): Long {
        val id = observationDao.insertObservation(observation)
        syncQueueManager.addToQueue(
            operationType = OperationType.UPLOAD_OBSERVATION,
            tableName = "observations",
            recordId = id,
            data = observation
        )
        return id
    }
}
```

## Use Cases

### Use Case Implementation
```kotlin
@Singleton
class CreateObservationUseCase @Inject constructor(
    private val observationRepository: ObservationRepository,
    private val countdownManager: CountdownManager,
    private val ticketNumberGenerator: TicketNumberGenerator
) {
    suspend operator fun invoke(
        vrm: String,
        streetId: Long,
        contraventionId: Long,
        makeId: Long,
        modelId: Long,
        valvePositionFront: Int?,
        valvePositionRear: Int?,
        photoPath: String?,
        userId: Long
    ): Result<Long> {
        return try {
            val observation = Observation(
                vrm = vrm,
                streetId = streetId,
                contraventionId = contraventionId,
                makeId = makeId,
                modelId = modelId,
                valvePositionFront = valvePositionFront,
                valvePositionRear = valvePositionRear,
                observationStartTime = LocalDateTime.now(),
                status = ObservationStatus.ACTIVE,
                photoPath = photoPath,
                userId = userId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            val id = observationRepository.createObservation(observation)
            countdownManager.startCountdown(id)
            
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## ViewModels

### ViewModel Implementation
```kotlin
@HiltViewModel
class ObservationViewModel @Inject constructor(
    private val createObservationUseCase: CreateObservationUseCase,
    private val getStreetsUseCase: GetStreetsUseCase,
    private val getContraventionsUseCase: GetContraventionsUseCase,
    private val getVehicleMakesUseCase: GetVehicleMakesUseCase,
    private val cameraManager: CameraManager,
    private val anprProcessor: ANPRProcessor
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ObservationUiState())
    val uiState = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<ObservationEvent>()
    val events = _events.asSharedFlow()
    
    fun processAnprFrame(image: InputImage) {
        viewModelScope.launch {
            val result = anprProcessor.processFrame(image)
            result.onSuccess { vrm ->
                _uiState.update { it.copy(vrm = vrm, isTimerRunning = true) }
                startObservationTimer()
            }
        }
    }
    
    fun submitObservation(
        vrm: String,
        streetId: Long,
        contraventionId: Long,
        makeId: Long,
        modelId: Long,
        valvePositionFront: Int?,
        valvePositionRear: Int?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = createObservationUseCase(
                vrm = vrm,
                streetId = streetId,
                contraventionId = contraventionId,
                makeId = makeId,
                modelId = modelId,
                valvePositionFront = valvePositionFront,
                valvePositionRear = valvePositionRear,
                photoPath = _uiState.value.photoPath,
                userId = getCurrentUserId()
            )
            
            result.onSuccess { observationId ->
                _events.emit(ObservationEvent.NavigateToCountdown(observationId))
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
}
```

### UI State Management
```kotlin
data class ObservationUiState(
    val vrm: String = "",
    val selectedStreetId: Long? = null,
    val selectedContraventionId: Long? = null,
    val selectedMakeId: Long? = null,
    val selectedModelId: Long? = null,
    val valvePositionFront: Int? = null,
    val valvePositionRear: Int? = null,
    val photoPath: String? = null,
    val isTimerRunning: Boolean = false,
    val elapsedTime: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val streets: List<Street> = emptyList(),
    val contraventions: List<Contravention> = emptyList(),
    val vehicleMakes: List<VehicleMake> = emptyList(),
    val vehicleModels: List<VehicleModel> = emptyList()
)

sealed class ObservationEvent {
    data class NavigateToCountdown(val observationId: Long) : ObservationEvent()
    object NavigateBack : ObservationEvent()
    data class ShowError(val message: String) : ObservationEvent()
}
```

## Camera Implementation

### Camera Manager
```kotlin
@Singleton
class CameraManager @Inject constructor(
    private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    
    suspend fun initializeCamera(): ProcessCameraProvider {
        return ProcessCameraProvider.getInstance(context).await()
    }
    
    fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        imageAnalyzer: ImageAnalysis.Analyzer
    ) {
        viewModelScope.launch {
            val cameraProvider = initializeCamera()
            
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), imageAnalyzer)
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        }
    }
}
```

### ANPR Processing
```kotlin
@Singleton
class ANPRProcessor @Inject constructor() {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    suspend fun processFrame(image: InputImage): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val result = textRecognizer.process(image).await()
                val detectedText = result.textBlocks
                    .flatMap { it.lines }
                    .map { it.text }
                    .firstOrNull { isValidUKPlate(it) }
                
                if (detectedText != null) {
                    Result.success(formatUKPlate(detectedText))
                } else {
                    Result.failure(Exception("No valid UK plate detected"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun isValidUKPlate(text: String): Boolean {
        val cleanText = text.replace(" ", "").uppercase()
        val ukPlatePattern = Regex("^[A-Z]{2}[0-9]{2}[A-Z]{3}$")
        return ukPlatePattern.matches(cleanText)
    }
    
    private fun formatUKPlate(text: String): String {
        val cleanText = text.replace(" ", "").uppercase()
        return "${cleanText.substring(0, 2)}${cleanText.substring(2, 4)} ${cleanText.substring(4)}"
    }
}
```

## Compose UI Implementation

### Screen Composables
```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ObservationScreen(
    viewModel: ObservationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCountdown: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ObservationEvent.NavigateToCountdown -> onNavigateToCountdown(event.observationId)
                is ObservationEvent.NavigateBack -> onNavigateBack()
                is ObservationEvent.ShowError -> { /* Show error */ }
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (cameraPermission.status.isGranted) {
            CameraPreview(
                onVrmDetected = viewModel::processAnprFrame,
                modifier = Modifier.weight(1f)
            )
            
            ObservationForm(
                uiState = uiState,
                onSubmit = viewModel::submitObservation,
                onVrmChange = viewModel::updateVrm,
                onStreetChange = viewModel::updateStreet,
                onContraventionChange = viewModel::updateContravention,
                onMakeChange = viewModel::updateMake,
                onModelChange = viewModel::updateModel,
                modifier = Modifier.weight(1f)
            )
        } else {
            PermissionRequest(
                permission = cameraPermission,
                onPermissionGranted = { /* Handle permission granted */ }
            )
        }
    }
}
```

### Custom Composables
```kotlin
@Composable
fun CameraPreview(
    onVrmDetected: (InputImage) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = remember { CameraManager(context) }
    
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                cameraManager.setupCamera(
                    lifecycleOwner = lifecycleOwner,
                    previewView = this,
                    imageAnalyzer = { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            onVrmDetected(image)
                        }
                        imageProxy.close()
                    }
                )
            }
        },
        modifier = modifier
    )
}
```

## Navigation Implementation

### Navigation Graph
```kotlin
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("dashboard") }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                onNavigateToObservation = { navController.navigate("observation") },
                onNavigateToIssuance = { navController.navigate("issuance") },
                onNavigateToCountdown = { navController.navigate("countdown") },
                onNavigateToQueue = { navController.navigate("queue") },
                onNavigateToSync = { navController.navigate("sync") }
            )
        }
        
        composable("observation") {
            ObservationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCountdown = { observationId ->
                    navController.navigate("countdown/$observationId")
                }
            )
        }
        
        composable(
            "countdown/{observationId}",
            arguments = listOf(navArgument("observationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val observationId = backStackEntry.arguments?.getLong("observationId") ?: 0L
            CountdownScreen(
                observationId = observationId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToIssuance = { navController.navigate("issuance/$observationId") }
            )
        }
    }
}
```

## Dependency Injection

### Hilt Modules
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ceo_tickets_database"
        ).build()
    }
    
    @Provides
    fun provideObservationDao(database: AppDatabase): ObservationDao {
        return database.observationDao()
    }
    
    @Provides
    fun provideTicketDao(database: AppDatabase): TicketDao {
        return database.ticketDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideObservationRepository(
        observationDao: ObservationDao,
        syncQueueManager: SyncQueueManager
    ): ObservationRepository {
        return ObservationRepositoryImpl(observationDao, syncQueueManager)
    }
}
```

## Testing Implementation

### Unit Tests
```kotlin
@RunWith(MockitoJUnitRunner::class)
class CreateObservationUseCaseTest {
    
    @Mock
    private lateinit var observationRepository: ObservationRepository
    
    @Mock
    private lateinit var countdownManager: CountdownManager
    
    @Mock
    private lateinit var ticketNumberGenerator: TicketNumberGenerator
    
    private lateinit var createObservationUseCase: CreateObservationUseCase
    
    @Before
    fun setup() {
        createObservationUseCase = CreateObservationUseCase(
            observationRepository,
            countdownManager,
            ticketNumberGenerator
        )
    }
    
    @Test
    fun `createObservation should return success when observation is created`() = runTest {
        // Given
        val expectedId = 1L
        whenever(observationRepository.createObservation(any())).thenReturn(expectedId)
        
        // When
        val result = createObservationUseCase(
            vrm = "AB12 CDE",
            streetId = 1L,
            contraventionId = 1L,
            makeId = 1L,
            modelId = 1L,
            valvePositionFront = null,
            valvePositionRear = null,
            photoPath = null,
            userId = 1L
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        verify(countdownManager).startCountdown(expectedId)
    }
}
```

### Integration Tests
```kotlin
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ObservationRepositoryTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var database: AppDatabase
    
    @Inject
    lateinit var observationRepository: ObservationRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun createObservation_shouldSaveToDatabase() = runTest {
        // Given
        val observation = createTestObservation()
        
        // When
        val id = observationRepository.createObservation(observation)
        
        // Then
        val savedObservation = database.observationDao().getObservationById(id)
        assertNotNull(savedObservation)
        assertEquals(observation.vrm, savedObservation?.vrm)
    }
}
```

## Performance Optimization

### Memory Management
```kotlin
class ImageCache @Inject constructor() {
    private val cache = LruCache<String, Bitmap>(20)
    
    fun get(key: String): Bitmap? = cache.get(key)
    
    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }
    
    fun clear() {
        cache.evictAll()
    }
}
```

### Background Processing
```kotlin
@Singleton
class BackgroundTaskManager @Inject constructor() {
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun schedulePeriodicSync() {
        backgroundScope.launch {
            while (true) {
                delay(TimeUnit.MINUTES.toMillis(30))
                if (isConnected() && shouldSync()) {
                    syncQueueManager.processQueue()
                }
            }
        }
    }
}
```

## Error Handling

### Global Error Handler
```kotlin
@Singleton
class ErrorHandler @Inject constructor() {
    fun handleError(error: Throwable): ErrorState {
        return when (error) {
            is NetworkException -> ErrorState.NetworkError(error.message)
            is DatabaseException -> ErrorState.DatabaseError(error.message)
            is ValidationException -> ErrorState.ValidationError(error.message)
            else -> ErrorState.UnknownError(error.message)
        }
    }
}

sealed class ErrorState {
    data class NetworkError(val message: String?) : ErrorState()
    data class DatabaseError(val message: String?) : ErrorState()
    data class ValidationError(val message: String?) : ErrorState()
    data class UnknownError(val message: String?) : ErrorState()
}
```

## Build Configuration

### Gradle Configuration
```kotlin
// build.gradle.kts (Module: app)
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.ceo.ticketissuance"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}
```

This comprehensive technical specification provides the foundation for implementing the CEO Ticket Issuance Android application using modern Kotlin technologies and best practices.