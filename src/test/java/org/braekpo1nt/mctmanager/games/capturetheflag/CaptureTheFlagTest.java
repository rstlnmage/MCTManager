package org.braekpo1nt.mctmanager.games.capturetheflag;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.MyPlayerMock;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.braekpo1nt.mctmanager.ui.MockFastBoardManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static org.mockito.Mockito.*;

public class CaptureTheFlagTest {
    
    private ServerMock server;
    private Main plugin;
    private PluginCommand command;
    private CommandSender sender;
    private MockFastBoardManager mockFastBoardManager;
    private GameManager gameManager;
    
    
    @BeforeEach
    void setUpServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(Main.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
//        FastBoardManager mockFastBoardManager = mock(FastBoardManager.class, RETURNS_DEFAULTS);
        mockFastBoardManager = new MockFastBoardManager();
        gameManager = plugin.getGameManager();
        gameManager.setFastBoardManager(mockFastBoardManager);
        command = plugin.getCommand("mct");
        sender = server.getConsoleSender();
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    @Test
    @DisplayName("Starting capture the flag with two players has no errors up to the class selection period")
    void twoPlayersGetToMatchStart() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            createParticipant("Player1", "red", "Red");
            createParticipant("Player2", "blue", "Blue");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            server.getScheduler().performTicks((20 * 10) + 1); // speed through the startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 20) + 1); // speed through the startClassSelectionPeriod()
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in twoPlayersGetToMatchStart()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    MyPlayerMock createParticipant(String name, String teamName, String displayName) {
        MyPlayerMock player = new MyPlayerMock(server, name, UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        server.addPlayer(player);
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "join", teamName, player.getName()});
        player.assertSaidPlaintext("You've been joined to "+displayName);
        return player;
    }
    
