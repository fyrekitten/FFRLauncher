package net.protolauncher.function;

import java.util.function.BiConsumer;

public interface StepProgressConsumer extends BiConsumer<Integer, Integer> {

    @Override
    void accept(Integer totalSteps, Integer currentStep);

}
