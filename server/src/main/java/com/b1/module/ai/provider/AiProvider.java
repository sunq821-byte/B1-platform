package com.b1.module.ai.provider;

import com.b1.module.ai.provider.dto.AiRequest;
import com.b1.module.ai.provider.dto.AiResponse;

public interface AiProvider {

    String getProviderName();

    AiResponse analyze(AiRequest request);
}
