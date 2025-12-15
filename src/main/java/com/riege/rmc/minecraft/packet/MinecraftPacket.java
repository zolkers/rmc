package com.riege.rmc.minecraft.packet;

import java.util.Arrays;
import java.util.Optional;
public enum MinecraftPacket {

    // Configuration - toClient
    COOKIE_REQUEST_CONFIG_C(0x00, "cookie_request", Direction.TO_CLIENT, State.CONFIGURATION),
    CUSTOM_PAYLOAD_CONFIG_C(0x01, "custom_payload", Direction.TO_CLIENT, State.CONFIGURATION),
    DISCONNECT_CONFIG_C(0x02, "disconnect", Direction.TO_CLIENT, State.CONFIGURATION),
    FINISH_CONFIGURATION_C(0x03, "finish_configuration", Direction.TO_CLIENT, State.CONFIGURATION),
    KEEP_ALIVE_CONFIG_C(0x04, "keep_alive", Direction.TO_CLIENT, State.CONFIGURATION),
    PING_CONFIG_C(0x05, "ping", Direction.TO_CLIENT, State.CONFIGURATION),
    RESET_CHAT_C(0x06, "reset_chat", Direction.TO_CLIENT, State.CONFIGURATION),
    REGISTRY_DATA_C(0x07, "registry_data", Direction.TO_CLIENT, State.CONFIGURATION),
    REMOVE_RESOURCE_PACK_CONFIG_C(0x08, "remove_resource_pack", Direction.TO_CLIENT, State.CONFIGURATION),
    ADD_RESOURCE_PACK_CONFIG_C(0x09, "add_resource_pack", Direction.TO_CLIENT, State.CONFIGURATION),
    STORE_COOKIE_CONFIG_C(0x0A, "store_cookie", Direction.TO_CLIENT, State.CONFIGURATION),
    TRANSFER_CONFIG_C(0x0B, "transfer", Direction.TO_CLIENT, State.CONFIGURATION),
    FEATURE_FLAGS_C(0x0C, "feature_flags", Direction.TO_CLIENT, State.CONFIGURATION),
    TAGS_CONFIG_C(0x0D, "tags", Direction.TO_CLIENT, State.CONFIGURATION),
    SELECT_KNOWN_PACKS_CONFIG_C(0x0E, "select_known_packs", Direction.TO_CLIENT, State.CONFIGURATION),
    CUSTOM_REPORT_DETAILS_CONFIG_C(0x0F, "custom_report_details", Direction.TO_CLIENT, State.CONFIGURATION),
    SERVER_LINKS_CONFIG_C(0x10, "server_links", Direction.TO_CLIENT, State.CONFIGURATION),

    // Configuration - toServer
    SETTINGS_CONFIG_S(0x00, "settings", Direction.TO_SERVER, State.CONFIGURATION),
    COOKIE_RESPONSE_CONFIG_S(0x01, "cookie_response", Direction.TO_SERVER, State.CONFIGURATION),
    CUSTOM_PAYLOAD_CONFIG_S(0x02, "custom_payload", Direction.TO_SERVER, State.CONFIGURATION),
    FINISH_CONFIGURATION_S(0x03, "finish_configuration", Direction.TO_SERVER, State.CONFIGURATION),
    KEEP_ALIVE_CONFIG_S(0x04, "keep_alive", Direction.TO_SERVER, State.CONFIGURATION),
    PONG_CONFIG_S(0x05, "pong", Direction.TO_SERVER, State.CONFIGURATION),
    RESOURCE_PACK_RECEIVE_CONFIG_S(0x06, "resource_pack_receive", Direction.TO_SERVER, State.CONFIGURATION),
    SELECT_KNOWN_PACKS_CONFIG_S(0x07, "select_known_packs", Direction.TO_SERVER, State.CONFIGURATION),
    CUSTOM_REPORT_DETAILS_CONFIG_S(0x08, "custom_report_details", Direction.TO_SERVER, State.CONFIGURATION),
    SERVER_LINKS_CONFIG_S(0x09, "server_links", Direction.TO_SERVER, State.CONFIGURATION),

