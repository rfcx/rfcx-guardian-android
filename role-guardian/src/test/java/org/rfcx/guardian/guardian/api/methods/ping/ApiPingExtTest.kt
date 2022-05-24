package org.rfcx.guardian.guardian.api.methods.ping

import org.json.JSONObject
import org.junit.Test
import kotlin.test.assertEquals

class ApiPingExtTest {

    @Test
    fun canShortenPingJson() {
        // Arrange
        val expectDevice = JSONObject().apply {
            val expectAndroid = JSONObject().apply {
                put("p", "Guardian")
                put("br", "RFCx")
                put("m", "3G_IOT")
                put("bu", "0.2.3")
                put("a", "4.4.2")
                put("mf", "OrangePi")
            }
            put("a", expectAndroid)

            val expectPhone = JSONObject().apply {
                put("s", "89014103273391284191")
                put("n", "17876906829")
                put("imei", "358877840010721")
                put("imsi", "310410339128419")
            }
            put("p", expectPhone)
        }

        val expectPrefs = JSONObject().apply {
            val expectSha1 = "323bac4c3f4ec6962fc406b9f92468305b19c080"
            put("s", expectSha1)

            val expectVals = JSONObject().apply {
                put("pref1", "0")
                put("pref2", "0")
            }
            put("v", expectVals)
        }
        val expectJson = JSONObject().apply {
            put("sw", "g*0.9.0|a*0.9.0|c*0.8.1")
            put("btt","1638333717221*100*25*0*1")
            put("p","meta*1639553694642|audio*1639553604530|audio*1639550541979")
            put("chn","s*0*0|q*2*326589|m*0*0|sk*23*3666426|st*0*0|a*584*104424960|v*0*0")
            put("ma",1639553745821)
            put("nw","1639662807479*-91*hspa*AT&T|1639661194334*-97*hspa*AT&T")
            put("dt","1639662436136*1639662614136*0*0*490602*9344564|1639662257911*1639662436136*0*0*490602*9344564|1639662079919*1639662257911*0*0*490602*9344564|1639661901896*1639662079919*0*0*490602*9344564|1639661723676*1639661901896*0*0*490602*9344564|1639661545671*1639661723676*0*0*490602*9344564|1639661367657*1639661545671*0*0*490602*9344564|1639661189492*1639661367657*0*0*490602*9344564")
            put("str","i*1639662807198*287502336*1077907456|e*1639662807198*396197888*127436980224|i*1639662257985*287502336*1077907456|e*1639662257985*396197888*127436980224|i*1639661723779*287502336*1077907456|e*1639661723779*396197888*127436980224")
            put("dv", expectDevice)
            put("dtt", "c*chainsaw-v5*1637772067416*975000*,,,0.97,,,0.97,,0.99,0.99,0.99,,0.96,n10,0.97,n4,0.96,n7,0.96,n8,0.97,n4,0.99,,,0.99,0.98,,0.97,0.99,,0.98,,,0.97,,0.99,0.98|c*chainsaw-v5*1637770716128*975000*n17,0.98,n19,0.99,n16,0.98,n13,0.99,,0.98,n8,0.98,,,0.97,n7,0.96|c*chainsaw-v5*1637770355771*975000*n26,0.97,n15,0.98,n7,0.97,n4,0.96,n7,0.97,n5,0.97,,0.96,n9,0.98,0.99,n6,0.95,0.96,,0.99|c*chainsaw-v5*1637769635088*975000*n5,0.98,n43,0.98,n15,0.98,0.99,,0.98,n11,1.00,,0.98,,0.97,0.96|c*chainsaw-v5*1637768013430*975000*n24,0.97,n6,0.99,n15,0.98,n4,0.96,n9,0.97,n10,0.97|c*chainsaw-v5*1637767923336*975000*n55,1.00,,0.98,n6,0.97,n4,0.99,n12,0.98,0.97|c*chainsaw-v5*1637767833252*975000*,,,0.96,,0.98,,0.99,n9,0.97,,,,0.98,,,1.00,,0.99,n10,0.96,n36,0.98,,0.95|c*chainsaw-v5*1637767743164*975000*,0.98,0.97,0.98,,0.99,,0.98,,,,1.00,0.98,0.99,n11,0.99,1.00,,,,0.99,0.97,,0.95,1.00,0.96,1.00,,0.95,0.99,,0.98,0.99,0.97,0.99,,0.99,0.99,,,0.98,0.95,n5,0.98,,0.98,1.00,,,0.96,n4,0.99,0.98,,,,0.99,n6,0.98,n5,1.00,0.98,0.99,0.99,0.99,0.99|c*chainsaw-v5*1637767653074*975000*,,0.96,,,,0.99,,,,0.97,,,0.99,n4,0.95,n5,0.97,,,0.98,0.99,0.97,,1.00,,,0.97,1.00,,0.98,,0.99,,0.99,0.96,1.00,1.00,0.99,0.98,1.00,0.98,,0.98,1.00,1.00,1.00,0.99,0.98,0.98,0.99,0.98,0.96,0.97,0.98,0.98,0.98,1.00,,,0.99,,0.98,0.98,0.99,1.00,,,,1.00,0.99,1.00,0.99,0.99,0.99,,0.97,1.00,0.99,0.99,0.99,0.99,1.00,0.98,,0.98|c*chainsaw-v5*1637767562984*975000*n11,0.99,0.98,0.99,n4,0.96,,,,0.95,n22,0.98,n8,0.96,0.95,n24,0.97|c*chainsaw-v5*1637767292704*975000*n5,0.96,n11,0.97,n15,0.96,n21,0.95,n11,0.97,n9,0.96|c*chainsaw-v5*1637766922524*975000*n13,0.98,,,0.98,0.98,0.96,n15,0.99,n15,0.95,n16,0.96,,0.97,n5,0.98|c*chainsaw-v5*1637766832427*975000*n6,0.97,n14,0.96,n8,0.95,n18,0.96,n25,0.96,,,0.98,0.95|c*chainsaw-v5*1637764670137*975000*n8,0.96,0.99,n5,0.99,0.99,,0.99,,0.99,,,0.98,,,0.95,,0.98,n22,0.98,n4,0.99,0.99,0.96,,0.98,0.97,1.00,n20,0.97|c*chainsaw-v5*1637764580053*975000*n12,0.99,n6,0.98,n19,0.99,,,,0.97,n11,0.98,n4,0.97,,,,0.97,0.98,0.97,n18,0.95,,1.00,0.99,0.99,0.99,0.96,0.99|c*chainsaw-v5*1637764399875*975000*,,0.96,n11,0.96,n10,0.96,n7,0.98,n28,0.95,n22,0.95,0.98")
            put("sp", "s*1639660944000*5386*150*36*798|i*1639660944000*5436*573*92*3080|b*1639660944000*3329*684*1.81*2282|s*1639660795815*5465*151*36*821|i*1639660795815*5502*509*67*2792|b*1639660795815*3324*593*1.64*1971|s*1639660616882*5525*150*36*827|i*1639660616882*5562*484*64*2690|b*1639660616882*3321*561*1.46*1862|s*1639660437945*5825*141*36*819|i*1639660437945*5862*429*64*2515|b*1639660437945*3317*511*1.29*1696|s*1639660258966*5824*141*35*820|i*1639660258966*5862*411*64*2412|b*1639660258966*3315*480*1.13*1591|s*1639660080023*5431*146*35*794|i*1639660080023*5468*458*64*2499|b*1639660080023*3315*514*0.97*1705|s*1639659901032*5535*149*35*827|i*1639659901032*5571*498*64*2772|b*1639659901032*3317*587*0.80*1946|s*1639659722056*5778*145*34*837|i*1639659722056*5816*479*64*2787|b*1639659722056*3314*588*0.61*1950|s*1639659543136*5882*114*32*660|i*1639659543136*5928*228*118*1328|b*1639659543136*3292*202*0.47*668")
            put("pf", expectPrefs)
            put("s", "bm*1.0*1.0|bm*1.0*1.0|ifn*1.0*1.0")
        }


        val mockDevice = JSONObject().apply {
            val mockAndroid = JSONObject().apply {
                put("product","Guardian")
                put("brand","RFCx")
                put("model","3G_IOT")
                put("build","0.2.3")
                put("android","4.4.2")
                put("manufacturer","OrangePi")
            }
            put("android", mockAndroid)

            val mockPhone = JSONObject().apply {
                put("sim","89014103273391284191")
                put("number","17876906829")
                put("imei","358877840010721")
                put("imsi","310410339128419")
            }
            put("phone", mockPhone)
        }

        val mockPrefs = JSONObject().apply {
            val mockSha1 = "323bac4c3f4ec6962fc406b9f92468305b19c080"
            put("sha1", mockSha1)

            val mockVals = JSONObject().apply {
                put("pref1", "0")
                put("pref2", "0")
            }
            put("vals", mockVals)
        }
        val mockJson = JSONObject().apply {
            put("software", "guardian*0.9.0|admin*0.9.0|classify*0.8.1")
            put("battery","1638333717221*100*25*0*1")
            put("purged","meta*1639553694642|audio*1639553604530|audio*1639550541979")
            put("checkins","sent*0*0|queued*2*326589|meta*0*0|skipped*23*3666426|stashed*0*0|archived*584*104424960|vault*0*0")
            put("measured_at",1639553745821)
            put("network","1639662807479*-91*hspa*AT&T|1639661726813*-91*hspa*AT&T|1639661194334*-97*hspa*AT&T")
            put("data_transfer","1639662436136*1639662614136*0*0*490602*9344564|1639662257911*1639662436136*0*0*490602*9344564|1639662079919*1639662257911*0*0*490602*9344564|1639661901896*1639662079919*0*0*490602*9344564|1639661723676*1639661901896*0*0*490602*9344564|1639661545671*1639661723676*0*0*490602*9344564|1639661367657*1639661545671*0*0*490602*9344564|1639661189492*1639661367657*0*0*490602*9344564")
            put("storage","internal*1639662807198*287502336*1077907456|external*1639662807198*396197888*127436980224|internal*1639662257985*287502336*1077907456|external*1639662257985*396197888*127436980224|internal*1639661723779*287502336*1077907456|external*1639661723779*396197888*127436980224")
            put("device", mockDevice)
            put("detections", "chainsaw*chainsaw-v5*1637772067416*975000*,,,0.97,,,0.97,,0.99,0.99,0.99,,0.96,,,,,,,,,,,0.97,,,,,0.96,,,,,,,,0.96,,,,,,,,,0.97,,,,,0.99,,,0.99,0.98,,0.97,0.99,,0.98,,,0.97,,0.99,0.98,,,,,,,,,,,,,,,,,,,,,,,,,,|chainsaw*chainsaw-v5*1637770716128*975000*,,,,,,,,,,,,,,,,,0.98,,,,,,,,,,,,,,,,,,,,0.99,,,,,,,,,,,,,,,,,0.98,,,,,,,,,,,,,,0.99,,0.98,,,,,,,,,0.98,,,0.97,,,,,,,,0.96,,|chainsaw*chainsaw-v5*1637770355771*975000*,,,,,,,,,,,,,,,,,,,,,,,,,,0.97,,,,,,,,,,,,,,,,0.98,,,,,,,,0.97,,,,,0.96,,,,,,,,0.97,,,,,,0.97,,0.96,,,,,,,,,,0.98,0.99,,,,,,,0.95,0.96,,0.99|chainsaw*chainsaw-v5*1637769635088*975000*,,,,,0.98,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,0.98,,,,,,,,,,,,,,,,0.98,0.99,,0.98,,,,,,,,,,,,1.00,,0.98,,0.97,0.96,,,,,,,|chainsaw*chainsaw-v5*1637768013430*975000*,,,,,,,,,,,,,,,,,,,,,,,,0.97,,,,,,,0.99,,,,,,,,,,,,,,,,0.98,,,,,0.96,,,,,,,,,,0.97,,,,,,,,,,,0.97,,,,,,,,,,,,,,,,,,,|chainsaw*chainsaw-v5*1637767923336*975000*,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,1.00,,0.98,,,,,,,0.97,,,,,0.99,,,,,,,,,,,,,0.98,0.97,,,,,,,,,|chainsaw*chainsaw-v5*1637767833252*975000*,,,0.96,,0.98,,0.99,,,,,,,,,,0.97,,,,0.98,,,1.00,,0.99,,,,,,,,,,,0.96,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,0.98,,0.95,,,,,,,,,,,,,,,,|chainsaw*chainsaw-v5*1637767743164*975000*,0.98,0.97,0.98,,0.99,,0.98,,,,1.00,0.98,0.99,,,,,,,,,,,,0.99,1.00,,,,0.99,0.97,,0.95,1.00,0.96,1.00,,0.95,0.99,,0.98,0.99,0.97,0.99,,0.99,0.99,,,0.98,0.95,,,,,,0.98,,0.98,1.00,,,0.96,,,,,0.99,0.98,,,,0.99,,,,,,,0.98,,,,,,1.00,0.98,0.99,0.99,0.99,0.99,|chainsaw*chainsaw-v5*1637767653074*975000*,,0.96,,,,0.99,,,,0.97,,,0.99,,,,,0.95,,,,,,0.97,,,0.98,0.99,0.97,,1.00,,,0.97,1.00,,0.98,,0.99,,0.99,0.96,1.00,1.00,0.99,0.98,1.00,0.98,,0.98,1.00,1.00,1.00,0.99,0.98,0.98,0.99,0.98,0.96,0.97,0.98,0.98,0.98,1.00,,,0.99,,0.98,0.98,0.99,1.00,,,,1.00,0.99,1.00,0.99,0.99,0.99,,0.97,1.00,0.99,0.99,0.99,0.99,1.00,0.98,,0.98|chainsaw*chainsaw-v5*1637767562984*975000*,,,,,,,,,,,0.99,0.98,0.99,,,,,0.96,,,,0.95,,,,,,,,,,,,,,,,,,,,,,,0.98,,,,,,,,,0.96,0.95,,,,,,,,,,,,,,,,,,,,,,,,,0.97,,,,,,,,,,,,|chainsaw*chainsaw-v5*1637767292704*975000*,,,,,0.96,,,,,,,,,,,,0.97,,,,,,,,,,,,,,,,0.96,,,,,,,,,,,,,,,,,,,,,,0.95,,,,,,,,,,,,0.97,,,,,,,,,,0.96,,,,,,,,,,,,,,,|chainsaw*chainsaw-v5*1637766922524*975000*,,,,,,,,,,,,,0.98,,,0.98,0.98,0.96,,,,,,,,,,,,,,,,0.99,,,,,,,,,,,,,,,,0.95,,,,,,,,,,,,,,,,,0.96,,0.97,,,,,,0.98,,,,,,,,,,,,,,,,|chainsaw*chainsaw-v5*1637766832427*975000*,,,,,,0.97,,,,,,,,,,,,,,,0.96,,,,,,,,,0.95,,,,,,,,,,,,,,,,,,,0.96,,,,,,,,,,,,,,,,,,,,,,,,,,0.96,,,0.98,0.95,,,,,,,,,,,,,|chainsaw*chainsaw-v5*1637764670137*975000*,,,,,,,,0.96,0.99,,,,,,0.99,0.99,,0.99,,0.99,,,0.98,,,0.95,,0.98,,,,,,,,,,,,,,,,,,,,,,,0.98,,,,,0.99,0.99,0.96,,0.98,0.97,1.00,,,,,,,,,,,,,,,,,,,,,0.97,,,,,,,,,|chainsaw*chainsaw-v5*1637764580053*975000*,,,,,,,,,,,,0.99,,,,,,,0.98,,,,,,,,,,,,,,,,,,,,0.99,,,,0.97,,,,,,,,,,,,0.98,,,,,0.97,,,,0.97,0.98,0.97,,,,,,,,,,,,,,,,,,,0.95,,1.00,0.99,0.99,0.99,0.96,0.99|chainsaw*chainsaw-v5*1637764399875*975000*,,0.96,,,,,,,,,,,,0.96,,,,,,,,,,,0.96,,,,,,,,0.98,,,,,,,,,,,,,,,,,,,,,,,,,,,,,0.95,,,,,,,,,,,,,,,,,,,,,,,0.95,0.98,,,,,,,")
            put("sentinel_power", "system*1639660944000*5386*150*36*798|input*1639660944000*5436*573*92*3080|battery*1639660944000*3329*684*1.81*2282|system*1639660795815*5465*151*36*821|input*1639660795815*5502*509*67*2792|battery*1639660795815*3324*593*1.64*1971|system*1639660616882*5525*150*36*827|input*1639660616882*5562*484*64*2690|battery*1639660616882*3321*561*1.46*1862|system*1639660437945*5825*141*36*819|input*1639660437945*5862*429*64*2515|battery*1639660437945*3317*511*1.29*1696|system*1639660258966*5824*141*35*820|input*1639660258966*5862*411*64*2412|battery*1639660258966*3315*480*1.13*1591|system*1639660080023*5431*146*35*794|input*1639660080023*5468*458*64*2499|battery*1639660080023*3315*514*0.97*1705|system*1639659901032*5535*149*35*827|input*1639659901032*5571*498*64*2772|battery*1639659901032*3317*587*0.80*1946|system*1639659722056*5778*145*34*837|input*1639659722056*5816*479*64*2787|battery*1639659722056*3314*588*0.61*1950|system*1639659543136*5882*114*32*660|input*1639659543136*5928*228*118*1328|battery*1639659543136*3292*202*0.47*668")
            put("prefs", mockPrefs)
            put("sentinel_sensor", "bme688*1.0*1.0|bme688*1.0*1.0|infineon*1.0*1.0")

        }

        // Act
        val json = ApiPingExt.shortenPingJson(mockJson)

        // Assert
        assertEquals(expectJson.get("sw"), json.get("sw"))
        assertEquals(expectJson.get("btt"), json.get("btt"))
        assertEquals(expectJson.get("p"), json.get("p"))
        assertEquals(expectJson.get("chn"), json.get("chn"))
        assertEquals(expectJson.get("ma"), json.get("ma"))
        assertEquals(expectJson.get("nw"), json.get("nw"))
        assertEquals(expectJson.get("dt"), json.get("dt"))
        assertEquals(expectJson.get("str"), json.get("str"))
        assertEquals(expectJson.get("dtt"), json.get("dtt"))
        assertEquals(expectJson.get("sp"), json.get("sp"))
        assertEquals(expectJson.get("dv").toString(), json.get("dv").toString())
        assertEquals(expectJson.get("pf").toString(), json.get("pf").toString())
        assertEquals(expectJson.get("s"), json.get("s"))
    }

