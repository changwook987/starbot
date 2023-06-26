import command.configureSchoolMeal
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.GatewayIntent

fun main(args: Array<String>) {
    val jda = light(args[0]) {
        intents += GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)

    }
    jda.module()
}

fun JDA.module() {
    configureSchoolMeal()
}