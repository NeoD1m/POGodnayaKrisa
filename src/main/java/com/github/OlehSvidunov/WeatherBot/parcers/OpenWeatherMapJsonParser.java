package com.github.OlehSvidunov.WeatherBot.parcers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.OlehSvidunov.WeatherBot.util.WeatherUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OpenWeatherMapJsonParser implements WeatherParser {
    private final static String API_CALL_TEMPLATE = "https://api.openweathermap.org/data/2.5/forecast?q=";
    //Replace "..." with your OpenWeatherMap API key
    private final static String API_KEY_TEMPLATE = "&units=metric&APPID=b0b4be40082b1a4bd06dfcd468073ac7";
    private final static String USER_AGENT = "Mozilla/5.0";
    private final static DateTimeFormatter INPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static DateTimeFormatter OUTPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM-dd HH:mm", Locale.US);

    @Override
    public String getReadyForecast(String city) {
        String result;
        try {
            String jsonRawData = downloadJsonRawData(city);
            List<String> linesOfForecast = convertRawDataToList(jsonRawData);
            result = String.format("%s:%s%s",firstUpperCase(city), System.lineSeparator(), parseForecastDataFromList(linesOfForecast));
        } catch (IllegalArgumentException e) {
            return String.format("Can't find \"%s\" city. Try another one, for example: \"Moscow\" or \"New York\"", city);
        } catch (Exception e) {
            e.printStackTrace();
            return "The service is not available, please try later";
        }
        return result;
    }
    public String firstUpperCase(String word){
        if(word == null || word.isEmpty()) return ""; //или return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private static String downloadJsonRawData(String city) throws Exception {
        String urlString = API_CALL_TEMPLATE + city + API_KEY_TEMPLATE;
        URL urlObject = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = connection.getResponseCode();
        if (responseCode == 404) {
            throw new IllegalArgumentException();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }


    private static List<String> convertRawDataToList(String data) throws Exception {
        List<String> weatherList = new ArrayList<>();



        JsonNode arrNode = new ObjectMapper().readTree(data).get("list");
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                String forecastTime = objNode.get("dt_txt").toString();
                if (forecastTime.contains("09:00") || forecastTime.contains("9:00")) {
                    weatherList.add(objNode.toString());
                }
            }
        }
        return weatherList;
    }

    private static String parseForecastDataFromList(List<String> weatherList) throws Exception {
        final StringBuffer sb = new StringBuffer();
        ObjectMapper objectMapper = new ObjectMapper();

        for (String line : weatherList) {
            {
                String dateTime;
                JsonNode mainNode;
                JsonNode windNode;
                JsonNode nameNode;
                JsonNode weatherArrNode;
                try {
                    mainNode = objectMapper.readTree(line).get("main");
                    windNode = objectMapper.readTree(line).get("wind");
                    nameNode  = objectMapper.readTree(line).get("sys");
                    weatherArrNode = objectMapper.readTree(line).get("weather");
                    for (final JsonNode objNode : weatherArrNode) {
                        dateTime = objectMapper.readTree(line).get("dt_txt").toString();
                        sb.append(formatForecastData(dateTime, objNode.get("main").toString(), mainNode.get("temp").asDouble()));
                        sb.append(humidityData(dateTime, objNode.get("main").toString(), mainNode.get("humidity").asDouble()));
                        sb.append(windData(dateTime, objNode.get("main").toString(), windNode.get("speed").asInt()));

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return sb.toString();
            }
        }
        return sb.toString();
    }
    private static String Name(String dateTime, String description, String temperature) throws Exception {
        LocalDateTime forecastDateTime = LocalDateTime.parse(dateTime.replaceAll("\"", ""), INPUT_DATE_TIME_FORMAT);
        String formattedDateTime = forecastDateTime.format(OUTPUT_DATE_TIME_FORMAT);

        String formattedTemperature;


        String formattedDescription = description.replaceAll("\"", "");

        String weatherIconCode = WeatherUtils.weatherIconsCodes.get(formattedDescription);
        return String.format("%s%s  %s %s %s", "", temperature, formattedDescription, "", System.lineSeparator());
    }
    private static String formatForecastData(String dateTime, String description, double temperature) throws Exception {
        LocalDateTime forecastDateTime = LocalDateTime.parse(dateTime.replaceAll("\"", ""), INPUT_DATE_TIME_FORMAT);
        String formattedDateTime = forecastDateTime.format(OUTPUT_DATE_TIME_FORMAT);

        String formattedTemperature;
        long roundedTemperature = Math.round(temperature);
        if (roundedTemperature > 0) {
            formattedTemperature = "Temperature: +" + String.valueOf(Math.round(temperature));
        } else {
            formattedTemperature = String.valueOf(Math.round(temperature));
        }

        String formattedDescription = description.replaceAll("\"", "");

        String weatherIconCode = WeatherUtils.weatherIconsCodes.get(formattedDescription);
        return String.format("%s%s  %s %s %s", "", formattedTemperature, formattedDescription, weatherIconCode, System.lineSeparator());
    }
    private static String windData(String dateTime, String description, double temperature) throws Exception {
        LocalDateTime forecastDateTime = LocalDateTime.parse(dateTime.replaceAll("\"", ""), INPUT_DATE_TIME_FORMAT);
        String formattedDateTime = forecastDateTime.format(OUTPUT_DATE_TIME_FORMAT);

        String formattedTemperature;
        long roundedTemperature = Math.round(temperature);
        if (roundedTemperature > 0) {
            formattedTemperature = "Wind speed: " + String.valueOf(Math.round(temperature)) + " kmph";
        } else {
            formattedTemperature = "Wind speed: " + String.valueOf(Math.round(temperature)) + " kmph";
        }

        String formattedDescription = description.replaceAll("\"", "");

        String weatherIconCode = WeatherUtils.weatherIconsCodes.get(formattedDescription);

        return String.format("%s%s%s%s%s", "", formattedTemperature, "", "", System.lineSeparator());
    }
    private static String humidityData(String dateTime, String description, double temperature) throws Exception {
        LocalDateTime forecastDateTime = LocalDateTime.parse(dateTime.replaceAll("\"", ""), INPUT_DATE_TIME_FORMAT);
        String formattedDateTime = forecastDateTime.format(OUTPUT_DATE_TIME_FORMAT);

        String formattedTemperature;
        long roundedTemperature = Math.round(temperature);
        if (roundedTemperature > 0) {
            formattedTemperature = "Humidity: " + String.valueOf(Math.round(temperature)) + "%";
        } else {
            formattedTemperature = String.valueOf(Math.round(temperature));
        }

        String formattedDescription = description.replaceAll("\"", "");

        String weatherIconCode = WeatherUtils.weatherIconsCodes.get(formattedDescription);

        return String.format("%s%s%s%s%s", "", formattedTemperature, "", "", System.lineSeparator());
    }
}
