plugins {
    val kotlinVersion = "1.7.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.11.1"
}

group = "com.king250.bot"
version = "1.0.0"

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.alibaba:fastjson:2.0.7")
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}