    // Handshaking - toServer
    SET_PROTOCOL_S(0x00, "set_protocol", Direction.TO_SERVER, State.HANDSHAKING),
    LEGACY_SERVER_LIST_PING_S(0xFE, "legacy_server_list_ping", Direction.TO_SERVER, State.HANDSHAKING),

    // Login - toClient
    DISCONNECT_LOGIN_C(0x00, "disconnect", Direction.TO_CLIENT, State.LOGIN),
    ENCRYPTION_BEGIN_LOGIN_C(0x01, "encryption_begin", Direction.TO_CLIENT, State.LOGIN),
    SUCCESS_LOGIN_C(0x02, "success", Direction.TO_CLIENT, State.LOGIN),
    COMPRESS_LOGIN_C(0x03, "compress", Direction.TO_CLIENT, State.LOGIN),
    LOGIN_PLUGIN_REQUEST_C(0x04, "login_plugin_request", Direction.TO_CLIENT, State.LOGIN),
    COOKIE_REQUEST_LOGIN_C(0x05, "cookie_request", Direction.TO_CLIENT, State.LOGIN),

    // Login - toServer
    LOGIN_START_S(0x00, "login_start", Direction.TO_SERVER, State.LOGIN),
    ENCRYPTION_BEGIN_LOGIN_S(0x01, "encryption_begin", Direction.TO_SERVER, State.LOGIN),
    LOGIN_PLUGIN_RESPONSE_S(0x02, "login_plugin_response", Direction.TO_SERVER, State.LOGIN),
    LOGIN_ACKNOWLEDGED_S(0x03, "login_acknowledged", Direction.TO_SERVER, State.LOGIN),
    COOKIE_RESPONSE_LOGIN_S(0x04, "cookie_response", Direction.TO_SERVER, State.LOGIN),

