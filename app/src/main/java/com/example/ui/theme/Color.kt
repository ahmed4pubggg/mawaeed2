package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Modern Professional Palette — "Emerald & Warm Gold"
// Designed for a refined, contemporary Quranic scheduling app
// ============================================================

// --- Emerald (Primary brand color) ---
val EmeraldPrimary = Color(0xFF0E7C66)      // Rich, modern emerald (main brand color)
val EmeraldPrimaryDark = Color(0xFF57D9BB)  // Brighter emerald for dark theme contrast
val EmeraldDeep = Color(0xFF063D32)         // Very deep emerald, near-black
val EmeraldContainerLight = Color(0xFFCFF2E8) // Soft container tint (light theme)
val EmeraldContainerDark = Color(0xFF0F5647)  // Container tint (dark theme)

// --- Warm Gold (Secondary / accent brand color) ---
val WarmGold = Color(0xFFC9971F)            // Muted, elegant gold (not garish)
val WarmGoldBright = Color(0xFFF0C24B)      // Bright gold for dark theme accents
val GoldContainerLight = Color(0xFFFCEBC2)
val GoldContainerDark = Color(0xFF4A3A0C)

// --- Neutral surfaces (Light theme) ---
val SurfaceLight = Color(0xFFFBFDFB)
val SurfaceLightDim = Color(0xFFF1F5F3)
val SurfaceLightVariant = Color(0xFFE3EDE9)
val OnSurfaceLight = Color(0xFF161D1B)
val OnSurfaceLightVariant = Color(0xFF48544F)
val OutlineLight = Color(0xFFC9D2CD)

// --- Neutral surfaces (Dark theme) ---
val SurfaceDark = Color(0xFF0C1613)         // Deep, near-black emerald-tinted background
val SurfaceDarkElevated = Color(0xFF13211D) // Cards / elevated surfaces
val SurfaceDarkVariant = Color(0xFF1D2E28)  // Subtle variant surface (chips, inputs)
val OnSurfaceDark = Color(0xFFE7F0EC)
val OnSurfaceDarkVariant = Color(0xFFA9BAB3)
val OutlineDark = Color(0xFF3A4B44)

// --- Status colors (kept consistent across themes) ---
val SuccessColor = Color(0xFF2E9E6B)
val SuccessContainerLight = Color(0xFFD8F5E4)
val SuccessContainerDark = Color(0xFF17402C)
val ErrorColor = Color(0xFFDC4C4C)
val ErrorContainerLight = Color(0xFFFDE2E1)
val ErrorContainerDark = Color(0xFF4A1E1E)
val WarningColor = Color(0xFFE0A32E)

// --- Legacy aliases kept for compatibility with existing screens ---
// (Some composables in QuranAppUi.kt / AlarmActivity.kt reference these names directly)
val DarkTeal = EmeraldPrimary
val MediumTeal = EmeraldPrimaryDark
val LightTeal = EmeraldContainerLight
val GoldAccent = WarmGold
val DeepGold = Color(0xFF8A6A10)
val GreenSuccess = SuccessColor
val LightGreenSuccess = SuccessContainerLight
val MintGreen80 = EmeraldPrimaryDark
val DarkTeal80 = EmeraldPrimary
val GoldAccent80 = WarmGoldBright
val DeepPineNight = SurfaceDark
val PineSurface = SurfaceDarkElevated

// --- More legacy aliases referenced directly in QuranAppUi.kt ---
val SlateDarkBg = SurfaceDark
val SlateSurface = SurfaceDarkElevated
val SlateSurfaceVariant = SurfaceDarkVariant
val CyanPrimary = EmeraldPrimaryDark
val CyanSecondary = EmeraldPrimary
val LightText = OnSurfaceDark
val LightTextSecondary = OnSurfaceDarkVariant

// --- Alarm screen palette (used directly by AlarmActivity.kt) ---
val AlarmBgTop = Color(0xFF07231D)
val AlarmBgMid = Color(0xFF0E3A30)
val AlarmBgBottom = Color(0xFF051713)
val AlarmGold = WarmGoldBright
val AlarmGoldDeep = Color(0xFFD79E2A)
val AlarmCardTint = Color(0xFF0E3A30)
