package com.cafe3rasa.kasir.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.abs

// ─── Colors (Flat solid palette) ─────────────────────────────────────────────

private val ColorTeal        = Color(0xFF35B2AC)
private val ColorOrange      = Color(0xFFF95324)
private val ColorLightYellow = Color(0xFFFCDE84)
private val ColorDark        = Color(0xFF63281B) // Warm medium-dark chocolate brown (distinctly brown & matching logo)
private val ColorWhite       = Color(0xFFFBFAF7)
private val ColorYellow      = Color(0xFFFCC253)
private val ColorNavy        = Color(0xFF060773)

// ─── Timing constants ────────────────────────────────────────────────────────

private const val STAGE2_START   = 0.40f  // Letters rebel starts (<1s)
private const val REBEL_DURATION = 0.75f  // Duration per rebel letter
private const val GATE_START     = 1.40f  // Gate lines start drawing from bottom-left
private const val GATE_END       = 2.60f  // Gate lines meet
private const val FRAME_D_START  = 2.55f  // Dark brown gate rises from bottom
private const val FRAME_Y_START  = 2.60f  // Frame light yellow fades in
private const val FRAME_O_START  = 2.60f  // Frame orange fades in
private const val FRAME_T_START  = 2.95f  // Frame teal fades in
private const val SWEEP_START    = 3.40f  // White beam sweeps from right to left
private const val SWEEP_END      = 4.90f  // Beam finishes sweep
private const val TOTAL_DURATION = 6.80f  // Splash screen finishes cleanly after wave settles

// ─── Per-letter rebel & wave parameters (exact from HTML reference) ──────────

private data class LetterConfig(
    val name: String,
    val delay: Float,      // var(--d): Rebel start time in seconds
    val waveDelay: Float,  // var(--w): Wave start time in seconds
    val r: Float,          // var(--r): Rebel initial rotation degrees
    val dx: Float,         // var(--dx): Translate X in SVG units
    val dy: Float          // var(--dy): Translate Y in SVG units
)

private val LETTER_CONFIGS = listOf(
    LetterConfig("T",       0.40f, 4.70f,  17f,  29f,  28f),
    LetterConfig("i",       0.47f, 4.78f,  18f,  53f,  39f),
    LetterConfig("titik_i", 0.54f, 4.86f, -16f, -67f,  33f),
    LetterConfig("g",       0.61f, 4.94f, -17f, -32f,  25f),
    LetterConfig("a1",      0.68f, 5.02f, -15f,  31f,  69f),
    LetterConfig("R",       0.75f, 5.10f,  18f,  49f, -25f),
    LetterConfig("a2",      0.82f, 5.18f,  19f, -39f, -29f),
    LetterConfig("s",       0.89f, 5.26f,  10f,  60f,  33f),
    LetterConfig("a3",      0.96f, 5.34f, -22f, -25f,  56f),
)

// ─── Easing helpers ──────────────────────────────────────────────────────────

private fun seg(t: Float, s: Float, e: Float) = ((t - s) / (e - s)).coerceIn(0f, 1f)
private fun easeOut3(x: Float): Float { val c = 1f - x; return 1f - c * c * c }
private fun lerp(a: Float, b: Float, p: Float) = a + (b - a) * p
private fun lerp(a: Color, b: Color, p: Float) = Color(
    red   = lerp(a.red,   b.red,   p),
    green = lerp(a.green, b.green, p),
    blue  = lerp(a.blue,  b.blue,  p),
    alpha = 1f
)

// Cubic bezier approximation (p1x, p1y, p2x, p2y)
private fun cubicBezier(x: Float, p1x: Float, p1y: Float, p2x: Float, p2y: Float): Float {
    var t = x
    for (i in 0 until 5) {
        val tx = 3 * (1 - t) * (1 - t) * t * p1x + 3 * (1 - t) * t * t * p2x + t * t * t
        val dx = 3 * (1 - t) * (1 - t) * p1x + 6 * (1 - t) * t * (p2x - p1x) + 3 * t * t * (1 - p2x)
        if (abs(dx) < 1e-5f) break
        t -= (tx - x) / dx
    }
    return 3 * (1 - t) * (1 - t) * t * p1y + 3 * (1 - t) * t * t * p2y + t * t * t
}

