package data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*


/**
 * Serializable Meal data class
 *
 * [여기](https://open.neis.go.kr/portal/data/service/selectServicePage.do?page=1&rows=10&sortColumn=&sortDirection=&infId=OPEN17320190722180924242823&infSeq=1)
 * 의 급식 데이터 정보 모델을 그대로 옮긴 것이다
 */
@Serializable
data class Meal(
    /** 시도교육청코드 */
    @SerialName("ATPT_OFCDC_SC_CODE")
    val eduOfficeCode: String,
    /** 시도교육청명 */
    @SerialName("ATPT_OFCDC_SC_NM")
    val eduOfficeName: String,
    /** 표준학교코드 */
    @SerialName("SD_SCHUL_CODE")
    val schoolCode: String,
    /** 학교명 */
    @SerialName("SCHUL_NM")
    val schoolName: String,
    /** 식사코드 */
    @SerialName("MMEAL_SC_CODE")
    val code: String,
    /** 식사명 */
    @SerialName("MMEAL_SC_NM")
    val mealName: String,
    /** 급식일자 */
    @SerialName("MLSV_YMD")
    val date: String,
    /** 급식인원수 */
    @SerialName("MLSV_FGR")
    val peopleNumber: String,
    /** 요리명 */
    @SerialName("DDISH_NM")
    val dishName: String,
    /** 원산지정보 */
    @SerialName("ORPLC_INFO")
    val originInfo: String,
    /** 칼로리정보 */
    @SerialName("CAL_INFO")
    val calorie: String,
    /** 영양정보 */
    @SerialName("NTR_INFO")
    val nutritionalInfo: String,
    /** 급식시작일자 */
    @SerialName("MLSV_FROM_YMD")
    @Serializable(with = DateSerializer::class)
    val startDate: Date,
    /** 급식종료일자 */
    @SerialName("MLSV_TO_YMD")
    @Serializable(with = DateSerializer::class)
    val endDate: Date,
) {
    private object DateSerializer : KSerializer<Date> {
        private val format = SimpleDateFormat("yyyyMMdd")

        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Date =
            format.parse(decoder.decodeString())

        override fun serialize(encoder: Encoder, value: Date) =
            encoder.encodeString(format.format(value))
    }
}

