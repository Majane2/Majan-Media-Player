package com.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {

    private BorderPane root;
    private BorderPane mainLayout;
    private StackPane centerStack;

    private HBox topBar;
    private VBox sidebar;
    private VBox playlistPanel;

    private VBox playlistBox;
    private VBox customPlaylistBox;

    private VBox playerScreen;
    private VBox musicScreen;
    private VBox videoScreen;
    private VBox playlistsScreen;
    private VBox settingsScreen;

    private VBox musicListBox;
    private VBox videoListBox;
    private FlowPane playlistsGrid;
    private VBox playlistsListBox;
    private ScrollPane playlistsGridScroll;
    private ScrollPane playlistsListScroll;

    private Button navHome;
    private Button navMusic;
    private Button navVideo;
    private Button navPlaylists;
    private Button navSettings;

    private String currentScreen = "home";

    private Label titleLabel;
    private Label artistLabel;
    private Label albumLabel;

    private Label currentTimeLabel;
    private Label totalTimeLabel;

    private Slider progressSlider;
    private Slider volumeSlider;

    private Slider settingsVolumeSlider;
    private Slider settingsStartupVolumeSlider;

    private Button playButton;

    // Two separate views of the same cover: one small in the info bar,
    // one large filling the player area. Keeping them apart is important —
    // otherwise the info bar steals the reference and the big one stays blank.
    private ImageView albumArt;
    private ImageView bigArtwork;

    private MediaView mediaView;
    private ImageView artworkBackground;
    private Label mediaTypeLabel;
    private Label qualityLabel;
    private Label artworkText;

    private StackPane artworkContainer;
    private HBox progressArea;
    private HBox trackInfoBox;
    private HBox controlsBox;
    private VBox playerPanel;

    private Label playlistTitleLabel;
    private Label playlistCountLabel;

    private Button favoriteButton;

    private HBox selectedTrackRow;

    private MediaPlayer mediaPlayer;

    private Playlist currentPlaylist;
    private Track currentTrack;

    private int currentTrackIndex = 0;

    private final List<Playlist> playlists = new ArrayList<>();

    private boolean muted = false;

    private double previousVolume = 70;

    @Override
    public void start(Stage stage) {

        Playlist library = new Playlist("Library");
        Playlist favorites = new Playlist("Favorites");

        playlists.add(library);
        playlists.add(favorites);

        currentPlaylist = library;

        root = new BorderPane();
        root.setStyle("""
            -fx-background-color: #160610;
            -fx-font-family: "Segoe UI";
            """);

        mainLayout = new BorderPane();

        topBar = createTopBar();
        sidebar = createSidebar();

        mainLayout.setTop(topBar);
        mainLayout.setLeft(sidebar);

        centerStack = new StackPane();

        playerScreen = createPlayerScreen();
        musicScreen = createMusicScreen();
        videoScreen = createVideoScreen();
        playlistsScreen = createPlaylistsScreen();
        settingsScreen = createSettingsScreen();

        centerStack.getChildren().addAll(
                playerScreen,
                musicScreen,
                videoScreen,
                playlistsScreen,
                settingsScreen);

        mainLayout.setCenter(centerStack);

        root.setCenter(mainLayout);

        showScreen("home");

        Scene scene = new Scene(root, 1500, 900);

        // Using an event filter rather than a handler so shortcuts fire
        // before any focused control (button, slider, menu) can consume
        // the same key for its own action.
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {

            // Let the search field and other text inputs type normally.
            if (scene.getFocusOwner() instanceof TextField) {
                return;
            }

            switch (event.getCode()) {

                case SPACE -> {
                    event.consume();
                    togglePlay();
                }
                case S -> {
                    event.consume();
                    stopMusic();
                }
                case N -> {
                    event.consume();
                    nextTrack();
                }
                case P -> {
                    event.consume();
                    previousTrack();
                }
                case M -> {
                    event.consume();
                    toggleMute();
                }
                case UP -> {
                    event.consume();
                    volumeSlider.setValue(
                            Math.min(100, volumeSlider.getValue() + 5));
                }
                case DOWN -> {
                    event.consume();
                    volumeSlider.setValue(
                            Math.max(0, volumeSlider.getValue() - 5));
                }
                default -> { }
            }
        });

        stage.setTitle("MediaPlayer+");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();
    }

    private void showScreen(String name) {

        currentScreen = name;

        playerScreen.setVisible(name.equals("home"));
        playerScreen.setManaged(name.equals("home"));

        musicScreen.setVisible(name.equals("music"));
        musicScreen.setManaged(name.equals("music"));

        videoScreen.setVisible(name.equals("video"));
        videoScreen.setManaged(name.equals("video"));

        playlistsScreen.setVisible(name.equals("playlists"));
        playlistsScreen.setManaged(name.equals("playlists"));

        settingsScreen.setVisible(name.equals("settings"));
        settingsScreen.setManaged(name.equals("settings"));

        if (name.equals("music")) refreshMusicScreen();
        if (name.equals("video")) refreshVideoScreen();
        if (name.equals("playlists")) refreshPlaylistsScreen();

        highlightNav();
    }

    private void highlightNav() {

        styleNavButton(navHome, currentScreen.equals("home"));
        styleNavButton(navMusic, currentScreen.equals("music"));
        styleNavButton(navVideo, currentScreen.equals("video"));
        styleNavButton(navPlaylists, currentScreen.equals("playlists"));
        styleNavButton(navSettings, currentScreen.equals("settings"));
    }

    private void styleNavButton(Button button, boolean active) {

        if (button == null) return;

        if (active) {
            button.setStyle("""
                    -fx-background-color: linear-gradient(to right, #ff2d95, #c918b8);
                    -fx-background-radius: 9;
                    -fx-text-fill: white;
                    -fx-font-size: 16px;
                    -fx-alignment: center-left;
                    -fx-padding: 0 15;
                    -fx-cursor: hand;
                    """);
        } else {
            button.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: #f2c4dc;
                    -fx-font-size: 16px;
                    -fx-alignment: center-left;
                    -fx-padding: 0 15;
                    -fx-cursor: hand;
                    """);
        }
    }

    private HBox createTopBar() {

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(15, 25, 15, 25));
        top.setSpacing(25);
        top.setStyle("""
                -fx-background-color: #1c0815;
                -fx-border-color: #3d1030;
                -fx-border-width: 0 0 1 0;
                """);

        // Load the logo from assets and clip it into the same
        // rounded square the old rectangle had.
        ImageView logoImage = new ImageView(
                new Image(getClass().getResource("/assets/logo.png")
                        .toExternalForm()));
        logoImage.setFitWidth(48);
        logoImage.setFitHeight(48);
        logoImage.setPreserveRatio(true);

        Rectangle logoClip = new Rectangle(48, 48);
        logoClip.setArcWidth(18);
        logoClip.setArcHeight(18);
        logoImage.setClip(logoClip);

        StackPane logoIcon = new StackPane(logoImage);

        VBox brand = new VBox(0);

        Label name = new Label("Mothorolla");
        name.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 27px;
                -fx-font-weight: bold;
                """);

        Label subtitle = new Label("Your Music  •  Your Playlists  •  Your Way");
        subtitle.setStyle("""
                -fx-text-fill: #e5a4c9;
                -fx-font-size: 13px;
                """);

        brand.getChildren().addAll(name, subtitle);

        HBox logo = new HBox(12, logoIcon, brand);
        logo.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("Search in playlist...");
        search.setPrefWidth(380);
        search.setPrefHeight(42);
        search.setStyle("""
                -fx-background-color: #2b0e22;
                -fx-background-radius: 12;
                -fx-border-color: #5e1a45;
                -fx-border-radius: 12;
                -fx-text-fill: white;
                -fx-prompt-text-fill: #d18cb3;
                -fx-padding: 0 15;
                """);

        HBox.setHgrow(search, Priority.ALWAYS);

        Label date = new Label("Media Player");
        date.setStyle("-fx-text-fill: #f0b3d2; -fx-font-size: 13px;");

        Label time = new Label("Ready");
        time.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                """);

        Button maximize = createTopButton("□");
        Button close = createTopButton("×");

        maximize.setOnAction(e -> {
            Stage s = (Stage) maximize.getScene().getWindow();
            s.setMaximized(!s.isMaximized());
        });

        close.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
            Stage s = (Stage) close.getScene().getWindow();
            s.close();
        });

        HBox right = new HBox(18, date, time, maximize, close);
        right.setAlignment(Pos.CENTER_RIGHT);

        top.getChildren().addAll(logo, search, right);

        return top;
    }

    private Button createTopButton(String text) {
        Button button = new Button(text);
        // Without this, the button steals SPACE/ENTER and the
        // global shortcuts stop working after any click.
        button.setFocusTraversable(false);
        button.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #f2c4dc;
                -fx-font-size: 18px;
                -fx-cursor: hand;
                """);
        return button;
    }

    private VBox createSidebar() {

        VBox sb = new VBox(10);
        sb.setPrefWidth(225);
        sb.setPadding(new Insets(20, 12, 20, 12));
        sb.setStyle("""
            -fx-background-color: #1c0814;
            -fx-border-color: #3b0f2d;
            -fx-border-width: 0 1 0 0;
            """);

        navHome = navigationButton("⌂", "Home");
        navMusic = navigationButton("♫", "Music");
        navVideo = navigationButton("▣", "Video");
        navPlaylists = navigationButton("☷", "Playlists");
        navSettings = navigationButton("⚙", "Settings");

        navHome.setOnAction(e -> showScreen("home"));
        navMusic.setOnAction(e -> showScreen("music"));
        navVideo.setOnAction(e -> showScreen("video"));
        navPlaylists.setOnAction(e -> showScreen("playlists"));
        navSettings.setOnAction(e -> showScreen("settings"));

        sb.getChildren().addAll(
                navHome, navMusic, navVideo, navPlaylists, navSettings);

        Region sepSpace = new Region();
        sepSpace.setPrefHeight(12);
        sb.getChildren().add(sepSpace);

        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setMinHeight(1);
        sep.setMaxHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);
        sep.setStyle("-fx-background-color: #5e1a45;");
        sb.getChildren().add(sep);

        Label myPlaylists = new Label("MY PLAYLISTS");
        myPlaylists.setStyle("""
            -fx-text-fill: #d18cb3;
            -fx-font-size: 11px;
            -fx-font-weight: bold;
            -fx-padding: 10 5 4 5;
            """);

        HBox playlistHeader = new HBox();
        playlistHeader.setAlignment(Pos.CENTER_LEFT);
        playlistHeader.getChildren().add(myPlaylists);

        Region playlistPush = new Region();
        HBox.setHgrow(playlistPush, Priority.ALWAYS);

        Button addPlaylist = new Button("+");
        addPlaylist.setFocusTraversable(false);
        addPlaylist.setStyle("""
            -fx-background-color: #2b0e22;
            -fx-text-fill: white;
            -fx-font-size: 20px;
            -fx-background-radius: 8;
            -fx-border-color: #5e1a45;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            """);

        addPlaylist.setOnAction(e -> createNewPlaylist());

        playlistHeader.getChildren().addAll(playlistPush, addPlaylist);
        sb.getChildren().add(playlistHeader);

        customPlaylistBox = new VBox(5);
        sb.getChildren().add(customPlaylistBox);

        refreshPlaylists();

        Region bottomSpace = new Region();
        VBox.setVgrow(bottomSpace, Priority.ALWAYS);

        Label musicNote = new Label("♫");
        musicNote.setStyle("-fx-text-fill: #ff3d9a; -fx-font-size: 75px;");

        Label quote = new Label("Good Music\nBrighter Days");
        quote.setStyle("""
            -fx-text-fill: #e5a4c9;
            -fx-font-size: 18px;
            -fx-alignment: center;
            """);

        VBox bottom = new VBox(2, musicNote, quote);
        bottom.setAlignment(Pos.CENTER);

        sb.getChildren().addAll(bottomSpace, bottom);

        return sb;
    }

    private Button navigationButton(String icon, String text) {

        Button button = new Button(icon + "    " + text);
        button.setFocusTraversable(false);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(50);
        button.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #f2c4dc;
                -fx-font-size: 16px;
                -fx-alignment: center-left;
                -fx-padding: 0 15;
                -fx-cursor: hand;
                """);
        return button;
    }

    private VBox createPlayerScreen() {

        VBox main = new VBox(12);
        main.setPadding(new Insets(18));

        HBox content = new HBox(12);

        VBox player = createPlayerPanel();
        playlistPanel = createPlaylistPanel();

        HBox.setHgrow(player, Priority.ALWAYS);

        content.getChildren().addAll(player, playlistPanel);

        main.getChildren().add(content);
        VBox.setVgrow(content, Priority.ALWAYS);

        return main;
    }

    private VBox createMusicScreen() {

        VBox screen = new VBox(15);
        screen.setPadding(new Insets(25));
        screen.setStyle("-fx-background-color: #160610;");

        Label heading = new Label("♫   Music");
        heading.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 28px;
                -fx-font-weight: bold;
                """);

        Label sub = new Label("All audio tracks in the current playlist");
        sub.setStyle("-fx-text-fill: #d18cb3; -fx-font-size: 13px;");

        VBox titleBox = new VBox(4, heading, sub);

        musicListBox = new VBox(8);

        ScrollPane scroll = new ScrollPane(musicListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("""
                -fx-background: transparent;
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                """);

        VBox.setVgrow(scroll, Priority.ALWAYS);

        screen.getChildren().addAll(titleBox, scroll);
        return screen;
    }

    private void refreshMusicScreen() {

        if (musicListBox == null) return;

        musicListBox.getChildren().clear();

        if (currentPlaylist == null || currentPlaylist.size() == 0) {
            musicListBox.getChildren().add(emptyHint("No audio in this playlist."));
            return;
        }

        int count = 0;

        for (Track t : currentPlaylist.getTracks()) {
            if (t.isVideo()) continue;
            count++;
            musicListBox.getChildren().add(createMediaTile(t, "audio"));
        }

        if (count == 0) {
            musicListBox.getChildren().add(emptyHint("No audio in this playlist."));
        }
    }

    private VBox createVideoScreen() {

        VBox screen = new VBox(15);
        screen.setPadding(new Insets(25));
        screen.setStyle("-fx-background-color: #160610;");

        Label heading = new Label("▣   Video");
        heading.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 28px;
                -fx-font-weight: bold;
                """);

        Label sub = new Label("All video tracks in the current playlist");
        sub.setStyle("-fx-text-fill: #d18cb3; -fx-font-size: 13px;");

        VBox titleBox = new VBox(4, heading, sub);

        videoListBox = new VBox(8);

        ScrollPane scroll = new ScrollPane(videoListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("""
                -fx-background: transparent;
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                """);

        VBox.setVgrow(scroll, Priority.ALWAYS);

        screen.getChildren().addAll(titleBox, scroll);
        return screen;
    }

    private void refreshVideoScreen() {

        if (videoListBox == null) return;

        videoListBox.getChildren().clear();

        if (currentPlaylist == null || currentPlaylist.size() == 0) {
            videoListBox.getChildren().add(emptyHint("No video in this playlist."));
            return;
        }

        int count = 0;

        for (Track t : currentPlaylist.getTracks()) {
            if (!t.isVideo()) continue;
            count++;
            videoListBox.getChildren().add(createMediaTile(t, "video"));
        }

        if (count == 0) {
            videoListBox.getChildren().add(emptyHint("No video in this playlist."));
        }
    }

    private HBox emptyHint(String message) {

        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #c47ea4; -fx-font-size: 14px;");

        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        return box;
    }

    private HBox createMediaTile(Track track, String kind) {

        HBox row = new HBox(15);
        row.setPadding(new Insets(12));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("""
                -fx-background-color: #1a0713;
                -fx-background-radius: 12;
                -fx-border-color: #4a1235;
                -fx-border-radius: 12;
                -fx-cursor: hand;
                """);

        StackPane thumb = new StackPane();
        thumb.setPrefSize(70, 70);
        thumb.setMinSize(70, 70);
        thumb.setMaxSize(70, 70);
        thumb.setStyle("""
                -fx-background-color: #2a0c1d;
                -fx-background-radius: 12;
                """);

        Image cover = track.getCoverImage();
        if (cover != null) {
            ImageView iv = new ImageView(cover);
            iv.setFitWidth(70);
            iv.setFitHeight(70);
            iv.setPreserveRatio(false);

            // ImageView has no CSS radius — need an explicit clip.
            Rectangle ivClip = new Rectangle(70, 70);
            ivClip.setArcWidth(24);
            ivClip.setArcHeight(24);
            iv.setClip(ivClip);

            thumb.getChildren().add(iv);
        } else {
            Label icon = new Label(kind.equals("video") ? "▶" : "♫");
            icon.setStyle("-fx-text-fill: #ff2d95; -fx-font-size: 30px;");
            thumb.getChildren().add(icon);
        }

        VBox info = new VBox(3);

        Label title = new Label(track.getTitle());
        title.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                """);

        Label artist = new Label(track.getArtist());
        artist.setStyle("-fx-text-fill: #d18cb3; -fx-font-size: 12px;");

        info.getChildren().addAll(title, artist);

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        Label duration = new Label(track.getDuration());
        duration.setStyle("-fx-text-fill: #e5a4c9; -fx-font-size: 13px;");

        Button playBtn = new Button("▶ Play");
        playBtn.setFocusTraversable(false);
        playBtn.setStyle("""
                -fx-background-color: linear-gradient(to right, #ff2d95, #c918b8);
                -fx-background-radius: 8;
                -fx-text-fill: white;
                -fx-font-size: 12px;
                -fx-padding: 8 14;
                -fx-cursor: hand;
                """);
        playBtn.setOnAction(e -> playTrack(track));

        row.getChildren().addAll(thumb, info, push, duration, playBtn);
        row.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) playTrack(track);
        });

        return row;
    }

    private VBox createPlaylistsScreen() {

        VBox screen = new VBox(15);
        screen.setPadding(new Insets(25));
        screen.setStyle("-fx-background-color: #160610;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("☷   Playlists");
        heading.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 28px;
                -fx-font-weight: bold;
                """);

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        Button gridBtn = new Button("▦  Grid");
        Button listBtn = new Button("☰  List");

        gridBtn.setFocusTraversable(false);
        listBtn.setFocusTraversable(false);

        String activeStyle = """
                -fx-background-color: linear-gradient(to right, #ff2d95, #c918b8);
                -fx-background-radius: 8;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-padding: 8 16;
                -fx-cursor: hand;
                """;

        String inactiveStyle = """
                -fx-background-color: #2b0e22;
                -fx-background-radius: 8;
                -fx-text-fill: #f2c4dc;
                -fx-font-size: 13px;
                -fx-padding: 8 16;
                -fx-cursor: hand;
                """;

        gridBtn.setStyle(activeStyle);
        listBtn.setStyle(inactiveStyle);

        playlistsGrid = new FlowPane(15, 15);
        playlistsGrid.setPadding(new Insets(10));
        playlistsGrid.setStyle("-fx-background-color: transparent;");

        playlistsGridScroll = new ScrollPane(playlistsGrid);
        playlistsGridScroll.setFitToWidth(true);
        playlistsGridScroll.setStyle("""
                -fx-background: transparent;
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                """);

        playlistsListBox = new VBox(8);

        playlistsListScroll = new ScrollPane(playlistsListBox);
        playlistsListScroll.setFitToWidth(true);
        playlistsListScroll.setStyle("""
                -fx-background: transparent;
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                """);

        playlistsListScroll.setVisible(false);
        playlistsListScroll.setManaged(false);

        gridBtn.setOnAction(e -> {
            playlistsGridScroll.setVisible(true);
            playlistsGridScroll.setManaged(true);
            playlistsListScroll.setVisible(false);
            playlistsListScroll.setManaged(false);
            gridBtn.setStyle(activeStyle);
            listBtn.setStyle(inactiveStyle);
            refreshPlaylistsScreen();
        });

        listBtn.setOnAction(e -> {
            playlistsGridScroll.setVisible(false);
            playlistsGridScroll.setManaged(false);
            playlistsListScroll.setVisible(true);
            playlistsListScroll.setManaged(true);
            gridBtn.setStyle(inactiveStyle);
            listBtn.setStyle(activeStyle);
            refreshPlaylistsScreen();
        });

        header.getChildren().addAll(heading, push, gridBtn, listBtn);

        VBox.setVgrow(playlistsGridScroll, Priority.ALWAYS);
        VBox.setVgrow(playlistsListScroll, Priority.ALWAYS);

        screen.getChildren().addAll(
                header, playlistsGridScroll, playlistsListScroll);

        refreshPlaylistsScreen();

        return screen;
    }

    private void refreshPlaylistsScreen() {

        if (playlistsGrid == null) return;

        playlistsGrid.getChildren().clear();
        for (Playlist playlist : playlists) {
            playlistsGrid.getChildren().add(createPlaylistCard(playlist));
        }

        if (playlistsListBox == null) return;

        playlistsListBox.getChildren().clear();
        for (Playlist playlist : playlists) {
            playlistsListBox.getChildren().add(createPlaylistRow(playlist));
        }
    }

    private VBox createPlaylistCard(Playlist playlist) {

        VBox card = new VBox(10);
        card.setPrefSize(220, 200);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18));
        card.setStyle("""
                -fx-background-color: #1a0713;
                -fx-background-radius: 15;
                -fx-border-color: #4a1235;
                -fx-border-radius: 15;
                -fx-cursor: hand;
                """);

        Label icon = new Label("♫");
        icon.setStyle("-fx-text-fill: #ff2d95; -fx-font-size: 48px;");

        Label name = new Label(playlist.getName());
        name.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 17px;
                -fx-font-weight: bold;
                """);

        Label count = new Label(playlist.size() + " tracks");
        count.setStyle("-fx-text-fill: #d18cb3; -fx-font-size: 12px;");

        card.getChildren().addAll(icon, name, count);

        card.setOnMouseClicked(e -> {
            openPlaylist(playlist);
            showScreen("home");
        });

        return card;
    }

    private HBox createPlaylistRow(Playlist playlist) {

        HBox row = new HBox(15);
        row.setPadding(new Insets(14));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("""
                -fx-background-color: #1a0713;
                -fx-background-radius: 12;
                -fx-border-color: #4a1235;
                -fx-border-radius: 12;
                -fx-cursor: hand;
                """);

        Label icon = new Label("♫");
        icon.setStyle("-fx-text-fill: #ff2d95; -fx-font-size: 26px;");

        VBox info = new VBox(3);

        Label name = new Label(playlist.getName());
        name.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                """);

        Label count = new Label(playlist.size() + " tracks");
        count.setStyle("-fx-text-fill: #d18cb3; -fx-font-size: 12px;");

        info.getChildren().addAll(name, count);

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        Button open = new Button("Open");
        open.setFocusTraversable(false);
        open.setStyle("""
                -fx-background-color: linear-gradient(to right, #ff2d95, #c918b8);
                -fx-background-radius: 8;
                -fx-text-fill: white;
                -fx-font-size: 12px;
                -fx-padding: 8 16;
                -fx-cursor: hand;
                """);

        open.setOnAction(e -> {
            openPlaylist(playlist);
            showScreen("home");
        });

        row.getChildren().addAll(icon, info, push, open);

        row.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openPlaylist(playlist);
                showScreen("home");
            }
        });

        return row;
    }

    private void openPlaylist(Playlist playlist) {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        clearMediaDisplay();

        currentPlaylist = playlist;
        currentTrack = null;
        currentTrackIndex = 0;

        if (playButton != null) playButton.setText("▶");

        refreshTrackList();
        updatePlaylistHeader();
        updateFavoriteButton();
        refreshPlaylists();
    }

    private VBox createSettingsScreen() {

        VBox screen = new VBox(20);
        screen.setPadding(new Insets(30));
        screen.setStyle("-fx-background-color: #160610;");

        Label heading = new Label("⚙   Settings");
        heading.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 28px;
                -fx-font-weight: bold;
                """);

        VBox volumeCard = new VBox(10);
        volumeCard.setPadding(new Insets(20));
        volumeCard.setStyle("""
                -fx-background-color: #1a0713;
                -fx-background-radius: 15;
                -fx-border-color: #4a1235;
                -fx-border-radius: 15;
                """);

        Label volTitle = new Label("Current Volume");
        volTitle.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                """);

        settingsVolumeSlider = new Slider(0, 100, 70);
        settingsVolumeSlider.setFocusTraversable(false);
        settingsVolumeSlider.setStyle("-fx-accent: #ff2d95;");
        settingsVolumeSlider.setMaxWidth(Double.MAX_VALUE);

        Label volValue = new Label("70%");
        volValue.setStyle("-fx-text-fill: #e5a4c9; -fx-font-size: 13px;");

        settingsVolumeSlider.valueProperty().addListener((o, oldV, newV) ->
                volValue.setText(String.format("%.0f%%", newV.doubleValue())));

        volumeCard.getChildren().addAll(volTitle, settingsVolumeSlider, volValue);

        VBox startupCard = new VBox(10);
        startupCard.setPadding(new Insets(20));
        startupCard.setStyle("""
                -fx-background-color: #1a0713;
                -fx-background-radius: 15;
                -fx-border-color: #4a1235;
                -fx-border-radius: 15;
                """);

        Label startupTitle = new Label("Startup Volume");
        startupTitle.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                """);

        settingsStartupVolumeSlider = new Slider(0, 100, 70);
        settingsStartupVolumeSlider.setFocusTraversable(false);
        settingsStartupVolumeSlider.setStyle("-fx-accent: #ff2d95;");
        settingsStartupVolumeSlider.setMaxWidth(Double.MAX_VALUE);

        Label startupValue = new Label("70%");
        startupValue.setStyle("-fx-text-fill: #e5a4c9; -fx-font-size: 13px;");

        settingsStartupVolumeSlider.valueProperty().addListener((o, oldV, newV) ->
                startupValue.setText(String.format("%.0f%%", newV.doubleValue())));

        startupCard.getChildren().addAll(
                startupTitle, settingsStartupVolumeSlider, startupValue);

        VBox aboutCard = new VBox(6);
        aboutCard.setPadding(new Insets(20));
        aboutCard.setStyle("""
                -fx-background-color: #1a0713;
                -fx-background-radius: 15;
                -fx-border-color: #4a1235;
                -fx-border-radius: 15;
                """);

        Label aboutTitle = new Label("About");
        aboutTitle.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                """);

        Label aboutText = new Label("""
                Mothorolla
                Version 1.0.0
                Built with JavaFX""");
        aboutText.setStyle("-fx-text-fill: #d18cb3; -fx-font-size: 13px;");

        aboutCard.getChildren().addAll(aboutTitle, aboutText);

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button apply = new Button("Apply");
        apply.setFocusTraversable(false);
        apply.setStyle("""
                -fx-background-color: linear-gradient(to right, #ff2d95, #c918b8);
                -fx-background-radius: 9;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-padding: 12 26;
                -fx-cursor: hand;
                """);

        apply.setOnAction(e -> {
            double newVol = settingsVolumeSlider.getValue();
            volumeSlider.setValue(newVol);
            previousVolume = newVol;
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVol / 100);
            }
            showMessage("Settings applied.");
        });

        Button reset = new Button("Reset Library");
        reset.setFocusTraversable(false);
        reset.setStyle("""
                -fx-background-color: #c9185c;
                -fx-background-radius: 9;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-padding: 12 26;
                -fx-cursor: hand;
                """);

        reset.setOnAction(e -> resetLibrary());

        buttons.getChildren().addAll(apply, reset);

        screen.getChildren().addAll(
                heading, volumeCard, startupCard, aboutCard, buttons);

        return screen;
    }

    private void resetLibrary() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Library");
        confirm.setHeaderText("Reset the entire library?");
        confirm.setContentText("This will clear all playlists and tracks.");

        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }

            clearMediaDisplay();

            playlists.clear();
            playlists.add(new Playlist("Library"));
            playlists.add(new Playlist("Favorites"));

            currentPlaylist = playlists.get(0);
            currentTrack = null;
            currentTrackIndex = 0;

            if (playButton != null) playButton.setText("▶");

            refreshPlaylists();
            refreshTrackList();
            updatePlaylistHeader();
            updateFavoriteButton();
            refreshPlaylistsScreen();

            showMessage("Library has been reset.");
        });
    }

    private VBox createPlayerPanel() {

        playerPanel = new VBox(12);
        playerPanel.setPadding(new Insets(14));
        playerPanel.setStyle("""
                -fx-background-color: #1a0713;
                -fx-background-radius: 15;
                -fx-border-color: #4a1235;
                -fx-border-radius: 15;
                """);

        artworkContainer = new StackPane();
        artworkContainer.setPrefHeight(410);
        artworkContainer.setStyle("""
                -fx-background-color: #200818;
                -fx-background-radius: 12;
                """);

        Image background = new Image(
                getClass().getResource("/assets/music_background.png")
                        .toExternalForm());

        artworkBackground = new ImageView(background);
        artworkBackground.setPreserveRatio(false);
        artworkBackground.setFitWidth(700);
        artworkBackground.setFitHeight(310);

        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);
        mediaView.setVisible(false);
        mediaView.setManaged(false);
        mediaView.fitWidthProperty().bind(artworkContainer.widthProperty());
        mediaView.fitHeightProperty().bind(artworkContainer.heightProperty());

        bigArtwork = new ImageView();
        bigArtwork.setPreserveRatio(true);
        bigArtwork.setVisible(false);
        bigArtwork.setManaged(false);
        bigArtwork.fitWidthProperty().bind(artworkContainer.widthProperty());
        bigArtwork.fitHeightProperty().bind(artworkContainer.heightProperty());
        bigArtwork.setOpacity(0.95);

        artworkContainer.getChildren().addAll(
                artworkBackground, bigArtwork, mediaView);

        mediaTypeLabel = new Label("♫  Music");
        mediaTypeLabel.setStyle("""
                -fx-background-color: #2a0a1edd;
                -fx-background-radius: 8;
                -fx-text-fill: white;
                -fx-padding: 10 15;
                -fx-font-size: 14px;
                """);
        StackPane.setAlignment(mediaTypeLabel, Pos.TOP_LEFT);
        StackPane.setMargin(mediaTypeLabel, new Insets(15));
        artworkContainer.getChildren().add(mediaTypeLabel);

        qualityLabel = new Label("MP3");
        qualityLabel.setStyle("""
                -fx-background-color: #2a0a1edd;
                -fx-background-radius: 7;
                -fx-text-fill: white;
                -fx-padding: 9 12;
                -fx-font-size: 13px;
                """);
        StackPane.setAlignment(qualityLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(qualityLabel, new Insets(15));
        artworkContainer.getChildren().add(qualityLabel);

        artworkText = new Label("M U S I C\n\nL I F E\n\nB E T T E R");
        artworkText.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 28px;
                -fx-font-weight: 300;
                -fx-alignment: center;
                """);
        artworkText.setMouseTransparent(true);
        artworkContainer.getChildren().add(artworkText);

        progressArea = new HBox(10);

        currentTimeLabel = new Label("00:00");
        currentTimeLabel.setTextFill(Color.WHITE);

        totalTimeLabel = new Label("00:00");
        totalTimeLabel.setTextFill(Color.WHITE);

        progressSlider = new Slider(0, 100, 0);
        progressSlider.setFocusTraversable(false);
//         progressSlider.setMouseTransparent(true);
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressSlider, Priority.ALWAYS);
        progressSlider.setStyle("-fx-accent: #ff2d95;");
        progressSlider.setOnMouseReleased(event -> seekMusic());

        progressArea.getChildren().addAll(
                currentTimeLabel, progressSlider, totalTimeLabel);

        trackInfoBox = createTrackInfo();
        controlsBox = createPlaybackControls();

        playerPanel.getChildren().addAll(
                artworkContainer,
                progressArea,
                trackInfoBox,
                controlsBox);

        VBox.setVgrow(artworkContainer, Priority.ALWAYS);

        return playerPanel;
    }

    private HBox createTrackInfo() {

        HBox info = new HBox(15);
        info.setPadding(new Insets(12));
        info.setStyle("""
                -fx-background-color: #240a1a;
                -fx-background-radius: 12;
                -fx-border-color: #4a1235;
                -fx-border-radius: 12;
                """);

        albumArt = new ImageView();
        albumArt.setImage(new Image(
                getClass().getResource("/assets/music_background.png")
                        .toExternalForm()));
        albumArt.setFitWidth(110);
        albumArt.setFitHeight(90);
        albumArt.setPreserveRatio(false);

        VBox text = new VBox(5);

        titleLabel = new Label("No Track Selected");
        titleLabel.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                """);

        artistLabel = new Label("Add music to begin");
        artistLabel.setStyle("-fx-text-fill: #e5a4c9; -fx-font-size: 14px;");

        albumLabel = new Label("♫  MP3     ◷  00:00     ▱  ---");
        albumLabel.setStyle("-fx-text-fill: #e5a4c9; -fx-font-size: 13px;");

        text.getChildren().addAll(titleLabel, artistLabel, albumLabel);

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        favoriteButton = new Button("♡");
        Button more = new Button("⋮");

        favoriteButton.setFocusTraversable(false);
        more.setFocusTraversable(false);

        styleSmallButton(favoriteButton);
        styleSmallButton(more);

        favoriteButton.setOnAction(e -> toggleFavorite());

        info.getChildren().addAll(
                albumArt, text, push, favoriteButton, more);

        return info;
    }

    private HBox createPlaybackControls() {

        HBox controls = new HBox(18);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(15));
        controls.setStyle("""
                -fx-background-color: #240a1a;
                -fx-background-radius: 12;
                -fx-border-color: #4a1235;
                -fx-border-radius: 12;
                """);

        Button shuffle = controlButton("🔀");

        Button previous = controlButton("◀");
        previous.setOnAction(e -> previousTrack());

        playButton = controlButton("▶");
        playButton.setPrefSize(78, 78);
        playButton.setStyle("""
                -fx-background-color: linear-gradient(to bottom right, #ff2d95, #c918b8);
                -fx-background-radius: 50;
                -fx-text-fill: white;
                -fx-font-size: 27px;
                -fx-border-color: #ff5eae;
                -fx-border-width: 3;
                -fx-border-radius: 50;
                -fx-cursor: hand;
                """);
        playButton.setOnAction(e -> togglePlay());

        Button next = controlButton("▶");
        next.setOnAction(e -> nextTrack());

        Button stop = controlButton("■");
        stop.setOnAction(e -> stopMusic());

        Label speaker = new Label("🔊");
        speaker.setStyle("-fx-text-fill: #f2c4dc; -fx-font-size: 20px;");

        volumeSlider = new Slider(0, 100, 70);
        volumeSlider.setFocusTraversable(false);
        volumeSlider.setPrefWidth(140);

        // Keep the settings slider in sync when the user changes
        // volume from the player controls.
        volumeSlider.valueProperty().addListener((obs, o, n) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(n.doubleValue() / 100);
            }
            if (settingsVolumeSlider != null) {
                settingsVolumeSlider.setValue(n.doubleValue());
            }
        });

        Label volume = new Label("70%");
        volume.setTextFill(Color.web("#e5a4c9"));

        volumeSlider.valueProperty().addListener((obs, o, n) ->
                volume.setText(String.format("%.0f%%", n.doubleValue())));

        controls.getChildren().addAll(
                shuffle, previous, playButton, next, stop,
                speaker, volumeSlider, volume);

        return controls;
    }

    private Button controlButton(String text) {
        Button button = new Button(text);
        button.setFocusTraversable(false);
        button.setPrefSize(62, 62);
        button.setStyle("""
                -fx-background-color: #2a0c1d;
                -fx-background-radius: 50;
                -fx-border-color: #5e1a45;
                -fx-border-radius: 50;
                -fx-text-fill: white;
                -fx-font-size: 20px;
                -fx-cursor: hand;
                """);
        return button;
    }

    private void styleSmallButton(Button button) {
        button.setFocusTraversable(false);
        button.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #f2c4dc;
                -fx-font-size: 25px;
                -fx-cursor: hand;
                """);
    }

    private VBox createPlaylistPanel() {

        VBox panel = new VBox(12);
        panel.setPrefWidth(390);
        panel.setPadding(new Insets(15));
        panel.setStyle("""
                -fx-background-color: #1a0713;
                -fx-background-radius: 15;
                -fx-border-color: #4a1235;
                -fx-border-radius: 15;
                """);

        HBox header = new HBox();

        playlistTitleLabel = new Label("♫  " + currentPlaylist.getName());
        playlistTitleLabel.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 22px;
                -fx-font-weight: bold;
                """);

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        playlistCountLabel = new Label(currentPlaylist.size() + " items");
        playlistCountLabel.setStyle("""
                -fx-background-color: #2b0e22;
                -fx-background-radius: 8;
                -fx-text-fill: #f2c4dc;
                -fx-padding: 8 12;
                """);

        header.getChildren().addAll(
                playlistTitleLabel, push, playlistCountLabel);

        HBox fileButtons = new HBox(10);

        Button addFiles = new Button("+  Add Files");
        Button addFolder = new Button("▣  Add Folder");
        Button remove = new Button("▣  Remove");

        addFiles.setFocusTraversable(false);
        addFolder.setFocusTraversable(false);
        remove.setFocusTraversable(false);

        styleActionButton(addFiles, "#ff2d95");
        styleActionButton(addFolder, "#2b0e22");
        styleActionButton(remove, "#c9185c");

        addFiles.setOnAction(e ->addFiles());
        addFolder.setOnAction(e -> addFolder());
        remove.setOnAction(e -> removeSelectedTrack());

        fileButtons.getChildren().addAll(addFiles, addFolder, remove);

        playlistBox = new VBox(2);

        ScrollPane scroll = new ScrollPane(playlistBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("""
                -fx-background: transparent;
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                """);

        VBox.setVgrow(scroll, Priority.ALWAYS);

        panel.getChildren().addAll(header, fileButtons, scroll);

        refreshTrackList();

        return panel;
    }

    private void styleActionButton(Button button, String color) {
        button.setFocusTraversable(false);
        button.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 8;
            -fx-text-fill: white;
            -fx-font-size: 9px;
            -fx-padding: 9 12;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            """.formatted(color));
    }

    private void refreshTrackList() {

        if (playlistBox == null) return;

        playlistBox.getChildren().clear();
        updatePlaylistHeader();

        if (currentPlaylist == null || currentPlaylist.size() == 0) {

            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);

            Label icon = new Label("♫");
            icon.setStyle("-fx-text-fill: #ff2d95; -fx-font-size: 50px;");

            Label text = new Label("Your playlist is empty");
            text.setStyle("-fx-text-fill: #e5a4c9; -fx-font-size: 15px;");

            Label hint = new Label("Click Add Files to add music");
            hint.setStyle("-fx-text-fill: #c47ea4; -fx-font-size: 12px;");

            empty.getChildren().addAll(icon, text, hint);
            playlistBox.getChildren().add(empty);
            return;
        }

        int number = 1;

        for (Track track : currentPlaylist.getTracks()) {
            playlistBox.getChildren().add(createTrackRow(track, number));
            number++;
        }
    }

    private HBox createTrackRow(Track track, int number) {

        HBox row = new HBox(10);
        row.setPadding(new Insets(8));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("""
                -fx-background-color: transparent;
                -fx-border-color: #4a1235;
                -fx-border-width: 0 0 1 0;
                -fx-cursor: hand;
                """);

        Label numberLabel = new Label(String.valueOf(number));
        numberLabel.setPrefWidth(20);
        numberLabel.setStyle("-fx-text-fill: #e5a4c9; -fx-font-size: 14px;");

        ImageView image = new ImageView();
        image.setFitWidth(58);
        image.setFitHeight(58);
        image.setPreserveRatio(false);

        Image cover = track.getCoverImage();
        if (cover != null) {
            image.setImage(cover);
        } else {
            image.setImage(new Image(
                    getClass().getResource("/assets/music_background.png")
                            .toExternalForm()));
        }

        VBox info = new VBox(3);

        Label title = new Label(track.getTitle());
        title.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                """);

        Label artist = new Label(track.getArtist());
        artist.setStyle("-fx-text-fill: #d18cb3; -fx-font-size: 12px;");

        info.getChildren().addAll(title, artist);

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        Label duration = new Label(track.getDuration());
        duration.setStyle("-fx-text-fill: #e5a4c9; -fx-font-size: 12px;");

        MenuItem playItem = new MenuItem("▶  Play");
        MenuItem favoriteItem = new MenuItem(
                isFavorite(track)
                        ? "♥  Remove from Favorites"
                        : "♡  Add to Favorites");
        MenuItem removeItem = new MenuItem("✖  Remove from playlist");

        MenuButton more = new MenuButton("⋮");
        more.setFocusTraversable(false);
        more.getItems().addAll(playItem, favoriteItem, removeItem);
        more.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #f2c4dc;
                -fx-font-size: 20px;
                -fx-cursor: hand;
                -fx-padding: 0 6;
                -fx-mark-color: transparent;
                """);
        more.getStyleClass().add("track-menu-button");

        playItem.setOnAction(e -> playTrack(track));

        favoriteItem.setOnAction(e -> {
            Playlist favorites = getFavoritesPlaylist();
            if (favorites.getTracks().contains(track)) {
                favorites.getTracks().remove(track);
            } else {
                favorites.addTrack(track);
            }
            refreshPlaylists();
            refreshTrackList();
            updateFavoriteButton();
        });

        removeItem.setOnAction(e -> removeTrackFromList(track));

        row.getChildren().addAll(
                numberLabel, image, info, push, duration, more);

        row.setOnMouseClicked(e -> {
            selectTrackRow(row);
            currentTrack = track;
            currentTrackIndex = currentPlaylist.getTracks().indexOf(track);
            updateFavoriteButton();
            if (e.getClickCount() == 2) playTrack(track);
        });

        if (currentTrack != null && currentTrack.equals(track)) {
            applySelectedStyle(row);
            selectedTrackRow = row;
        }

        return row;
    }

    // Shared by the 3-dot menu and the Remove button.
    private void removeTrackFromList(Track track) {

        if (track == null || currentPlaylist == null) return;

        if (mediaPlayer != null
                && currentTrack != null
                && currentTrack.equals(track)) {

            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;

            clearMediaDisplay();

            currentTrack = null;
            currentTrackIndex = 0;

            playButton.setText("▶");
            titleLabel.setText("No Track Selected");
            artistLabel.setText("Add music to begin");
            albumLabel.setText("♫  MP3     ◷  00:00     ▱  ---");
            currentTimeLabel.setText("00:00");
            totalTimeLabel.setText("00:00");
            progressSlider.setValue(0);
        }

        currentPlaylist.getTracks().remove(track);
        selectedTrackRow = null;

        refreshTrackList();
        refreshPlaylists();
        updatePlaylistHeader();
        updateFavoriteButton();
    }

    private void selectTrackRow(HBox row) {
        if (selectedTrackRow != null && selectedTrackRow != row) {
            clearSelectedStyle(selectedTrackRow);
        }
        applySelectedStyle(row);
        selectedTrackRow = row;
    }

    private void applySelectedStyle(HBox row) {
        row.setStyle("""
                -fx-background-color: linear-gradient(to right, #4a0f2e, #2a0a20);
                -fx-background-radius: 10;
                -fx-border-color: #ff2d95;
                -fx-border-width: 1;
                -fx-border-radius: 10;
                -fx-cursor: hand;
                """);
    }

    private void clearSelectedStyle(HBox row) {
        row.setStyle("""
                -fx-background-color: transparent;
                -fx-border-color: #4a1235;
                -fx-border-width: 0 0 1 0;
                -fx-cursor: hand;
                """);
    }

    private void createNewPlaylist() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create Playlist");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("""
                -fx-background-color: #1a0713;
                -fx-border-color: #ff2d95;
                -fx-border-width: 1;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                """);

        ButtonType createType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(createType, cancelType);

        Label title = new Label("Create a new playlist");
        title.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                """);

        Label hint = new Label("Playlist name:");
        hint.setStyle("-fx-text-fill: #e5a4c9; -fx-font-size: 13px;");

        TextField field = new TextField();
        field.setPromptText("e.g. Chill Vibes");
        field.setPrefWidth(320);
        field.setStyle("""
                -fx-background-color: #2b0e22;
                -fx-background-radius: 10;
                -fx-border-color: #5e1a45;
                -fx-border-radius: 10;
                -fx-text-fill: white;
                -fx-prompt-text-fill: #d18cb3;
                -fx-padding: 10 14;
                """);

        VBox content = new VBox(10, title, hint, field);
        content.setPadding(new Insets(10, 5, 5, 5));

        pane.setContent(content);

        javafx.scene.Node createBtn = pane.lookupButton(createType);
        createBtn.setFocusTraversable(false);
        createBtn.setStyle("""
                -fx-background-color: linear-gradient(to right, #ff2d95, #c918b8);
                -fx-background-radius: 8;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-padding: 9 20;
                -fx-cursor: hand;
                """);

        javafx.scene.Node cancelBtn = pane.lookupButton(cancelType);
        cancelBtn.setFocusTraversable(false);
        cancelBtn.setStyle("""
                -fx-background-color: #2b0e22;
                -fx-background-radius: 8;
                -fx-text-fill: #f2c4dc;
                -fx-font-size: 13px;
                -fx-padding: 9 20;
                -fx-cursor: hand;
                """);

        dialog.setResultConverter(btn -> {
            if (btn == createType) return field.getText();
            return null;
        });

        dialog.showAndWait().ifPresent(name -> {
            if (name == null || name.trim().isEmpty()) return;

            Playlist playlist = new Playlist(name.trim());
            playlists.add(playlist);

            currentPlaylist = playlist;

            refreshPlaylists();
            refreshTrackList();
            updatePlaylistHeader();
            updateFavoriteButton();
            refreshPlaylistsScreen();

            showScreen("home");
        });
    }

    private void refreshPlaylists() {

        if (customPlaylistBox == null) return;

        customPlaylistBox.getChildren().clear();

        for (Playlist playlist : playlists) {
            Button button = new Button(
                    "♫   " + playlist.getName()
                            + "\n     " + playlist.size() + " tracks");

            button.setFocusTraversable(false);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: #f2c4dc;
                    -fx-font-size: 14px;
                    -fx-padding: 8 10;
                    -fx-cursor: hand;
                    """);

            button.setOnAction(e -> {
                openPlaylist(playlist);
                showScreen("home");
            });

            customPlaylistBox.getChildren().add(button);
        }

        if (playlistsGrid != null) refreshPlaylistsScreen();
    }

    private void addFiles() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Add Audio / Video Files");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Audio / Video Files",
                        "*.mp3", "*.wav", "*.m4a", "*.aac",
                        "*.mp4", "*.m4v", "*.mov"));

        List<File> files = chooser.showOpenMultipleDialog(
                root.getScene().getWindow());

        if (files == null) return;

        for (File file : files) {
            currentPlaylist.addTrack(new Track(
                    removeExtension(file.getName()),
                    "Unknown Artist",
                    "Unknown Album",
                    file.toURI().toString(),
                    "/assets/music_background.png",
                    "00:00"));
        }

        refreshTrackList();
        refreshPlaylists();
    }

    // A folder becomes a playlist named after it.
    private void addFolder() {

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Media Folder");

        File folder = chooser.showDialog(root.getScene().getWindow());
        if (folder == null) return;

        File[] files = folder.listFiles();
        if (files == null) return;

        Playlist folderPlaylist = new Playlist(folder.getName());

        for (File file : files) {
            if (!isSupportedMedia(file)) continue;

            folderPlaylist.addTrack(new Track(
                    removeExtension(file.getName()),
                    "Unknown Artist",
                    "Unknown Album",
                    file.toURI().toString(),
                    "/assets/music_background.png",
                    "00:00"));
        }

        if (folderPlaylist.size() == 0) {
            showMessage("No supported audio or video files were found in this folder.");
            return;
        }

        playlists.add(folderPlaylist);
        clearMediaDisplay();
        currentPlaylist = folderPlaylist;
        currentTrack = null;
        currentTrackIndex = 0;

        refreshPlaylists();
        refreshTrackList();
        updatePlaylistHeader();
        updateFavoriteButton();
    }

    private boolean isSupportedMedia(File file) {
        if (file == null || !file.isFile()) return false;
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3")
                || name.endsWith(".wav")
                || name.endsWith(".m4a")
                || name.endsWith(".aac")
                || name.endsWith(".mp4")
                || name.endsWith(".m4v")
                || name.endsWith(".mov");
    }

    private boolean isVideoFile(String path) {
        String name = path.toLowerCase();
        return name.endsWith(".mp4")
                || name.endsWith(".m4v")
                || name.endsWith(".mov");
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("MediaPlayer+");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // JavaFX can't grab a video frame without playing, so we play it
    // muted for a moment into an off-screen MediaView and snapshot that.
    private void grabVideoThumbnail(Track track) {

        if (track.getCoverImage() != null || track.isCoverAttempted()) {
            return;
        }

        MediaPlayer grabber = new MediaPlayer(new Media(track.getFilePath()));
        grabber.setMute(true);
        grabber.setVolume(0);

        MediaView grabView = new MediaView(grabber);
        grabView.setPreserveRatio(true);
        grabView.setFitWidth(240);

        StackPane grabBox = new StackPane(grabView);
        grabBox.setPrefSize(240, 135);
        grabBox.setStyle("-fx-background-color: black;");
        new Scene(grabBox);

        grabber.setOnReady(() -> {
            grabber.seek(Duration.seconds(1));
            grabber.play();
        });

        grabber.setOnPlaying(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ignored) {}

                Platform.runLater(() -> {
                    try {
                        WritableImage snapshot = grabBox.snapshot(null, null);
                        if (snapshot != null) {
                            track.setCoverImage(snapshot);
                            track.setCoverAttempted(true);
                            refreshTrackList();
                            if (currentScreen.equals("music")) refreshMusicScreen();
                            if (currentScreen.equals("video")) refreshVideoScreen();
                            if (track.equals(currentTrack)) updateBigArtwork(track);
                        }
                    } catch (Exception ignored) {
                    } finally {
                        grabber.stop();
                        grabber.dispose();
                    }
                });
            }).start();
        });

        grabber.setOnError(() -> grabber.dispose());
    }

    private void updateBigArtwork(Track track) {
        if (bigArtwork == null || track == null) return;

        Image cover = track.getCoverImage();
        if (cover != null) {
            bigArtwork.setImage(cover);
        } else {
            bigArtwork.setImage(new Image(
                    getClass().getResource("/assets/music_background.png")
                            .toExternalForm()));
        }
    }

    private void clearMediaDisplay() {

        if (mediaView != null) {
            mediaView.setMediaPlayer(null);
            mediaView.setVisible(false);
            mediaView.setManaged(false);
        }

        if (artworkBackground != null) artworkBackground.setVisible(true);
        if (bigArtwork != null) {
            bigArtwork.setVisible(false);
            bigArtwork.setManaged(false);
        }
        if (artworkText != null) artworkText.setVisible(true);
        if (mediaTypeLabel != null) mediaTypeLabel.setText("♫  Music");
        if (qualityLabel != null) qualityLabel.setText("MP3");
    }

    private void removeSelectedTrack() {

        if (currentPlaylist == null || currentTrack == null) {
            showMessage("Select a track first.");
            return;
        }

        removeTrackFromList(currentTrack);
    }

    private Playlist getFavoritesPlaylist() {
        for (Playlist playlist : playlists) {
            if ("Favorites".equalsIgnoreCase(playlist.getName())) {
                return playlist;
            }
        }
        Playlist favorites = new Playlist("Favorites");
        playlists.add(0, favorites);
        return favorites;
    }

    private boolean isFavorite(Track track) {
        if (track == null) return false;
        return getFavoritesPlaylist().getTracks().contains(track);
    }

    private void toggleFavorite() {
        if (currentTrack == null) {
            showMessage("Play or select a track first.");
            return;
        }
        Playlist favorites = getFavoritesPlaylist();
        if (favorites.getTracks().contains(currentTrack)) {
            favorites.getTracks().remove(currentTrack);
        } else {
            favorites.addTrack(currentTrack);
        }
        refreshPlaylists();
        refreshTrackList();
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (favoriteButton == null) return;
        boolean favorite = isFavorite(currentTrack);
        favoriteButton.setText(favorite ? "♥" : "♡");
        favoriteButton.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-font-size: 25px;
                -fx-cursor: hand;
                """.formatted(favorite ? "#ff2d95" : "#f2c4dc"));
    }

    private void updatePlaylistHeader() {
        if (playlistTitleLabel != null && currentPlaylist != null) {
            playlistTitleLabel.setText("♫  " + currentPlaylist.getName());
        }
        if (playlistCountLabel != null && currentPlaylist != null) {
            playlistCountLabel.setText(currentPlaylist.size() + " items");
        }
    }

    private String removeExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index > 0) return name.substring(0, index);
        return name;
    }

    private void loadTrack(int index) {

        if (currentPlaylist == null || currentPlaylist.size() == 0) return;

        if (index < 0) index = 0;
        if (index >= currentPlaylist.size()) index = 0;

        currentTrackIndex = index;
        currentTrack = currentPlaylist.getTracks().get(index);

        updateFavoriteButton();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        titleLabel.setText(currentTrack.getTitle());
        artistLabel.setText(currentTrack.getArtist());

        boolean video = isVideoFile(currentTrack.getFilePath());

        albumLabel.setText(
                (video ? "▶  VIDEO" : "♫  AUDIO")
                        + "     ◷  " + currentTrack.getDuration());

        progressSlider.setValue(0);
        currentTimeLabel.setText("00:00");
        totalTimeLabel.setText("00:00");

        if (video) {
            artworkBackground.setVisible(false);
            artworkText.setVisible(false);
            bigArtwork.setVisible(false);
            bigArtwork.setManaged(false);
            mediaView.setVisible(true);
            mediaView.setManaged(true);
        } else {
            artworkBackground.setVisible(false);
            artworkText.setVisible(false);
            mediaView.setVisible(false);
            mediaView.setManaged(false);

            updateBigArtwork(currentTrack);
            bigArtwork.setVisible(true);
            bigArtwork.setManaged(true);

            // Keep the small info-bar thumbnail in sync too.
            if (albumArt != null) {
                Image cover = currentTrack.getCoverImage();
                if (cover != null) {
                    albumArt.setImage(cover);
                } else {
                    albumArt.setImage(new Image(
                            getClass().getResource("/assets/music_background.png")
                                    .toExternalForm()));
                }
            }
        }

        mediaTypeLabel.setText(video ? "▶  Video" : "♫  Music");
        qualityLabel.setText(video ? "VIDEO" : "AUDIO");

        try {
            mediaPlayer = new MediaPlayer(
                    new Media(currentTrack.getFilePath()));

            if (video) {
                mediaView.setMediaPlayer(mediaPlayer);
            }

            mediaPlayer.setVolume(volumeSlider.getValue() / 100);

            mediaPlayer.setOnReady(() -> {
                Duration duration = mediaPlayer.getTotalDuration();
                totalTimeLabel.setText(formatTime(duration));
                currentTrack.setDuration(formatTime(duration));
                refreshTrackList();

                if (video
                        && currentTrack.getCoverImage() == null
                        && !currentTrack.isCoverAttempted()) {
                    grabVideoThumbnail(currentTrack);
                }
            });

            mediaPlayer.currentTimeProperty().addListener(
                    (obs, oldTime, newTime) -> {
                        if (!progressSlider.isValueChanging()) {
                            Duration total = mediaPlayer.getTotalDuration();
                            if (total != null && total.toMillis() > 0) {
                                double progress =
                                        newTime.toMillis() / total.toMillis() * 100;
                                progressSlider.setValue(progress);
                                currentTimeLabel.setText(formatTime(newTime));
                            }
                        }
                    });

            mediaPlayer.setOnEndOfMedia(this::nextTrack);

        } catch (Exception e) {
            System.out.println("Could not load media: " + e.getMessage());
        }
    }

    private void playTrack(Track track) {
        int index = currentPlaylist.getTracks().indexOf(track);
        if (index < 0) return;
        loadTrack(index);
        if (mediaPlayer != null) {
            mediaPlayer.play();
            playButton.setText("Ⅱ");
        }
    }

    private void togglePlay() {
        if (currentPlaylist == null || currentPlaylist.size() == 0) return;

        if (mediaPlayer == null) {
            loadTrack(currentTrackIndex);
            if (mediaPlayer == null) return;
        }

        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playButton.setText("▶");
        } else {
            mediaPlayer.play();
            playButton.setText("Ⅱ");
        }
    }

    private void stopMusic() {
        if (mediaPlayer == null) return;
        mediaPlayer.stop();
        playButton.setText("▶");
        progressSlider.setValue(0);
        currentTimeLabel.setText("00:00");
    }

    private void nextTrack() {
        if (currentPlaylist == null || currentPlaylist.size() == 0) return;
        currentTrackIndex++;
        if (currentTrackIndex >= currentPlaylist.size()) currentTrackIndex = 0;
        loadTrack(currentTrackIndex);
        if (mediaPlayer != null) {
            mediaPlayer.play();
            playButton.setText("Ⅱ");
        }
    }

    private void previousTrack() {
        if (currentPlaylist == null || currentPlaylist.size() == 0) return;
        currentTrackIndex--;
        if (currentTrackIndex < 0) currentTrackIndex = currentPlaylist.size() - 1;
        loadTrack(currentTrackIndex);
        if (mediaPlayer != null) {
            mediaPlayer.play();
            playButton.setText("Ⅱ");
        }
    }

    private void seekMusic() {
        if (mediaPlayer == null) return;
        Duration total = mediaPlayer.getTotalDuration();
        if (total == null || total.toMillis() <= 0) return;
        double percentage = progressSlider.getValue() / 100.0;
        mediaPlayer.seek(total.multiply(percentage));
    }

    private void toggleMute() {
        if (mediaPlayer == null) return;
        if (!muted) {
            previousVolume = volumeSlider.getValue();
            volumeSlider.setValue(0);
            muted = true;
        } else {
            volumeSlider.setValue(previousVolume);
            muted = false;
        }
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) return "00:00";
        int totalSeconds = (int) duration.toSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static void main(String[] args) {
        launch(args);
    }
}