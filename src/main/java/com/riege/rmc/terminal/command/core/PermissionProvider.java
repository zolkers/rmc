package com.riege.rmc.terminal.command.core;

@FunctionalInterface
public interface PermissionProvider {
    boolean hasPermission(String sender, String node);
}