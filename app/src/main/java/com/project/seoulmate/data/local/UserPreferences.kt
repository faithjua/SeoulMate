package com.project.seoulmate.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사용자 관련 로컬 데이터 저장소
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * 로그인 시 받은 회원 ID 저장
     */
    fun saveMemberId(memberId: Long) {
        prefs.edit().putLong(KEY_MEMBER_ID, memberId).apply()
    }

    /**
     * 저장된 회원 ID 가져오기
     * @return 저장된 memberId, 없으면 null
     */
    fun getMemberId(): Long? {
        val id = prefs.getLong(KEY_MEMBER_ID, -1L)
        return if (id == -1L) null else id
    }

    /**
     * 프로필 이미지 URL 저장
     */
    fun saveProfileImageUrl(imageUrl: String?) {
        if (imageUrl != null) {
            prefs.edit().putString(KEY_PROFILE_IMAGE_URL, imageUrl).apply()
        } else {
            prefs.edit().remove(KEY_PROFILE_IMAGE_URL).apply()
        }
    }

    /**
     * 저장된 프로필 이미지 URL 가져오기
     * @return 저장된 이미지 URL, 없으면 null
     */
    fun getProfileImageUrl(): String? {
        return prefs.getString(KEY_PROFILE_IMAGE_URL, null)
    }

    /**
     * 로그아웃 시 모든 사용자 데이터 삭제
     */
    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "seoulmate_user_prefs"
        private const val KEY_MEMBER_ID = "member_id"
        private const val KEY_PROFILE_IMAGE_URL = "profile_image_url"
    }
}
