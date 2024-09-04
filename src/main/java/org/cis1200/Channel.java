package org.cis1200;

import java.util.Objects;
import java.util.TreeMap;

public class Channel implements Comparable<Channel> {
    private final String name;
    private String owner;
    private boolean invite;
    private TreeMap<Integer, String> users;

    public Channel(String name, String owner, TreeMap<Integer, String> users, boolean invite) {
        this.name = name;
        this.owner = owner;
        this.invite = invite;
        this.users = users;
    }

    public String getId() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public boolean getStatus() {
        return invite;
    }

    public TreeMap<Integer, String> getUsers() {
        return users;
    }

    public void makeOwner(String makeOwner) {
        this.owner = makeOwner;
    }

    public void makeUser(TreeMap<Integer, String> makeUser) {
        users = makeUser;
    }

    @Override
    public boolean equals(Object c) {
        if (this == c) {
            return true;
        }
        if (c == null || getClass() != c.getClass()) {
            return false;
        }
        Channel channel = (Channel) c;
        return Objects.equals(name, channel.name) &&
                Objects.equals(owner, channel.owner) &&
                Objects.equals(users, channel.users);
    }

    @Override
    public int compareTo(Channel c) {
        if (Objects.equals(name, c.getId())) {
            return 0;
        } else {
            return name.compareTo(c.getId());
        }
    }

}