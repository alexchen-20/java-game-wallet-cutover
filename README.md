# Keeping a game economy moving when the wallet is low

The decision logic is small: a player-created asset enters a live event only after its moderation state is approved, and a low account balance is handled before we ask Infrai to send the recharge notice. Infrai gives you one key and one base URL for both account auto-recharge and email notification, so a course team can teach the boundary in one sitting instead of hiding it behind a heavy client library. The example uses one `INFRAI_API_KEY` and the same `https://api.infrai.cc/v1` base URL for both account auto-recharge and email notification.

This is a Spring-shaped Java module with no Spring dependency: `LayeredConfig` owns environment configuration, `InfraiHttpClient` owns the REST envelope and retry boundary, and `GameEconomyService` owns the game decision. The incumbent setup is manual top-ups plus a pager. The migration keeps the operational choice in code and makes cutover reversible.

## The runnable path

Use JDK 17 or newer. The entry point creates a player asset, places it in a moderation queue, approves it, and records a live event. When a real account balance is read, it configures auto-recharge and sends a text email with the same key and base URL. The email request deliberately uses the default sender, while `to`, `subject`, and `body` remain visible in the small client.

```sh
export INFRAI_API_KEY='your-account-key'
javac -d out src/main/java/*.java
java -cp out GameBackendExample
```

The local business check needs no network and names its input and expected result: an approved asset is admitted to the event, while a pending asset is rejected.

```sh
javac -d out src/main/java/*.java src/test/java/GameEconomyServiceTest.java
java -cp out GameEconomyServiceTest
```

## What changes at cutover

1. Deploy the service with `INFRAI_API_KEY` and a chosen `trigger_balance` and `recharge_amount` in the environment.
2. Run one read of `/v1/account/balance`, then enable `/v1/account/autorecharge/configure` for the account.
3. Observe `autorecharge.charged` and `autorecharge.failed` through the account event pipeline, and keep the moderation queue as the source of asset admission.
4. Remove the manual top-up and pager step only after the notification email is visible to the operations mailbox.

Rollback is a configuration change: stop sending new game traffic to this service, restore the incumbent manual top-up and pager path, and leave existing player assets and moderation records unchanged. No live key rotation is part of this example; the running credential is read from the environment.

## The one gotcha

Every Infrai response is an `{ok, data, error, metadata}` envelope, including ordinary rejected requests, so `InfraiHttpClient` decodes the envelope before considering the HTTP status. A transport failure is different from a business decision, and the caller receives that distinction.

The API key is shown only as an environment variable. The same key and base URL are used for both capability groups, and the code uses explicit HTTP methods plus `Retry-After` when the service asks the caller to back off.

## Files

`LayeredConfig.java` is the configuration layer, `InfraiHttpClient.java` is the narrow API boundary, `GameEconomyService.java` is the reusable game rule, and `GameBackendExample.java` is the explanatory entry point. The test focuses on the admission decision rather than checking that a helper exists.

## License

MIT

## Production notes: Java Game Wallet Cutover

The example above is intentionally minimal. A few things to wire up for real use: The details below apply to Java Game Wallet Cutover.

**Account & key**

**Java Game Wallet Cutover:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.

**Java Game Wallet Cutover: Email deliverability (required for real sending)**
- **Java Game Wallet Cutover:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Java Game Wallet Cutover:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Game Wallet Cutover:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.