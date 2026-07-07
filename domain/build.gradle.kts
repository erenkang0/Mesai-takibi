// Saf Kotlin/JVM modülü — Android'e bağımlı değildir, bu sayede iş mantığı
// (mesai algılama, bordro, finans) birim testleriyle bağımsız olarak doğrulanabilir.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
}
