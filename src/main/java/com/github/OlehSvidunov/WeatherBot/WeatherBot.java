package com.github.OlehSvidunov.WeatherBot;

import com.github.OlehSvidunov.WeatherBot.parcers.OpenWeatherMapJsonParser;
import com.github.OlehSvidunov.WeatherBot.parcers.WeatherParser;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;

import static org.telegram.abilitybots.api.objects.Flag.TEXT;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

public class WeatherBot extends AbilityBot {
    //Replace "..." with your Telegram bot token
    private static final String BOT_TOKEN = "1234557246:AAGj809mfN2WMaW8Ox9vQV6TM9itN31ItgE";
    //Replace "..." with your Telegram bot name
    private static final String BOT_NAME = "pogkris_bot";
    private WeatherParser weatherParser = new OpenWeatherMapJsonParser();

    public WeatherBot() {
        super(BOT_TOKEN, BOT_NAME);
    }

    //Replace "-1" with your Telegram user id
    @Override
    public int creatorId() {
        return 727524554;
    }

    public Ability startCommand() {
        return Ability
                .builder()
                .name("start")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Hello! Enter the city in chat. " +
                        "For example: \"New York\" or \"Istanbul\"", ctx.chatId()))
                .build();
    }

    public Ability sendWeather() {
        return Ability.builder()
                .name(DEFAULT)
                .flag(TEXT)
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action((MessageContext ctx) -> {
                    if (ctx.firstArg().equals(ctx.secondArg())) {
                        silent.send(weatherParser.getReadyForecast(ctx.firstArg()), ctx.chatId());
                    } else {
                        silent.send(weatherParser.getReadyForecast(String.format("%s %s", ctx.firstArg(), ctx.secondArg())), ctx.chatId());
                    }
                })
                .build();
    }
}
