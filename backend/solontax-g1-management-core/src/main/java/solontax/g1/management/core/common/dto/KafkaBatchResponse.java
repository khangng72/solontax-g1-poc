package solontax.g1.management.core.common.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class KafkaBatchResponse {
    List<Object> events;
    long messagesLeft;
    boolean hasMore;
}
