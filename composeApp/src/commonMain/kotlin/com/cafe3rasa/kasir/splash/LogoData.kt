package com.cafe3rasa.kasir.splash

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.PathParser
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pbl.composeapp.generated.resources.Res

// ─── Data classes ───────────────────────────────────────────────────────────

class LetterPaths(
    val white: Path,
    val yellow: Path,
    val centerX: Float,
    val centerY: Float
)

class LogoData(
    val lightYellow: Path,
    val orange: Path,
    val dark: Path,
    val teal: Path,
    val letters: List<LetterPaths>,   // T, i, titik_i, g, a1, R, a2, s, a3
    val stickerWhite: Path,
    val stickerNavy: Path,
    val gateOuterYellow: Path,        // subpath starting at bottom-left corner
    val gateOuterOrange: Path,        // subpath starting at bottom-left corner
)

// ─── Parser helpers ──────────────────────────────────────────────────────────

private fun String.toComposePath(): Path {
    val p = PathParser().parsePathString(this.trim()).toPath()
    p.fillType = PathFillType.EvenOdd
    return p
}

/** Extract only the first closed subpath (up to and including first 'Z') */
private fun firstSubpath(d: String): String {
    val idx = d.indexOf('Z', ignoreCase = true)
    return if (idx >= 0) d.substring(0, idx + 1) else d
}

/**
 * Rotates a closed subpath (M...Z) so that it starts at [startPoint]
 * (e.g. "291.67 999.67" at the bottom-left corner of the gate).
 */
private fun rotateClosedSubpath(subpath: String, startPoint: String): String {
    val idx = subpath.indexOf(startPoint)
    if (idx < 0) return subpath
    val endCoord = idx + startPoint.length
    val head = subpath.substring(0, endCoord)
    val tail = subpath.substring(endCoord).trimEnd { it == 'Z' || it == 'z' || it.isWhitespace() }
    val firstCmdIdx = head.indexOfFirst { it in "CcSsQqTtAaLlHhVv" }
    val headCurves = if (firstCmdIdx >= 0) head.substring(firstCmdIdx) else ""
    return "M$startPoint$tail$headCurves" + "Z"
}

/** Find d="…" by element id attribute, DOT_MATCHES_ALL mode */
private fun extractById(svg: String, id: String): String {
    val regex = Regex(
        """id="$id"[^>]*?\sd="([^"]+)"""",
        setOf(RegexOption.DOT_MATCHES_ALL)
    )
    return regex.find(svg)?.groupValues?.get(1)
        ?: error("SVG: id '$id' not found")
}

/** Extract all d="…" values from inside <g id="huruf_NAME">…</g> */
private fun extractLetterPaths(svg: String, name: String): List<String> {
    val groupRegex = Regex(
        """<g\s[^>]*id="huruf_$name"[^>]*>(.*?)</g>""",
        setOf(RegexOption.DOT_MATCHES_ALL)
    )
    val dRegex = Regex("""(?<!\w)d="([^"]+)"""")
    val groupContent = groupRegex.find(svg)?.groupValues?.get(1)
        ?: error("SVG: letter group 'huruf_$name' not found")
    return dRegex.findAll(groupContent).map { it.groupValues[1] }.toList()
}

// ─── Main loader ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalResourceApi::class)
suspend fun loadLogoData(): LogoData {
    val svg = Res.readBytes("files/logo_tiga_rasa.svg").decodeToString()

    // Frame paths
    val dLightYellow = extractById(svg, "frame_lightyellow")
    val dOrange      = extractById(svg, "frame_orange")
    val dDark        = extractById(svg, "frame_dark")
    val dTeal        = extractById(svg, "frame_teal")

    // Sticker paths
    val dStickerWhite = extractById(svg, "sticker_white")
    val dStickerNavy  = extractById(svg, "sticker_navy")

    // Letter groups: T, i, titik_i, g, a1, R, a2, s, a3
    val letterNames = listOf("T", "i", "titik_i", "g", "a1", "R", "a2", "s", "a3")
    val letters = letterNames.map { name ->
        val paths = extractLetterPaths(svg, name)
        if (paths.size < 2) error("SVG: huruf_$name has only ${paths.size} path(s)")
        val w = paths[0].toComposePath()
        val y = paths[1].toComposePath()
        val bounds = w.getBounds()
        LetterPaths(
            white   = w,
            yellow  = y,
            centerX = bounds.left + bounds.width / 2f,
            centerY = bounds.top + bounds.height / 2f
        )
    }

    val subYellow = firstSubpath(dLightYellow)
    val subOrange = firstSubpath(dOrange)

    val rotatedYellow = rotateClosedSubpath(subYellow, "291.67 999.67")
    val rotatedOrange = rotateClosedSubpath(subOrange, "302.00 994.00")

    return LogoData(
        lightYellow     = dLightYellow.toComposePath(),
        orange          = dOrange.toComposePath(),
        dark            = dDark.toComposePath(),
        teal            = dTeal.toComposePath(),
        letters         = letters,
        stickerWhite    = dStickerWhite.toComposePath(),
        stickerNavy     = dStickerNavy.toComposePath(),
        gateOuterYellow = rotatedYellow.toComposePath(),
        gateOuterOrange = rotatedOrange.toComposePath(),
    )
}
