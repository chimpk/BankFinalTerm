import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Chỉ áp dụng plugin google-services khi nhóm đã đặt app/google-services.json (file không đưa vào Git).
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

// Khóa dịch vụ ngoài đọc từ -P<tên>=... (CI) hoặc local.properties (không commit). Xem local.properties.example.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun localProp(name: String, default: String = ""): String =
    (findProperty(name)?.toString() ?: localProps.getProperty(name) ?: default).replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "vn.edu.tdtu.tdtbank"
    compileSdk = 36

    defaultConfig {
        applicationId = "vn.edu.tdtu.tdtbank"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "VNPAY_TMN_CODE", "\"${localProp("vnpay.tmnCode")}\"")
        buildConfigField("String", "VNPAY_HASH_SECRET", "\"${localProp("vnpay.hashSecret")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProp("gemini.apiKey")}\"")
        buildConfigField("String", "GEMINI_MODEL", "\"${localProp("gemini.model", "gemini-2.0-flash")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.exifinterface)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    implementation(libs.play.services.location)
    implementation(libs.mlkit.face)
    implementation(libs.osmdroid)

    testImplementation(libs.junit)
    testImplementation(libs.org.json)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
