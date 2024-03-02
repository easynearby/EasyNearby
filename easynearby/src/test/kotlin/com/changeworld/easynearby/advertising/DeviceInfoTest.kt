package com.changeworld.easynearby.advertising

import com.changeworld.easynearby.ConnectionStrategy
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.Test

class DeviceInfoTest {

    @Test
    fun `test Two same devices are equal`() {
        assertThat(
            DeviceInfo("name", "serviceId", ConnectionStrategy.STAR),
            equalTo(DeviceInfo("name", "serviceId", ConnectionStrategy.STAR))
        )
    }

    @Test
    fun `test Two different devices are not equal`() {
        assertThat(
            DeviceInfo("name", "serviceId", ConnectionStrategy.STAR),
            not(equalTo(DeviceInfo("name1", "serviceId", ConnectionStrategy.STAR)))
        )
    }
}