// Retreive data from api - backend logic
// returns data and GUI displays it

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WeatherApp {
    // fetch weather for given location
    public static JSONObject getWeatherData(String locationName){

        JSONArray locationData = getLocationData(locationName);
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
}
