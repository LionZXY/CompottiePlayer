package uk.kulikov.compottie.sample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform