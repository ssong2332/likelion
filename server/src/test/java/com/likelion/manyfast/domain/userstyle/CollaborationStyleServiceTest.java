package com.likelion.manyfast.domain.userstyle;

import com.likelion.manyfast.domain.userstyle.dto.CollaborationStyleRequest;
import com.likelion.manyfast.domain.userstyle.dto.CollaborationStyleResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollaborationStyleServiceTest {

    @Mock
    private CollaborationStyleRepository collaborationStyleRepository;

    @InjectMocks
    private CollaborationStyleService collaborationStyleService;

    @Test
    void getsStoredCollaborationStyle() {
        CollaborationStyle stored = style("friendly", "direct", "detailed");
        when(collaborationStyleRepository.findById(1L)).thenReturn(Optional.of(stored));

        CollaborationStyleResponse response = collaborationStyleService.get();

        assertThat(response).isEqualTo(new CollaborationStyleResponse("friendly", "direct", "detailed"));
    }

    @Test
    void returnsDefaultsWithoutSavingWhenStyleDoesNotExist() {
        when(collaborationStyleRepository.findById(1L)).thenReturn(Optional.empty());

        CollaborationStyleResponse response = collaborationStyleService.get();

        assertThat(response).isEqualTo(new CollaborationStyleResponse("polite", "balanced", "concise"));
        verify(collaborationStyleRepository, never()).save(any(CollaborationStyle.class));
        verify(collaborationStyleRepository, never()).saveAndFlush(any(CollaborationStyle.class));
    }

    @Test
    void updatesExistingCollaborationStyleKeepingSingletonId() {
        CollaborationStyle stored = style("polite", "balanced", "concise");
        CollaborationStyleRequest request = new CollaborationStyleRequest("friendly", "direct", "detailed");
        when(collaborationStyleRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(collaborationStyleRepository.saveAndFlush(stored)).thenReturn(stored);

        CollaborationStyleResponse response = collaborationStyleService.update(request);

        assertThat(response).isEqualTo(new CollaborationStyleResponse("friendly", "direct", "detailed"));
        assertThat(stored.getId()).isEqualTo(1L);
    }

    @Test
    void createsSingletonStyleWhenStyleDoesNotExist() {
        CollaborationStyleRequest request = new CollaborationStyleRequest("friendly", "direct", "detailed");
        when(collaborationStyleRepository.findById(1L)).thenReturn(Optional.empty());
        when(collaborationStyleRepository.saveAndFlush(any(CollaborationStyle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CollaborationStyleResponse response = collaborationStyleService.update(request);

        assertThat(response).isEqualTo(new CollaborationStyleResponse("friendly", "direct", "detailed"));
    }

    @Test
    void returnsOnlyCollaborationStyleValuesAfterUpdate() {
        CollaborationStyle stored = style("polite", "balanced", "concise");
        CollaborationStyleRequest request = new CollaborationStyleRequest("concise", "direct", "short");
        when(collaborationStyleRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(collaborationStyleRepository.saveAndFlush(stored)).thenReturn(stored);

        CollaborationStyleResponse response = collaborationStyleService.update(request);

        assertThat(response.tone()).isEqualTo("concise");
        assertThat(response.directness()).isEqualTo("direct");
        assertThat(response.detailLevel()).isEqualTo("short");
    }

    @Test
    void createsEntityWithExactSingletonId() {
        CollaborationStyleRequest request = new CollaborationStyleRequest("friendly", "direct", "detailed");
        when(collaborationStyleRepository.findById(1L)).thenReturn(Optional.empty());
        when(collaborationStyleRepository.saveAndFlush(any(CollaborationStyle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        collaborationStyleService.update(request);

        ArgumentCaptor<CollaborationStyle> captor = ArgumentCaptor.forClass(CollaborationStyle.class);
        verify(collaborationStyleRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void repeatedUpdatesReuseSingletonId() {
        CollaborationStyle stored = style("polite", "balanced", "concise");
        when(collaborationStyleRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(collaborationStyleRepository.saveAndFlush(stored)).thenReturn(stored);

        collaborationStyleService.update(new CollaborationStyleRequest("friendly", "direct", "detailed"));
        CollaborationStyleResponse response = collaborationStyleService.update(
                new CollaborationStyleRequest("concise", "balanced", "brief")
        );

        assertThat(stored.getId()).isEqualTo(1L);
        assertThat(response).isEqualTo(new CollaborationStyleResponse("concise", "balanced", "brief"));
        verify(collaborationStyleRepository, times(2)).findById(1L);
        verify(collaborationStyleRepository, times(2)).saveAndFlush(stored);
    }

    private CollaborationStyle style(String tone, String directness, String detailLevel) {
        return new CollaborationStyle(1L, tone, directness, detailLevel);
    }
}
