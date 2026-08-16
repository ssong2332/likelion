package com.likelion.manyfast.domain.userstyle;

import com.likelion.manyfast.domain.userstyle.dto.CollaborationStyleRequest;
import com.likelion.manyfast.domain.userstyle.dto.CollaborationStyleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollaborationStyleService {

    private static final Long SINGLETON_ID = 1L;
    private static final String DEFAULT_TONE = "polite";
    private static final String DEFAULT_DIRECTNESS = "balanced";
    private static final String DEFAULT_DETAIL_LEVEL = "concise";

    private final CollaborationStyleRepository collaborationStyleRepository;

    public CollaborationStyleResponse get() {
        return collaborationStyleRepository.findById(SINGLETON_ID)
                .map(CollaborationStyleResponse::from)
                .orElseGet(this::defaultResponse);
    }

    @Transactional
    public CollaborationStyleResponse update(CollaborationStyleRequest request) {
        CollaborationStyle style = collaborationStyleRepository.findById(SINGLETON_ID)
                .orElseGet(() -> new CollaborationStyle(
                        SINGLETON_ID,
                        request.tone(),
                        request.directness(),
                        request.detailLevel()
                ));

        style.update(request.tone(), request.directness(), request.detailLevel());
        return CollaborationStyleResponse.from(collaborationStyleRepository.saveAndFlush(style));
    }

    private CollaborationStyleResponse defaultResponse() {
        return new CollaborationStyleResponse(
                DEFAULT_TONE,
                DEFAULT_DIRECTNESS,
                DEFAULT_DETAIL_LEVEL
        );
    }
}
