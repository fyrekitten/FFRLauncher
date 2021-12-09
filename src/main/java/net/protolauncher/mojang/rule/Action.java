package net.protolauncher.mojang.rule;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a mojang "action" for a rule.
 */
public enum Action {

    @SerializedName("disallow")
    DISALLOW,

    @SerializedName("allow")
    ALLOW

}
