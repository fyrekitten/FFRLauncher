package net.protolauncher.mojang.rule;

import net.protolauncher.util.SystemInfo;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Mojang "rule" for including arguments, libraries, or artifacts.
 */
public class Rule {

    // JSON Properties
    private Action action;
    @Nullable
    private OS os;

    // Constructor
    public Rule(Action action, @Nullable OS os) {
        this.action = action;
        this.os = os;
    }
    public Rule(Action action) {
        this.action = action;
        this.os = null;
    }

    // Getters
    public Action getAction() {
        return action;
    }
    @Nullable
    public OS getOs() {
        return os;
    }

    /**
     * Determines the final {@link Action} from the given list of rules.
     * If a rule fails to pass any requirements, the rule should not be used.
     *
     * @param rules The rules to determine the final {@link Action}.
     * @return The final {@link Action}
     */
    public static Action determine(Rule[] rules) {
        // The default action is to disallow
        Action finalAction = Action.DISALLOW;

        // Check rules
        for (Rule rule : rules) {
            // Firstly determine the rule's default action
            Action action = rule.action;

            // Check against OS checks
            if (rule.os != null) {
                // Check the name against the current system name
                if (rule.os.getName() != null && !SystemInfo.OS_NAME.equals(rule.os.getName())) {
                    action = null;
                }

                // Check the version against the current system version
                if (rule.os.getVersion() != null && SystemInfo.OS_VERSION.matches(rule.os.getVersion())) {
                    action = null;
                }

                // Check the architecture against the current system architecture
                if (rule.os.getArch() != null && !SystemInfo.OS_ARCH.equals(rule.os.getArch())) {
                    action = null;
                }
            }

            // If the rule passed all the checks, set the final action to this rule's default action
            if (action != null) {
                finalAction = action;
            }
        }

        // Return the final action after checking all rules
        return finalAction;
    }

    /**
     * Represents the 'os' property of a rule.
     */
    public static class OS {

        // JSON Properties
        @Nullable
        private String name;
        @Nullable
        private String version;
        @Nullable
        private String arch;

        // Suppress default constructor
        private OS() { }

        // Getters
        @Nullable
        public String getName() {
            return name;
        }
        @Nullable
        public String getVersion() {
            return version;
        }
        @Nullable
        public String getArch() {
            return arch;
        }

    }

}
