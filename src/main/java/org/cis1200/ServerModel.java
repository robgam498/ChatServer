package org.cis1200;

import java.util.*;

/*
 * Make sure to write your own tests in ServerModelTest.java.
 * The tests we provide for each task are NOT comprehensive!
 */

/**
 * The {@code ServerModel} is the class responsible for tracking the
 * state of the server, including its current users and the channels
 * they are in.
 * This class is used by subclasses of {@link Command} to:
 * 1. handle commands from clients, and
 * 2. handle commands from {@link ServerBackend} to coordinate
 * client connection/disconnection.
 */

public final class ServerModel {
    private TreeMap<Integer, String> users;
    private TreeSet<Channel> channels;

    private TreeSet<String> name;

    /**
     * Constructs a {@code ServerModel}. Make sure to initialize any collections
     * used to model the server state here.
     */

    public ServerModel() {
        users = new TreeMap<>();
        channels = new TreeSet<>();
        name = new TreeSet<>();
    }

    // =========================================================================
    // == Task 2: Basic Server model queries
    // == These functions provide helpful ways to test the state of your model.
    // == You may also use them in later tasks.
    // =========================================================================

    /**
     * Gets the user ID currently associated with the given
     * nickname. The returned ID is -1 if the nickname is not
     * currently in use.
     *
     * @param nickname The nickname for which to get the associated user ID
     * @return The user ID of the user with the argued nickname if
     *         such a user exists, otherwise -1
     */
    public int getUserId(String nickname) {
        for (Map.Entry<Integer, String> entry : users.entrySet()) {
            if (nickname.equals(entry.getValue())) {
                int userName = entry.getKey();
                return userName;
            }
        }
        return -1;
    }

    /**
     * Gets the nickname currently associated with the given user
     * ID. The returned nickname is null if the user ID is not
     * currently in use.
     *
     * @param userId The user ID for which to get the associated
     *               nickname
     * @return The nickname of the user with the argued user ID if
     *         such a user exists, otherwise null
     */
    public String getNickname(int userId) {
        return users.get(userId);
    }

    /**
     * Gets a collection of the nicknames of all users who are
     * registered with the server. Changes to the returned collection
     * should not affect the server state.
     *
     * This method is provided for testing.
     *
     * @return The collection of registered user nicknames
     */
    public Collection<String> getRegisteredUsers() {
        Collection<String> registeredUsers = new TreeSet<>(users.values());
        return registeredUsers;
    }

    /**
     * Gets a collection of the names of all the channels that are
     * present on the server. Changes to the returned collection
     * should not affect the server state.
     *
     * This method is provided for testing.
     *
     * @return The collection of channel names
     */
    public Collection<String> getChannels() {
        Collection<String> chanSet = new TreeSet<>();
        for (Channel chanCurr : channels) {
            chanSet.add(chanCurr.getId());
        }
        return chanSet;
    }

    /**
     * Gets a collection of the nicknames of all the users in a given
     * channel. The collection is empty if no channel with the given
     * name exists. Modifications to the returned collection should
     * not affect the server state.
     *
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get member nicknames
     * @return A collection of all user nicknames in the channel
     */
    public Collection<String> getUsersInChannel(String channelName) {
        Collection<String> totalUsers = new TreeSet<>();
        for (Channel chanCurr : channels) {
            if (Objects.equals(chanCurr.getId(), channelName)) {
                for (Map.Entry<Integer, String> e : chanCurr.getUsers().entrySet()) {
                    totalUsers.add(e.getValue());
                }
            }
        }
        return totalUsers;
    }

    /**
     * Gets the nickname of the owner of the given channel. The result
     * is {@code null} if no channel with the given name exists.
     *
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get the owner nickname
     * @return The nickname of the channel owner if such a channel
     *         exists; otherwise, return null
     */
    public String getOwner(String channelName) {
        String activeUser;
        for (Channel chanCurr : channels) {
            activeUser = chanCurr.getId();
            if (activeUser.equals(channelName)) {
                return chanCurr.getOwner();
            }
        }
        return null;
    }

    // ===============================================
    // == Task 3: Connections and Setting Nicknames ==
    // ===============================================

