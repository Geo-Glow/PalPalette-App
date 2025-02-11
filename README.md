# PalPalette

PalPalette is an Android application that allows users to extract color palettes from images and share them with friends.

## Features

### Current Features

- 📸 Image Capture & Selection
  - Take photos directly within the app
  - Choose images from device gallery
- 🎨 Color Palette Generation
  - Automatic extraction of dominant colors
  - Drag and drop reordering of colors
- 👥 Social Features
  - Friend system for color sharing
  - Real-time color palette sharing
  - Message history of shared palettes
- ⚙️ Settings
  - Customizable availability times

### Planned Features

- [ ] Color palette history
- [ ] Favorite palettes
- [ ] Share palettes to other apps
- [ ] Custom color extraction settings

## Technology Stack

- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM
- **Networking**: Retrofit2
- **Image Processing**: Android Palette API
- **State Management**: Kotlin Flow
- **Data Storage**: DataStore
- **Dependency Injection**: Manual
- **Image Loading**: Coil
- **Real-time Communication**: MQTT

## Setup & Installation

1. Clone the repository:

```bash
git clone https://github.com/PalPalette/PalPalette-App.git
```

2. Open the project in Android Studio

3. Create a `local.properties` file in the root directory and add your API configuration:

```properties
sdk.dir=YOUR_ANDROID_SDK_PATH
BASE_URL="YOUR_API_BASE_URL"
```

4. Build and run the project

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/geoglow/
│   │   │   ├── data/           # Data models and repositories
│   │   │   ├── network/        # API and network related code
│   │   │   ├── ui/            # Compose UI components
│   │   │   ├── utils/         # Utility classes
│   │   │   └── viewmodel/     # ViewModels
│   │   └── res/               # Resources
│   └── test/                  # Unit tests
└── build.gradle.kts          # Project dependencies
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Architecture

The app follows the MVVM (Model-View-ViewModel) architecture pattern:

- **View**: Compose UI components in the `ui` package
- **ViewModel**: Business logic and state management in the `viewmodel` package
- **Model**: Data classes and repositories in the `data` package

## Dependencies

Major dependencies include:

- Jetpack Compose: UI framework
- Retrofit2: REST API client
- Coil: Image loading
- DataStore: Data persistence
- MQTT: Real-time messaging
- Palette: Color extraction

For a complete list of dependencies, see `app/build.gradle.kts`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [K4tara](https://github.com/K4tara) - Created the intial version of the app
- Android Palette API for color extraction
- Material Design 3 for UI components

## Contact

Project Link: [https://github.com/PalPalette/PalPalette-App](https://github.com/PalPalette/PalPalette-App)
