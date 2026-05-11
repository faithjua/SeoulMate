package com.project.seoulmate.di

import com.project.seoulmate.data.repository.AuthRepository
import com.project.seoulmate.data.repository.AuthRepositoryImpl
import com.project.seoulmate.data.repository.CourseRepository
import com.project.seoulmate.data.repository.CourseRepositoryImpl
import com.project.seoulmate.data.repository.MeetingRepository
import com.project.seoulmate.data.repository.MeetingRepositoryImpl
import com.project.seoulmate.data.repository.FavoriteRepository
import com.project.seoulmate.data.repository.FavoriteRepositoryImpl
import com.project.seoulmate.data.repository.UserActionRepository
import com.project.seoulmate.data.repository.UserActionRepositoryImpl
import com.project.seoulmate.data.repository.ApplicationRepository
import com.project.seoulmate.data.repository.ApplicationRepositoryImpl
import com.project.seoulmate.data.repository.NotificationRepository
import com.project.seoulmate.data.repository.NotificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI(의존성 주입) 모듈.
 *
 * "누군가 MeetingRepository를 달라고 하면 MeetingRepositoryImpl을 줘라"
 * 라고 Hilt에게 알려주는 설정 파일
 *
 * @Module: 이 클래스가 Hilt 모듈임을 선언
 * @InstallIn(SingletonComponent): 앱 전체 생명주기 동안 단 하나의 인스턴스만 유지
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * MeetingRepository 인터페이스 → MeetingRepositoryImpl 구현체로 바인딩
     *
     * @Binds: 인터페이스와 구현체를 연결
     * @Singleton: 앱 전체에서 하나의 인스턴스만 생성 (메모리 효율)
     */
    @Binds
    @Singleton
    abstract fun bindMeetingRepository(
        impl: MeetingRepositoryImpl
    ): MeetingRepository

    // login signup 위한 바인딩
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        impl: CourseRepositoryImpl
    ): CourseRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        impl: FavoriteRepositoryImpl
    ): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindUserActionRepository(
        impl: UserActionRepositoryImpl
    ): UserActionRepository

    @Binds
    @Singleton
    abstract fun bindApplicationRepository(
        impl: ApplicationRepositoryImpl
    ): ApplicationRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

}