    // Play - toClient
    BUNDLE_DELIMITER_C(0x00, "bundle_delimiter", Direction.TO_CLIENT, State.PLAY),
    SPAWN_ENTITY_C(0x01, "spawn_entity", Direction.TO_CLIENT, State.PLAY),
    SPAWN_ENTITY_EXPERIENCE_ORB_C(0x02, "spawn_entity_experience_orb", Direction.TO_CLIENT, State.PLAY),
    ANIMATION_C(0x03, "animation", Direction.TO_CLIENT, State.PLAY),
    STATISTICS_C(0x04, "statistics", Direction.TO_CLIENT, State.PLAY),
    ACKNOWLEDGE_PLAYER_DIGGING_C(0x05, "acknowledge_player_digging", Direction.TO_CLIENT, State.PLAY),
    BLOCK_BREAK_ANIMATION_C(0x06, "block_break_animation", Direction.TO_CLIENT, State.PLAY),
    TILE_ENTITY_DATA_C(0x07, "tile_entity_data", Direction.TO_CLIENT, State.PLAY),
    BLOCK_ACTION_C(0x08, "block_action", Direction.TO_CLIENT, State.PLAY),
    BLOCK_CHANGE_C(0x09, "block_change", Direction.TO_CLIENT, State.PLAY),
    BOSS_BAR_C(0x0A, "boss_bar", Direction.TO_CLIENT, State.PLAY),
    DIFFICULTY_C(0x0B, "difficulty", Direction.TO_CLIENT, State.PLAY),
    CHUNK_BATCH_FINISHED_C(0x0C, "chunk_batch_finished", Direction.TO_CLIENT, State.PLAY),
    CHUNK_BATCH_START_C(0x0D, "chunk_batch_start", Direction.TO_CLIENT, State.PLAY),
    CHUNK_BIOMES_C(0x0E, "chunk_biomes", Direction.TO_CLIENT, State.PLAY),
    CLEAR_TITLES_C(0x0F, "clear_titles", Direction.TO_CLIENT, State.PLAY),
    TAB_COMPLETE_C(0x10, "tab_complete", Direction.TO_CLIENT, State.PLAY),
    DECLARE_COMMANDS_C(0x11, "declare_commands", Direction.TO_CLIENT, State.PLAY),
    CLOSE_WINDOW_C(0x12, "close_window", Direction.TO_CLIENT, State.PLAY),
    WINDOW_ITEMS_C(0x13, "window_items", Direction.TO_CLIENT, State.PLAY),
    CRAFT_PROGRESS_BAR_C(0x14, "craft_progress_bar", Direction.TO_CLIENT, State.PLAY),
    SET_SLOT_C(0x15, "set_slot", Direction.TO_CLIENT, State.PLAY),
    COOKIE_REQUEST_PLAY_C(0x16, "cookie_request", Direction.TO_CLIENT, State.PLAY),
    SET_COOLDOWN_C(0x17, "set_cooldown", Direction.TO_CLIENT, State.PLAY),
    CHAT_SUGGESTIONS_C(0x18, "chat_suggestions", Direction.TO_CLIENT, State.PLAY),
    CUSTOM_PAYLOAD_PLAY_C(0x19, "custom_payload", Direction.TO_CLIENT, State.PLAY),
    DAMAGE_EVENT_C(0x1A, "damage_event", Direction.TO_CLIENT, State.PLAY),
    DEBUG_SAMPLE_C(0x1B, "debug_sample", Direction.TO_CLIENT, State.PLAY),
    HIDE_MESSAGE_C(0x1C, "hide_message", Direction.TO_CLIENT, State.PLAY),
    KICK_DISCONNECT_C(0x1D, "kick_disconnect", Direction.TO_CLIENT, State.PLAY),
    PROFILELESS_CHAT_C(0x1E, "profileless_chat", Direction.TO_CLIENT, State.PLAY),
    ENTITY_STATUS_C(0x1F, "entity_status", Direction.TO_CLIENT, State.PLAY),
    SYNC_ENTITY_POSITION_C(0x20, "sync_entity_position", Direction.TO_CLIENT, State.PLAY),
    EXPLOSION_C(0x21, "explosion", Direction.TO_CLIENT, State.PLAY),
    UNLOAD_CHUNK_C(0x22, "unload_chunk", Direction.TO_CLIENT, State.PLAY),
    GAME_STATE_CHANGE_C(0x23, "game_state_change", Direction.TO_CLIENT, State.PLAY),
    OPEN_HORSE_WINDOW_C(0x24, "open_horse_window", Direction.TO_CLIENT, State.PLAY),
    HURT_ANIMATION_C(0x25, "hurt_animation", Direction.TO_CLIENT, State.PLAY),
    INITIALIZE_WORLD_BORDER_C(0x26, "initialize_world_border", Direction.TO_CLIENT, State.PLAY),
    KEEP_ALIVE_PLAY_C(0x27, "keep_alive", Direction.TO_CLIENT, State.PLAY),
    MAP_CHUNK_C(0x28, "map_chunk", Direction.TO_CLIENT, State.PLAY),
    WORLD_EVENT_C(0x29, "world_event", Direction.TO_CLIENT, State.PLAY),
    WORLD_PARTICLES_C(0x2A, "world_particles", Direction.TO_CLIENT, State.PLAY),
    UPDATE_LIGHT_C(0x2B, "update_light", Direction.TO_CLIENT, State.PLAY),
    LOGIN_PLAY_C(0x2C, "login", Direction.TO_CLIENT, State.PLAY),
    MAP_C(0x2D, "map", Direction.TO_CLIENT, State.PLAY),
    TRADE_LIST_C(0x2E, "trade_list", Direction.TO_CLIENT, State.PLAY),
    REL_ENTITY_MOVE_C(0x2F, "rel_entity_move", Direction.TO_CLIENT, State.PLAY),
    ENTITY_MOVE_LOOK_C(0x30, "entity_move_look", Direction.TO_CLIENT, State.PLAY),
    MOVE_MINECART_C(0x31, "move_minecart", Direction.TO_CLIENT, State.PLAY),
    ENTITY_LOOK_C(0x32, "entity_look", Direction.TO_CLIENT, State.PLAY),
    VEHICLE_MOVE_C(0x33, "vehicle_move", Direction.TO_CLIENT, State.PLAY),
    OPEN_BOOK_C(0x34, "open_book", Direction.TO_CLIENT, State.PLAY),
    OPEN_WINDOW_C(0x35, "open_window", Direction.TO_CLIENT, State.PLAY),
    OPEN_SIGN_ENTITY_C(0x36, "open_sign_entity", Direction.TO_CLIENT, State.PLAY),
    PING_PLAY_C(0x37, "ping", Direction.TO_CLIENT, State.PLAY),
    PING_RESPONSE_C(0x38, "ping_response", Direction.TO_CLIENT, State.PLAY),
    CRAFT_RECIPE_RESPONSE_C(0x39, "craft_recipe_response", Direction.TO_CLIENT, State.PLAY),
    ABILITIES_PLAY_C(0x3A, "abilities", Direction.TO_CLIENT, State.PLAY),
    PLAYER_CHAT_C(0x3B, "player_chat", Direction.TO_CLIENT, State.PLAY),
    END_COMBAT_EVENT_C(0x3C, "end_combat_event", Direction.TO_CLIENT, State.PLAY),
    ENTER_COMBAT_EVENT_C(0x3D, "enter_combat_event", Direction.TO_CLIENT, State.PLAY),
    DEATH_COMBAT_EVENT_C(0x3E, "death_combat_event", Direction.TO_CLIENT, State.PLAY),
    PLAYER_REMOVE_C(0x3F, "player_remove", Direction.TO_CLIENT, State.PLAY),
    PLAYER_INFO_C(0x40, "player_info", Direction.TO_CLIENT, State.PLAY),
    FACE_PLAYER_C(0x41, "face_player", Direction.TO_CLIENT, State.PLAY),
    POSITION_C(0x42, "position", Direction.TO_CLIENT, State.PLAY),
    PLAYER_ROTATION_C(0x43, "player_rotation", Direction.TO_CLIENT, State.PLAY),
    RECIPE_BOOK_ADD_C(0x44, "recipe_book_add", Direction.TO_CLIENT, State.PLAY),
    RECIPE_BOOK_REMOVE_C(0x45, "recipe_book_remove", Direction.TO_CLIENT, State.PLAY),
    RECIPE_BOOK_SETTINGS_C(0x46, "recipe_book_settings", Direction.TO_CLIENT, State.PLAY),
    ENTITY_DESTROY_C(0x47, "entity_destroy", Direction.TO_CLIENT, State.PLAY),
    REMOVE_ENTITY_EFFECT_C(0x48, "remove_entity_effect", Direction.TO_CLIENT, State.PLAY),
    RESET_SCORE_C(0x49, "reset_score", Direction.TO_CLIENT, State.PLAY),
    REMOVE_RESOURCE_PACK_PLAY_C(0x4A, "remove_resource_pack", Direction.TO_CLIENT, State.PLAY),
    ADD_RESOURCE_PACK_PLAY_C(0x4B, "add_resource_pack", Direction.TO_CLIENT, State.PLAY),
    RESPAWN_C(0x4C, "respawn", Direction.TO_CLIENT, State.PLAY),
    ENTITY_HEAD_ROTATION_C(0x4D, "entity_head_rotation", Direction.TO_CLIENT, State.PLAY),
    MULTI_BLOCK_CHANGE_C(0x4E, "multi_block_change", Direction.TO_CLIENT, State.PLAY),
    SELECT_ADVANCEMENT_TAB_C(0x4F, "select_advancement_tab", Direction.TO_CLIENT, State.PLAY),
    SERVER_DATA_C(0x50, "server_data", Direction.TO_CLIENT, State.PLAY),
    ACTION_BAR_C(0x51, "action_bar", Direction.TO_CLIENT, State.PLAY),
    WORLD_BORDER_CENTER_C(0x52, "world_border_center", Direction.TO_CLIENT, State.PLAY),
    WORLD_BORDER_LERP_SIZE_C(0x53, "world_border_lerp_size", Direction.TO_CLIENT, State.PLAY),
    WORLD_BORDER_SIZE_C(0x54, "world_border_size", Direction.TO_CLIENT, State.PLAY),
    WORLD_BORDER_WARNING_DELAY_C(0x55, "world_border_warning_delay", Direction.TO_CLIENT, State.PLAY),
    WORLD_BORDER_WARNING_REACH_C(0x56, "world_border_warning_reach", Direction.TO_CLIENT, State.PLAY),
    CAMERA_C(0x57, "camera", Direction.TO_CLIENT, State.PLAY),
    UPDATE_VIEW_POSITION_C(0x58, "update_view_position", Direction.TO_CLIENT, State.PLAY),
    UPDATE_VIEW_DISTANCE_C(0x59, "update_view_distance", Direction.TO_CLIENT, State.PLAY),
    SET_CURSOR_ITEM_C(0x5A, "set_cursor_item", Direction.TO_CLIENT, State.PLAY),
    SPAWN_POSITION_C(0x5B, "spawn_position", Direction.TO_CLIENT, State.PLAY),
    SCOREBOARD_DISPLAY_OBJECTIVE_C(0x5C, "scoreboard_display_objective", Direction.TO_CLIENT, State.PLAY),
    ENTITY_METADATA_C(0x5D, "entity_metadata", Direction.TO_CLIENT, State.PLAY),
    ATTACH_ENTITY_C(0x5E, "attach_entity", Direction.TO_CLIENT, State.PLAY),
    ENTITY_VELOCITY_C(0x5F, "entity_velocity", Direction.TO_CLIENT, State.PLAY),
    ENTITY_EQUIPMENT_C(0x60, "entity_equipment", Direction.TO_CLIENT, State.PLAY),
    EXPERIENCE_C(0x61, "experience", Direction.TO_CLIENT, State.PLAY),
    UPDATE_HEALTH_C(0x62, "update_health", Direction.TO_CLIENT, State.PLAY),
    HELD_ITEM_SLOT_PLAY_C(0x63, "held_item_slot", Direction.TO_CLIENT, State.PLAY),
    SCOREBOARD_OBJECTIVE_C(0x64, "scoreboard_objective", Direction.TO_CLIENT, State.PLAY),
    SET_PASSENGERS_C(0x65, "set_passengers", Direction.TO_CLIENT, State.PLAY),
    SET_PLAYER_INVENTORY_C(0x66, "set_player_inventory", Direction.TO_CLIENT, State.PLAY),
    TEAMS_C(0x67, "teams", Direction.TO_CLIENT, State.PLAY),
    SCOREBOARD_SCORE_C(0x68, "scoreboard_score", Direction.TO_CLIENT, State.PLAY),
    SIMULATION_DISTANCE_C(0x69, "simulation_distance", Direction.TO_CLIENT, State.PLAY),
    SET_TITLE_SUBTITLE_C(0x6A, "set_title_subtitle", Direction.TO_CLIENT, State.PLAY),
    UPDATE_TIME_C(0x6B, "update_time", Direction.TO_CLIENT, State.PLAY),
    SET_TITLE_TEXT_C(0x6C, "set_title_text", Direction.TO_CLIENT, State.PLAY),
    SET_TITLE_TIME_C(0x6D, "set_title_time", Direction.TO_CLIENT, State.PLAY),
    ENTITY_SOUND_EFFECT_C(0x6E, "entity_sound_effect", Direction.TO_CLIENT, State.PLAY),
    SOUND_EFFECT_C(0x6F, "sound_effect", Direction.TO_CLIENT, State.PLAY),
    START_CONFIGURATION_C(0x70, "start_configuration", Direction.TO_CLIENT, State.PLAY),
    STOP_SOUND_C(0x71, "stop_sound", Direction.TO_CLIENT, State.PLAY),
    STORE_COOKIE_PLAY_C(0x72, "store_cookie", Direction.TO_CLIENT, State.PLAY),
    SYSTEM_CHAT_C(0x73, "system_chat", Direction.TO_CLIENT, State.PLAY),
    PLAYERLIST_HEADER_C(0x74, "playerlist_header", Direction.TO_CLIENT, State.PLAY),
    NBT_QUERY_RESPONSE_C(0x75, "nbt_query_response", Direction.TO_CLIENT, State.PLAY),
    COLLECT_C(0x76, "collect", Direction.TO_CLIENT, State.PLAY),
    ENTITY_TELEPORT_C(0x77, "entity_teleport", Direction.TO_CLIENT, State.PLAY),
    SET_TICKING_STATE_C(0x78, "set_ticking_state", Direction.TO_CLIENT, State.PLAY),
    STEP_TICK_C(0x79, "step_tick", Direction.TO_CLIENT, State.PLAY),
    TRANSFER_PLAY_C(0x7A, "transfer", Direction.TO_CLIENT, State.PLAY),
    ADVANCEMENTS_C(0x7B, "advancements", Direction.TO_CLIENT, State.PLAY),
    ENTITY_UPDATE_ATTRIBUTES_C(0x7C, "entity_update_attributes", Direction.TO_CLIENT, State.PLAY),
    ENTITY_EFFECT_C(0x7D, "entity_effect", Direction.TO_CLIENT, State.PLAY),
    DECLARE_RECIPES_C(0x7E, "declare_recipes", Direction.TO_CLIENT, State.PLAY),
    TAGS_PLAY_C(0x7F, "tags", Direction.TO_CLIENT, State.PLAY),
    SET_PROJECTILE_POWER_C(0x80, "set_projectile_power", Direction.TO_CLIENT, State.PLAY),
    CUSTOM_REPORT_DETAILS_PLAY_C(0x81, "custom_report_details", Direction.TO_CLIENT, State.PLAY),
    SERVER_LINKS_PLAY_C(0x82, "server_links", Direction.TO_CLIENT, State.PLAY),

