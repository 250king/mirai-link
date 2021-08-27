plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.7.0"
}

group = "love.hana.bot.qq.link"
version = "2.0.0"

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.3.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("org.json:json:20210307")
    implementation("org.jsoup:jsoup:1.14.2")
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}
