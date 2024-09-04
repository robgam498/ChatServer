package org.cis1200;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class ServerModelTest {
    private ServerModel model;

    /**
     * Before each test, we initialize model to be
     * a new ServerModel (with all new, empty state)
     */
    @BeforeEach
    public void setUp() {
        // We initialize a fresh ServerModel for each test
        model = new ServerModel();
    }

    /**
     * Here is an example test that checks the functionality of your
     * changeNickname error handling. Each line has commentary directly above
     * it which you can use as a framework for the remainder of your tests.
     */
    @Test
    public void testInvalidNickname() {
        // A user must be registered before their nickname can be changed,
        // so we first register a user with an arbitrarily chosen id of 0.
        model.registerUser(0);

        // We manually create a Command that appropriately tests the case
        // we are checking. In this case, we create a NicknameCommand whose
        // new Nickname is invalid.
        Command command = new NicknameCommand(0, "User0", "!nv@l!d!");

        // We manually create the expected Broadcast using the Broadcast
        // factory methods. In this case, we create an error Broadcast with
        // our command and an INVALID_NAME error.
        Broadcast expected = Broadcast.error(
                command, ServerResponse.INVALID_NAME
        );

        // We then get the actual Broadcast returned by the method we are
        // trying to test. In this case, we use the updateServerModel method
        // of the NicknameCommand.
        Broadcast actual = command.updateServerModel(model);

        // The first assertEquals call tests whether the method returns
        // the appropriate Broadcast.
        assertEquals(expected, actual, "Broadcast");

        // We also want to test whether the state has been correctly
        // changed.In this case, the state that would be affected is
        // the user's Collection.
        Collection<String> users = model.getRegisteredUsers();

        // We now check to see if our command updated the state
        // appropriately. In this case, we first ensure that no
        // additional users have been added.
        assertEquals(1, users.size(), "Number of registered users");

        // We then check if the username was updated to an invalid value
        // (it should not have been).
        assertTrue(users.contains("User0"), "Old nickname still registered");

        // Finally, we check that the id 0 is still associated with the old,
        // unchanged nickname.
        assertEquals(
                "User0", model.getNickname(0),
                "User with id 0 nickname unchanged"
        );
    }

    /*
     * Your TAs will be manually grading the tests that you write below this
     * comment block. Don't forget to test the public methods you have added to
     * your ServerModel class, as well as the behavior of the server in
     * different scenarios.
     * You might find it helpful to take a look at the tests we have already
     * provided you with in Task4Test, Task3Test, and Task5Test.
     */

    // ID TESTS

    @Test
    public void testFindID() {
        model.registerUser(0);
        assertEquals(0, model.getUserId("User0"), "returns ID");
    }

    @Test
    public void testChangeID() {
        model.registerUser(0);
        Command newId = new NicknameCommand(0, "User0", "Valid");
        Set<String> joinee = new TreeSet<>();
        joinee.add("User0");
        Broadcast expected = Broadcast.okay(newId, joinee);
        Broadcast actual = newId.updateServerModel(model);
        assertEquals(expected, actual, "New Name Broadcast");
        Collection<String> users = model.getRegisteredUsers();
        assertTrue(users.contains("Valid"), "New Name Successfully Added");
        assertFalse(users.contains("User0"), "Old Name Successfully Deleted");
        assertEquals(users.size(), 1);
    }

    @Test
    public void testFindNickName() {
        model.registerUser(0);
        assertEquals("User0", model.getNickname(0), "returns nickname");
    }

    @Test
    public void testNicknameChangeStillOwner() {
        model.registerUser(0);

        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);
        Command nick = new NicknameCommand(0, "User0", "NewName");
        nick.updateServerModel(model);

        assertEquals("NewName", model.getChannel("Channel1").getOwner(), "No Change");
    }

    // REGISTRATION / DE-REGISTRATION TESTS

    @Test
    public void testDeregisterfromSingleChannel() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        model.deregisterUser(0);
        model.deregisterUser(1);
        model.deregisterUser(2);
        assertFalse(model.getRegisteredUsers().contains("User0"), "User0 still registered");
        assertFalse(model.getRegisteredUsers().contains("User1"), "User1 still registered");
        assertFalse(model.getRegisteredUsers().contains("User2"), "User2 still registered");
        assertTrue(model.getRegisteredUsers().isEmpty(), "All users have left the channel");
    }

    @Test
    public void testDeregisterfromMultipleChannels() {
        model.registerUser(0);
        model.registerUser(1);
        CreateCommand com1 = new CreateCommand(0, "User0", "Channel1", false);
        CreateCommand com2 = new CreateCommand(0, "User0", "Channel2", false);
        CreateCommand com3 = new CreateCommand(1, "User1", "Channel3", false);
        model.createChannel(com1);
        model.createChannel(com2);
        model.createChannel(com3);

        TreeSet<String> updated = new TreeSet<>();
        updated.addAll(model.getChannels());
        assertEquals(updated.size(), 3);
        assertTrue(updated.contains("Channel1"));
        assertTrue(updated.contains("Channel2"));
        assertTrue(updated.contains("Channel3"));

        model.deregisterUser(0);
        updated.clear();
        updated.addAll(model.getChannels());

        assertTrue(model.getRegisteredUsers().contains("User1"), "User1 still registered");
        assertFalse(model.getRegisteredUsers().contains("User0"), "User0 no longer registered");
        assertTrue(updated.contains("Channel3"));
        assertEquals(1, updated.size());
    }

    @Test
    public void testDeregisterOwner() {
        model.registerUser(0);
        model.registerUser(1);

        CreateCommand com1 = new CreateCommand(0, "User0", "Channel1", false);
        CreateCommand com2 = new CreateCommand(0, "User0", "Channel2", false);
        CreateCommand com3 = new CreateCommand(1, "User1", "Channel3", false);
        JoinCommand join = new JoinCommand(1, "User1", "ocaml");

        model.createChannel(com1);
        model.createChannel(com2);
        model.createChannel(com3);
        model.joinChannel(join);

        model.deregisterUser(0);

        assertFalse(model.getChannels().contains("Channel1"), "Channel1 does not exist");
        assertFalse(model.getChannels().contains("Channel2"), "Channel2 does not exist");
        assertFalse(model.getRegisteredUsers().contains("User0"), "User0 deregistered");
        assertTrue(model.getChannels().contains("Channel3"), "User1 still registered");
        assertEquals(model.getChannels().size(), 1);
    }

    @Test
    public void testDeregisterAll() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        model.deregisterUser(0);
        model.deregisterUser(1);
        model.deregisterUser(2);
        assertFalse(model.getRegisteredUsers().contains("User0"), "No Change");
        assertFalse(model.getRegisteredUsers().contains("User1"), "No Change");
        assertFalse(model.getRegisteredUsers().contains("User0"), "No Change");
        assertTrue(model.getRegisteredUsers().isEmpty(), "All users have left the channel");
    }

    @Test
    public void testRegistered() {
        model.registerUser(0);
        assertTrue(model.getRegisteredUsers().contains("User0"), "User0 is member");
        assertEquals(model.getRegisteredUsers().size(), 1, "size is one");
    }

    // CHANNELS TESTS

    @Test
    public void testMembersinChannel() {
        model.registerUser(0);
        Command com = new CreateCommand(0, "User0", "Channel1", false);
        Set<String> recipients = new TreeSet<>();
        recipients.add("User0");
        Broadcast expected = Broadcast.okay(com, recipients);
        Broadcast actual = com.updateServerModel(model);
        assertEquals(expected, actual, "Broadcast:");
        assertEquals(recipients, model.getUsersInChannel("Channel1"), "User0 in Channel1");
    }

    @Test
    public void testChannelsActive() {
        model.registerUser(0);
        Command com = new CreateCommand(0, "User0", "Channel1", false);
        Set<String> recipients = new TreeSet<>();
        recipients.add("User0");
        Broadcast expected = Broadcast.okay(com, recipients);
        Broadcast actual = com.updateServerModel(model);
        assertEquals(expected, actual, "Broadcast");
        assertEquals(1, model.getChannels().size(), "Number of Channels Online");
    }

    // INVITE TESTS

    @Test
    public void invitePublic() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);
        Command invite = new InviteCommand(0, "User0", "Channel1", "User1");
        Broadcast expected = Broadcast.error(invite, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
        assertEquals(expected, invite.updateServerModel(model), "Broadcast");
    }

    @Test
    public void invitePrivate() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "Channel1", true);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "User0", "Channel1");
        Broadcast expected = Broadcast.error(join, ServerResponse.JOIN_PRIVATE_CHANNEL);
        assertEquals(expected, join.updateServerModel(model), "Broadcast");
    }

    @Test
    public void inviteIsRedundant() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "ChannelOne", true);
        create.updateServerModel(model);
        Command invite = new InviteCommand(0, "User0", "ChannelOne", "User1");
        invite.updateServerModel(model);
        Command invite2 = new InviteCommand(0, "User0", "ChannelOne", "User1");
        Set<String> joinee = new TreeSet<>();
        joinee.add("User1");
        joinee.add("User0");
        Broadcast expected = Broadcast.names(invite, joinee, "User0");
        assertEquals(expected, invite2.updateServerModel(model), "Broadcast");
    }

    @Test
    public void inviteNonMember() {
        model.registerUser(0);

        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);
        Command inviteCommand = new InviteCommand(0, "User0", "Channel1", "User1");
        Broadcast expected = Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, inviteCommand.updateServerModel(model));

        assertEquals(1, model.getUsersInChannel("Channel1").size(), "no change");
        assertTrue(model.getUsersInChannel("Channel1").contains("User0"), "User0 in channel");
        assertFalse(model.getUsersInChannel("Channel1").contains("User1"), "User1 not in channel");
    }

    @Test
    public void inviteNotOwner() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);

        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);

        Command invite = new InviteCommand(1, "User1", "Channel1", "User2");

        Broadcast expected = Broadcast.error(invite, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
        assertEquals(expected, invite.updateServerModel(model), "Broadcast");
    }

    @Test
    public void inviteToInvalidChannel() {
        model.registerUser(0);
        model.registerUser(1);

        Command inviteCommand = new InviteCommand(0, "User0", "Invalid", "User1");
        Broadcast expected = Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, inviteCommand.updateServerModel(model));
    }

    // MESSAGING TESTS

    @Test
    public void msgUserInvalid() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);

        Command sendM = new MessageCommand(1, "User1", "Channel1", "Join!");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        Broadcast expected = Broadcast.error(sendM, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, sendM.updateServerModel(model), "Broadcast");
    }

    @Test
    public void msgToInvalid() {
        model.registerUser(0);
        Command sendM = new MessageCommand(0, "User0", "Channel1", "Join!");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User0");
        Broadcast expected = Broadcast.error(sendM, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, sendM.updateServerModel(model), "Broadcast");
    }

    // KICKING TESTS

    @Test
    public void testOwnerKicksThemselves() {
        model.registerUser(0);
        model.registerUser(1);

        Command create = new CreateCommand(0, "User0", "Channel1", false);

        create.updateServerModel(model);

        Command join = new JoinCommand(1, "User1", "Channel1");
        join.updateServerModel(model);

        Command leave = new LeaveCommand(0, "User0", "Channel1");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("User0");
        Broadcast expected = Broadcast.okay(leave, recipients);
        assertEquals(expected, leave.updateServerModel(model), "Broadcast");
        assertFalse(model.getChannels().contains("Channel1"), "Channel no longer exists");
    }

    @Test
    public void testKickNonExistingUser() {
        model.registerUser(0);
        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);

        Command kick = new KickCommand(0, "User0", "Channel1", "User1");
        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, kick.updateServerModel(model));

        assertEquals(1, model.getUsersInChannel("Channel1").size(), "Channel Users");
        assertTrue(model.getUsersInChannel("Channel1").contains("User0"), "No Change");
    }

    @Test
    public void kickUserWhoIsNotOwner() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);

        Command join = new JoinCommand(1, "User1", "Channel1");
        join.updateServerModel(model);

        Command kick = new KickCommand(1, "User1", "Channel1", "User0");
        Broadcast expected = Broadcast.error(kick, ServerResponse.USER_NOT_OWNER);
        assertEquals(expected, kick.updateServerModel(model));

        assertEquals(2, model.getUsersInChannel("Channel1").size(), "Users still in channel");
        assertTrue(model.getUsersInChannel("Channel1").contains("User0"), "Users still in channel");
    }

    @Test
    public void kickInvalidKick() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);

        Command join = new JoinCommand(1, "User1", "Channel1");
        join.updateServerModel(model);

        Command kick = new KickCommand(1, "User1", "Channel1", "User0");
        Broadcast expected = Broadcast.error(kick, ServerResponse.USER_NOT_OWNER);
        assertEquals(expected, kick.updateServerModel(model));

        assertTrue(model.getUsersInChannel("Channel1").contains("User0"), "User still in channel");
        assertEquals(2, model.getUsersInChannel("Channel1").size(), "No change");
    }

    @Test
    public void kickUserNotMember() {
        model.registerUser(0);
        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);

        Command kick = new KickCommand(0, "User0", "Channel1", "User1");
        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, kick.updateServerModel(model));

        assertEquals(1, model.getUsersInChannel("Channel1").size(), "No Change");
        assertTrue(model.getUsersInChannel("Channel1").contains("User0"), "No Change");
    }

    @Test
    public void kickFromInvalidChannel() {
        model.registerUser(0);
        Command create = new CreateCommand(0, "User0", "Invalid", false);
        create.updateServerModel(model);

        Command kick = new KickCommand(0, "User0", "NONE", "User0");
        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, kick.updateServerModel(model));

        assertTrue(model.getUsersInChannel("Invalid").contains("User0"), "User0 still in channel");
        assertEquals(1, model.getUsersInChannel("Invalid").size(), "No Change");
    }

    // JOINING/LEAVING TESTS

    @Test
    public void joinNonexistentChannel() {
        model.registerUser(0);
        Command join = new JoinCommand(0, "User0", "Channel1");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User0");
        Broadcast expected = Broadcast.error(join, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, join.updateServerModel(model), "Broadcast");
    }

    @Test
    public void joinPrivateChannel() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "Channel1", true);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "User1", "Channel1");
        Broadcast expected = Broadcast.error(join, ServerResponse.JOIN_PRIVATE_CHANNEL);

        assertEquals(1, model.getUsersInChannel("Channel1").size(), "Same Members");
        assertTrue(model.getUsersInChannel("Channel1").contains("User0"), "User0 in channel");
        assertFalse(model.getUsersInChannel("Channel1").contains("User1"), "User1 not in channel");
    }

    @Test
    public void leaveInvalidChannel() {
        model.registerUser(0);
        Command leave = new LeaveCommand(0, "User0", "Invalid");
        Broadcast expected = Broadcast.error(leave, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, leave.updateServerModel(model));
    }

    @Test
    public void leaveInavlidNotInChannel() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);
        Command leave = new LeaveCommand(1, "User1", "Channel1");
        Broadcast expected = Broadcast.error(leave, ServerResponse.USER_NOT_IN_CHANNEL);

        assertEquals(0, model.getUsersInChannel("java").size(), "No change");
        assertTrue(model.getUsersInChannel("Channel1").contains("User0"), "User0 in channel");
        assertFalse(model.getUsersInChannel("Channel1").contains("User1"), "User1 not in channel");
    }

    @Test
    public void testOwnerLeaves() {
        model.registerUser(0);
        model.registerUser(1);

        Command create = new CreateCommand(0, "User0", "Channel1", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "User1", "Channel1");
        join.updateServerModel(model);
        Command leave = new LeaveCommand(0, "User0", "Channel1");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("User0");
        Broadcast expected = Broadcast.okay(leave, recipients);
        assertEquals(expected, leave.updateServerModel(model));
        assertFalse(model.getChannels().contains("Channel1"), "Channel Closed: Owner Left");
    }

}
