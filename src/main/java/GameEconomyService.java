import java.util.ArrayDeque;
import java.util.Queue;

public final class GameEconomyService {
    private final Queue<Asset> moderationQueue = new ArrayDeque<>();

    public Asset submitAsset(String playerId, String assetId, String title) {
        Asset asset = new Asset(playerId, assetId, title, Moderation.PENDING);
        moderationQueue.add(asset);
        return asset;
    }

    public Asset approveNextAsset() {
        Asset pending = moderationQueue.poll();
        if (pending == null) throw new IllegalStateException("moderation queue is empty");
        return pending.withModeration(Moderation.APPROVED);
    }

    public boolean canEnterLiveEvent(Asset asset) {
        return asset.moderation() == Moderation.APPROVED;
    }

    public record Asset(String playerId, String assetId, String title, Moderation moderation) {
        Asset withModeration(Moderation state) { return new Asset(playerId, assetId, title, state); }
    }

    public enum Moderation { PENDING, APPROVED }
}