    /**
     * This method is automatically called by the backend when a new client
     * connects to the server. It should generate a default nickname with
     * {@link #generateUniqueNickname()}, store the new user's ID and username
     * in your data structures for {@link ServerModel} state, and construct
     * and return a {@link Broadcast} object using
     * {@link Broadcast#connected(String)}}.
     *
     * @param userId The new user's unique ID (automatically created by the
     *               backend)
     * @return The {@link Broadcast} object generated by calling
     *         {@link Broadcast#connected(String)} with the proper parameter
     */
    public Broadcast registerUser(int userId) {
        String nickname = generateUniqueNickname();
        // We have taken care of generating the nickname and returning
        // the Broadcast for you. You need to modify this method to
        // store the new user's ID and username in this model's internal state.
        users.put(userId, nickname);
        return Broadcast.connected(nickname);
    }

    /**
     * Helper for {@link #registerUser(int)}. (Nothing to do here.)
     *
     * Generates a unique nickname of the form "UserX", where X is the
     * smallest non-negative integer that yields a unique nickname for a user.
     *
     * @return The generated nickname
     */
    private String generateUniqueNickname() {
        int suffix = 0;
        String nickname;
        Collection<String> existingUsers = getRegisteredUsers();
        do {
            nickname = "User" + suffix++;
        } while (existingUsers.contains(nickname));
        return nickname;
    }

    /**
     * This method is automatically called by the backend when a client
     * disconnects from the server. This method should take the following
     * actions, not necessarily in this order:
     *
     * (1) All users who shared a channel with the disconnected user should be
     * notified that they left
     * (2) All channels owned by the disconnected user should be deleted
     * (3) The disconnected user's information should be removed from
     * {@link ServerModel}'s internal state
     * (4) Construct and return a {@link Broadcast} object using
     * {@link Broadcast#disconnected(String, Collection)}.
     *
     * @param userId The unique ID of the user to deregister
     * @return The {@link Broadcast} object generated by calling
     *         {@link Broadcast#disconnected(String, Collection)} with the proper
     *         parameters
     */
    public Broadcast deregisterUser(int userId) {
        String user = getNickname(userId);
        Collection<String> remainingUser = new TreeSet<>();
        TreeSet<Channel> removeChan = new TreeSet<>();

        for (Channel chanCurr : channels) {
            if (getUsersInChannel(chanCurr.getId()).contains(user)) {
                remainingUser.addAll(getUsersInChannel(chanCurr.getId()));
                remainingUser.remove(user);
            }
            if (chanCurr.getOwner().equals(user)) {
                removeChan.add(chanCurr);
            }
        }
        for (Channel chanCurr : removeChan) {
            channels.remove(chanCurr);
        }

        for (Channel chanCurr : channels) {
            if (chanCurr.getUsers().containsKey(userId)) {
                TreeMap<Integer, String> newUser = chanCurr.getUsers();
                newUser.remove(userId);
                chanCurr.makeUser(newUser);
            }
        }

        users.remove(userId);
        return Broadcast.disconnected(user, remainingUser);
    }

