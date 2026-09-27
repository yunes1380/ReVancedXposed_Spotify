package io.github.chsbuffer.revancedxposed.spotify.misc.privacy

import android.content.ClipData
import app.revanced.extension.shared.Logger
import app.revanced.extension.spotify.misc.privacy.SanitizeSharingLinksPatch
import de.robv.android.xposed.XposedHelpers
import io.github.chsbuffer.revancedxposed.scopedHook
import io.github.chsbuffer.revancedxposed.spotify.SpotifyHook

fun SpotifyHook.SanitizeSharingLinks() {
    // 9.1.84: each hook isolated — one miss must not fail the whole group.
    runCatching {
        ::shareCopyUrlFingerprint.hookMethod(
            scopedHook(
                XposedHelpers.findMethodExact(
                    ClipData::class.java.name,
                    lpparam.classLoader,
                    "newPlainText",
                    CharSequence::class.java,
                    CharSequence::class.java
                )
            ) {
                before { param ->
                    val url = param.args[1] as String
                    param.args[1] = SanitizeSharingLinksPatch.sanitizeSharingLink(url)
                }
            })
    }.onFailure { Logger.printDebug { "shareCopyUrl hook skipped: ${it.message}" } }

    runCatching {
        ::formatAndroidShareSheetUrlFingerprint.hookMethod {
            before { param ->
                val url = param.args[1] as String
                param.args[1] = SanitizeSharingLinksPatch.sanitizeSharingLink(url)
            }
        }
    }.onFailure { Logger.printDebug { "formatShareSheetUrl hook skipped: ${it.message}" } }
}