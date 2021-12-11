package net.protolauncher.api.function;

import java.util.function.BiConsumer;

public interface DownloadProgressConsumer extends BiConsumer<Long, Long> {

    @Override
    void accept(Long total, Long transferred);

}
