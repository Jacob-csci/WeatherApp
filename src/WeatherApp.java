// Retreive data from api - backend logic
// returns data and GUI displays it

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WeatherApp {
    // fetch weather for given location
    public static JSONObject getWeatherData(String locationName){

        JSONArray locationData = getLocationData(locationName);

        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        //build API request url with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
        "latitude=" + latitude + "&longitude=" + longitude + 
        "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=America%2FChicago";

        try{
            //Connect to api
            HttpURLConnection conn = fetchApiResponse(urlString);

            //Check that we connected to the API
            if(conn.getResponseCode() != 200){
                System.out.println("Error: could not connect to API");
                return null;
            }

            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()){
                //read and store into string builder
                resultJson.append(scanner.nextLine());
            }

            scanner.close();
            conn.disconnect();

            //parse through data
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            //retreive hourly data
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            
            //we want to get the current hours data
            //so we need the index of the hour

            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            //get weather code
            JSONArray weathercode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            //humidity
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            //Windspeed
            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windspeed = (double) windspeedData.get(index);

            //build json data we are going to recieve for the frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;


        }catch(Exception e){
            e.printStackTrace();
        }
            
        return null;
    }

    // retreive geographic coordinates for given locations
    public static JSONArray getLocationData(String locationName){
        // replace whitespace in location name with + to follow the api request format
        locationName = locationName.replaceAll( " ",  "+");

        // build api url with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
        locationName + "&count=10&language=en&format=json";

        try{
            // call api and get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check reponse status
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }else{
                //store api results
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                //read and store the data into our string builder
                while (scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }

                //close scanner
                scanner.close();

                //close connection
                conn.disconnect();

                //parse the JSON string into a JSON object
                JSONParser parser = new JSONParser();
                JSONObject resulJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                //Get location data from the API
                JSONArray locationData = (JSONArray) resulJsonObj.get("results");
                return locationData;

            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            //attempt to create connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //request method to get
            conn.setRequestMethod("GET");

            //connect to our API
            conn.connect();
            return conn;

        }catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){

        String currentTime = getCurrentTime();

        // iterate through time list to see which matches ours
        for(int i = 0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);

            if(time.equalsIgnoreCase(currentTime)){
                //return index
                return i;
            }
        }
        return 0;
    }

    public static String getCurrentTime(){
        //get current date and time

        LocalDateTime currentDateTime = LocalDateTime.now();
        
        //format time from api
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        //print current date and time 
        String formattedDateTime = currentDateTime.format(formatter);


        return formattedDateTime;
        
    }
    //Convert weather code to something readable
    private static String convertWeatherCode(long weathercode){
        String weatherCondition = "";
        if(weathercode == 0L){
            weatherCondition = "Clear";
        }else if(weathercode <= 3L && weathercode > 0){
            weatherCondition = "Cloudy";
        }else if((weathercode >= 51L && weathercode <= 67L) || (weathercode >= 80L && weathercode <= 99L)){
            weatherCondition = "Rain";
        }else if(weathercode >= 71L && weathercode <= 77L){
            weatherCondition = "Snow";
        }

        return weatherCondition;

    }

}