// Rebel overshoot easing: cubic-bezier(.3, 1.2, .5, 1)
private fun rebelEase(x: Float) = cubicBezier(x, 0.3f, 1.2f, 0.5f, 1.0f)

// Gate draw easing: cubic-bezier(.5, 0, .3, 1)
private fun gateDrawEase(x: Float) = cubicBezier(x, 0.5f, 0.0f, 0.3f, 1.0f)

// Dark gate rise easing: cubic-bezier(.2, 1, .3, 1)
private fun gateRiseEase(x: Float) = cubicBezier(x, 0.2f, 1.0f, 0.3f, 1.0f)

// Sweep light easing: cubic-bezier(.45, 0, .4, 1)
private fun sweepEase(x: Float) = cubicBezier(x, 0.45f, 0.0f, 0.4f, 1.0f)

// ─── Main Splash Composable ──────────────────────────────────────────────────

@Composable
fun TigaRasaSplash(onFinished: () -> Unit) {
    var logoData by remember { mutableStateOf<LogoData?>(null) }
    val timeline = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoData = loadLogoData()
        timeline.animateTo(
            targetValue = TOTAL_DURATION,
            animationSpec = tween(
                durationMillis = (TOTAL_DURATION * 1000).toInt(),
                easing = { it }
            )
        )
        onFinished()
    }

    val logo = logoData
    val t    = timeline.value

    // Background starts solid Teal, transitions smoothly to pure White following the sweep light
    val bgSweepProgress = sweepEase(seg(t, SWEEP_START, SWEEP_END))
    val bgColor         = lerp(ColorTeal, Color.White, bgSweepProgress)

    Box(
        modifier = Modifier.fillMaxSize().background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (logo != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scale = minOf(size.width / 1233f, size.height / 1276f) * 0.74f
                val offX  = (size.width  - 1233f * scale) / 2f
                val offY  = (size.height - 1276f * scale) / 2f
                renderVecSplash(logo, t, scale, offX, offY, size)
            }
        }
    }
}

// ─── Vector Renderer ─────────────────────────────────────────────────────────