    // Play - toServer
    TELEPORT_CONFIRM_S(0x00, "teleport_confirm", Direction.TO_SERVER, State.PLAY),
    QUERY_BLOCK_NBT_S(0x01, "query_block_nbt", Direction.TO_SERVER, State.PLAY),
    SELECT_BUNDLE_ITEM_S(0x02, "select_bundle_item", Direction.TO_SERVER, State.PLAY),
    SET_DIFFICULTY_S(0x03, "set_difficulty", Direction.TO_SERVER, State.PLAY),
    MESSAGE_ACKNOWLEDGEMENT_S(0x04, "message_acknowledgement", Direction.TO_SERVER, State.PLAY),
    CHAT_COMMAND_S(0x05, "chat_command", Direction.TO_SERVER, State.PLAY),
    CHAT_COMMAND_SIGNED_S(0x06, "chat_command_signed", Direction.TO_SERVER, State.PLAY),
    CHAT_MESSAGE_S(0x07, "chat_message", Direction.TO_SERVER, State.PLAY),
    CHAT_SESSION_UPDATE_S(0x08, "chat_session_update", Direction.TO_SERVER, State.PLAY),
    CHUNK_BATCH_RECEIVED_S(0x09, "chunk_batch_received", Direction.TO_SERVER, State.PLAY),
    CLIENT_COMMAND_S(0x0A, "client_command", Direction.TO_SERVER, State.PLAY),
    TICK_END_S(0x0B, "tick_end", Direction.TO_SERVER, State.PLAY),
    SETTINGS_PLAY_S(0x0C, "settings", Direction.TO_SERVER, State.PLAY),
    TAB_COMPLETE_S(0x0D, "tab_complete", Direction.TO_SERVER, State.PLAY),
    CONFIGURATION_ACKNOWLEDGED_S(0x0E, "configuration_acknowledged", Direction.TO_SERVER, State.PLAY),
    ENCHANT_ITEM_S(0x0F, "enchant_item", Direction.TO_SERVER, State.PLAY),
    WINDOW_CLICK_S(0x10, "window_click", Direction.TO_SERVER, State.PLAY),
    CLOSE_WINDOW_S(0x11, "close_window", Direction.TO_SERVER, State.PLAY),
    SET_SLOT_STATE_S(0x12, "set_slot_state", Direction.TO_SERVER, State.PLAY),
    COOKIE_RESPONSE_PLAY_S(0x13, "cookie_response", Direction.TO_SERVER, State.PLAY),
    CUSTOM_PAYLOAD_PLAY_S(0x14, "custom_payload", Direction.TO_SERVER, State.PLAY),
    DEBUG_SAMPLE_SUBSCRIPTION_S(0x15, "debug_sample_subscription", Direction.TO_SERVER, State.PLAY),
    EDIT_BOOK_S(0x16, "edit_book", Direction.TO_SERVER, State.PLAY),
    QUERY_ENTITY_NBT_S(0x17, "query_entity_nbt", Direction.TO_SERVER, State.PLAY),
    USE_ENTITY_S(0x18, "use_entity", Direction.TO_SERVER, State.PLAY),
    GENERATE_STRUCTURE_S(0x19, "generate_structure", Direction.TO_SERVER, State.PLAY),
    KEEP_ALIVE_PLAY_S(0x1A, "keep_alive", Direction.TO_SERVER, State.PLAY),
    LOCK_DIFFICULTY_S(0x1B, "lock_difficulty", Direction.TO_SERVER, State.PLAY),
    POSITION_S(0x1C, "position", Direction.TO_SERVER, State.PLAY),
    POSITION_LOOK_S(0x1D, "position_look", Direction.TO_SERVER, State.PLAY),
    LOOK_S(0x1E, "look", Direction.TO_SERVER, State.PLAY),
    FLYING_S(0x1F, "flying", Direction.TO_SERVER, State.PLAY),
    VEHICLE_MOVE_S(0x20, "vehicle_move", Direction.TO_SERVER, State.PLAY),
    STEER_BOAT_S(0x21, "steer_boat", Direction.TO_SERVER, State.PLAY),
    PICK_ITEM_FROM_BLOCK_S(0x22, "pick_item_from_block", Direction.TO_SERVER, State.PLAY),
    PICK_ITEM_FROM_ENTITY_S(0x23, "pick_item_from_entity", Direction.TO_SERVER, State.PLAY),
    PING_REQUEST_S(0x24, "ping_request", Direction.TO_SERVER, State.PLAY),
    CRAFT_RECIPE_REQUEST_S(0x25, "craft_recipe_request", Direction.TO_SERVER, State.PLAY),
    ABILITIES_PLAY_S(0x26, "abilities", Direction.TO_SERVER, State.PLAY),
    BLOCK_DIG_S(0x27, "block_dig", Direction.TO_SERVER, State.PLAY),
    ENTITY_ACTION_S(0x28, "entity_action", Direction.TO_SERVER, State.PLAY),
    PLAYER_INPUT_S(0x29, "player_input", Direction.TO_SERVER, State.PLAY),
    PLAYER_LOADED_S(0x2A, "player_loaded", Direction.TO_SERVER, State.PLAY),
    PONG_PLAY_S(0x2B, "pong", Direction.TO_SERVER, State.PLAY),
    RECIPE_BOOK_S(0x2C, "recipe_book", Direction.TO_SERVER, State.PLAY),
    DISPLAYED_RECIPE_S(0x2D, "displayed_recipe", Direction.TO_SERVER, State.PLAY),
    NAME_ITEM_S(0x2E, "name_item", Direction.TO_SERVER, State.PLAY),
    RESOURCE_PACK_RECEIVE_PLAY_S(0x2F, "resource_pack_receive", Direction.TO_SERVER, State.PLAY),
    ADVANCEMENT_TAB_S(0x30, "advancement_tab", Direction.TO_SERVER, State.PLAY),
    SELECT_TRADE_S(0x31, "select_trade", Direction.TO_SERVER, State.PLAY),
    SET_BEACON_EFFECT_S(0x32, "set_beacon_effect", Direction.TO_SERVER, State.PLAY),
    HELD_ITEM_SLOT_PLAY_S(0x33, "held_item_slot", Direction.TO_SERVER, State.PLAY),
    UPDATE_COMMAND_BLOCK_S(0x34, "update_command_block", Direction.TO_SERVER, State.PLAY),
    UPDATE_COMMAND_BLOCK_MINECART_S(0x35, "update_command_block_minecart", Direction.TO_SERVER, State.PLAY),
    SET_CREATIVE_SLOT_S(0x36, "set_creative_slot", Direction.TO_SERVER, State.PLAY),
    UPDATE_JIGSAW_BLOCK_S(0x37, "update_jigsaw_block", Direction.TO_SERVER, State.PLAY),
    UPDATE_STRUCTURE_BLOCK_S(0x38, "update_structure_block", Direction.TO_SERVER, State.PLAY),
    UPDATE_SIGN_S(0x39, "update_sign", Direction.TO_SERVER, State.PLAY),
    ARM_ANIMATION_S(0x3A, "arm_animation", Direction.TO_SERVER, State.PLAY),
    SPECTATE_S(0x3B, "spectate", Direction.TO_SERVER, State.PLAY),
    BLOCK_PLACE_S(0x3C, "block_place", Direction.TO_SERVER, State.PLAY),
    USE_ITEM_S(0x3D, "use_item", Direction.TO_SERVER, State.PLAY),

