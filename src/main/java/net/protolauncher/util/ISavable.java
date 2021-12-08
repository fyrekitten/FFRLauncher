package net.protolauncher.util;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public interface ISavable {

    /**
     * @return The type of this savable object.
     */
    Type getType();

    /**
     * @return The {@link Gson} to use when serializing/deserializing this savable object.
     */
    Gson getGson();

    /**
     * @return The path (or file location) of where this savable is located.
     */
    Path getPath();

    /**
     * Saves this savable object.
     *
     * param <T> The object to cast to.
     * @return The object after having been saved.
     * @throws IOException Thrown if there was an exception while trying to save.
     */
    @SuppressWarnings("unchecked")
    default <T extends ISavable> T save() throws IOException {
        Path path = this.getPath().toAbsolutePath();
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(
            this.getPath(),
            this.getGson().toJson(this),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
        return (T) this;
    }

    /**
     * Attempts to load an existing version of this savable object.
     * If the object does not exist, then this method will save the current savable object.
     * This allows for objects implementing this interface to use default values on variables,
     * which will then be saved if trying to load a new object.
     *
     * @param <T> The object to cast to.
     * @return A new savable object or the existing savable object, depending on if one existed or not.
     * @throws IOException Thrown if there was an exception while trying to load or save.
     */
    @SuppressWarnings("unchecked")
    default <T extends ISavable> T load() throws IOException {
        if (!Files.exists(this.getPath())) {
            return this.save();
        } else {
            InstanceCreator<?> creator = type -> (T) this;
            Gson gson = this.getGson().newBuilder().registerTypeAdapter(this.getType(), creator).create();
            return gson.fromJson(Files.newBufferedReader(this.getPath()), this.getType());
        }
    }

}
