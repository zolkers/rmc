package com.riege.rmc.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;
import com.riege.rmc.persistence.dto.AppSettings;
import com.riege.rmc.persistence.dto.PersistedProfile;
import com.riege.rmc.persistence.dto.ServerFavorite;
import com.riege.rmc.terminal.logging.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PersistenceManager {
    private static final PersistenceManager INSTANCE = new PersistenceManager();

    private final Gson gson;
    private final Path profilesFile;
    private final Path serversFile;
    private final Path settingsFile;

    private PersistenceManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        Path configDir = ConfigDirectory.getConfigPath();
        this.profilesFile = configDir.resolve("profiles.enc");
        this.serversFile = configDir.resolve("servers.json");
        this.settingsFile = configDir.resolve("settings.json");
    }

    public static PersistenceManager getInstance() {
        return INSTANCE;
    }

    public Optional<AuthenticatedProfile> loadProfile() {
        if (!Files.exists(profilesFile)) {
            return Optional.empty();
        }

        try {
            byte[] encrypted = Files.readAllBytes(profilesFile);
            String json = SecurityUtils.decrypt(encrypted);
            PersistedProfile persisted = gson.fromJson(json, PersistedProfile.class);

            if (persisted == null) {
                return Optional.empty();
            }

            if (persisted.isExpired()) {
                clearProfile();
                return Optional.empty();
            }

            return Optional.of(persisted.toAuthenticatedProfile());
        } catch (Exception e) {
            Logger.error("Failed to load profile: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void saveProfile(AuthenticatedProfile profile) throws Exception {
        ConfigDirectory.ensureExists();
        PersistedProfile persisted = PersistedProfile.from(profile);
        String json = gson.toJson(persisted);
        byte[] encrypted = SecurityUtils.encrypt(json);
        Files.write(profilesFile, encrypted);
    }

    public void clearProfile() throws Exception {
        Files.deleteIfExists(profilesFile);
    }

    public List<ServerFavorite> loadServers() {
        if (!Files.exists(serversFile)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(serversFile);
            Type listType = new TypeToken<List<ServerFavorite>>(){}.getType();
            List<ServerFavorite> servers = gson.fromJson(json, listType);
            return servers != null ? servers : new ArrayList<>();
        } catch (Exception e) {
            Logger.error("Failed to load servers: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveServer(ServerFavorite server) throws Exception {
        ConfigDirectory.ensureExists();
        List<ServerFavorite> servers = loadServers();

        servers.removeIf(s -> s.alias().equalsIgnoreCase(server.alias()));

        servers.add(server);

        String json = gson.toJson(servers);
        Files.writeString(serversFile, json);
    }

    public void updateServerLastConnected(String aliasOrAddress) {
        try {
            List<ServerFavorite> servers = loadServers();
            boolean updated = false;

            for (int i = 0; i < servers.size(); i++) {
                ServerFavorite server = servers.get(i);
                if (server.alias().equalsIgnoreCase(aliasOrAddress) ||
                    server.address().equalsIgnoreCase(aliasOrAddress)) {
                    servers.set(i, server.incrementConnectCount());
                    updated = true;
                    break;
                }
            }

            if (updated) {
                ConfigDirectory.ensureExists();
                String json = gson.toJson(servers);
                Files.writeString(serversFile, json);
            }
        } catch (Exception e) {
            // Silently fail - not critical
        }
    }

    public Optional<ServerFavorite> findByAlias(String alias) {
        List<ServerFavorite> servers = loadServers();
        return servers.stream()
            .filter(s -> s.alias().equalsIgnoreCase(alias))
            .findFirst();
    }

    public AppSettings loadSettings() {
        if (!Files.exists(settingsFile)) {
            return AppSettings.defaults();
        }

        try {
            String json = Files.readString(settingsFile);
            AppSettings settings = gson.fromJson(json, AppSettings.class);
            return settings != null ? settings : AppSettings.defaults();
        } catch (Exception e) {
            Logger.error("Failed to load settings: " + e.getMessage());
            return AppSettings.defaults();
        }
    }

    public void saveSettings(AppSettings settings) throws Exception {
        ConfigDirectory.ensureExists();
        String json = gson.toJson(settings);
        Files.writeString(settingsFile, json);
    }
}
