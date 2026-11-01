package com.aura.ambientdashboard.data

enum class WidgetType(val title: String, val description: String) {
    CLOCK("Clock", "Large live time display"),
    DATE("Date", "Today's date and weekday"),
    WEATHER("Weather", "Current conditions from open-meteo.com"),
    RAM("RAM monitor", "Memory in use with a rolling graph"),
    STORAGE("Storage monitor", "Internal storage usage"),
    CPU("CPU", "Core count and processor details"),
    WIFI("Wi-Fi", "Wi-Fi connection state"),
    MOBILE("Mobile network", "Cellular data status"),
    INTERNET("Internet", "Internet reachability"),
    DEVICE("Device", "Manufacturer, model and Android version"),
    UPTIME("System uptime", "Time since the last boot"),
    NETWORK("Network details", "Local address and link information");
}
