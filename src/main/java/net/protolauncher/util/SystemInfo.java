package net.protolauncher.util;

/**
 * Provides various constant variables for pieces of system information.
 */
public class SystemInfo {

    // Constants
    /**
     * The name of the operating system.
     * Will be one of the following:
     * <ul>
     *     <li>windows</li>
     *     <li>osx</li>
     *     <li>linux</li>
     *     <li>other</li>
     * </ul>
     */
    public static final String OS_NAME;

    /**
     * The version of the operating system.
     * Fetched from the `os.version` environment variable.
     */
    public static final String OS_VERSION;

    /**
     * The architecture of the operating system.
     * Will be either <code>x86</code> or <code>x64</code>
     */
    public static final String OS_ARCH;

    /**
     * The 'bit' of the operating system, as fetched from the architecture.
     * Will be either <code>32</code> or <code>64</code>
     */
    public static final String OS_BIT;

    /**
     * The file path of the user's home folder.
     * Fetched from the <code>user.home</code> environment variable,
     * however on Windows it will point to the user's desktop.
     */
    public static final String USER_HOME;

    // Static Initializer
    static {
        OS_NAME = getSystemPlatform();
        OS_VERSION = getSystemVersion();
        OS_ARCH = getSystemArchitecture();
        OS_BIT = OS_ARCH.replace("x84", "32").replace("x64", "64");
        USER_HOME = getUserHome();
    }

    // Suppress default constructor
    private SystemInfo() {}

    // Determines the Mojang-Based string to use to determine the platform
    private static String getSystemPlatform() {
        final String name = System.getProperty("os.name").toLowerCase();
        if (name.contains("win")) {
            return "windows";
        } else if (name.contains("mac")) {
            return "osx";
        } else if (name.contains("nix") || name.contains("nux") || name.contains("aix")) {
            return "linux";
        } else {
            return "other";
        }
    }

    // Straight up get the version property
    private static String getSystemVersion() {
        return System.getProperty("os.version");
    }

    // Determine whether the architecture is 64-bit or 32-bit
    private static String getSystemArchitecture() {
        final String procArch = System.getenv("PROCESSOR_ARCHITECTURE");
        final String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        if (procArch != null && procArch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64")) {
            return "x64";
        } else {
            return "x86";
        }
    }

    // Fetches the user home and appends "desktop" if windows
    private static String getUserHome() {
        String home = System.getProperty("user.home");
        if (OS_NAME.equals("windows")) {
            home += "\\Desktop";
        }
        return home;
    }

}
