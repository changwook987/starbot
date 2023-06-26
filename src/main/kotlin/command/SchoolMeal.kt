package command

import data.Meal
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import kotlinx.serialization.json.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.slf4j.LoggerFactory
import util.Net
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.minutes

private const val API_SERVER = "https://open.neis.go.kr/hub/mealServiceDietInfo"
private const val COMMAND = "밥내놔"
private val dateFormat = SimpleDateFormat("yyyyMMdd")
private val prettyDateFormat = SimpleDateFormat("yyyy년 MM월 dd일")

private val logger = LoggerFactory.getLogger("SchoolMeal")

/**
 * configure JDA school meal command
 */
fun JDA.configureSchoolMeal() {
    // [ add | update ] command
    upsertCommand(COMMAND, "밥갖고와!").queue()

    // register command event listener
    onCommand(COMMAND, 5.minutes) { event ->
        logger.debug("received school meal command")
        val user = event.user

        // school meal date
        val date = Calendar.getInstance(Locale.KOREA)

        // date functions
        val plusDate = { date.add(Calendar.DATE, 1) }
        val minusDate = { date.add(Calendar.DATE, -1) }
        val curDate = { date.timeInMillis = System.currentTimeMillis() }
        val prettyDate = { prettyDateFormat.format(date.time) }

        // create school meal embed
        val createEmbed = suspend {
            // True if catch exception on get meal data
            var error = false
            val meals = try {
                getMeals(date)
            } catch (e: Exception) {
                error = true
                emptyList()
            }

            Embed {
                title = "대소 고급식"
                color = (0x000000..0xFFFFFF).random()
                description = buildString {
                    append("~ ")
                    append(prettyDate())
                    append("의 급식 ~")
                }
                if (meals.isEmpty()) {
                    field {
                        name = if (error) {
                            "급식정보를 가져올 수 없었어요"
                        } else {
                            "오늘은 급식이 없어요!"
                        }
                    }
                } else {
                    for (meal in meals) {
                        val line = buildString {
                            for (dish in meal.dishName.split("<br/>")) {
                                val (name) = dish.split("\\s+".toRegex())

                                // trim leading special character
                                appendLine(name.trimStart {
                                    // region non-special characters
                                    it !in '가'..'힣' &&
                                        it !in 'a'..'z' &&
                                        it !in 'A'..'Z' &&
                                        it !in '0'..'9'
                                    // endregion
                                })
                            }
                        }
                        field {
                            name = meal.mealName
                            value = line
                            inline = true
                        }
                    }
                }
            }
        }


        val buttonAction: (() -> Unit) -> suspend (ButtonInteractionEvent) -> Unit = { action ->
            { event ->
                if (event.user == user) {
                    action()
                    event.editMessageEmbeds(createEmbed()).queue()
                } else {
                    event.reply_(
                        content = "다른 사람의 임베드와 상호작용할 수 없어요",
                        ephemeral = true
                    ).queue()
                }
            }
        }

        event.replyEmbeds(
            createEmbed()
        ).setComponents(
            row(
                button(
                    label = "어제 밥",
                    style = ButtonStyle.PRIMARY,
                    listener = buttonAction(minusDate)
                ),
                button(
                    label = "오늘 밥",
                    style = ButtonStyle.DANGER,
                    listener = buttonAction(curDate)
                ),
                button(
                    label = "내일 밥",
                    style = ButtonStyle.PRIMARY,
                    listener = buttonAction(plusDate)
                )
            ),
        ).queue()
    }
}

/**
 * get school meal
 */
private suspend fun getMeals(calendar: Calendar): List<Meal> {
    try {
        return Net.get<List<Meal>>(
            url = API_SERVER,
            query = mapOf(
                "KEY" to "d5f465075b8446dd9176764bf778e709",
                "TYPE" to "JSON",
                "ATPT_OFCDC_SC_CODE" to "D10",
                "SD_SCHUL_CODE" to "7240454",
                "MLSV_YMD" to dateFormat.format(calendar.time)
            )
        ) { response ->
            val body = response.body ?: return@get emptyList()
            val jsonArray = Json.parseToJsonElement(body.string())
                .jsonObject["mealServiceDietInfo"]
                ?.jsonArray?.get(1)
                ?.jsonObject?.get("row")
                ?.jsonArray ?: JsonArray(emptyList())
            jsonArray.map { Json.decodeFromJsonElement(it) }
        }.await() ?: emptyList()
    } catch (e: Exception) {
        logger.error("unexpected error caught!")
        logger.trace(e.stackTraceToString())
        throw e
    }
}