    void addTeam(String teamName, String teamDisplayName, String teamColor) {
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "add", teamName, String.format("\"%s\"", teamDisplayName), teamColor});
    }
    
    @Test
    @DisplayName("With 3 teams, the third team gets notified they're on deck")
    void threePlayerOnDeckTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            
            player1.assertSaidPlaintext("Red is competing against Blue this round.");
            player2.assertSaidPlaintext("Blue is competing against Red this round.");
            player3.assertSaidPlaintext("Green is not competing in this round. Their next round is 1");
            mockFastBoardManager.assertLine(player3.getUniqueId(), 1, "On Deck");
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if two participants are on a team, and one quits during round countdown, the show goes on")
    void playerQuitDuringRoundCountdownTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            server.getScheduler().performTicks((20 * 5) + 1); // speed through half the startMatchesStartingCountDown()
            player3.disconnect();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            List<Player> participants = ctf.getParticipants();
            Assertions.assertEquals(2, participants.size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            List<CaptureTheFlagMatch> currentMatches = currentRound.getMatches();
            Assertions.assertEquals(1, currentMatches.size());
            
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits during round countdown, the round is cancelled")
    void teamQuitDuringRoundCountdownTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            server.getScheduler().performTicks((20 * 5) + 1); // speed through half the startMatchesStartingCountDown()
            player2.disconnect();
            server.getScheduler().performTicks((20 * 5) + 1); // speed through the second half the startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 20) + 1); // speed through startClassSelectionPeriod()
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getParticipants().size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            Assertions.assertEquals(1, currentRound.getParticipants().size());
            List<CaptureTheFlagMatch> matches = currentRound.getMatches();
            Assertions.assertEquals(1, matches.size());
            CaptureTheFlagMatch match = matches.get(0);
            Assertions.assertTrue(match.isAliveInMatch(player1));
            Assertions.assertFalse(match.isAliveInMatch(player2));
            Assertions.assertEquals(1, match.getNorthParticipants().size());
            Assertions.assertEquals(0, match.getSouthParticipants().size());
    
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if two participants are on a team, and one quits during class selection, the show goes on")
    void playerQuitDuringClassSelectionTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            server.getScheduler().performTicks((20 * 10) + 1); // speed through startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 10) + 1); // speed through half the startClassSelectionPeriod()
            player3.disconnect();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            List<Player> participants = ctf.getParticipants();
            Assertions.assertEquals(2, participants.size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            List<CaptureTheFlagMatch> currentMatches = currentRound.getMatches();
            Assertions.assertEquals(1, currentMatches.size());
            CaptureTheFlagMatch match = currentMatches.get(0);
            ClassPicker northClassPicker = match.getNorthClassPicker();
            Assertions.assertTrue(northClassPicker.isActive());
            Assertions.assertEquals(1, northClassPicker.getTeamMates().size());
            ClassPicker southClassPicker = match.getSouthClassPicker();
            Assertions.assertTrue(southClassPicker.isActive());
            Assertions.assertEquals(1, southClassPicker.getTeamMates().size());
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits during class selection, the team is considered dead at the start of the match")
    void teamQuitDuringClassSelectionTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            server.getScheduler().performTicks((20 * 10) + 1); // speed through startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 10) + 1); // speed through half the startClassSelectionPeriod()
            player2.disconnect();
            server.getScheduler().performTicks((20 * 10) + 1); // speed through the rest of startClassSelectionPeriod()
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getParticipants().size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            List<Player> participants = currentRound.getParticipants();
            Assertions.assertNotNull(participants);
            Assertions.assertEquals(1, participants.size());
            List<CaptureTheFlagMatch> matches = currentRound.getMatches();
            Assertions.assertEquals(1, matches.size());
            CaptureTheFlagMatch match = matches.get(0);
            Assertions.assertFalse(match.isAliveInMatch(player2));
            Assertions.assertEquals(1, match.getNorthParticipants().size());
            Assertions.assertEquals(0, match.getSouthParticipants().size());
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if two participants are on a team, and one quits during the match, the show goes on")
    void playerQuitDuringMatchTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            server.getScheduler().performTicks((20 * 10) + 1); // speed through startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 20) + 1); // speed through startClassSelectionPeriod()
            player3.disconnect();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(2, ctf.getParticipants().size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            Assertions.assertEquals(2, currentRound.getParticipants().size());
            List<CaptureTheFlagMatch> currentMatches = currentRound.getMatches();
            Assertions.assertEquals(1, currentMatches.size());
            CaptureTheFlagMatch match = currentMatches.get(0);
            Assertions.assertEquals(1, match.getNorthParticipants().size());
            Assertions.assertEquals(1, match.getSouthParticipants().size());
            
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits during the match, they are considered dead")
    void teamQuitDuringMatchTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            server.getScheduler().performTicks((20 * 10) + 1); // speed through startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 20) + 1); // speed through startClassSelectionPeriod()
            player2.disconnect();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getParticipants().size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            Assertions.assertEquals(1, currentRound.getParticipants().size());
            List<CaptureTheFlagMatch> currentMatches = currentRound.getMatches();
            Assertions.assertEquals(1, currentMatches.size());
            CaptureTheFlagMatch match = currentMatches.get(0);
            Assertions.assertEquals(1, match.getNorthParticipants().size());
            Assertions.assertEquals(0, match.getSouthParticipants().size());
            
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits while they still have a round, their next rounds are removed")
    void teamQuitBeforeAllTheirRoundsTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(3, ctf.getParticipants().size());
            Assertions.assertEquals(3, ctf.getRounds().size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            Assertions.assertEquals(2, currentRound.getParticipants().size());
            List<Player> onDeckParticipants = currentRound.getOnDeckParticipants();
            Assertions.assertEquals(1, onDeckParticipants.size());
            Assertions.assertTrue(onDeckParticipants.contains(player3));
            mockFastBoardManager.assertLine(player1.getUniqueId(), 2, "Round 1/3");
            mockFastBoardManager.assertLine(player2.getUniqueId(), 2, "Round 1/3");
            mockFastBoardManager.assertLine(player3.getUniqueId(), 2, "Round 1/3");
            
            player3.disconnect();
    
            Assertions.assertEquals(2, ctf.getParticipants().size());
            Assertions.assertEquals(1, ctf.getRounds().size());
            CaptureTheFlagRound currentRoundAfterDisconnect = ctf.getCurrentRound();
            Assertions.assertSame(currentRound, currentRoundAfterDisconnect);
            Assertions.assertEquals(2, currentRoundAfterDisconnect.getParticipants().size());
            Assertions.assertEquals(0, currentRoundAfterDisconnect.getOnDeckParticipants().size());
            mockFastBoardManager.assertLine(player1.getUniqueId(), 2, "Round 1/1");
            mockFastBoardManager.assertLine(player2.getUniqueId(), 2, "Round 1/1");
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits while they still have a round, their next rounds are removed")
    void teamQuitRoundsRemovedTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            addTeam("purple", "Purple", "dark_purple");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            MyPlayerMock player4 = createParticipant("Player4", "purple", "Purple");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(4, ctf.getParticipants().size());
            List<CaptureTheFlagRound> roundsBeforeDisconnect = ctf.getRounds();
            Assertions.assertEquals(3, roundsBeforeDisconnect.size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(0).getMatches().size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(1).getMatches().size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(2).getMatches().size());
            
            CaptureTheFlagRound firstRoundBeforeDisconnect = ctf.getCurrentRound();
            Assertions.assertNotNull(firstRoundBeforeDisconnect);
            List<Player> currentRoundParticipants = firstRoundBeforeDisconnect.getParticipants();
            Assertions.assertEquals(4, currentRoundParticipants.size());
            Assertions.assertTrue(currentRoundParticipants.contains(player1));
            Assertions.assertTrue(currentRoundParticipants.contains(player2));
            Assertions.assertTrue(currentRoundParticipants.contains(player3));
            Assertions.assertTrue(currentRoundParticipants.contains(player4));
            Assertions.assertEquals(0, firstRoundBeforeDisconnect.getOnDeckParticipants().size());
            
            player4.disconnect();
            
            Assertions.assertEquals(3, ctf.getParticipants().size());
            List<CaptureTheFlagRound> roundsAfterDisconnect = ctf.getRounds();
            Assertions.assertEquals(3, roundsAfterDisconnect.size());
            Assertions.assertEquals(2, roundsAfterDisconnect.get(0).getMatches().size());
            Assertions.assertEquals(1, roundsAfterDisconnect.get(1).getMatches().size());
            Assertions.assertEquals(1, roundsAfterDisconnect.get(2).getMatches().size());
    
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits while they still have a round, the current round is unchanged")
    void teamQuitCurrentRoundUnchangedTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            addTeam("purple", "Purple", "dark_purple");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            MyPlayerMock player4 = createParticipant("Player4", "purple", "Purple");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(4, ctf.getParticipants().size());
            List<CaptureTheFlagRound> roundsBeforeDisconnect = ctf.getRounds();
            Assertions.assertEquals(3, roundsBeforeDisconnect.size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(0).getMatches().size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(1).getMatches().size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(2).getMatches().size());
            
            CaptureTheFlagRound currentRoundBeforeDisconnect = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundBeforeDisconnect);
            List<Player> currentRoundParticipants = currentRoundBeforeDisconnect.getParticipants();
            Assertions.assertEquals(4, currentRoundParticipants.size());
            Assertions.assertTrue(currentRoundParticipants.contains(player1));
            Assertions.assertTrue(currentRoundParticipants.contains(player2));
            Assertions.assertTrue(currentRoundParticipants.contains(player3));
            Assertions.assertTrue(currentRoundParticipants.contains(player4));
            Assertions.assertEquals(0, currentRoundBeforeDisconnect.getOnDeckParticipants().size());
            
            player4.disconnect();
    
            CaptureTheFlagRound currentRoundAfterDisconnect = ctf.getCurrentRound();
            Assertions.assertSame(currentRoundBeforeDisconnect, currentRoundAfterDisconnect);
            Assertions.assertEquals(3, currentRoundAfterDisconnect.getParticipants().size());
            Assertions.assertEquals(0, currentRoundAfterDisconnect.getOnDeckParticipants().size());
            List<CaptureTheFlagMatch> currentMatchesAfterDisconnect = currentRoundAfterDisconnect.getMatches();
            Assertions.assertEquals(2, currentMatchesAfterDisconnect.size());
            
            server.getScheduler().performTicks((20 * 10) + 1); // speed through startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 20) + 1); // speed through startClassSelectionPeriod()
            
            Assertions.assertTrue(currentMatchesAfterDisconnect.get(0).isAliveInMatch(player1));
            Assertions.assertTrue(currentMatchesAfterDisconnect.get(0).isAliveInMatch(player2));
            Assertions.assertTrue(currentMatchesAfterDisconnect.get(1).isAliveInMatch(player3));
            Assertions.assertFalse(currentMatchesAfterDisconnect.get(1).isAliveInMatch(player4));
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits while they still have a round, the new set of rounds still progress")
    void teamQuitRoundProgressionTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            addTeam("purple", "Purple", "dark_purple");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            MyPlayerMock player4 = createParticipant("Player4", "purple", "Purple");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            CaptureTheFlagRound firstRound = ctf.getCurrentRound();
            Assertions.assertNotNull(firstRound);
            
            player4.disconnect();
            
            server.getScheduler().performTicks((20 * 10) + 1); // speed through startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 20) + 1); // speed through startClassSelectionPeriod()
            server.getScheduler().performTicks((20*60*3)+1); // speed through first round
            server.getScheduler().performTicks((20 * 10) + 1); // speed through startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 20) + 1); // speed through startClassSelectionPeriod()
            
            Assertions.assertEquals(3, ctf.getRounds().size());
            CaptureTheFlagRound secondRound = ctf.getCurrentRound();
            Assertions.assertNotSame(firstRound, secondRound);
            Assertions.assertEquals(2, secondRound.getParticipants().size());
            Assertions.assertTrue(secondRound.getParticipants().contains(player1));
            Assertions.assertTrue(secondRound.getParticipants().contains(player3));
            Assertions.assertEquals(1, secondRound.getOnDeckParticipants().size());
            Assertions.assertTrue(secondRound.getOnDeckParticipants().contains(player2));
            
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
}