    @Test
    fun canShortenDetectionType() {
        val expectedType = "c"
        val actualType = "chainsaw"

        val type = ApiPingExt.shortenDetectionType(actualType)

        assertEquals(expectedType, type)
    }

    @Test
    fun canShortenDetectionWrongType() {
        val expectedType = "human"
        val actualType = "human"

        val type = ApiPingExt.shortenDetectionType(actualType)

        assertEquals(expectedType, type)
    }

    @Test
    fun canShortenDetectionConfidences() {
        val expectedConfidences = ",,,0.97,,,0.97,,0.99,0.99,0.99,,0.96,n10,0.97,n4,0.96,n7,0.96,n8,0.97,n4,0.99,,,0.99,0.98,,0.97,0.99,,0.98,,,0.97,,0.99,0.98"
        val actualConfidences = ",,,0.97,,,0.97,,0.99,0.99,0.99,,0.96,,,,,,,,,,,0.97,,,,,0.96,,,,,,,,0.96,,,,,,,,,0.97,,,,,0.99,,,0.99,0.98,,0.97,0.99,,0.98,,,0.97,,0.99,0.98,,,,,,,,,,,,,,,,,,,,,,,,,,"

        val confidences = ApiPingExt.shortenDetectionConfidence(actualConfidences)

        assertEquals(expectedConfidences, confidences)
    }

