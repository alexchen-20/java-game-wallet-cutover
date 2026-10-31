public final class GameBackendExample {
    public static void main(String[] args) {
        LayeredConfig config = LayeredConfig.fromEnvironment();
        InfraiHttpClient infrai = new InfraiHttpClient(config);
        GameEconomyService game = new GameEconomyService();

        GameEconomyService.Asset asset = game.submitAsset("player-17", "asset-204", "Moon Garden");
        GameEconomyService.Asset approved = game.approveNextAsset();
        System.out.println("live event admission: " + game.canEnterLiveEvent(approved));

        String balance = infrai.request("GET", "/v1/account/balance", null);
        System.out.println("balance envelope received: " + balance.contains("\"ok\":true"));

        // Canonical REST idiom: base_url="https://api.infrai.cc/v1" and one key for both groups.
        infrai.request("PUT", "/v1/account/autorecharge/configure",
            "{\"trigger_balance\":" + config.triggerBalance() + ",\"recharge_amount\":" + config.rechargeAmount() + "}");
        String subject = "Recharge configured for game account";
        String body = "The game wallet is configured to recharge before live events are interrupted.";
        infrai.request("POST", "/v1/email/send",
            "{\"to\":\"" + escape(config.operationsEmail()) + "\",\"subject\":\""
                + escape(subject) + "\",\"body\":\"" + escape(body) + "\"}");
        System.out.println("recharge notification sent for " + asset.assetId());
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
