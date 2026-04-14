package com.project.seoulmate

import com.project.seoulmate.data.model.NaverSearchItem
import org.junit.Test
import org.junit.Assert.*

class CoordinateTest {

    @Test
    fun testNaverCoordinateConversion() {
        // 서울시청 부근 TM128 좌표 샘플
        val seoulCityHall = NaverSearchItem(
            title = "서울시청",
            address = "서울특별시 중구 태평로1가 31",
            roadAddress = "서울특별시 중구 세종대로 110",
            mapx = "309589",
            mapy = "552125"
        )

        val lat = seoulCityHall.getLatitude()
        val lng = seoulCityHall.getLongitude()

        println("테스트 결과 - 위도: $lat, 경도: $lng")

        // 현재 로직: mapy / 10,000,000.0 => 0.0552125
        // 예상되는 실제 위도: 약 37.5665...
        
        // 이 단언문은 현재 로직이 '틀렸음'을 증명하기 위해 작성되었습니다.
        // 만약 37.0보다 작다면, 서울을 벗어난 잘못된 좌표임을 의미합니다.
        assertTrue("좌표가 서울 범위를 벗어났습니다 ($lat)", lat > 37.0)
    }
}
