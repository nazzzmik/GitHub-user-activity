import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        if(args.length < 1 || args.length > 1){
            System.out.println("Usage: Invalid number of Arguments");
            return;
        }
        String username = args[0];
        String apiurl = "https://api.github.com/users/" + username + "/events";
        System.out.println(username);

        try {
            URL url = new URL(apiurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection .setRequestMethod("GET");
            int response_code = connection.getResponseCode();
            //add response code
            if (response_code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                // Close the streams
                in.close();

                //Print the raw JSON response
                //System.out.println("Raw JSON response:");
                //System.out.println(content.toString());
                IterateJSON(content.toString());
            }
            else{
                System.out.println("Error;HTTP response code"+ response_code);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void IterateJSON (String jsonResponse){
        ArrayList<HashMap<String,String>> activities = new ArrayList<>();

        String[] events = jsonResponse.split("\\},\\{");

        for(String event: events){
            HashMap<String, String> activity = new HashMap<>();

            // Find the "type" field (event type)
            if (event.contains("\"type\":\"")) {
                String type = extractValue(event, "\"type\":\"", "\"");
                activity.put("type", type);
            }

            // Find the "repo.name" field (repository name)
            if (event.contains("\"repo\":{\"name\":\"")) {
                String repoName = extractValue(event, "\"repo\":{\"name\":\"", "\"");
                activity.put("repo", repoName);
            }

            // Handle "WatchEvent" and "PullRequestReviewEvent" by extracting "action"
            if (event.contains("\"action\":\"")) {
                String action = extractValue(event, "\"action\":\"", "\"");
                activity.put("action", action);
            }

            // Handle "ForkEvent" by extracting "forkee.name"
            if (event.contains("\"forkee\":{\"name\":\"")) {
                String forkedRepo = extractValue(event, "\"forkee\":{\"name\":\"", "\"");
                activity.put("forked", forkedRepo);
            }

            // Handle "PullRequestReviewEvent" by extracting review details
            if (event.contains("\"review\":{\"state\":\"")) {
                String review = extractValue(event, "\"review\":{\"state\":\"", "\"");
                activity.put("review", review);
            }

            // Extract the creation timestamp
            if (event.contains("\"created_at\":\"")) {
                String createdAt = extractValue(event, "\"created_at\":\"", "\"");
                activity.put("created_at", createdAt);
            }

            // Add the parsed activity to the list
            activities.add(activity);
        }

        for (HashMap<String, String> activity : activities) {
            System.out.println("Event: " + activity.get("type") + ", Repo: " + activity.get("repo"));
            if (activity.containsKey("action")) {
                System.out.println("Action: " + activity.get("action"));
            }
            if (activity.containsKey("forked")) {
                System.out.println("Forked to: " + activity.get("forked"));
            }
            if (activity.containsKey("review")) {
                System.out.println("Pull Request Review: " + activity.get("review"));
            }
            System.out.println("Timestamp: " + activity.get("created_at"));
            System.out.println("----------");
        }

    }
    //Helper function to extract key value pairs
    public static String extractValue(String json, String key, String end) {
        int startIndex = json.indexOf(key) + key.length();
        int endIndex = json.indexOf(end, startIndex);
        return json.substring(startIndex, endIndex);
    }
}