    // Status - toClient
    SERVER_INFO_C(0x00, "server_info", Direction.TO_CLIENT, State.STATUS),
    PING_STATUS_C(0x01, "ping", Direction.TO_CLIENT, State.STATUS),

    // Status - toServer
    PING_START_S(0x00, "ping_start", Direction.TO_SERVER, State.STATUS),
    PING_STATUS_S(0x01, "ping", Direction.TO_SERVER, State.STATUS);

    private final int packetId;
    private final String packetName;
    private final Direction direction;
    private final State state;

    MinecraftPacket(int packetId, String packetName, Direction direction, State state) {
        this.packetId = packetId;
        this.packetName = packetName;
        this.direction = direction;
        this.state = state;
    }

    public int getPacketId() {
        return packetId;
    }

    public String getHexId() {
        return String.format("0x%02X", packetId);
    }

    public String getPacketName() {
        return packetName;
    }

    public Direction getDirection() {
        return direction;
    }

    public State getState() {
        return state;
    }

    public static Optional<MinecraftPacket> findByIdAndStateAndDirection(int packetId, State state, Direction direction) {
        return Arrays.stream(values())
                .filter(p -> p.packetId == packetId && p.state == state && p.direction == direction)
                .findFirst();
    }

    public static Optional<MinecraftPacket> findByName(String name) {
        return Arrays.stream(values())
                .filter(p -> p.packetName.equalsIgnoreCase(name))
                .findFirst();
    }

    public enum Direction {
        TO_CLIENT("toClient"),
        TO_SERVER("toServer");

        private final String value;

        Direction(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum State {
        CONFIGURATION("configuration"),
        HANDSHAKING("handshaking"),
        LOGIN("login"),
        PLAY("play"),
        STATUS("status");

        private final String value;

        State(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
