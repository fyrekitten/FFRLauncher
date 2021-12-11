package net.protolauncher.function;

import java.util.function.Consumer;

public interface StepInfoConsumer extends Consumer<String> {

    @Override
    void accept(String info);

}
