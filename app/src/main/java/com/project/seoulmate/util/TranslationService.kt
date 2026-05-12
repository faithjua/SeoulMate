package com.project.seoulmate.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale

/**
 * ML Kit 온디바이스 번역 (한국어 → 디바이스 시스템 언어).
 *
 * - 디바이스 언어가 한국어면 번역 기능 비활성 (isEnabled = false). UI는 아이콘을 숨기면 됨.
 * - 그 외 언어(en, ja, zh, fr, de, …)면 해당 언어를 타겟으로 잡고 페어별 Translator를 캐싱.
 * - 모델 다운로드는 Wi-Fi 환경에서만, 페어당 1회.
 */
object TranslationService {

    // target lang code → Translator (페어별 캐시)
    private val translators = mutableMapOf<String, Translator>()
    private val readyModels = mutableSetOf<String>()

    /** 디바이스 시스템 언어 코드 (ML Kit 기준). 미지원 언어면 영어로 폴백. */
    fun targetLanguage(): String {
        val tag = Locale.getDefault().language
        return TranslateLanguage.fromLanguageTag(tag) ?: TranslateLanguage.ENGLISH
    }

    /** 번역이 의미 있는지 여부 (디바이스가 한국어면 false). */
    val isEnabled: Boolean
        get() = targetLanguage() != TranslateLanguage.KOREAN

    /** 번역 결과 박스에 표시할 라벨 (예: "EN", "JA"). */
    fun targetLabel(): String = Locale.getDefault().language.uppercase(Locale.ROOT)

    @Synchronized
    private fun translator(target: String): Translator =
        translators.getOrPut(target) {
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.KOREAN)
                    .setTargetLanguage(target)
                    .build()
            )
        }

    /** 모델 다운로드. Wi-Fi 필요. 이미 받아져 있으면 no-op. */
    suspend fun ensureModelDownloaded(): Result<Unit> = runCatching {
        if (!isEnabled) return@runCatching
        val target = targetLanguage()
        if (target in readyModels) return@runCatching
        val conditions = DownloadConditions.Builder().requireWifi().build()
        translator(target).downloadModelIfNeeded(conditions).await()
        readyModels.add(target)
        Timber.d("ML Kit translation model ready (ko → $target)")
    }

    /** 한국어 텍스트를 디바이스 언어로 번역. 디바이스가 한국어거나 빈 문자열이면 원문 반환. */
    suspend fun translate(text: String): Result<String> = runCatching {
        if (text.isBlank() || !isEnabled) return@runCatching text
        val target = targetLanguage()
        ensureModelDownloaded().getOrThrow()
        translator(target).translate(text).await()
    }

    fun close() {
        translators.values.forEach { runCatching { it.close() } }
        translators.clear()
        readyModels.clear()
    }
}