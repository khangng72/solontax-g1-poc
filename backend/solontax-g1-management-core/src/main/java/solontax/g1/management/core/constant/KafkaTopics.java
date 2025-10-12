package solontax.g1.management.core.constant;

public class KafkaTopics {
    public static final String DELETE_PERSON_TOPIC = "delete-person";
    public static final String UPSERT_PERSON_TOPIC = "upsert-person";
    public static final String TAX_CALCULATION_TOPIC = "tax-calculation";
    public static final String TAX_CALCULATION_TOPIC_BATCH = "tax-calculation-batch";
    public static final String PARKING_LOT = "parking-lot";

    private KafkaTopics() {
    }
}
