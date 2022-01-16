package net.protolauncher.mojang.version;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.protolauncher.mojang.rule.Action;
import net.protolauncher.mojang.rule.Rule;

import java.lang.reflect.Type;

/**
 * Represents a list of CLI arguments required to launch the client.
 * Stems from the {@link Version}.
 * <br/><br/>
 * Due to Mojang refusing to follow basic JSON standards, they mixed types in the JSON array,
 * so we need a custom type adapter for the entire class. Thanks, Mojang!
 */
@JsonAdapter(VersionArguments.VersionArgumentsJsonAdapter.class)
public class VersionArguments {

    // JSON Properties
    protected String game;
    protected String jvm;
    private JsonElement gameOriginal;
    private JsonElement jvmOriginal;

    // Suppress default constructor
    private VersionArguments() { }

    // Getters
    public String getGame() {
        return game;
    }
    public String getJvm() {
        return jvm;
    }

    /**
     * Merges this VersionArguments with another.
     *
     * @param arguments The arguments to merge into this one.
     * @return The merged arguments.
     * @see Version#merge(Version, boolean)
     */
    public VersionArguments merge(VersionArguments arguments) {
        if (arguments == null) {
            return this;
        }

        // Merge game arguments
        this.game += arguments.game;
        if (arguments.gameOriginal != null) {
            if (this.gameOriginal == null) {
                this.gameOriginal = arguments.gameOriginal;
            } else {
                this.gameOriginal.getAsJsonArray().addAll(arguments.gameOriginal.getAsJsonArray());
            }
        }

        // Merge jvm arguments
        this.jvm += arguments.jvm;
        if (arguments.jvmOriginal != null) {
            if (this.jvmOriginal == null) {
                this.jvmOriginal = arguments.jvmOriginal;
            } else {
                this.jvmOriginal.getAsJsonArray().addAll(arguments.jvmOriginal.getAsJsonArray());
            }
        }

        return this;
    }

    /**
     * Handles converting the arguments to a string and back into an array.
     * <br/><br/>
     * We need to preserve the original array due to the fact that it may be modified,
     * but we need to convert it to a string to use it when launching. As such, we only
     * store the original JsonElement in the file, and then re-parse the string each
     * time it is loaded.
     * <br/><br/>
     * The alternative is to have a method to construct the string every time the game
     * is launched, however I think the tradeoff of just parsing it each time the file
     * is loaded then keeping that in RAM is worth the extra CPU time it would take
     * each time the game is launched.
     * <br/><br/>
     * It's minor details, really, and what matters it that it works. The actual performance
     * hit from either solution is probably minimal to none.
     */
    public static class VersionArgumentsJsonAdapter implements JsonDeserializer<VersionArguments>, JsonSerializer<VersionArguments> {

        @Override
        public VersionArguments deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            VersionArguments versionArguments = new VersionArguments();
            versionArguments.game = new GameArgumentsDeserializer().deserialize(obj.get("game"), Object.class, context);
            versionArguments.jvm = new JvmArgumentsDeserializer().deserialize(obj.get("jvm"), Object.class, context);
            versionArguments.gameOriginal = obj.get("game");
            versionArguments.jvmOriginal = obj.get("jvm");
            return versionArguments;
        }

        @Override
        public JsonElement serialize(VersionArguments src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("game", src.gameOriginal);
            obj.add("jvm", src.jvmOriginal);
            return obj;
        }

    }

    /**
     * Deserializes the possible 'game' arguments in the array into a string.
     */
    public static class GameArgumentsDeserializer implements JsonDeserializer<String> {

        @Override
        public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            StringBuilder args = new StringBuilder();

            // Handle null
            if (json == null) {
                return null;
            }

            // Parse the types
            JsonArray arr = json.getAsJsonArray();
            for (JsonElement element : arr) {
                // If the element is a primitive, it's a string and they can be appended to the arguments
                if (element.isJsonPrimitive()) {
                    if (args.length() > 0) {
                        args.append(' ');
                    }
                    args.append(element.getAsString());
                }
                // TODO: Handle options and rules
            }

            // Return the final string
            return args.toString();
        }

    }

    /**
     * Deserializes the possible 'jvm' arguments in the array into a string.
     */
    public static class JvmArgumentsDeserializer implements JsonDeserializer<String> {

        @Override
        public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            StringBuilder args = new StringBuilder();

            // Handle null
            if (json == null) {
                return null;
            }

            // Parse the types
            JsonArray arr = json.getAsJsonArray();
            for (JsonElement element : arr) {
                // If the element is a primitive, it's a string and they can be appended to the arguments
                // Otherwise, it's an object (value with rules) and must be parsed further
                if (element.isJsonPrimitive()) {
                    if (args.length() > 0) {
                        args.append(' ');
                    }
                    args.append(element.getAsString());
                } else {
                    JsonObject obj = element.getAsJsonObject();

                    // Fetch the rules from the object
                    Rule[] rules = (new Gson()).fromJson(obj.get("rules"), Rule[].class);

                    // Check the rules to determine if we should even bother parsing the value
                    if (Rule.determine(rules) == Action.ALLOW) {

                        // The final value to append to the arguments
                        StringBuilder value = new StringBuilder();

                        // Check if the rule's value contains an array or a string
                        JsonElement valueElement = obj.get("value");

                        // If the element is a primitive, it's a string and they can be appended to the arguments
                        if (valueElement.isJsonPrimitive()) {
                            value = new StringBuilder(valueElement.getAsString());

                            // Otherwise, the element is an array, it's an array of string values and we should convert it to be one string
                        } else {
                            JsonArray multivalueElement = valueElement.getAsJsonArray();
                            for (JsonElement multivalueValue : multivalueElement) {
                                if (value.length() > 0) {
                                    value.append(' ');
                                }
                                value.append('"').append(multivalueValue.getAsString()).append('"');
                            }
                        }

                        // Append the value
                        if (args.length() > 0) {
                            args.append(' ');
                        }
                        args.append(value);
                    }
                }
            }

            // Return the final string
            return args.toString();
        }

    }

}
