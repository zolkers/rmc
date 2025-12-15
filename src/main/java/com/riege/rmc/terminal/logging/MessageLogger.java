package com.riege.rmc.terminal.logging;

import com.riege.rmc.terminal.command.bridge.RustTerminal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class MessageLogger {

    private final int maxCapacity;
    private final LinkedList<Message> messages;
    private static RustTerminal rustBridge = null;

    public MessageLogger(final int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.messages = new LinkedList<>();
    }

    public static void setRustBridge(RustTerminal bridge) {
        rustBridge = bridge;
    }

    public void log(final String content, final MessageType type) {
        if (content == null) return;

        final Message message = new Message(content, type);

        synchronized (messages) {
            messages.add(message);
            while (messages.size() > maxCapacity) {
                messages.removeFirst();
            }
        }

        printToSystemOut(message);
    }
    public void logPlain(String s) { log(s, MessageType.PLAIN); }
    public void info(String s) { log(s, MessageType.INFO); }
    public void error(String s) { log(s, MessageType.ERROR); }
    public void success(String s) { log(s, MessageType.SUCCESS); }
    public void debug(String s) { log(s, MessageType.DEBUG); }
    public void warning(String s) { log(s, MessageType.WARNING); }
    public List<Message> getMessages() { synchronized(messages) { return new ArrayList<>(messages); } }
    public int size() { synchronized(messages) { return messages.size(); } }
    public boolean isEmpty() { synchronized(messages) { return messages.isEmpty(); } }
    public void clear() { synchronized(messages) { messages.clear(); } }
    public List<Message> getRecentMessages() { synchronized(messages) { return new ArrayList<>(messages); } }
    private void printToSystemOut(final Message message) {
        if (rustBridge != null) {
            String txt = message.getContent();
            switch (message.getType()) {
                case ERROR -> rustBridge.terminal_log_error(txt);
                case SUCCESS -> rustBridge.terminal_log_success(txt);
                case WARNING -> rustBridge.terminal_log_warning(txt);
                case DEBUG -> rustBridge.terminal_log_debug(txt);
                case INFO, PLAIN -> rustBridge.terminal_log_info(txt);
            }
        } else {
            System.out.println("[" + message.getType() + "] " + message.getContent());
        }
    }
}