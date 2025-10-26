package com.example.myapplication.models

data class AudioConfig(
    val sampleRate: Int,
    val channelIn: Int,
    val channelOut: Int,
    val encoding: Int,
    val bufferSize: Int
)