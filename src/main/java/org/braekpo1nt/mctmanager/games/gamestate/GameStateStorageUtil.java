package org.braekpo1nt.mctmanager.games.gamestate;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.*;

/**
 * Handles the CRUD operations for storing GameState objects
 */
public class GameStateStorageUtil {
    
    private static final String GAME_STATE_FILE_NAME = "gameState.json";
    private final File gameStateDirectory;
    private GameState gameState = new GameState();
    
    public GameStateStorageUtil(Main plugin) {
        this.gameStateDirectory = plugin.getDataFolder().getAbsoluteFile();
    }
    
    /**
     * Save the GameState to storage
     * @throws IOException if there is a problem
     * - creating a new game state file
     * - writing to the game state file
     * - converting the game state to json
     */
    public void saveGameState() throws IOException {
        Gson gson = new Gson();
        File gameStateFile = getGameStateFile();
        Writer writer = new FileWriter(gameStateFile, false);
        gson.toJson(this.gameState, writer);
        writer.flush();
        writer.close();
        Bukkit.getLogger().info("[MCTManager] Saved game state.");
    }
    
    /**
     * Load the GameState from storage
     * @throws IOException if there is a problem 
     * - creating a new game state file
     * - reading the existing game state file
     * - parsing the game state from json
     */
    public void loadGameState() throws IOException {
        Gson gson = new Gson();
        File gameStateFile = getGameStateFile();
        Reader reader = new FileReader(gameStateFile);
        GameState newGameState = gson.fromJson(reader, GameState.class);
        reader.close();
        if (newGameState == null) {
            newGameState = new GameState();
        }
        this.gameState = newGameState;
        Bukkit.getLogger().info("[MCTManager] Loaded game state.");
    }
    
    /**
     * Get the game state file from the given game state directory. Creates the file
     * if one doesn't already exist.
     * @return The game state file
     * @throws IOException if there is an error creating a new game state file
     */
    private File getGameStateFile() throws IOException {
        File gameStateFile = new File(gameStateDirectory.getAbsolutePath(), this.GAME_STATE_FILE_NAME);
        if (!gameStateFile.exists()) {
            if (!gameStateDirectory.exists()) {
                gameStateDirectory.mkdirs();
            }
            gameStateFile.createNewFile();
        }
        return gameStateFile;
    }
    
    /**
     * Checks if the game state contains a team with the given name
     * @param teamName The internal name of the team to check for
     * @return True if the team exists in the game state, false otherwise
     */
    public boolean containsTeam(String teamName) {
        return gameState.containsTeam(teamName);
    }
    
    /**
     * Add a team to the game state.
     * @param teamName The internal name of the team.
     * @param teamDisplayName The display name of the team.
     * @param color The color of the team
     * @throws IOException If there is an error saving the game state while adding a new team.
     */
    public void addTeam(String teamName, String teamDisplayName, NamedTextColor color) throws IOException {
        gameState.addTeam(teamName, teamDisplayName, color);
        saveGameState();
    }
    
    public void removeTeam(String teamName) throws IOException {
        gameState.removeTeam(teamName);
        saveGameState();
    }
    
    /**
     * Registers all teams in the game state with the given scoreboard
     * @param scoreboard The scoreboard to register the teams for
     */
    public void registerTeams(Scoreboard scoreboard) {
        for (MCTTeam mctTeam : gameState.getTeams()) {
            Team team = scoreboard.registerNewTeam(mctTeam.getName());
            team.displayName(Component.text(mctTeam.getDisplayName()));
            team.color(mctTeam.getColor());
        }
    }
}
