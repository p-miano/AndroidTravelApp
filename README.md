# Android Travel App

The Android Travel App is a basic application designed to help users view the weather in different cities using the Google Maps API and OpenWeatherMap API. The app displays a map, allows users to choose from a list of cities, and provides weather updates with an option to switch between Celsius and Fahrenheit.

## Features

- Displays the map for selected cities using Google Maps API.
- Fetches weather data from the OpenWeatherMap API.
- Notifies users about the weather changes.
- Allows switching between Celsius and Fahrenheit temperature units.

## Permissions

The app requests the following permissions:
- **Internet**: To fetch weather data.
- **Location Access**: To display the user's location on the map.
- **Notification**: To send weather updates to the user.

## Technologies

- **Google Maps API**: For showing map locations.
- **OpenWeatherMap API**: For fetching weather updates.
- **Picasso**: For loading weather icons.
- **Android Notification System**: For weather notifications.

## How to Run

1. Clone the repository.
2. Open the project in Android Studio.
3. Add your Google Maps API key and OpenWeatherMap API key in the `strings.xml` file.
4. Build and run the project on your emulator or connected device.

## Screenshots

Include screenshots of your app to give users a visual idea of its interface and functionality.

1. **City Selection Screen**
   - A screen where users can select a city from the dropdown.

2. **Weather Information**
   - Shows weather details like temperature and weather icon.

3. **Map View**
   - Displays the selected city's location on the map.

## How it Works

1. Users select a city from a spinner dropdown menu.
2. The app fetches the city's location and displays it on a Google Map.
3. Weather information is fetched from the OpenWeatherMap API and displayed in the app.
4. The app sends a notification with weather updates for the selected city.

## Files

- **MainActivity.java**: Contains the main logic for map rendering, weather fetching, and notification sending.
- **activity_main.xml**: Defines the layout for the app, including the map fragment and weather info section.
- **AndroidManifest.xml**: Manages app permissions and metadata.

## License

This project is open-source and available under the [MIT License](LICENSE).