    @Test
    fun canShortenFieldThatNotInCases() {
        val mockJson = JSONObject().apply {
            put("test_key", "test_values")
        }

        val json = ApiPingExt.shortenPingJson(mockJson)
        assertEquals("test_values", json.get("test_key"))
    }

    @Test
    fun canShortenFieldThatGetShortenOnlyKey() {
        val mockJson = JSONObject().apply {
            put("measured_at",1639553745821)
            put("data_transfer","1639662436136*1639662614136*0*0*490602*9344564|1639662257911*1639662436136*0*0*490602*9344564|1639662079919*1639662257911*0*0*490602*9344564|1639661901896*1639662079919*0*0*490602*9344564|1639661723676*1639661901896*0*0*490602*9344564|1639661545671*1639661723676*0*0*490602*9344564|1639661367657*1639661545671*0*0*490602*9344564|1639661189492*1639661367657*0*0*490602*9344564")
        }

        val json = ApiPingExt.shortenPingJson(mockJson)
        assertEquals(true, json.keys().asSequence().contains("ma"))
        assertEquals(true, json.keys().asSequence().contains("dt"))
    }

    @Test
    fun canShortenPrefs() {
        val mockPrefs = JSONObject().apply {
            val mockSha1 = "323bac4c3f4ec6962fc406b9f92468305b19c080"
            put("sha1", mockSha1)

            val mockVals = JSONObject().apply {
                put("pref1", "0")
                put("pref2", "0")
            }
            put("vals", mockVals)
        }
        val mockJson = JSONObject().put("prefs", mockPrefs)

        val json = ApiPingExt.shortenPingJson(mockJson)
        assertEquals(true, json.keys().asSequence().contains("pf"))
        assertEquals(true, json.getJSONObject("pf").keys().asSequence().contains("s"))
        assertEquals(true, json.getJSONObject("pf").keys().asSequence().contains("v"))
    }

    @Test
    fun canShortenPrefsButMissingSomeFields() {
        val mockPrefs = JSONObject().apply {
            val mockSha1 = "323bac4c3f4ec6962fc406b9f92468305b19c080"
            put("sha1", mockSha1)
        }
        val mockJson = JSONObject().put("prefs", mockPrefs)

        val json = ApiPingExt.shortenPingJson(mockJson)
        assertEquals(true, json.keys().asSequence().contains("pf"))
        assertEquals(true, json.getJSONObject("pf").keys().asSequence().contains("s"))
        assertEquals(false, json.getJSONObject("pf").keys().asSequence().contains("v"))
    }
}