    /**
     * This method is called when a user wants to change their nickname.
     *
     * @param nickCommand The {@link NicknameCommand} object containing
     *                    all information needed to attempt a nickname change
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the nickname
     *         change is successful. The command should be the original nickCommand
     *         and the collection of recipients should be any clients who
     *         share at least one channel with the sender, including the sender.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#INVALID_NAME} if the proposed nickname
     *         is not valid according to
     *         {@link ServerModel#isValidName(String)}
     *         (2) {@link ServerResponse#NAME_ALREADY_IN_USE} if there is
     *         already a user with the proposed nickname
     */
    public Broadcast changeNickname(NicknameCommand nickCommand) {
        if (isValidName(nickCommand.getNewNickname())
                && !(users.containsValue(nickCommand.getNewNickname()))) {
            Collection<String> chanUsers = new TreeSet<>();
            for (Channel chanCurr : channels) {
                if (getUsersInChannel(chanCurr.getId()).contains(nickCommand.getSender())) {
                    chanUsers.addAll(getUsersInChannel(chanCurr.getId()));
                }
            }
            for (Channel chanCurr : channels) {
                if (Objects.equals(chanCurr.getOwner(), nickCommand.getSender())) {
                    chanCurr.makeOwner(nickCommand.getNewNickname());
                }
            }
            users.replace(
                    nickCommand.getSenderId(), nickCommand.getSender(), nickCommand.getNewNickname()
            );

            for (Channel chanCurr : channels) {
                if (getUsersInChannel(chanCurr.getId()).contains(nickCommand.getSender())) {
                    TreeMap<Integer, String> newUsers = chanCurr.getUsers();
                    newUsers.replace(
                            nickCommand.getSenderId(), nickCommand.getSender(),
                            nickCommand.getNewNickname()
                    );
                    chanCurr.makeUser(newUsers);
                }
            }
            return Broadcast.okay(nickCommand, chanUsers);
        } else if (!(isValidName(nickCommand.getNewNickname()))) {
            return Broadcast.error(nickCommand, ServerResponse.INVALID_NAME);
        }
        return Broadcast.error(nickCommand, ServerResponse.NAME_ALREADY_IN_USE);
    }

    /**
     * Determines if a given nickname is valid or invalid (contains at least
     * one alphanumeric character, and no non-alphanumeric characters).
     * (Nothing to do here.)
     *
     * @param name The channel or nickname string to validate
     * @return true if the string is a valid name
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    // ===================================
    // == Task 4: Channels and Messages ==
    // ===================================

    Channel getChannel(String chanName) {
        for (Channel chan : channels) {
            if (Objects.equals(chanName, chan.getId())) {
                return chan;
            }
        }
        return null;
    }

    /**
     * This method is called when a user wants to create a channel.
     * You can ignore the privacy aspect of this method for task 4, but
     * make sure you come back and implement it in task 5.
     *
     * @param createCommand The {@link CreateCommand} object containing all
     *                      information needed to attempt channel creation
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the channel
     *         creation is successful. The only recipient should be the new
     *         channel's owner.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#INVALID_NAME} if the proposed
     *         channel name is not valid according to
     *         {@link ServerModel#isValidName(String)}
     *         (2) {@link ServerResponse#CHANNEL_ALREADY_EXISTS} if there is
     *         already a channel with the proposed name
     */
    public Broadcast createChannel(CreateCommand createCommand) {

        TreeSet<String> currName = new TreeSet<>();
        for (Channel chanCurr : channels) {
            currName.add(chanCurr.getId());
        }

        if (!currName.contains(createCommand.getChannel())) {
            if (isValidName(createCommand.getChannel())) {
                TreeMap<Integer, String> newPeople = new TreeMap<>();
                newPeople.put(createCommand.getSenderId(), createCommand.getSender());

                Channel ch = new Channel(
                        createCommand.getChannel(), createCommand.getSender(), newPeople,
                        createCommand.isInviteOnly()
                );
                channels.add(ch);

                Collection<String> newAdmin = new TreeSet<>();
                newAdmin.add(createCommand.getSender());

                return Broadcast.okay(createCommand, newAdmin);
            } else if (!isValidName(createCommand.getChannel())) {
                return Broadcast.error(createCommand, ServerResponse.INVALID_NAME);
            }
        }
        return Broadcast.error(createCommand, ServerResponse.CHANNEL_ALREADY_EXISTS);
    }

    /**
     * This method is called when a user wants to join a channel.
     * You can ignore the privacy aspect of this method for task 4, but
     * make sure you come back and implement it in task 5.
     *
     * @param joinCommand The {@link JoinCommand} object containing all
     *                    information needed for the user's join attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#names(Command, Collection, String)} if the user
     *         joins the channel successfully. The recipients should be all
     *         people in the joined channel (including the sender).
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) (after Task 5) {@link ServerResponse#JOIN_PRIVATE_CHANNEL} if
     *         the sender is attempting to join a private channel
     */
    public Broadcast joinChannel(JoinCommand joinCommand) {
        Channel ch = null;
        for (Channel chanCurr : channels) {
            if (chanCurr.getId().equals(joinCommand.getChannel())) {
                ch = chanCurr;
            }
        }

        if (ch != null) {
            if (!(ch.getStatus())) {
                TreeMap<Integer, String> currUsers = ch.getUsers();
                currUsers.put(joinCommand.getSenderId(), joinCommand.getSender());
                ch.makeUser(currUsers);
                return Broadcast.names(joinCommand, currUsers.values(), ch.getOwner());
            } else {
                return Broadcast.error(joinCommand, ServerResponse.JOIN_PRIVATE_CHANNEL);
            }
        } else {
            return Broadcast.error(joinCommand, ServerResponse.NO_SUCH_CHANNEL);
        }
    }

