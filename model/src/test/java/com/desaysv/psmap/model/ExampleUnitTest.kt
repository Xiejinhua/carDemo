package com.desaysv.psmap.model

import com.desaysv.psmap.model.bean.iflytek.SlotsQuery
import com.google.gson.Gson
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val slots = Gson().fromJson("{\"startLoc\":{\"ori_loc\":\"CURRENT_ORI_LOC\"},\"endLoc\":{\"ori_loc\":\"望江西路666号\",\"topic\":\"others\",\"road_num\":\"666号\",\"road_cross_first\":\"望江西路\"}}",
            SlotsQuery::class.java
        )
        println("==== $slots")
    }
}
