package solontax.g1.management.kafka.constants;

public class ConsumerGroups {
    // For single event consumer
    public static final String SINGLE_DEFAULT_GROUP = "single-default-group";
    public static final String SINGLE_UPSERT_PERSON_GROUP = "single-person-upsert-group";
    public static final String SINGLE_DELETE_PERSON_GROUP = "single-person-delete-group";
    public static final String SINGLE_TAX_CALCULATION_GROUP = "single-tax-calculation-group";

    // For batch event consumer
    public static final String BATCH_DEFAULT_GROUP = "batch-default-group";
    public static final String BATCH_TAX_CALCULATION_GROUP = "batch-tax-calculation-group";

    private ConsumerGroups() {
    }
}