private fun DrawScope.renderVecSplash(
    logo: LogoData,
    t: Float,
    scale: Float,
    offX: Float,
    offY: Float,
    cs: Size
) {
    // Transform path from SVG unit space (1233x1276) to Screen space
    fun sp(path: Path): Path {
        val m = Matrix(); m.scale(scale, scale)
        val s = Path().apply { addPath(path); transform(m) }
        return Path().apply { addPath(s, Offset(offX, offY)) }
    }

    fun fill(path: Path, color: Color, alpha: Float = 1f) {
        if (alpha <= 0f) return
        drawPath(sp(path), color = color.copy(alpha = alpha.coerceIn(0f, 1f)))
    }

    // ═════════════════════════════════════════════════════════════════════════
    // STAGE 1 (0 .. 0.40s): Initial solid Teal screen, nothing drawn yet
    // ═════════════════════════════════════════════════════════════════════════
    if (t < STAGE2_START) return

    // ═════════════════════════════════════════════════════════════════════════
    // STAGE 2 INK LEAKS ("background bocor" halo burst behind rebel letters)
    // Dark ink (#280C07) erupts behind each letter, peaking at 2.1x scale
    // ═════════════════════════════════════════════════════════════════════════
    val haloFade = 1f - easeOut3(seg(t, 2.95f, 3.20f))
    if (haloFade > 0f) {
        for (i in logo.letters.indices) {
            val cfg = LETTER_CONFIGS[i]
            val letter = logo.letters[i]
            val ts = cfg.delay
            val haloLp = seg(t, ts, ts + 0.90f)
            if (haloLp <= 0f) continue

            // Leak keyframes: 0%->0.3, 30%->2.1 (burst!), 60%->1.3, 100%->1.0
            val haloScale = when {
                haloLp <= 0.30f -> lerp(0.3f, 2.1f, haloLp / 0.30f)
                haloLp <= 0.60f -> lerp(2.1f, 1.3f, (haloLp - 0.30f) / 0.30f)
                else            -> lerp(1.3f, 1.0f, (haloLp - 0.60f) / 0.40f)
            }

            val cx = offX + letter.centerX * scale
            val cy = offY + letter.centerY * scale

            withTransform({
                translate(cx, cy)
                scale(haloScale, haloScale, Offset.Zero)
                translate(-cx, -cy)
            }) {
                // Fill and outer stroke create rich ink-splatter depth
                fill(letter.white, ColorDark, 0.85f * haloFade)
                drawPath(
                    path = sp(letter.white),
                    color = ColorDark.copy(alpha = 0.85f * haloFade),
                    style = Stroke(width = 16f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // STAGE 3: GATE LINE ANIMATION
    // Line draws starting from bottom-left corner (M291.67 999.67) until lines meet
    // 1.40s to 2.60s with cubic-bezier(.5, 0, .3, 1)
    // ═════════════════════════════════════════════════════════════════════════
    val gateDrawProgress = gateDrawEase(seg(t, GATE_START, GATE_END))
    val gateLineFadeOut  = 1f - easeOut3(seg(t, 2.95f, 3.20f))
    val gateStrokeAlpha  = (if (gateDrawProgress > 0f) 1f else 0f) * gateLineFadeOut

    if (gateStrokeAlpha > 0f && gateDrawProgress > 0f) {
        // Yellow gate line: stroke width 16
        drawGateSegment(
            sourcePath = sp(logo.gateOuterYellow),
            progress = gateDrawProgress,
            color = ColorLightYellow,
            strokeWidth = 16f * scale,
            alpha = gateStrokeAlpha
        )
        // Orange gate line: stroke width 22
        drawGateSegment(
            sourcePath = sp(logo.gateOuterOrange),
            progress = gateDrawProgress,
            color = ColorOrange,
            strokeWidth = 22f * scale,
            alpha = gateStrokeAlpha
        )
    }

    // ── Frame fills: MUST strictly follow SVG layering order ─────────────────
    // 1. Light yellow outer frame
    val frameYAlpha = easeOut3(seg(t, FRAME_Y_START, FRAME_Y_START + 0.40f))
    if (frameYAlpha > 0f) fill(logo.lightYellow, ColorLightYellow, frameYAlpha)

    // 2. Orange frame
    val frameOAlpha = easeOut3(seg(t, FRAME_O_START, FRAME_O_START + 0.40f))
    if (frameOAlpha > 0f) fill(logo.orange, ColorOrange, frameOAlpha)

    // 3. Dark Chocolate gate (MUST be drawn ON TOP of orange to remain chocolate brown!)
    val frameDarkProgress = gateRiseEase(seg(t, FRAME_D_START, FRAME_D_START + 0.55f))
    if (frameDarkProgress > 0f) {
        val darkBounds = logo.dark.getBounds()
        val pivotX  = offX + darkBounds.center.x * scale
        val bottomY = offY + darkBounds.bottom * scale
        withTransform({
            translate(pivotX, bottomY)
            scale(1f, frameDarkProgress, Offset.Zero)
            translate(-pivotX, -bottomY)
        }) {
            fill(logo.dark, ColorDark, 1f)
        }
    }

    // 4. Frame teal inside gate arch
    val frameTAlpha = easeOut3(seg(t, FRAME_T_START, FRAME_T_START + 0.35f))
    if (frameTAlpha > 0f) fill(logo.teal, ColorTeal, frameTAlpha)

    // ═════════════════════════════════════════════════════════════════════════
    // STAGE 2 + 5: LETTERS ("Tiga Rasa")
    // Rendered on top of frames, with exact Rebel keyframes + sequential Wave
    // Each letter transforms around its OWN exact centroid (centerX, centerY)
    // ═════════════════════════════════════════════════════════════════════════
    for (i in logo.letters.indices) {
        val cfg    = LETTER_CONFIGS[i]
        val letter = logo.letters[i]
        val ts     = cfg.delay
        val te     = ts + REBEL_DURATION
        val lp     = seg(t, ts, te)
        if (lp <= 0f) continue // Letter not born yet

        // ── Rebel keyframes ──
        // 0%:  scale=0.2, rotate=r,     tx=0,          ty=0
        // 25%: scale=1.5, rotate=-r,    tx=dx,         ty=dy
        // 45%: scale=0.9, rotate=r,     tx=-dx*0.6,    ty=-dy*0.5
        // 65%: scale=1.15,rotate=-r*0.5,tx=dx*0.4,     ty=dy*0.3
        // 85%: scale=0.97,rotate=r*0.2, tx=-dx*0.15,   ty=0
        // 100%: scale=1.0,rotate=0,     tx=0,          ty=0
        val (rebelScale, rebelRot, rebelTx, rebelTy, rebelAlpha) = when {
            lp <= 0.25f -> {
                val p = rebelEase(lp / 0.25f)
                Tuple5(
                    lerp(0.2f, 1.5f, p),
                    lerp(cfg.r, -cfg.r, p),
                    lerp(0f, cfg.dx, p),
                    lerp(0f, cfg.dy, p),
                    (lp / 0.15f).coerceIn(0f, 1f)
                )
            }
            lp <= 0.45f -> {
                val p = (lp - 0.25f) / 0.20f
                Tuple5(
                    lerp(1.5f, 0.9f, p),
                    lerp(-cfg.r, cfg.r, p),
                    lerp(cfg.dx, -cfg.dx * 0.6f, p),
                    lerp(cfg.dy, -cfg.dy * 0.5f, p),
                    1f
                )
            }
            lp <= 0.65f -> {
                val p = (lp - 0.45f) / 0.20f
                Tuple5(
                    lerp(0.9f, 1.15f, p),
                    lerp(cfg.r, -cfg.r * 0.5f, p),
                    lerp(-cfg.dx * 0.6f, cfg.dx * 0.4f, p),
                    lerp(-cfg.dy * 0.5f, cfg.dy * 0.3f, p),
                    1f
                )
            }
            lp <= 0.85f -> {
                val p = (lp - 0.65f) / 0.20f
                Tuple5(
                    lerp(1.15f, 0.97f, p),
                    lerp(-cfg.r * 0.5f, cfg.r * 0.2f, p),
                    lerp(cfg.dx * 0.4f, -cfg.dx * 0.15f, p),
                    lerp(cfg.dy * 0.3f, 0f, p),
                    1f
                )
            }
            lp < 1.0f -> {
                val p = (lp - 0.85f) / 0.15f
                Tuple5(
                    lerp(0.97f, 1.0f, p),
                    lerp(cfg.r * 0.2f, 0f, p),
                    lerp(-cfg.dx * 0.15f, 0f, p),
                    0f,
                    1f
                )
            }
            else -> {
                Tuple5(1.0f, 0f, 0f, 0f, 1f)
            }
        }

        // ── Stage 5 Wave keyframes: sequential wave ripple ──
        // 0% -> none, 30% -> translateY(-36px) rotate(-3deg), 60% -> translateY(14px) rotate(2deg), 100% -> none
        val waveLp = seg(t, cfg.waveDelay, cfg.waveDelay + 0.85f)
        val (waveTy, waveRot) = when {
            waveLp <= 0f -> 0f to 0f
            waveLp <= 0.30f -> {
                val p = easeOut3(waveLp / 0.30f)
                lerp(0f, -36f * scale, p) to lerp(0f, -3f, p)
            }
            waveLp <= 0.60f -> {
                val p = easeOut3((waveLp - 0.30f) / 0.30f)
                lerp(-36f * scale, 14f * scale, p) to lerp(-3f, 2f, p)
            }
            else -> {
                val p = easeOut3((waveLp - 0.60f) / 0.40f)
                lerp(14f * scale, 0f, p) to lerp(2f, 0f, p)
            }
        }

        val totalScale = if (lp >= 1f) 1f else rebelScale
        val totalRot   = (if (lp >= 1f) 0f else rebelRot) + waveRot
        val totalTx    = (if (lp >= 1f) 0f else rebelTx) * scale
        val totalTy    = (if (lp >= 1f) 0f else rebelTy) * scale + waveTy

        val cx = offX + letter.centerX * scale
        val cy = offY + letter.centerY * scale

        withTransform({
            translate(cx + totalTx, cy + totalTy)
            rotate(totalRot, Offset.Zero)
            scale(totalScale, totalScale, Offset.Zero)
            translate(-cx, -cy)
        }) {
            fill(letter.white,  ColorWhite,  rebelAlpha)
            fill(letter.yellow, ColorYellow, rebelAlpha)
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // STAGE 4: SHINING LIGHT BEAM & "by Maharewa" REVEAL
    // White light beam sweeps diagonally at 25° from right (+1350) to left (-1350)
    // Simultaneously reveals "by Maharewa" (stickerWhite + stickerNavy)
    // ═════════════════════════════════════════════════════════════════════════
    val sweepLp = seg(t, SWEEP_START, SWEEP_END)
    if (sweepLp > 0f) {
        val sweepProg = sweepEase(sweepLp)
        val sweepTx   = lerp(1350f, -1350f, sweepProg)
        val pivot     = Offset(offX + 616.5f * scale, offY + 638f * scale)

        // ── Reveal sticker in sync with sweeping beam ──
        if (sweepLp >= 1f) {
            // Sweep completed: sticker is permanently 100% visible
            fill(logo.stickerWhite, ColorWhite, 1f)
            fill(logo.stickerNavy,  ColorNavy,  1f)
        } else {
            // Sweep in progress: clip to reveal from right to left along rotated 25° axis
            withTransform({
                rotate(25f, pivot)
                clipRect(
                    left   = pivot.x + sweepTx * scale,
                    top    = pivot.y - 3000f * scale,
                    right  = pivot.x + 4000f * scale,
                    bottom = pivot.y + 3000f * scale
                )
                rotate(-25f, pivot)
            }) {
                fill(logo.stickerWhite, ColorWhite, 1f)
                fill(logo.stickerNavy,  ColorNavy,  1f)
            }
        }

        // ── Solid white light beam glides across the logo ──
        if (sweepLp in 0.001f..0.999f) {
            withTransform({
                rotate(25f, pivot)
            }) {
                val beamX = pivot.x + sweepTx * scale
                val flankW = 100f * scale
                val coreW  = 40f * scale

                // Soft outer flanks of the light beam
                drawRect(
                    color   = ColorWhite.copy(alpha = 0.35f),
                    topLeft = Offset(beamX - flankW, pivot.y - 3000f * scale),
                    size    = Size(flankW * 2f, 6000f * scale)
                )
                // Bright solid core of the beam
                drawRect(
                    color   = ColorWhite.copy(alpha = 0.80f),
                    topLeft = Offset(beamX - coreW / 2f, pivot.y - 3000f * scale),
                    size    = Size(coreW, 6000f * scale)
                )
            }
        }
    }
}

// ─── Gate segment path drawing helper ────────────────────────────────────────

private fun DrawScope.drawGateSegment(
    sourcePath: Path,
    progress: Float,
    color: Color,
    strokeWidth: Float,
    alpha: Float
) {
    val pm = PathMeasure()
    pm.setPath(sourcePath, forceClosed = false)
    val totalLength = pm.length
    if (totalLength <= 0f) return

    val segPath = Path()
    pm.getSegment(0f, totalLength * progress.coerceIn(0f, 1f), segPath, startWithMoveTo = true)
    drawPath(
        path = segPath,
        color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

// ─── Simple tuple helper ─────────────────────────────────────────────────────

private data class Tuple5(
    val scale: Float,
    val rot: Float,
    val tx: Float,
    val ty: Float,
    val alpha: Float
)

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun TigaRasaSplashPreview() {
    TigaRasaSplash(onFinished = {})
}
