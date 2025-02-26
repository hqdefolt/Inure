package app.simple.inure.util

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.simple.inure.R
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.data.MaterialYou
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import java.util.*

object ThemeUtils {
    fun setAppTheme(resources: Resources) {
        when (AppearancePreferences.getTheme()) {
            ThemeConstants.LIGHT_THEME -> {
                ThemeManager.theme = Theme.LIGHT
            }
            ThemeConstants.SOAPSTONE -> {
                ThemeManager.theme = Theme.SOAPSTONE
            }
            ThemeConstants.DARK_THEME -> {
                ThemeManager.theme = Theme.DARK
            }
            ThemeConstants.AMOLED -> {
                ThemeManager.theme = Theme.AMOLED
            }
            ThemeConstants.SLATE -> {
                ThemeManager.theme = Theme.SLATE
            }
            ThemeConstants.OIL -> {
                ThemeManager.theme = Theme.OIL
            }
            ThemeConstants.HIGH_CONTRAST -> {
                ThemeManager.theme = Theme.HIGH_CONTRAST
            }
            ThemeConstants.FOLLOW_SYSTEM -> {
                // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        when (AppearancePreferences.getLastDarkTheme()) {
                            ThemeConstants.DARK_THEME -> {
                                ThemeManager.theme = Theme.DARK
                            }
                            ThemeConstants.AMOLED -> {
                                ThemeManager.theme = Theme.AMOLED
                            }
                            ThemeConstants.SLATE -> {
                                ThemeManager.theme = Theme.SLATE
                            }
                            ThemeConstants.HIGH_CONTRAST -> {
                                ThemeManager.theme = Theme.HIGH_CONTRAST
                            }
                            ThemeConstants.OIL -> {
                                ThemeManager.theme = Theme.OIL
                            }
                        }
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        when (AppearancePreferences.getLastLightTheme()) {
                            ThemeConstants.LIGHT_THEME -> {
                                ThemeManager.theme = Theme.LIGHT
                            }
                            ThemeConstants.SOAPSTONE -> {
                                ThemeManager.theme = Theme.SOAPSTONE
                            }
                        }
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        ThemeManager.theme = Theme.LIGHT
                    }
                }
            }
            ThemeConstants.DAY_NIGHT -> {
                val calendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (calendar < 7 || calendar > 18) {
                    when (AppearancePreferences.getLastDarkTheme()) {
                        ThemeConstants.DARK_THEME -> {
                            ThemeManager.theme = Theme.DARK
                        }
                        ThemeConstants.AMOLED -> {
                            ThemeManager.theme = Theme.AMOLED
                        }
                        ThemeConstants.SLATE -> {
                            ThemeManager.theme = Theme.SLATE
                        }
                        ThemeConstants.HIGH_CONTRAST -> {
                            ThemeManager.theme = Theme.HIGH_CONTRAST
                        }
                        ThemeConstants.OIL -> {
                            ThemeManager.theme = Theme.OIL
                        }
                    }
                } else if (calendar < 18 || calendar > 6) {
                    when (AppearancePreferences.getLastLightTheme()) {
                        ThemeConstants.LIGHT_THEME -> {
                            ThemeManager.theme = Theme.LIGHT
                        }
                        ThemeConstants.SOAPSTONE -> {
                            ThemeManager.theme = Theme.SOAPSTONE
                        }
                    }
                }
            }
            ThemeConstants.MATERIAL_YOU -> {
                if (isNightMode(resources)) {
                    ThemeManager.theme = Theme.MATERIAL_YOU_DARK
                } else {
                    ThemeManager.theme = Theme.MATERIAL_YOU_LIGHT
                }
            }
        }
    }

    fun setBarColors(resources: Resources, window: Window) {
        when (AppearancePreferences.getTheme()) {
            ThemeConstants.LIGHT_THEME,
            ThemeConstants.SOAPSTONE -> {
                lightBars(window)
            }
            ThemeConstants.DARK_THEME,
            ThemeConstants.AMOLED,
            ThemeConstants.HIGH_CONTRAST,
            ThemeConstants.SLATE,
            ThemeConstants.OIL -> {
                darkBars(window)
            }
            ThemeConstants.MATERIAL_YOU,
            ThemeConstants.FOLLOW_SYSTEM -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        darkBars(window)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        lightBars(window)
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        lightBars(window)
                    }
                }
            }
            ThemeConstants.DAY_NIGHT -> {
                val calendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (calendar < 7 || calendar > 18) {
                    darkBars(window)
                } else if (calendar < 18 || calendar > 6) {
                    lightBars(window)
                }
            }
        }
    }

    private fun lightBars(window: Window) {
        setStatusAndNavColors(window)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = !AppearancePreferences.isAccentOnNavigationBar()
    }

    private fun darkBars(window: Window) {
        setStatusAndNavColors(window)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
    }

    private fun setStatusAndNavColors(window: Window) {
        if (AppearancePreferences.isTransparentStatusDisabled()) {
            window.statusBarColor = ThemeManager.theme.viewGroupTheme.background
        } else {
            window.statusBarColor = Color.TRANSPARENT
        }

        if (!AppearancePreferences.isAccentOnNavigationBar()) {
            window.navigationBarColor = ThemeManager.theme.viewGroupTheme.background
        }
    }

    fun isNightMode(resources: Resources): Boolean {
        when (AppearancePreferences.getTheme()) {
            ThemeConstants.LIGHT_THEME,
            ThemeConstants.SOAPSTONE -> {
                return false
            }
            ThemeConstants.DARK_THEME,
            ThemeConstants.AMOLED,
            ThemeConstants.HIGH_CONTRAST,
            ThemeConstants.SLATE,
            ThemeConstants.OIL -> {
                return true
            }
            ThemeConstants.MATERIAL_YOU,
            ThemeConstants.FOLLOW_SYSTEM -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        return true
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        return false
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        return false
                    }
                }
            }
            ThemeConstants.DAY_NIGHT -> {
                val calendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (calendar < 7 || calendar > 18) {
                    return true
                } else if (calendar < 18 || calendar > 6) {
                    return false
                }
            }
        }

        return false
    }

    fun isFollowSystem(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AppearancePreferences.getTheme() == ThemeConstants.FOLLOW_SYSTEM || AppearancePreferences.getTheme() == ThemeConstants.MATERIAL_YOU
        } else {
            AppearancePreferences.getTheme() == ThemeConstants.FOLLOW_SYSTEM
        }
    }

    fun updateNavAndStatusColors(resources: Resources, window: Window) {
        if (isNightMode(resources)) {
            darkBars(window)
        } else {
            lightBars(window)
        }
    }

    fun Activity.setTheme() {
        when (AppearancePreferences.getAccentColor()) {
            ContextCompat.getColor(baseContext, R.color.inure) -> {
                setTheme(R.style.Inure)
            }
            ContextCompat.getColor(baseContext, R.color.blue) -> {
                setTheme(R.style.Blue)
            }
            ContextCompat.getColor(baseContext, R.color.blueGrey) -> {
                setTheme(R.style.BlueGrey)
            }
            ContextCompat.getColor(baseContext, R.color.darkBlue) -> {
                setTheme(R.style.DarkBlue)
            }
            ContextCompat.getColor(baseContext, R.color.red) -> {
                setTheme(R.style.Red)
            }
            ContextCompat.getColor(baseContext, R.color.green) -> {
                setTheme(R.style.Green)
            }
            ContextCompat.getColor(baseContext, R.color.orange) -> {
                setTheme(R.style.Orange)
            }
            ContextCompat.getColor(baseContext, R.color.purple) -> {
                setTheme(R.style.Purple)
            }
            ContextCompat.getColor(baseContext, R.color.yellow) -> {
                setTheme(R.style.Yellow)
            }
            ContextCompat.getColor(baseContext, R.color.caribbeanGreen) -> {
                setTheme(R.style.CaribbeanGreen)
            }
            ContextCompat.getColor(baseContext, R.color.persianGreen) -> {
                setTheme(R.style.PersianGreen)
            }
            ContextCompat.getColor(baseContext, R.color.amaranth) -> {
                setTheme(R.style.Amaranth)
            }
            ContextCompat.getColor(baseContext, R.color.indian_red) -> {
                setTheme(R.style.IndianRed)
            }
            ContextCompat.getColor(baseContext, R.color.light_coral) -> {
                setTheme(R.style.LightCoral)
            }
            ContextCompat.getColor(baseContext, R.color.pink_flare) -> {
                setTheme(R.style.PinkFlare)
            }
            ContextCompat.getColor(baseContext, R.color.makeup_tan) -> {
                setTheme(R.style.MakeupTan)
            }
            ContextCompat.getColor(baseContext, R.color.egg_yellow) -> {
                setTheme(R.style.EggYellow)
            }
            ContextCompat.getColor(baseContext, R.color.medium_green) -> {
                setTheme(R.style.MediumGreen)
            }
            ContextCompat.getColor(baseContext, R.color.olive) -> {
                setTheme(R.style.Olive)
            }
            ContextCompat.getColor(baseContext, R.color.copperfield) -> {
                setTheme(R.style.Copperfield)
            }
            ContextCompat.getColor(baseContext, R.color.mineral_green) -> {
                setTheme(R.style.MineralGreen)
            }
            ContextCompat.getColor(baseContext, R.color.lochinvar) -> {
                setTheme(R.style.Lochinvar)
            }
            ContextCompat.getColor(baseContext, R.color.beach_grey) -> {
                setTheme(R.style.BeachGrey)
            }
            ContextCompat.getColor(baseContext, R.color.cashmere) -> {
                setTheme(R.style.Cashmere)
            }
            ContextCompat.getColor(baseContext, R.color.grape) -> {
                setTheme(R.style.Grape)
            }
            ContextCompat.getColor(baseContext, R.color.roman_silver) -> {
                setTheme(R.style.RomanSilver)
            }
            ContextCompat.getColor(baseContext, R.color.horizon) -> {
                setTheme(R.style.Horizon)
            }
            ContextCompat.getColor(baseContext, R.color.limed_spruce) -> {
                setTheme(R.style.LimedSpruce)
            }
            ContextCompat.getColor(baseContext, MaterialYou.materialYouAccentResID) -> {
                setTheme(R.style.MaterialYou)
            }
            else -> {
                setTheme(R.style.Inure)
                AppearancePreferences.setAccentColor(ContextCompat.getColor(baseContext, R.color.inure))
            }
        }
    }

    fun Activity.setTransparentTheme() {
        when (AppearancePreferences.getAccentColor()) {
            ContextCompat.getColor(baseContext, R.color.inure) -> {
                setTheme(R.style.Inure_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.blue) -> {
                setTheme(R.style.Blue_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.blueGrey) -> {
                setTheme(R.style.BlueGrey_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.darkBlue) -> {
                setTheme(R.style.DarkBlue_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.red) -> {
                setTheme(R.style.Red_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.green) -> {
                setTheme(R.style.Green_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.orange) -> {
                setTheme(R.style.Orange_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.purple) -> {
                setTheme(R.style.Purple_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.yellow) -> {
                setTheme(R.style.Yellow_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.caribbeanGreen) -> {
                setTheme(R.style.CaribbeanGreen_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.persianGreen) -> {
                setTheme(R.style.PersianGreen_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.amaranth) -> {
                setTheme(R.style.Amaranth_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.indian_red) -> {
                setTheme(R.style.IndianRed_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.light_coral) -> {
                setTheme(R.style.LightCoral_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.pink_flare) -> {
                setTheme(R.style.PinkFlare_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.makeup_tan) -> {
                setTheme(R.style.MakeupTan_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.egg_yellow) -> {
                setTheme(R.style.EggYellow_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.medium_green) -> {
                setTheme(R.style.MediumGreen_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.olive) -> {
                setTheme(R.style.Olive_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.copperfield) -> {
                setTheme(R.style.Copperfield_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.mineral_green) -> {
                setTheme(R.style.MineralGreen_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.lochinvar) -> {
                setTheme(R.style.Lochinvar_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.beach_grey) -> {
                setTheme(R.style.BeachGrey_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.cashmere) -> {
                setTheme(R.style.Cashmere_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.grape) -> {
                setTheme(R.style.Grape_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.roman_silver) -> {
                setTheme(R.style.RomanSilver_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.horizon) -> {
                setTheme(R.style.Horizon_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.limed_spruce) -> {
                setTheme(R.style.LimedSpruce_Transparent)
            }
            ContextCompat.getColor(baseContext, MaterialYou.materialYouAccentResID) -> {
                setTheme(R.style.MaterialYou_Transparent)
            }
            else -> {
                setTheme(R.style.Inure_Transparent)
                AppearancePreferences.setAccentColor(ContextCompat.getColor(baseContext, R.color.inure))
            }
        }
    }
}