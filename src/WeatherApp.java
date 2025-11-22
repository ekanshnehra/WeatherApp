import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class WeatherApp extends JFrame {
    private JTextField cityField;
    private JTextArea resultArea;
    private JCheckBox darkModeCheckbox;

    public WeatherApp() {
        setTitle("Weather App");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Weather App", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());
        JLabel cityLabel = new JLabel("Enter city:");
        cityField = new JTextField(20);
        cityField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String city = cityField.getText();
                getWeather(city);
            }
        });
        searchPanel.add(cityLabel);
        searchPanel.add(cityField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String city = cityField.getText();
                getWeather(city);
            }
        });
        searchPanel.add(searchButton);

        topPanel.add(searchPanel, BorderLayout.CENTER);

        darkModeCheckbox = new JCheckBox("Dark Mode");
        darkModeCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateUI();
            }
        });
        topPanel.add(darkModeCheckbox, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 16));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
    }

    private void getWeather(String city) {
        String apiKey = "e54f01496d14248f287fc22c3bb48ee1";
        String apiUrl = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());

            // Parse current weather
            double tempKelvin = jsonResponse.getJSONArray("list").getJSONObject(0).getJSONObject("main").getDouble("temp");
            double tempCelsius = Math.round((tempKelvin - 273.15) * 10) / 10.0;
            String weatherDescription = jsonResponse.getJSONArray("list").getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("description");
            double windSpeed = jsonResponse.getJSONArray("list").getJSONObject(0).getJSONObject("wind").getDouble("speed");
            int humidity = jsonResponse.getJSONArray("list").getJSONObject(0).getJSONObject("main").getInt("humidity");
            String cityName = jsonResponse.getJSONObject("city").getString("name");
            String country = jsonResponse.getJSONObject("city").getString("country");

            String currentWeatherString = "Current Weather in " + cityName + ", " + country + ":\nTemperature: " + tempCelsius + "°C\nDescription: " + weatherDescription + "\nWind Speed: " + windSpeed + " m/s\nHumidity: " + humidity + "%\n\n";

            // Parse forecasts for next five days
            JSONArray forecasts = jsonResponse.getJSONArray("list");
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String forecastsString = "Forecasts:\n";

            LocalDateTime nextDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).plusDays(1);
            int forecastsCount = 0;

            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                LocalDateTime forecastTime = LocalDateTime.ofEpochSecond(forecast.getLong("dt"), 0, ZoneOffset.UTC);

                if (forecastTime.toLocalDate().isAfter(today) && forecastTime.toLocalDate().isEqual(nextDay.toLocalDate())) {
                    double forecastTempKelvin = forecast.getJSONObject("main").getDouble("temp");
                    double forecastTempCelsius = Math.round((forecastTempKelvin - 273.15) * 10) / 10.0;
                    String forecastWeatherDescription = forecast.getJSONArray("weather").getJSONObject(0).getString("description");
                    forecastsString += forecastTime.format(DateTimeFormatter.ofPattern("EEE, MMM d")) + ": " + forecastTempCelsius + "°C, " + forecastWeatherDescription + "\n";
                    forecastsCount++;

                    if (forecastsCount >= 5) {
                        break;
                    }

                    nextDay = nextDay.plusDays(1);
                }
            }

            resultArea.setText(currentWeatherString + forecastsString);
        } catch (IOException ex) {
            ex.printStackTrace();
            resultArea.setText("Error: Unable to fetch weather data.");
        }
    }

    private void updateUI() {
        Color backgroundColor;
        Color textColor;
        if (darkModeCheckbox.isSelected()) {
            backgroundColor = Color.BLACK;
            textColor = Color.WHITE;
        } else {
            backgroundColor = Color.WHITE;
            textColor = Color.BLACK;
        }
        getContentPane().setBackground(backgroundColor);
        resultArea.setBackground(backgroundColor);
        resultArea.setForeground(textColor);
        cityField.setBackground(backgroundColor);
        cityField.setForeground(textColor);
        darkModeCheckbox.setBackground(backgroundColor);
        darkModeCheckbox.setForeground(textColor);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new WeatherApp().setVisible(true);
            }
        });
    }
}
