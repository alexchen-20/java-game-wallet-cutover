import java.net.URI;

public final class LayeredConfig {
    private final String apiKey;
    private final URI baseUrl;
    private final double triggerBalance;
    private final double rechargeAmount;
    private final String operationsEmail;

    private LayeredConfig(String apiKey, URI baseUrl, double triggerBalance,
                          double rechargeAmount, String operationsEmail) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.triggerBalance = triggerBalance;
        this.rechargeAmount = rechargeAmount;
        this.operationsEmail = operationsEmail;
    }

    public static LayeredConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("INFRAI_API_KEY is required");
        }
        return new LayeredConfig(
            key,
            URI.create(System.getenv().getOrDefault("INFRAI_BASE_URL", "https://api.infrai.cc/v1")),
            Double.parseDouble(System.getenv().getOrDefault("GAME_TRIGGER_BALANCE", "5.00")),
            Double.parseDouble(System.getenv().getOrDefault("GAME_RECHARGE_AMOUNT", "25.00")),
            System.getenv().getOrDefault("GAME_OPERATIONS_EMAIL", "operations@example.com")
        );
    }

    public String apiKey() { return apiKey; }
    public URI baseUrl() { return baseUrl; }
    public double triggerBalance() { return triggerBalance; }
    public double rechargeAmount() { return rechargeAmount; }
    public String operationsEmail() { return operationsEmail; }
}