    private boolean isActive(String channelName, String userName) {
        for (Channel c : channels) {
            if (Objects.equals(c.getId(), channelName)) {
                TreeMap<Integer, String> mem = c.getUsers();
                if (mem.containsValue(userName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method is called when a user wants to send a message to a channel.
     *
     * @param messageCommand The {@link MessageCommand} object containing all
     *                       information needed for the messaging attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the message
     *         attempt is successful. The recipients should be all clients
     *         in the channel.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the sender is
     *         not in the channel they are trying to send the message to
     */
    public Broadcast sendMessage(MessageCommand messageCommand) {
        String ch = null;
        for (Channel c : channels) {
            if (c.getId().equals(messageCommand.getChannel())) {
                ch = messageCommand.getChannel();
            }
        }

        if (ch != null) {
            if (isActive(messageCommand.getChannel(), messageCommand.getSender())) {
                return Broadcast
                        .okay(messageCommand, getUsersInChannel(messageCommand.getChannel()));
            }
            return Broadcast.error(messageCommand, ServerResponse.USER_NOT_IN_CHANNEL);
        } else {
            return Broadcast.error(messageCommand, ServerResponse.NO_SUCH_CHANNEL);
        }
    }

    /**
     * This method is called when a user wants to leave a channel.
     *
     * @param leaveCommand The {@link LeaveCommand} object containing all
     *                     information about the user's leave attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the user leaves
     *         the channel successfully. The recipients should be all clients
     *         who were in the channel, including the user who left.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the sender is
     *         not in the channel they are trying to leave
     */
    public Broadcast leaveChannel(LeaveCommand leaveCommand) {
        TreeSet<String> chanIds = new TreeSet<>();
        for (Channel chanCurr : channels) {
            chanIds.add(chanCurr.getId());
        }

        if (!(chanIds.contains(leaveCommand.getChannel()))) {
            return Broadcast.error(leaveCommand, ServerResponse.NO_SUCH_CHANNEL);
        } else if (chanIds.contains(leaveCommand.getChannel()) &&
                isActive(leaveCommand.getChannel(), leaveCommand.getSender())) {
            Channel ch = getChannel(leaveCommand.getChannel());
            TreeMap<Integer, String> mem = ch.getUsers();
            TreeMap<Integer, String> updateUsers = new TreeMap<>();

            if (Objects.equals(getOwner(leaveCommand.getChannel()), leaveCommand.getSender())) {
                TreeMap<Integer, String> sendTo = new TreeMap<>();
                sendTo.putAll(mem);
                chanIds.remove(ch.getId());
                channels.remove(ch);
                return Broadcast.okay(leaveCommand, sendTo.values());
            }
            mem.remove(leaveCommand.getSenderId(), leaveCommand.getSender());
            ch.makeUser(mem);

            updateUsers.putAll(mem);
            updateUsers.put(leaveCommand.getSenderId(), leaveCommand.getSender());
            return Broadcast.okay(leaveCommand, updateUsers.values());
        }
        return Broadcast.error(leaveCommand, ServerResponse.USER_NOT_IN_CHANNEL);

    }

    // =============================
    // == Task 5: Channel Privacy ==
    // =============================

    // Go back to createChannel and joinChannel and add
    // all privacy-related functionalities, then delete this when you're done.

    /**
     * This method is called when a channel's owner adds a user to that channel.
     *
     * @param inviteCommand The {@link InviteCommand} object containing all
     *                      information needed for the invite attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#names(Command, Collection, String)} if the user
     *         joins the channel successfully as a result of the invite.
     *         The recipients should be all people in the joined channel
     *         (including the new user).
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_USER} if the invited user
     *         does not exist
     *         (2) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no channel
     *         with the specified name
     *         (3) {@link ServerResponse#INVITE_TO_PUBLIC_CHANNEL} if the
     *         invite refers to a public channel
     *         (4) {@link ServerResponse#USER_NOT_OWNER} if the sender is not
     *         the owner of the channel
     */
    public Broadcast inviteUser(InviteCommand inviteCommand) {

        TreeSet<String> channelId = new TreeSet<>();
        for (Channel chanCurr : channels) {
            channelId.add(chanCurr.getId());
        }

        if (users.containsValue(inviteCommand.getUserToInvite())) {

            if (channelId.contains(inviteCommand.getChannel())) {
                Channel ch = getChannel(inviteCommand.getChannel());

                if (ch.getStatus()) {

                    if (Objects.equals(inviteCommand.getSender(), ch.getOwner())) {
                        TreeMap<Integer, String> mem = ch.getUsers();
                        mem.put(
                                getUserId(inviteCommand.getUserToInvite()),
                                inviteCommand.getUserToInvite()
                        );
                        ch.makeUser(mem);
                        return Broadcast.names(inviteCommand, mem.values(), ch.getOwner());
                    } else {
                        return Broadcast.error(inviteCommand, ServerResponse.USER_NOT_OWNER);
                    }

                } else {
                    return Broadcast.error(inviteCommand, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
                }

            } else {
                return Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_CHANNEL);
            }

        }
        return Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_USER);
    }

    /**
     * This method is called when a channel's owner removes a user from
     * that channel.
     *
     * @param kickCommand The {@link KickCommand} object containing all
     *                    information needed for the kick attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the user is
     *         successfully kicked from the channel. The recipients should be
     *         all clients who were in the channel, including the user
     *         who was kicked.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_USER} if the user being kicked
     *         does not exist
     *         (2) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no channel
     *         with the specified name
     *         (3) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the
     *         user being kicked is not a member of the channel
     *         (4) {@link ServerResponse#USER_NOT_OWNER} if the sender is not
     *         the owner of the channel
     */
    public Broadcast kickUser(KickCommand kickCommand) {

        TreeSet<String> channelId = new TreeSet<>();
        Channel ch;
        TreeMap<Integer, String> mem;
        for (Channel chanCurr : channels) {
            channelId.add(chanCurr.getId());
        }

        if (getRegisteredUsers().contains(kickCommand.getUserToKick())) {

            if (channelId.contains(kickCommand.getChannel())) {
                ch = getChannel(kickCommand.getChannel());

                if (ch.getUsers().containsValue(kickCommand.getUserToKick())) {

                    if (Objects.equals(kickCommand.getSender(), ch.getOwner())) {
                        mem = ch.getUsers();

                        if (Objects.equals(kickCommand.getSender(), kickCommand.getUserToKick())) {
                            TreeMap<Integer, String> broadcastTo = new TreeMap<>();
                            broadcastTo.putAll(mem);
                            name.remove(ch.getId());
                            channels.remove(mem);
                            return Broadcast.okay(kickCommand, broadcastTo.values());
                        } else {
                            mem.remove(
                                    getUserId(kickCommand.getUserToKick()),
                                    kickCommand.getUserToKick()
                            );
                            ch.makeUser(mem);
                            TreeMap<Integer, String> newMem = new TreeMap<>();
                            newMem.putAll(mem);
                            newMem.put(
                                    getUserId(kickCommand.getUserToKick()),
                                    kickCommand.getUserToKick()
                            );
                            return Broadcast.okay(kickCommand, newMem.values());
                        }

                    } else {
                        return Broadcast.error(kickCommand, ServerResponse.USER_NOT_OWNER);
                    }

                } else {
                    return Broadcast.error(kickCommand, ServerResponse.USER_NOT_IN_CHANNEL);
                }

            } else {
                return Broadcast.error(kickCommand, ServerResponse.NO_SUCH_CHANNEL);
            }

        } else {
            return Broadcast.error(kickCommand, ServerResponse.NO_SUCH_USER);
        }
    }

}