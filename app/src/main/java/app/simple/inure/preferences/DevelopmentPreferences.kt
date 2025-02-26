package app.simple.inure.preferences

import app.simple.inure.models.DevelopmentPreferencesModel

object DevelopmentPreferences {

    const val isWebViewXmlViewer = "is_xml_viewer_web_view"
    const val preferencesIndicator = "is_preferences_indicator_hidden"
    const val crashHandler = "is_using_native_crash_handler"
    const val music = "is_music_enabled"
    const val imageCaching = "is_image_caching_enabled"
    const val debugMessages = "is_debug_messages_enabled"
    const val hoverAnimation = "is_hover_animation_enabled"
    const val centerBottomMenu = "is_center_bottom_menu_enabled"
    const val alternativeBatteryOptimizationSwitch = "is_alternative_battery_optimization_switch_enabled"
    const val loadAlbumArtFromFile = "is_album_art_loaded_from_file"
    const val useOldStyleUsageStatsPanel = "is_old_style_usage_stats_panel_enabled"

    val developmentPreferences: List<DevelopmentPreferencesModel> = listOf(
            DevelopmentPreferencesModel("Use WebView for XML Preview",
                                        "Use WebView for XML Preview instead of TextView.",
                                        isWebViewXmlViewer,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Hide Preferences Indicator",
                                        "Hides the indicators in the settings and dialogs.",
                                        preferencesIndicator,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Enable Music",
                                        "Enable music player in the app.",
                                        music,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Disable Native Crash Handler",
                                        "Disable native crash handler of the app and let system handle crash reports.",
                                        crashHandler,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Disable Image Caching",
                                        "Disable image caching to save memory but at the cost of higher CPU usage due to regeneration of all image data everytime they\'re loaded.",
                                        imageCaching,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Enable Debug Messages",
                                        "Enable debug messages in the app to help with debugging and finding bugs.",
                                        debugMessages,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Enable Hover Animation",
                                        "Enable scale animation on hover on all views in the app.",
                                        hoverAnimation,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Center Bottom Menu",
                                        "Center gravity for the bottom menus in the app.",
                                        centerBottomMenu,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Enable Alternative Battery Optimization Switch",
                                        "Enable alternative battery optimization switcher popup in the app.",
                                        alternativeBatteryOptimizationSwitch,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Load Album Art From File",
                                        "Load album art from file instead of using MediaStore.\n\nThis will increase the time taken to load album art but will improve the album art quality.",
                                        loadAlbumArtFromFile,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Use Old Style Usage Stats Panel",
                                        "Use old raw data style usage stats panel instead of the current one.",
                                        useOldStyleUsageStatsPanel,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN)
    ).sortedBy {
        it.title
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun get(key: String): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(key, false)
    }

    fun set(key: String, value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(key, value).apply()
    }

    // ---------------------------------------------------------------------------------------------------------- //
}