package solontax.g1.management.core.common.dto;

import lombok.Data;

@Data
public class PersonQueryParams {
    private String query;

    private String sortBy = "id";

    private String sortDirection = "asc";

    private int size = 10;

    private int offset = 0;
}
