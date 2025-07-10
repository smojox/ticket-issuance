package com.ceo.ticketissuance.core.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import com.ceo.ticketissuance.core.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class PhotoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val photosDir: File by lazy {
        File(context.getExternalFilesDir(null), "photos").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    suspend fun capturePhoto(
        imageCapture: ImageCapture,
        plateNumber: String? = null
    ): Result<PhotoResult> = suspendCancellableCoroutine { continuation ->
        val timestamp = timestampFormat.format(Date())
        val photoFile = File(
            photosDir,
            "CEO_${plateNumber ?: "UNKNOWN"}_$timestamp.jpg"
        )
        
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val result = PhotoResult(
                        file = photoFile,
                        uri = Uri.fromFile(photoFile),
                        filename = photoFile.name,
                        plateNumber = plateNumber,
                        timestamp = System.currentTimeMillis()
                    )
                    continuation.resume(Result.Success(result))
                }
                
                override fun onError(exception: ImageCaptureException) {
                    continuation.resume(Result.Error(Exception(exception.message ?: "Photo capture failed")))
                }
            }
        )
    }
    
    fun getPhotoFile(filename: String): File {
        return File(photosDir, filename)
    }
    
    fun getPhotoUri(filename: String): Uri {
        return Uri.fromFile(getPhotoFile(filename))
    }
    
    fun deletePhoto(filename: String): Boolean {
        return try {
            val file = getPhotoFile(filename)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    fun getAllPhotos(): List<PhotoResult> {
        return try {
            photosDir.listFiles()
                ?.filter { it.name.endsWith(".jpg") }
                ?.map { file ->
                    val parts = file.nameWithoutExtension.split("_")
                    val plateNumber = if (parts.size >= 2) parts[1] else null
                    
                    PhotoResult(
                        file = file,
                        uri = Uri.fromFile(file),
                        filename = file.name,
                        plateNumber = plateNumber,
                        timestamp = file.lastModified()
                    )
                }
                ?.sortedByDescending { it.timestamp }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getPhotosForPlate(plateNumber: String): List<PhotoResult> {
        return getAllPhotos().filter { it.plateNumber == plateNumber }
    }
    
    fun compressPhoto(file: File, quality: Int = 80): Result<File> {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val compressedFile = File(file.parent, "compressed_${file.name}")
            
            FileOutputStream(compressedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            
            Result.Success(compressedFile)
        } catch (e: Exception) {
            Result.Error(Exception(e.message ?: "Photo compression failed"))
        }
    }
    
    fun rotatePhoto(file: File, degrees: Float): Result<File> {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val matrix = Matrix().apply { postRotate(degrees) }
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
            
            val rotatedFile = File(file.parent, "rotated_${file.name}")
            FileOutputStream(rotatedFile).use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            Result.Success(rotatedFile)
        } catch (e: Exception) {
            Result.Error(Exception(e.message ?: "Photo rotation failed"))
        }
    }
    
    fun getPhotoSize(file: File): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return Pair(options.outWidth, options.outHeight)
    }
    
    fun cleanupOldPhotos(maxAgeHours: Int = 24 * 7) { // Default 1 week
        try {
            val cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000)
            photosDir.listFiles()
                ?.filter { it.lastModified() < cutoffTime }
                ?.forEach { it.delete() }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    fun getStorageUsage(): StorageInfo {
        val files = photosDir.listFiles() ?: emptyArray()
        val totalSize = files.sumOf { it.length() }
        val photoCount = files.size
        
        return StorageInfo(
            totalSizeBytes = totalSize,
            photoCount = photoCount,
            averageSizeBytes = if (photoCount > 0) totalSize / photoCount else 0L
        )
    }
}

data class PhotoResult(
    val file: File,
    val uri: Uri,
    val filename: String,
    val plateNumber: String?,
    val timestamp: Long
)

data class StorageInfo(
    val totalSizeBytes: Long,
    val photoCount: Int,
    val averageSizeBytes: Long
) {
    val totalSizeMB: Double get() = totalSizeBytes / (1024.0 * 1024.0)
    val averageSizeMB: Double get() = averageSizeBytes / (1024.0 * 1024.0)
}