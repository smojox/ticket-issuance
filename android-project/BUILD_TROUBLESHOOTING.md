# Build Troubleshooting Guide

After updating to Kotlin 1.9.23 and KSP 1.9.24-1.0.20, you may encounter some common build issues. This guide provides solutions for the most frequent problems.

## Common Build Issues and Solutions

### 1. Hilt Annotation Processing Errors

**Error**: `Cannot find symbol: @HiltAndroidApp` or `@AndroidEntryPoint`

**Solution**:
- Ensure both Hilt compilers are present in `app/build.gradle.kts`:
  ```kotlin
  ksp("com.google.dagger:hilt-compiler:2.48")
  ksp("androidx.hilt:hilt-compiler:1.1.0")
  ```
- Clean and rebuild:
  ```bash
  ./gradlew clean build
  ```

### 2. Room Database Compilation Issues

**Error**: `cannot find symbol: Room.*_Impl` or `Cannot access database`

**Solution**:
- Verify Room schema export is configured in `app/build.gradle.kts`:
  ```kotlin
  javaCompileOptions {
      annotationProcessorOptions {
          arguments += mapOf(
              "room.schemaLocation" to "$projectDir/schemas",
              "room.incremental" to "true"
          )
      }
  }
  ```
- Ensure KSP incremental compilation is enabled in `gradle.properties`:
  ```properties
  ksp.incremental=true
  ksp.incremental.intermodule=true
  ksp.use.worker.api=true
  ```

### 3. Compose Compiler Warnings

**Warning**: `The Compose Compiler is compatible with Kotlin 1.9.23`

**Solution**:
- Verify `kotlinCompilerExtensionVersion` in `app/build.gradle.kts`:
  ```kotlin
  composeOptions {
      kotlinCompilerExtensionVersion = "1.5.11"
  }
  ```
- Add opt-in compiler arguments:
  ```kotlin
  kotlinOptions {
      freeCompilerArgs += listOf(
          "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
          "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
      )
  }
  ```

### 4. Dependency Version Conflicts

**Error**: `Module was compiled with an incompatible version of Kotlin`

**Solution**:
- Check the compatibility matrix:
  - Kotlin 1.9.23 → Compose Compiler 1.5.11
  - KSP 1.9.24-1.0.20 → Room 2.6.1
  - Hilt 2.48 → Compatible with both
- Update BOM to latest stable version:
  ```kotlin
  implementation(platform("androidx.compose:compose-bom:2024.04.01"))
  ```

### 5. AndroidX Compatibility Problems

**Error**: `Duplicate class` or `Cannot resolve symbol`

**Solution**:
- Ensure `android.useAndroidX=true` in `gradle.properties`
- Check for duplicate dependencies in `app/build.gradle.kts`
- Use BOM for consistent versions:
  ```kotlin
  implementation(platform("androidx.compose:compose-bom:2024.04.01"))
  ```

### 6. Java Version Compatibility

**Error**: `compileSdkVersion` or `source/target compatibility`

**Solution**:
- Verify Java 17 is used consistently:
  ```kotlin
  compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
      jvmTarget = "17"
  }
  ```
- Enable core library desugaring:
  ```kotlin
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
  ```

### 7. KSP Build Performance Issues

**Error**: Slow builds or KSP processing errors

**Solution**:
- Enable KSP optimizations in `gradle.properties`:
  ```properties
  ksp.incremental=true
  ksp.incremental.intermodule=true
  ksp.use.worker.api=true
  ```
- Increase Gradle heap size:
  ```properties
  org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
  ```

### 8. ProGuard/R8 Issues (Release Builds)

**Error**: `ClassNotFoundException` or `NoSuchMethodException` in release builds

**Solution**:
- Check `proguard-rules.pro` includes all necessary keep rules
- Common rules are already added for:
  - Room entities and DAOs
  - Hilt components
  - Compose classes
  - KSP generated classes

## Quick Diagnostic Commands

Run these commands to diagnose issues:

```bash
# Check build health
./build-health-check.sh

# Clean build
./gradlew clean

# Build with info logs
./gradlew assembleDebug --info

# Check dependency conflicts
./gradlew dependencies

# Verify KSP generated files
ls -la app/build/generated/ksp/debug/kotlin/
```

## Environment Setup

If you encounter Java/SDK issues:

1. **Install Java 17**:
   ```bash
   sudo apt install openjdk-17-jdk  # Linux
   brew install openjdk@17          # macOS
   ```

2. **Set JAVA_HOME**:
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
   ```

3. **Verify Gradle Wrapper**:
   ```bash
   ./gradlew wrapper --gradle-version 8.1.1
   ```

## Getting Help

If you still encounter issues:

1. Run the build health check script
2. Check the build logs for specific error messages
3. Verify all versions match the compatibility matrix
4. Try a clean build with `./gradlew clean build`
5. Check for Android Studio IDE sync issues

## Version Compatibility Matrix

| Component | Version | Compatible With |
|-----------|---------|-----------------|
| Kotlin | 1.9.23 | Compose Compiler 1.5.11 |
| KSP | 1.9.24-1.0.20 | Room 2.6.1, Hilt 2.48 |
| Java | 17 | Android Gradle Plugin 8.1.4 |
| Compose BOM | 2024.04.01 | Stable releases |
| Room | 2.6.1 | KSP incremental compilation |
| Hilt | 2.48 | KSP and androidx.hilt 1.1.0 |