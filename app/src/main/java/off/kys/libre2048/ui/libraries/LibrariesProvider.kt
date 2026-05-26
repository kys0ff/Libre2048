package off.kys.libre2048.ui.libraries

import off.kys.libre2048.domain.model.Library

object LibrariesProvider {

    val LIBRARIES: List<Library>
        get() = listOf(
            Library(
                "Jetpack Compose",
                "Google",
                "Apache 2.0",
                "https://developer.android.com/jetpack/compose"
            ),
            Library(
                "Voyager",
                "Adriel Café",
                "MIT",
                "https://github.com/adrielcafe/voyager"
            ),
            Library(
                "Koin",
                "InsertKoin",
                "Apache 2.0",
                "https://github.com/InsertKoinIO/koin"
            ),
            Library(
                "Kotlinx Serialization",
                "JetBrains",
                "Apache 2.0",
                "https://github.com/Kotlin/kotlinx.serialization"
            ),
            Library(
                "AndroidX DataStore",
                "Google",
                "Apache 2.0",
                "https://developer.android.com/topic/libraries/architecture/datastore"
            ),
            Library(
                "Material 3",
                "Google",
                "Apache 2.0",
                "https://m3.material.io"
            ),
            Library(
                "AndroidX Lifecycle",
                "Google",
                "Apache 2.0",
                "https://developer.android.com/jetpack/androidx/releases/lifecycle"
            ),
            Library(
                "AndroidX Core KTX",
                "Google",
                "Apache 2.0",
                "https://developer.android.com/kotlin/ktx"
            )
        )

}